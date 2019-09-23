package org.liws.framework.exception;

/**
 * 业务异常类<br>
 * 服务器端不捕获这类异常，直接将它们转发给客户端。<br>
 * 服务接口中应对此类异常进行声明。
 */
public class BusinessException extends Exception {

	private static final long serialVersionUID = 1L;

	public BusinessException() {
		super();
	}

	public BusinessException(String s) {
		super(s);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}
}
