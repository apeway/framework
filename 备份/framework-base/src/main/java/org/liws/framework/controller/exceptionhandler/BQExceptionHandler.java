package org.liws.framework.controller.exceptionhandler;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.shiro.authz.AuthorizationException;
import org.liws.framework.log.BQLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@ControllerAdvice(annotations = Controller.class)
public class BQExceptionHandler {

	@ExceptionHandler(value = AuthorizationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public ExceptionResponse authorizationException(AuthorizationException exception) {
		BQLogger.error(exception);
		return ExceptionResponse.create(HttpStatus.UNAUTHORIZED.value(), exception.getMessage(), exception);
	}

	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ExceptionResponse exception(Exception exception) {
		BQLogger.error(exception);
		return ExceptionResponse.create(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), exception);
	}
}

@Getter
class ExceptionResponse implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private boolean success = false;
    private String message;
    private Integer code;
    private String errorStack;

    private ExceptionResponse(Integer code, String message,String errorStack){
        this.message = message;
        this.code = code;
        this.errorStack = errorStack;
    }

    public static ExceptionResponse create(Integer code, String message,Throwable throwable){
        StringWriter sw = new StringWriter();
        if(throwable != null){
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
        }
        return new ExceptionResponse(code, message,sw.toString());
    }
}