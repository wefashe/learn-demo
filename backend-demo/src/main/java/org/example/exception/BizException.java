package org.example.exception;
import lombok.Data;
import org.example.enums.ResponseEnum;

/**
 * 自定义异常
 */
@Data
public class BizException extends RuntimeException{

    /**
     * 错误码
     */
    protected Integer errorCode;
    /**
     * 错误信息
     */
    protected String errorMsg;

    public BizException() {
        super();
    }

    public BizException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BizException(ResponseEnum responseEnum) {
        this(responseEnum.getCode(), responseEnum.getMsg());
    }

    public BizException(ResponseEnum responseEnum, Throwable cause) {
        this(responseEnum.getCode(), responseEnum.getMsg(), cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
