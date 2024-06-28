package org.example.steam;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.steam.codec.SteammessagesAuthSteamclient;
import org.example.steam.codec.SteammessagesBase;
import org.example.steam.codec.SteammessagesClientserverLogin;

import javax.net.ssl.SSLException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebSocketClient {

    private URI uri;
    private EventLoopGroup group;
    private Channel channel;
    private WebSocketHandler webSocketHandler;
    private String scheme;
    private String host;
    private int port;

    public WebSocketClient(String wss) throws URISyntaxException {
        this.uri = new URI(wss);
        this.scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        this.host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        this.port = (uri.getPort() == -1) ? (scheme.equals("wss") ? 443 : 80) : uri.getPort();

        this.group = new NioEventLoopGroup();
        this.webSocketHandler = new WebSocketHandler(this);
    }

    public void connect() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws SSLException {
                        ChannelPipeline pipeline = ch.pipeline();
                        if ("wss".equalsIgnoreCase(uri.getScheme())) {
                            SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).protocols("TLSv1.3").build();
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(webSocketHandler);
                    }
                });

        ChannelFuture future = b.connect(uri.getHost(), uri.getPort()).sync();
        future.addListener((ChannelFutureListener) connectFuture -> {
            if (connectFuture.isSuccess()) {
                log.debug("连接建立成功！");
                this.channel = connectFuture.channel();
                webSocketHandler.handshakeFuture().addListeners((ChannelFutureListener) handshakeFuture -> {
                    if (handshakeFuture.isSuccess()) {
                        log.debug("握手成功!");
                        // 认证包在连接成功后5秒内发送，否则会强制断开连接
                        this.sendAuthRequest();
                        // 心跳包30秒左右发送一次，否则60秒后会被强制断开连接,正文可以为空或任意字符
                        this.sendHeartbeat();
                    } else {
                        log.error("握手失败!", handshakeFuture.cause());
                        handshakeFuture.cause().printStackTrace();
                    }
                });
            } else {
                log.error("连接建立失败", connectFuture.cause());
                connectFuture.cause().printStackTrace();
            }
        });
    }

    private void sendHeartbeat() {
        if (this.channel != null && this.channel.isActive()) {
            channel.eventLoop().scheduleAtFixedRate(() -> {
                if (channel.isActive()) {
                    SteammessagesClientserverLogin.CMsgClientHeartBeat.Builder builder = SteammessagesClientserverLogin.CMsgClientHeartBeat.newBuilder();
                    builder.setSendReply(false);
                    byte[] byteArray = builder.build().toByteArray();
                    sendMessage(EMsg.ClientHeartBeat, byteArray, null);
                }
            }, 5000, 5000, TimeUnit.MILLISECONDS);

            channel.eventLoop().schedule(() -> {
                if (channel.isActive()) {
                    String refreshToken = "eyAidHlwIjogIkpXVCIsICJhbGciOiAiRWREU0EiIH0.eyAiaXNzIjogInN0ZWFtIiwgInN1YiI6ICI3NjU2MTE5OTYzOTAxNjAwMyIsICJhdWQiOiBbICJjbGllbnQiLCAid2ViIiwgInJlbmV3IiwgImRlcml2ZSIgXSwgImV4cCI6IDE3MzA1NjI4MjAsICJuYmYiOiAxNzAzNjMzNzkwLCAiaWF0IjogMTcxMjI3Mzc5MCwgImp0aSI6ICIwRUZEXzI0MzYyOTA1XzY5RDBGIiwgIm9hdCI6IDE3MTIyNzM3OTAsICJwZXIiOiAxLCAiaXBfc3ViamVjdCI6ICIzNy4yMzkuNjYuMjAiLCAiaXBfY29uZmlybWVyIjogIjM3LjIzOS42Ni4yMCIgfQ.cXk9FqY3gVzOkqf2KxuPnFnmT1FLgTsVFBQRe3iCcii-7-f_Oh62_Bd4xR6OYWmQDzXjaQYH4gaSke48DzQBBQ";
                    sendMessage(refreshToken);
                }
            },  50, TimeUnit.SECONDS);
        }
    }

    private void sendAuthRequest() {
        if (this.channel != null && this.channel.isActive()) {
           sendClientHello();
          	String refreshToken = "eyAidHlwIjogIkpXVCIsICJhbGciOiAiRWREU0EiIH0.eyAiaXNzIjogInN0ZWFtIiwgInN1YiI6ICI3NjU2MTE5OTE3NjgzMjExOCIsICJhdWQiOiBbICJjbGllbnQiLCAid2ViIiwgInJlbmV3IiwgImRlcml2ZSIgXSwgImV4cCI6IDE3MjgxNzkxODUsICJuYmYiOiAxNjg4NTk1MzA0LCAiaWF0IjogMTY5NzIzNTMwNCwgImp0aSI6ICIxNDU1XzIzNTBDMUJBX0UwQTA5IiwgIm9hdCI6IDE2OTcyMzUzMDQsICJnZW4iOiAxLCAicGVyIjogMSwgImlwX3N1YmplY3QiOiAiODIuMTEuMTU0LjUwIiwgImlwX2NvbmZpcm1lciI6ICI4Mi4xMS4xNTQuNTAiIH0.GDKSalpYq1c3f9NdPHqwxj3-QY_Jgx8by6GCAy1ftGraOK91b4TdQx9PGADTIc0U00K5JX3-GLsShO5xgXepDw";
            sendMessage(refreshToken);
            refreshToken = "eyAidHlwIjogIkpXVCIsICJhbGciOiAiRWREU0EiIH0.eyAiaXNzIjogInN0ZWFtIiwgInN1YiI6ICI3NjU2MTE5OTUzMjc0NDA0MyIsICJhdWQiOiBbICJjbGllbnQiLCAid2ViIiwgInJlbmV3IiwgImRlcml2ZSIgXSwgImV4cCI6IDE3MzI1NTQ0MzMsICJuYmYiOiAxNzA1ODM0NjYzLCAiaWF0IjogMTcxNDQ3NDY2MywgImp0aSI6ICIwRUY0XzI0NTFEOTMyX0U5RDJCIiwgIm9hdCI6IDE3MTQ0NzQ2NjMsICJwZXIiOiAxLCAiaXBfc3ViamVjdCI6ICIxNzQuMTYzLjE1MS4xOTYiLCAiaXBfY29uZmlybWVyIjogIjE3NC4xNjMuMTUxLjE5NiIgfQ.9GfKqefgLHHQvbUgtnrMqQAbskHi-tIj-5l5ecquR4wTxkEm-3xNgeb78I-sayIYs88XqeTB_LXuEqrszb7WCw";
            sendMessage(refreshToken);
            refreshToken = "eyAidHlwIjogIkpXVCIsICJhbGciOiAiRWREU0EiIH0.eyAiaXNzIjogInN0ZWFtIiwgInN1YiI6ICI3NjU2MTE5OTYzOTAxNjAwMyIsICJhdWQiOiBbICJjbGllbnQiLCAid2ViIiwgInJlbmV3IiwgImRlcml2ZSIgXSwgImV4cCI6IDE3MzA1NjI4MjAsICJuYmYiOiAxNzAzNjMzNzkwLCAiaWF0IjogMTcxMjI3Mzc5MCwgImp0aSI6ICIwRUZEXzI0MzYyOTA1XzY5RDBGIiwgIm9hdCI6IDE3MTIyNzM3OTAsICJwZXIiOiAxLCAiaXBfc3ViamVjdCI6ICIzNy4yMzkuNjYuMjAiLCAiaXBfY29uZmlybWVyIjogIjM3LjIzOS42Ni4yMCIgfQ.cXk9FqY3gVzOkqf2KxuPnFnmT1FLgTsVFBQRe3iCcii-7-f_Oh62_Bd4xR6OYWmQDzXjaQYH4gaSke48DzQBBQ";
            sendMessage(refreshToken);

        }
    }

    public void sendClientHello(){
        int PROTOCOL_VERSION = 65580;
        SteammessagesClientserverLogin.CMsgClientHello.Builder helloBuilder = SteammessagesClientserverLogin.CMsgClientHello.newBuilder();
        helloBuilder.setProtocolVersion(PROTOCOL_VERSION);
        byte[] body = helloBuilder.build().toByteArray();
        sendMessage(EMsg.ClientHello, body, null);
    }

    public void sendMessage(String refreshToken){
        SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request.Builder builder = SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request.newBuilder();
        builder.setRefreshToken(refreshToken);
        builder.setSteamid(TokenUtil.decodeToken(refreshToken).getLong("sub"));
        builder.setRenewalType(SteammessagesAuthSteamclient.ETokenRenewalType.k_ETokenRenewalType_None);
        byte[] body = builder.build().toByteArray();
        sendMessage(EMsg.ServiceMethodCallFromClientNonAuthed, body, refreshToken);
    }

    public void sendMessage(EMsg eMsg, byte[] body, String refreshToken){
        SteammessagesBase.CMsgProtoBufHeader.Builder protoHeader = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
        protoHeader.setSteamid(0);
        if (eMsg != EMsg.ServiceMethodCallFromClientNonAuthed) {
            protoHeader.setClientSessionid(Constant.getClientSessionid());
        } else {
            protoHeader.setClientSessionid(0);
        }

        if (eMsg == EMsg.ServiceMethodCallFromClientNonAuthed) {
            byte[] jobIdBuffer = new byte[8];
            new SecureRandom().nextBytes(jobIdBuffer);
            // 确保生成的是正数
            jobIdBuffer[0] &= 0x7F;
            long jobId = new BigInteger(1, jobIdBuffer).longValue();
            Constant.setJobs(jobId, refreshToken);
            protoHeader.setJobidSource(jobId);
            protoHeader.setTargetJobName("Authentication.GenerateAccessTokenForApp#1");
            protoHeader.setRealm(1);
        }
        byte[]  encodedProtoHeader = protoHeader.build().toByteArray();
        ByteBuf header = Unpooled.buffer(8);
        int PROTO_MASK = 0x80000000;
        header.writeIntLE((eMsg.getCode() | PROTO_MASK) >>> 0);
        header.writeIntLE(encodedProtoHeader.length);
        ByteBuf message = Unpooled.wrappedBuffer(header, Unpooled.wrappedBuffer(encodedProtoHeader), Unpooled.wrappedBuffer(body));

        channel.writeAndFlush(new BinaryWebSocketFrame(message)).addListener(messageFuture -> {
            if (messageFuture.isSuccess()) {
                log.debug("消息发送完成");
            } else {
                log.error("消息发送失败", messageFuture.cause());
                messageFuture.cause().printStackTrace();
            }
        });
    }

    public void close() {
        if (this.channel != null) {
            this.channel.close();
        }
        if (this.group != null) {
            this.group.shutdownGracefully();
        }
    }

    public URI getUri() {
        return uri;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
