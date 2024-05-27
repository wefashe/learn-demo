package org.example.exception;

import org.example.domain.Result;
import org.example.domain.ResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ResponseBody
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    protected static Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handlerValidationExceptions(MethodArgumentNotValidException ex){
        logger.error("[handlerValidationExceptions]", ex);
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach(error->{
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            sb.append(fieldName).append(": ").append(errorMessage).append(";");
        });
        return Result.error(ResultEnum.PARAM_ERROR, sb.toString());
    }

    public Result defaultExceptionHandler(Throwable ex){
        logger.error("[defaultExceptionHandler]", ex);
        return Result.error(ResultEnum.UNKNOWN_ERROR);
    }

}
