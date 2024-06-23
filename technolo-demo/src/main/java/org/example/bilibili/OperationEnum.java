package org.example.bilibili;

/**
 * 封包类型
 */
public enum OperationEnum {
    /**
     * 心跳包
     */
    HEARTBEAT(2),

    /**
     * 心跳包回复（人气值）
     */
    HEARTBEAT_REPLY(3),

    /**
     * 普通包（命令）
     */
    MESSAGE(5),

    /**
     * 认证包
     */
    USER_AUTHENTICATION(7),

    /**
     * 认证包回复
     */
    CONNECT_SUCCESS(8),

    HANDSHAKE(0),
    HANDSHAKE_REPLY(1),
    SEND_MSG(4),
    DISCONNECT_REPLY(6),
    RAW(9),
    PROTO_READY(10),
    PROTO_FINISH(11),
    CHANGE_ROOM(12),
    CHANGE_ROOM_REPLY(13),
    REGISTER(14),
    REGISTER_REPLY(15),
    UNREGISTER(16),
    UNREGISTER_REPLY(17),
    ;

    private final int code;

    OperationEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OperationEnum getByCode(int code) {
        for (OperationEnum value : OperationEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
