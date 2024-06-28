package org.example.steam;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
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
import java.util.Base64;

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
                        // pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
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
                        // this.sendHeartbeat();
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

    private void sendAuthRequest() {
        if (this.channel != null && this.channel.isActive()) {
           sendClientHello();
          	String refreshToken = "eyAidHlwIjogIkpXVCIsICJhbGciOiAiRWREU0EiIH0.eyAiaXNzIjogInN0ZWFtIiwgInN1YiI6ICI3NjU2MTE5OTY5Nzc1MTI1MiIsICJhdWQiOiBbICJjbGllbnQiLCAid2ViIiwgInJlbmV3IiwgImRlcml2ZSIgXSwgImV4cCI6IDE3MzY4OTk3NTIsICJuYmYiOiAxNzA5OTA3NjYwLCAiaWF0IjogMTcxODU0NzY2MCwgImp0aSI6ICIxNUM2XzI0OTNFMEY0XzI4QTg5IiwgIm9hdCI6IDE3MTg1NDc2NjAsICJwZXIiOiAxLCAiaXBfc3ViamVjdCI6ICIzNy4yMTMuMzQuMTIyIiwgImlwX2NvbmZpcm1lciI6ICIzNy4yMTMuMzQuMTIyIiB9.NFc_L1GLj8syG3hbbcNXuEsmxrdni5eTaIYp_hJ8Bs79drEDDW7au9gmhRMWTeyia-P75eElL516u6n9cJPKBw";
            sendMessage(refreshToken);
        }
    }

    public void sendClientHello(){
        int PROTOCOL_VERSION = 65580;
        SteammessagesClientserverLogin.CMsgClientHello.Builder helloBuilder = SteammessagesClientserverLogin.CMsgClientHello.newBuilder();
        helloBuilder.setProtocolVersion(PROTOCOL_VERSION);
        byte[] body = helloBuilder.build().toByteArray();
        sendMessage(EMsg.ClientHello, body);
    }

    public void sendMessage(String refreshToken){
        SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request.Builder builder = SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request.newBuilder();
        builder.setRefreshToken(refreshToken);
        builder.setSteamid(decodeJwt(refreshToken).getLong("sub"));
        builder.setRenewalType(SteammessagesAuthSteamclient.ETokenRenewalType.k_ETokenRenewalType_None);
        byte[] body = builder.build().toByteArray();
        sendMessage(EMsg.ServiceMethodCallFromClientNonAuthed, body);
    }

    public JSONObject decodeJwt(String refreshToken){
        String[] parts = refreshToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT");
        }
        String standardBase64 = parts[1].replace('-', '+').replace('_', '/');
        return JSONUtil.parseObj(new String(Base64.getDecoder().decode(standardBase64)));
    }

    public void sendMessage(EMsg eMsg, byte[] body){
        SteammessagesBase.CMsgProtoBufHeader.Builder protoHeader = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
        protoHeader.setSteamid(0);
        protoHeader.setClientSessionid(0);

        if (eMsg == EMsg.ServiceMethodCallFromClientNonAuthed) {
            byte[] jobIdBuffer = new byte[8];
            new SecureRandom().nextBytes(jobIdBuffer);
            // 确保生成的是正数
            jobIdBuffer[0] &= 0x7F;
            protoHeader.setJobidSource(new BigInteger(1, jobIdBuffer).longValue());
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
