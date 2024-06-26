package org.example.steam;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.example.steam.codec.SteammessagesAuthSteamclient;
import org.example.steam.codec.SteammessagesBase;
import org.example.steam.codec.SteammessagesClientserverLogin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClient client;
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public WebSocketHandler(WebSocketClient client) {
        this.client = client;
        HttpHeaders customHeaders = new DefaultHttpHeaders();
        customHeaders.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                client.getUri(), WebSocketVersion.V13, null, false,customHeaders);
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
        }  else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
            ByteBuf content = frame.content();
            handleMessage(content);
        } else {
            log.warn("其他类型{}", msg.getClass());
        }
    }

    public void handleMessage(ByteBuf msg) throws InvalidProtocolBufferException {
        // 读取无符号的 32 位小端整数
        long rawEmsg = msg.readUnsignedIntLE();
        long hdrLength = msg.readUnsignedIntLE();

        // 读取从第8字节开始的hdrLength字节
        ByteBuf hdrBuf = msg.slice(msg.readerIndex(), (int) hdrLength);
        // 更新读取索引
        msg.readerIndex(msg.readerIndex() + (int) hdrLength);
        // 读取剩余的字节作为消息体
        ByteBuf msgBody = msg.slice(msg.readerIndex(), msg.readableBytes());

        int PROTO_MASK = 0x80000000;
        // 检查消息是否是ProtoBuf类型
        if ((rawEmsg & PROTO_MASK) == 0) {
            throw new IllegalArgumentException("Received unexpected non-protobuf message " + rawEmsg);
        }
        SteammessagesBase.CMsgProtoBufHeader protoBufHeader = SteammessagesBase.CMsgProtoBufHeader.parseFrom(hdrBuf.nioBuffer());

        if (protoBufHeader.getClientSessionid() != Constant.getClientSessionid()) {
            Constant.setClientSessionid(protoBufHeader.getClientSessionid());
        }

        EMsg eMsg = EMsg.from((int) (rawEmsg & ~PROTO_MASK));
        if (eMsg != EMsg.Multi) {
            log.debug("返回消息类型: {} ({})", eMsg, protoBufHeader.getTargetJobName());
        }
        if (Constant.getJobs().containsKey(protoBufHeader.getJobidTarget())) {
            EResult result = EResult.from(protoBufHeader.getEresult());
            if (result != EResult.OK) {
                String errorMessage = protoBufHeader.getErrorMessage();
                log.error("返回错误消息: {} ({})", result, errorMessage);
            } else {
                SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Response response =
                        SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Response.parseFrom(msgBody.nioBuffer());
                String refreshToken = response.getRefreshToken();
                if (StrUtil.isBlankIfStr(refreshToken)) {
                    refreshToken = Constant.getJobs().get(protoBufHeader.getJobidTarget());
                }
                log.info("refreshToken: {}", refreshToken);
                String accessToken = response.getAccessToken();
                String cookie = URLUtil.encode(StrUtil.format("steamLoginSecure={}||{}",
                        TokenUtil.decodeToken(response.getAccessToken()).getLong("sub"), response.getAccessToken()));
                log.info("accessToken: {}", accessToken);
                log.info("cookie: {}", cookie);
            }
            Constant.getJobs().remove(protoBufHeader.getJobidTarget());
            return;
        }
        // 这不是一个响应消息，所以需要确定它是什么类型的消息
        switch (eMsg) {
            case ClientLogOnResponse:
                SteammessagesClientserverLogin.CMsgClientLogonResponse response =
                        SteammessagesClientserverLogin.CMsgClientLogonResponse.parseFrom(msgBody.nioBuffer());
                EResult result = EResult.from(response.getEresult());
                if (result == EResult.OK) {

                } else if (result == EResult.TryAnotherCM || result == EResult.ServiceUnavailable) {
                    log.error("返回错误消息: {}", result);
                    // try
                }
                break;
            case Multi:
               processMultiMsg(msgBody);
                break;

            default:
                System.out.printf("Received unexpected message: " + eMsg.getCode());
        }

    }

    public void processMultiMsg(ByteBuf body) throws InvalidProtocolBufferException {
        SteammessagesBase.CMsgMulti cMsgMulti = SteammessagesBase.CMsgMulti.parseFrom(body.nioBuffer());
        byte[] payload = cMsgMulti.getMessageBody().toByteArray();
        if (cMsgMulti.getSizeUnzipped() > 0) {
            payload = decompressGzip(payload);
        }
        ByteBuf buffer = Unpooled.wrappedBuffer(payload);
        while (buffer.readableBytes() > 0) {
            long chunkSize = buffer.readUnsignedIntLE();
            handleMessage(buffer.readSlice((int) chunkSize));
            buffer = buffer.slice(buffer.readerIndex(), buffer.readableBytes());
        }
    }

    public static byte[] decompressGzip(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("解压缩 gzip 数据失败", e);
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
