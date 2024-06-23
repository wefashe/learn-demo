package org.example.bilibili;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.zip.Inflater;

@Slf4j
public class MessageCodecUtil {

    public static int sequence = 0;
    public static final short HEADER_LENGTH = 16;
    private static final int BUFFER_SIZE = 1024;

    public static ByteBuf encode(MsgObject msg) {
        int totalLength = HEADER_LENGTH + msg.getDataLength();
        ByteBuf buffer = Unpooled.buffer(totalLength);
        // 封包总大小(头部大小+正文大小 32字节)
        buffer.writeInt(totalLength);
        // 头部大小(一般为0x0010，16字节)
        buffer.writeShort(HEADER_LENGTH);
        // 协议版本: 16字节
        // 0普通包正文不使用压缩
        // 1心跳及认证包正文不使用压缩
        // 2普通包正文使用zlib压缩
        // 3普通包正文使用brotli压缩,解压为一个带头部的协议0普通包
        buffer.writeShort(msg.getProtocolEnum().getCode());
        // 操作码（封包类型） 32字节
        buffer.writeInt(msg.getOperationEnum().getCode());
        // sequence，每次发包时向上递增 32字节
        buffer.writeInt(++sequence);
        // 正文数据
        buffer.writeBytes(msg.getDataBytes());
        return buffer;
    }

    public static List<MsgObject> decode(ByteBuf buffer) {
        List<MsgObject> msgList = new LinkedList<>();
        Queue<ByteBuf> byteBufQueue = new LinkedList<>();
        do {
            MsgObject msg = decode(buffer, byteBufQueue);
            if (msg != null && !msgList.contains(msg)) {
                msgList.add(msg);
            }
            buffer = byteBufQueue.poll();
        } while (buffer != null);

        return msgList;
    }

    public static MsgObject decode(ByteBuf buffer, Queue<ByteBuf> byteBufQueue) {
        int totalLength = buffer.readInt();
        short headerLength = buffer.readShort();
        short protocolVer = buffer.readShort();
        int operationCode = buffer.readInt();
        int sequence = buffer.readInt();

        ProtocolEnum protocolEnum = ProtocolEnum.getByCode(protocolVer);
        if (protocolEnum == null) {
            log.warn(StrUtil.format("未知的协议版本: {}", protocolVer));
        }

        OperationEnum operationEnum = OperationEnum.getByCode(operationCode);
        if (operationEnum == null) {
            log.warn(StrUtil.format("未知的操作编码: {}", operationCode));
        }


        MsgObject msg = new MsgObject(protocolEnum, operationEnum);

        byte[] data = new byte[totalLength-headerLength];
        buffer.readBytes(data);
        if (buffer.readableBytes() != 0) {
            byteBufQueue.offer(buffer);
        }

        switch (operationEnum) {
            case HEARTBEAT_REPLY:
                msg.setDataString(StrUtil.toString(Unpooled.wrappedBuffer(data).readInt()));
                break;
            case MESSAGE: {
                if (protocolEnum == ProtocolEnum.NORMAL_ZLIB) {
                    msg.setDataBytes(decode(Unpooled.wrappedBuffer(decompressZlib(data)), byteBufQueue).getDataBytes());
                } else if (protocolEnum == ProtocolEnum.NORMAL_BROTLI) {
                    msg.setDataBytes(decode(Unpooled.wrappedBuffer(decompressBrotli(data)),byteBufQueue).getDataBytes());
                } else {
                    msg.setDataBytes(data);
                }
                break;
            }
            default:
                msg.setDataBytes(data);
                break;
        }
        return msg;
    }


    public static byte[] decompressZlib(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) break;
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("解压缩 zlib 数据失败", e);
        } finally {
            inflater.end();
        }
    }

    public static byte[] decompressBrotli(byte[] data) {
        Brotli4jLoader.ensureAvailability();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BrotliInputStream brotliInputStream = new BrotliInputStream(byteArrayInputStream)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = brotliInputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("解压缩 Brotli 数据失败", e);
        }
    }

}
