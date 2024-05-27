package org.example.domain;

/**
 * 公共返回枚举
 */
public enum ResultEnum {

    SUCCESS(1000, "成功。"),

    PARAM_ERROR(1001,"参数验证失败。"),

    UNKNOWN_ERROR(9999,"未知错误。"),
    ;

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
