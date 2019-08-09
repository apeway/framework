package org.liws.framework.controller.model;

import lombok.Getter;

@Getter
public class ResponseData {

	/** 成功 */
	public final static int SUCCESS = 0;
	/** 失败 */
	public final static int ERROR = 1;
	/** 警告 */
	public final static int WARNING = 2;

	/** 执行结果,0-成功，1-失败, 2-警告 */
	private int resultStatus;
	/** 描述信息 */
	private String detail;
	/** 数据对象 */
	private Object data;
	/** 错误堆栈信息 */
	private String stacktrace;
	/** sessionId */
	private String SHAREJSESSIONID;

	// 链式设置
	public ResponseData setResult(int result) {
		this.resultStatus = result;
		return this;
	}
	public ResponseData setDetail(String detail) {
		this.detail = detail;
		return this;
	}
	public ResponseData setData(Object data) {
		this.data = data;
		return this;
	}
	public ResponseData setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
		return this;
	}
	public ResponseData setSHAREJSESSIONID(String sHAREJSESSIONID) {
		SHAREJSESSIONID = sHAREJSESSIONID;
		return this;
	}

	/**
	 * 无参数构造方法，返回成功对象
	 */
	public ResponseData() {
		this.resultStatus = SUCCESS;
	}

	public ResponseData(int resultStatus) {
		this.resultStatus = resultStatus;
	}

}