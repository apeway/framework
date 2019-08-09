package org.liws.framework.util.http;

/**
 * http请求的返回结果
 * @author skynet
 *
 */
public class HttpRequestResult {
	private int code;
	private String content;
	
	public HttpRequestResult(int code, String content) {
		this.setCode(code);
		this.setContent(content);
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
}
