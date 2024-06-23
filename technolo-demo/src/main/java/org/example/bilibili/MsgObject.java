package org.example.bilibili;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;


public class MsgObject {

    private ProtocolEnum protocolEnum;

    private OperationEnum operationEnum;

    private byte[] data;

    public MsgObject(ProtocolEnum protocolEnum, OperationEnum operationEnum) {
        this.protocolEnum = protocolEnum;
        this.operationEnum = operationEnum;
    }

    public MsgObject(ProtocolEnum protocolEnum, OperationEnum operationEnum, byte[] data) {
        this.protocolEnum = protocolEnum;
        this.operationEnum = operationEnum;
        this.data = data;
    }

    public MsgObject(ProtocolEnum protocolEnum, OperationEnum operationEnum, ByteBuf data) {
        this.protocolEnum = protocolEnum;
        this.operationEnum = operationEnum;
        byte[] body = new byte[data.readableBytes()];
        data.readBytes(body);
        this.data = body;
    }

    public MsgObject(ProtocolEnum protocolEnum, OperationEnum operationEnum, String data) {
        this.protocolEnum = protocolEnum;
        this.operationEnum = operationEnum;
        String jsonStr = StrUtil.EMPTY;
        if (operationEnum != OperationEnum.HEARTBEAT) {
            jsonStr = data;
            if (StrUtil.EMPTY_JSON.equals(data)) {
                jsonStr = StrUtil.EMPTY;
            }
        }
        this.data = jsonStr.getBytes(CharsetUtil.UTF_8);
    }

    public MsgObject(ProtocolEnum protocolEnum, OperationEnum operationEnum, JSONObject data) {
        this(protocolEnum, operationEnum, data.toString());
    }

    public ProtocolEnum getProtocolEnum() {
        return protocolEnum;
    }

    public void setProtocolEnum(ProtocolEnum protocolEnum) {
        this.protocolEnum = protocolEnum;
    }

    public OperationEnum getOperationEnum() {
        return operationEnum;
    }

    public void setOperationEnum(OperationEnum operationEnum) {
        this.operationEnum = operationEnum;
    }

    public byte[] getDataBytes() {
        return data;
    }

    public ByteBuf getDataByteBuf() {
        return Unpooled.wrappedBuffer(data);
    }

    public void setDataBytes(byte[] data) {
        this.data = data;
    }

    public void setDataByteBuf(ByteBuf data) {
        byte[] body = new byte[data.readableBytes()];
        data.readBytes(data);
        this.data = body;
    }

    public void setDataString(String data) {
        this.data = data.getBytes(CharsetUtil.UTF_8);
    }

    public int getDataLength() {
        return data.length;
    }
}
