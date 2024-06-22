package org.example.bilibili;

import cn.hutool.core.util.StrUtil;
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
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebSocketClient {
    private URI uri;
    private EventLoopGroup group;
    private Channel channel;
    private String scheme;
    private String host;
    private int port;
    private String authMessage;
    private WebSocketHandler webSocketHandler;

    public String getAuthMessage() {
        return authMessage;
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

    public WebSocketClient(String wss, String authMessage) throws URISyntaxException {
        this.uri = new URI(wss);
        this.scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        this.host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        this.port = (uri.getPort() == -1) ? (scheme.equals("wss") ? 443 : 80) : uri.getPort();

        this.group = new NioEventLoopGroup();
        this.authMessage = authMessage;
        this.webSocketHandler = new WebSocketHandler(this);
    }

    public void connect() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws SSLException {
                        ChannelPipeline pipeline = ch.pipeline();
                        if ("wss".equalsIgnoreCase(uri.getScheme())) {
                            SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(8192));
//                         pipeline.addLast(new LoggingHandler(LogLevel.INFO));
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
//                        认证包在连接成功后5秒内发送，否则会强制断开连接
                        this.sendAuthRequest();
//                        心跳包30秒左右发送一次，否则60秒后会被强制断开连接,正文可以为空或任意字符
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


    public ByteBuf createPacket(String data, OperationEnum operationEnum, ProtoverEnum protoverEnum) {

        int sequence = 0;
        final short HEADER_LENGTH = 16;

//        String jsonStr = StrUtil.EMPTY;
        String jsonStr = "hello w";
        // HeartbeatMsg不需要正文，如果序列化后得到`{}`，则替换为空字符串
        if (operationEnum != OperationEnum.HEARTBEAT) {
            jsonStr = data;
            if (StrUtil.EMPTY_JSON.equals(jsonStr)) {
                jsonStr = StrUtil.EMPTY;
            }
        }

        byte[] body = jsonStr.getBytes(CharsetUtil.UTF_8);
        int totalLength = HEADER_LENGTH + body.length;
        ByteBuf buffer = Unpooled.buffer(totalLength);
        // 封包总大小（头部大小+正文大小）
        buffer.writeInt(totalLength);
        // 头部大小（一般为0x0010，16字节）
        buffer.writeShort(HEADER_LENGTH);
        // 协议版本
        buffer.writeShort(protoverEnum.getCode());
        // 操作码
        buffer.writeInt(operationEnum.getCode());
        // sequence，每次发包时向上递增
        buffer.writeInt(++sequence);
        // 正文数据
        buffer.writeBytes(body);
        return buffer;
    }

    public void sendAuthRequest() {
        if (this.channel != null && this.channel.isActive()) {
            ByteBuf packet = createPacket(this.authMessage, OperationEnum.USER_AUTHENTICATION, ProtoverEnum.HEARTBEAT_AUTH_NO_COMPRESSION);
            log.debug("发送认证包");
            channel.writeAndFlush(new BinaryWebSocketFrame(packet)).addListener(messageFuture -> {
                if (messageFuture.isSuccess()) {
                    log.debug("认证包发送完成");
                } else {
                    log.error("认证包发送失败", messageFuture.cause());
                    messageFuture.cause().printStackTrace();
                }
            });
        }
    }

    public void sendHeartbeat() {
        if (this.channel != null && this.channel.isActive()) {
            channel.eventLoop().scheduleAtFixedRate(() -> {
                if (channel.isActive()) {
                    ByteBuf packet = createPacket("", OperationEnum.HEARTBEAT, ProtoverEnum.HEARTBEAT_AUTH_NO_COMPRESSION);
                    log.debug("发送心跳包");
                    channel.writeAndFlush(new BinaryWebSocketFrame(packet)).addListener(messageFuture -> {
                        if (messageFuture.isSuccess()) {
                            log.debug("心跳包发送完成");
                        } else {
                            log.error("心跳包发送失败", messageFuture.cause());
                            messageFuture.cause().printStackTrace();
                        }
                    });
                }
            },15,25, TimeUnit.SECONDS);
        }
    }

    public void send(String data, OperationEnum operationEnum, ProtoverEnum protoverEnum) {
        if (this.channel != null && this.channel.isActive()) {
            ByteBuf packet = createPacket(data, operationEnum,protoverEnum);
            channel.writeAndFlush(new BinaryWebSocketFrame(packet)).addListener(messageFuture -> {
                if (messageFuture.isSuccess()) {
                    log.debug("消息发送成功: {}", data);
                } else {
                    log.error("消息发送失败");
                    messageFuture.cause().printStackTrace();
                }
            });
        }
    }

    public void close() {
        if (this.channel != null) {
            this.channel.close();
        }
        if (this.group != null) {
            this.group.shutdownGracefully();
        }
    }
}
