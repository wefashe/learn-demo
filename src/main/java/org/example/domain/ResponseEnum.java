package org.example.domain;

/**
 * 公共返回枚举
 */
public enum ResponseEnum  {

    RC1000(1000,"操作成功"),
    RC9999(9999,"系统异常，请稍后重试"),
    RC1001(1001,"参数验证失败"),

    ;

    private final Integer code;
    private final String msg;

    ResponseEnum(Integer code, String msg) {
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
