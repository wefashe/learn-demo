package org.example.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

/**
 * 数据统一响应
 * @param <T>
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;
    private long timestamp ;

    public ResponseData(ResponseEnum resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.timestamp = System.currentTimeMillis();
    }

    public ResponseData(ResponseEnum resultCode, T data) {
        this(resultCode);
        this.data = data;
    }

    public static <T> ResponseData<T> success() {
        return new ResponseData<T>(ResponseEnum.RC1000);
    }

    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<T>(ResponseEnum.RC1000, data);
    }

    public static  <T> ResponseData<T> error(ResponseEnum resultCode) {
        return new ResponseData<T>(resultCode);
    }

    public static  <T> ResponseData<T> error(ResponseEnum resultCode, T data) {
        return new ResponseData<T>(resultCode, data);
    }
}
