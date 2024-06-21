package org.example.bilibili;

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

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClient {
    private URI uri;
    private EventLoopGroup group;
    private Channel channel;
    private String scheme;
    private String host;
    private int port;
    private String authMessage;

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
    }

    public void connect() throws Exception {
        final SslContext sslCtx;
        if ("wss".equalsIgnoreCase(uri.getScheme())) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        WebSocketHandler webSocketHandler = new WebSocketHandler(this);
        Bootstrap b = new Bootstrap();
        b.group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslCtx != null) {
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(webSocketHandler);
                    }
                });

        ChannelFuture future = b.connect(uri.getHost(), uri.getPort()).sync();
        future.addListener((ChannelFutureListener) connectFuture -> {
            if (connectFuture.isSuccess()) {
                System.out.println("连接建立成功！");
                this.channel = connectFuture.channel();
                webSocketHandler.handshakeFuture().addListeners((ChannelFutureListener) handshakeFuture -> {
                    if (handshakeFuture.isSuccess()) {
                        System.out.println("握手成功!");
                        send(authMessage, 7);
                    } else {
                        System.out.println("握手失败!");
                    }
                });
            } else {
                System.out.println("连接建立失败");
                connectFuture.cause().printStackTrace();
            }
        });
    }


    public ByteBuf createPacket(String data, int op) {
        byte[] body = data.getBytes(CharsetUtil.UTF_8);
        int headerLength = 16;
        int totalLength = headerLength + body.length;
        ByteBuf buffer = Unpooled.buffer(totalLength);
        // 封包总大小（头部大小+正文大小）
        buffer.writeInt(totalLength);
        // 头部大小（一般为0x0010，16字节）
        buffer.writeShort(headerLength);
        // 协议版本
        buffer.writeShort(0);
        // 操作码
        buffer.writeInt(op);
        // sequence，每次发包时向上递增
        buffer.writeInt(1);
        // 正文数据
        buffer.writeBytes(body);
        return buffer;
    }

    public void send(String data, int op) {
        if (this.channel != null && this.channel.isOpen()) {
            ByteBuf packet = createPacket(data, op);
            channel.writeAndFlush(new BinaryWebSocketFrame(packet)).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Sent message: " + data);
                } else {
                    System.err.println("Failed to send message");
                    future.cause().printStackTrace();
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
