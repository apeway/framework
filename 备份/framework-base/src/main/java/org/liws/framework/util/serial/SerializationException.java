package org.liws.framework.util.serial;

import org.liws.framework.exception.BusinessRuntimeException;

public class SerializationException extends BusinessRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public SerializationException() {
		super();
	}

	public SerializationException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public SerializationException(String msg) {
		super(msg);
	}
	
	public SerializationException(Throwable throwable) {
		this(throwable.getMessage(),throwable);
	}

}
