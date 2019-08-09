package org.liws.framework.controller.exhandler;

import org.apache.shiro.authz.AuthorizationException;
import org.liws.framework.controller.model.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 项目基于shiro，就拦截shiro的异常AuthorizationException
 */
@ControllerAdvice(annotations= Controller.class)
public class BQExceptionHandler {

    @ExceptionHandler(value = AuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ExceptionResponse authorizationException(AuthorizationException exception) {
        // logger.error(exception);
        return ExceptionResponse.create(HttpStatus.UNAUTHORIZED.value(),exception.getMessage(),exception);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ExceptionResponse exception(Exception exception) {
        // logger.error(exception);
        return ExceptionResponse.create(HttpStatus.INTERNAL_SERVER_ERROR.value(),exception.getMessage(),exception);
    }
}
