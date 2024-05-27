package org.example.domain;

/**
 * 公共返回枚举
 */
public enum ResponseEnum  {

    RC200(200,"操作成功"),
    RC400(400,"请求失败，参数错误，请检查后重试"),
    RC404(404,"未找到您请求的资源"),
    RC405(405,"请求方式错误，请检查后重试"),
    RC500(500,"操作失败，服务器繁忙或服务器错误，请稍后再试"),
    RC501(501,"参数验证失败"),
    RC502(502,"请求参数有误"),

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
