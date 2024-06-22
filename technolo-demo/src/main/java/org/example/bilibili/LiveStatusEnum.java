package org.example.bilibili;

public enum LiveStatusEnum {
    /**
     * 未开播
     */
    STOPPED(0),

    /**
     * 直播中
     */
    LIVING(1),

    /**
     * 投稿视频轮播
     */
    CAROUSEL(2),
    ;

    private final int code;

    LiveStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LiveStatusEnum getByCode(int code) {
        for (LiveStatusEnum value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
