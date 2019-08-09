package org.liws.framework.util.http;

/**
 * http请求异常
 */
public class HttpRequestException extends Exception {
	private static final long serialVersionUID = 2150783759254440351L;

	public HttpRequestException() {
	}

	public HttpRequestException(String message) {
		super(message);
	}

	public HttpRequestException(Throwable cause) {
		super(cause);
	}

	public HttpRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpRequestException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
