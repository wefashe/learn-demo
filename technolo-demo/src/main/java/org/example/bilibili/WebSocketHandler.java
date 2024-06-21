package org.example.bilibili;

import cn.hutool.http.GlobalHeaders;
import cn.hutool.http.Header;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClient client;
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public WebSocketHandler(WebSocketClient client) {
        this.client = client;
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(Header.USER_AGENT.name(), GlobalHeaders.INSTANCE.header(Header.USER_AGENT));
        headers.add(Header.ORIGIN.name(), "https://live.bilibili.com");
        headers.add(Header.PRAGMA.name(), "no-cache");
        headers.set(Header.HOST.name(), client.getHost() + ":" + client.getPort());
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                client.getUri(), WebSocketVersion.V13, null, false, headers);
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() +
                    ", content=" + response.content().toString(io.netty.util.CharsetUtil.UTF_8) + ')');
        } else if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            System.out.println("Received: " + frame.text());
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
            log.debug("服务器接收到二进制消息,消息长度:[{}]", frame.content().capacity());
            ByteBuf content = frame.content();
            processBinaryFrame(content);
        }
    }

    private void processBinaryFrame(ByteBuf content) throws IOException {
        if (content.readableBytes() < 16) {
            return;
        }

        content.markReaderIndex();

        int packetLength = content.readInt();
        // 响应头长度
        int headerLength = content.readShort();
        // 协议版本
        int protocolVersion = content.readShort();
        // 操作
        int operation = content.readInt();
        // 序号
        int sequence = content.readInt();


        if (content.readableBytes() < packetLength - 16) {
            content.resetReaderIndex();
            return;
        }

        // 协议版本:
        // 0普通包正文不使用压缩
        // 1心跳及认证包正文不使用压缩
        // 2普通包正文使用zlib压缩
        // 3普通包正文使用brotli压缩,解压为一个带头部的协议0普通包

        ByteBuf data = content.readSlice(packetLength - headerLength);

        if (protocolVersion == 2) {
            ByteBuf uncompressed = Unpooled.wrappedBuffer(decompress(data));
            processBinaryFrame(uncompressed);
        } else if (protocolVersion == 0 || protocolVersion == 1) {
            String jsonStr = data.toString(CharsetUtil.UTF_8);
            System.out.println("Received danmaku: " + jsonStr);
            JSONObject danmu = JSONUtil.parseObj(jsonStr);
            String cmd = danmu.getStr("cmd");
            if (!danmu.isNull("code")) {
                if (0 == danmu.getInt("code")) {
                    log.info("认证成功");
                }
            }
            if ("NOTICE_MSG".equals(cmd)) {
                log.info("通知消息：{}",danmu.getStr("msg_common"));
            } else if ("STOP_LIVE_ROOM_LIST".equals(cmd)) {
                log.info("下播的直播间列表：{}",danmu.getJSONObject("data").getJSONArray("room_id_list"));
            }
        } else {
            // 解压Brotli压缩的正文
            // byte[] body = new byte[packetLength - 16];
            // content.readBytes(body);
            byte[] byteArray = new byte[data.readableBytes()];
            data.readBytes(byteArray);

            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = brotliInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            log.info(new String(baos.toByteArray()));

            System.out.println("Unknown protocol version: " + protocolVersion);
        }
    }

    public static byte[] decompress(ByteBuf data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data.array(), data.arrayOffset() + data.readerIndex(), data.readableBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.readableBytes());
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress zlib data", e);
        } finally {
            inflater.end();
        }
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
            WebSocketClientProtocolHandler.ClientHandshakeStateEvent handshakeEvent = (WebSocketClientProtocolHandler.ClientHandshakeStateEvent) evt;
            if (handshakeEvent == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                handshakeFuture.setSuccess();
            } else if (handshakeEvent == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_TIMEOUT) {
                handshakeFuture.setFailure(new Exception("Handshake timed out"));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
