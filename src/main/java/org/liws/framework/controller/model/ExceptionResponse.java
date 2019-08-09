package org.liws.framework.controller.model;

import lombok.Getter;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

@Getter
public class ExceptionResponse implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private boolean success = false;
    private String message;
    private Integer code;
    private String errorStack;

    /**
     * Construction Method
     * @param code
     * @param message
     * @param errorStack
     */
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
