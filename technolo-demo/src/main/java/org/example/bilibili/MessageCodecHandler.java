package org.example.bilibili;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

public class MessageCodecHandler extends MessageToMessageCodec<BinaryWebSocketFrame, MsgObject> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MsgObject msg, List<Object> list) throws Exception {
        list.add(new BinaryWebSocketFrame(MessageCodecUtil.encode(msg)));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame, List<Object> list) throws Exception {
//        list.addAll(MessageCodecUtil.decode(frame.content()).toList(MsgObject.class));
    }

}
