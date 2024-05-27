package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

/**
 * 公共返回类
 * @param <T>
 */
@Data
@AllArgsConstructor
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    private Result(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
    }

    private Result(ResultEnum resultEnum, T data) {
        this(resultEnum);
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<T>(ResultEnum.SUCCESS);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultEnum.SUCCESS, data);
    }

    public static  <T> Result<T> error(ResultEnum resultEnum) {
        return new Result<T>(resultEnum);
    }

    public static  <T> Result<T> error(ResultEnum resultEnum, T data) {
        return new Result<T>(resultEnum, data);
    }
}
