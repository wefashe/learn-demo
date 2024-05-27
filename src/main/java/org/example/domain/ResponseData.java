package org.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;

/**
 * 数据统一响应
 * @param <T>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;
    private final long timestamp = System.currentTimeMillis();

    public ResponseData(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public ResponseData(Integer code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseData(ResponseEnum responseEnum) {
        this(responseEnum.getCode(), responseEnum.getMsg());
    }

    public ResponseData(ResponseEnum responseEnum, T data) {
        this(responseEnum.getCode(), responseEnum.getMsg(), data);
    }

    public static <T> ResponseData<T> success() {
        return new ResponseData<T>(ResponseEnum.RC200);
    }

    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<T>(ResponseEnum.RC200, data);
    }

    public static  <T> ResponseData<T> error(Integer code, String msg) {
        return new ResponseData<T>(code, msg);
    }

    public static  <T> ResponseData<T> error(ResponseEnum responseEnum) {
        return new ResponseData<T>(responseEnum);
    }

    public static  <T> ResponseData<T> error(ResponseEnum responseEnum, T data) {
        return new ResponseData<T>(responseEnum, data);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return ResponseEnum.RC200.getCode()  == this.getCode() ;
    }
}
