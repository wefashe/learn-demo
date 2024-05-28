package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.ResponseData;
import org.example.enums.ResponseEnum;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
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
        return ResponseData.error(ResponseEnum.RC501, sb.toString());
    }

    /**
     * 处理空指针的异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData nullPointerExceptionHandler(NullPointerException e) {
        log.error("空指针异常 ", e);
        return ResponseData.error(ResponseEnum.RC400);
    }

    /**
     * 处理请求方式错误(405)异常
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseData requestMethodNotSupportedExceptionHandler(HttpServletRequest req, Exception e) {
        log.error("405异常, method = {}, path = {}", req.getMethod(), req.getServletPath(), e);
        return ResponseData.error(ResponseEnum.RC405);
    }

    /**
     * 处理404异常
     * @param e
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseData noHandlerFoundExceptionHandler(HttpServletRequest req, Exception e) {
        log.error("404异常, method = {}, path = {} ", req.getMethod(), req.getServletPath(), e);
        return ResponseData.error(ResponseEnum.RC404);
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
        return ResponseData.error(ResponseEnum.RC500);
    }
}

