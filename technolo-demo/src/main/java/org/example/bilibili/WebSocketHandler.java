package org.example.bilibili;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.GlobalHeaders;
import cn.hutool.http.Header;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aayushatharva.brotli4j.Brotli4jLoader;
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
import java.io.IOException;
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
            processBinaryFrame( frame.content());
        }
    }

    private void processBinaryFrame(ByteBuf content) throws IOException {
        if (content.readableBytes() < 16) {
            return;
        }
        int packetLength = content.readInt();
        // 响应头长度
        int headerLength = content.readShort();
        // 协议版本
        int protoverCode = content.readShort();
        // 操作
        int operationCode = content.readInt();
        // 序号
        int sequence = content.readInt();

        int dataLength = packetLength - headerLength;
        ByteBuf data = content.readSlice(dataLength);

        ProtoverEnum protoverEnum = ProtoverEnum.getByCode(protoverCode);
        OperationEnum operationEnum = OperationEnum.getByCode(operationCode);

        if (protoverEnum == null) {
            log.warn(StrUtil.format("未知的协议版本: {}", protoverCode));
        }

        if (operationEnum == null) {
            log.warn(StrUtil.format("未知的操作编号: {}", protoverCode));
        }
        String jsonStr = data.toString(CharsetUtil.UTF_8);
        switch (operationEnum) {
            case HEARTBEAT_REPLY:
                // 正文分为两个部分，第一部分是人气值 [uint32整数，代表房间当前的人气值]
                // 第二部分是对于心跳包内容的复制，心跳包正文是什么这里就会回应什么
                System.out.println("心跳回复,人气: "+ data.readInt());
                // 读取剩余的所有内容
                byte[] body = new byte[content.readableBytes()];
                content.readBytes(body);
                System.out.println(new String(body, CharsetUtil.UTF_8));
                break;
            case CONNECT_SUCCESS: {
                JSONObject obj = JSONUtil.parseObj(jsonStr);
                if ("0".equals(obj.getStr("code"))) {
                    log.debug("认证成功!");
                }
                break;
            }
            case MESSAGE: {
                if (protoverEnum == ProtoverEnum.NORMAL_ZLIB) {
                    processBinaryFrame(decompressZlib(data));
                } else if (protoverEnum == ProtoverEnum.NORMAL_BROTLI) {
                    processBinaryFrame(decompressBrotli(data));
                } else {
                    JSONObject obj = JSONUtil.parseObj(jsonStr);
                    CmdMsgEnum cmdMsgEnum = CmdMsgEnum.getByString(obj.getStr("cmd"));
                    switch (cmdMsgEnum) {
                        case INTERACT_WORD:
                            System.out.println(StrUtil.format("{}进入直播间", obj.getJSONObject("data").getStr("uname")));
                            break;
                        case WATCHED_CHANGE:
                            System.out.println(StrUtil.format("看过人数刷新: {}人看过", obj.getJSONObject("data").getStr("num")));
                            break;
                        case LIKE_INFO_V3_UPDATE:
                            System.out.println(StrUtil.format("点赞人数刷新: {}人点赞", obj.getJSONObject("data").getStr("click_count")));
                            break;
                        case LIKE_INFO_V3_CLICK:
                            System.out.println(StrUtil.format("{}{}", obj.getJSONObject("data").getStr("uname"), obj.getJSONObject("data").getStr("like_text")));
                            break;
                        case ONLINE_RANK_V2:
                            System.out.println(StrUtil.format("高能用户刷新: {}个高能", obj.getJSONObject("data").getJSONArray("list").size()));
                            break;
                        case ONLINE_RANK_COUNT:
                            System.out.println(StrUtil.format("高能用户: {}", obj.getJSONObject("data").getStr("count")));
                            break;
                        case ROOM_REAL_TIME_MESSAGE_UPDATE:
                            System.out.println(StrUtil.format("当前粉丝数: {}, 粉丝团人数: {}", obj.getJSONObject("data").getStr("fans"), obj.getJSONObject("data").getStr("fans_club")));
                            break;
                        case STOP_LIVE_ROOM_LIST:
                            System.out.println(StrUtil.format("当前下播直播间数: {}", obj.getJSONObject("data").getJSONArray("room_id_list").size()));
                            break;
                        case DANMU_MSG:
                            System.out.println(StrUtil.format("{}: {}", obj.getJSONArray("info").get(2, JSONArray.class).get(1, String.class), obj.getJSONArray("info").get(1)));
                            break;
                        default:
                            System.out.println(jsonStr);
                            break;
                    }
                }
                break;
            }
            default:
                System.out.println(jsonStr);
                break;
        }
    }

    public static ByteBuf decompressZlib(ByteBuf data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data.array(), data.arrayOffset() + data.readerIndex(), data.readableBytes());

        ByteBuf buffer = Unpooled.buffer(data.readableBytes());
        byte[] bytes = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(bytes);
                buffer.writeBytes(bytes, 0, count);
            }
            return buffer;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress zlib data", e);
        } finally {
            inflater.end();
        }
    }


    public static ByteBuf decompressBrotli(ByteBuf data) {
        try {
            byte[] inputBytes = new byte[data.readableBytes()];
            data.readBytes(inputBytes);
            Brotli4jLoader.ensureAvailability();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputBytes);
            ByteBuf buffer = Unpooled.buffer(data.readableBytes());

            byte[] bytes = new byte[1024];
            BrotliInputStream brotliInputStream = new BrotliInputStream(byteArrayInputStream);;
            int count;
            while ((count = brotliInputStream.read(bytes)) > -1) {
                buffer.writeBytes(bytes, 0, count);
            }
            byteArrayInputStream.close();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress Brotli data", e);
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
        System.out.println("连接断开");
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
