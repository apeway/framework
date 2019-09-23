package org.liws.framework.exception;

/**
 * 运行时业务异常类
 */
public class BusinessRuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public BusinessRuntimeException() {
		super();
	}

	public BusinessRuntimeException(String msg) {
		super(msg);
	}

	public BusinessRuntimeException(String msg, Throwable throwable) {
		super(msg, throwable);
	}
	
	public BusinessRuntimeException(Throwable throwable) {
		super(throwable);
	}

}
