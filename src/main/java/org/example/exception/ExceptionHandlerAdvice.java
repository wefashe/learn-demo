package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.ResponseData;
import org.example.domain.ResponseEnum;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 */
@Slf4j
@ResponseBody
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    /**
     * 处理自定义的业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseData bizExceptionHandler(BizException e){
        log.warn("发生业务异常！原因是：{}", e.getErrorMsg(), e);
        return ResponseData.error(e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * 参数校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = {BindException.class, ValidationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData validationExceptionsHandler(Exception e){
        log.error("参数校验异常", e);
        StringBuilder sb = new StringBuilder();
        if (e instanceof MethodArgumentNotValidException) {
            sb.append(((MethodArgumentNotValidException) e).getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining("; ")));
        } else if (e instanceof ConstraintViolationException) {
            sb.append(((ConstraintViolationException) e).getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; ")));
        } else if (e instanceof BindException) {
            sb.append(((BindException) e).getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining("; ")));
        }
        return ResponseData.error(ResponseEnum.RC1001, sb.toString());
    }

    /**
     * 其余系统未知异常
     * @param e
     * @return
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseData throwableHandler(Throwable e){
        log.error("系统未知异常！", e);
        return ResponseData.error(ResponseEnum.RC9999);
    }
}

