package org.liws.framework.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.liws.framework.controller.model.ResponseData;
import org.liws.framework.util.json.JSONUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * SpringMVC Controller基类
 */
public class CommonController {

	/**
	 * 返回Json格式数据
	 * @param rd 返回数据
	 * @return
	 */
	private String response(ResponseData rd) {
		return JSONUtil.toJson(rd.setSHAREJSESSIONID(getSession() == null ? null : getSession().getId()));
	}

	public String success(Object object) {
		return response(new ResponseData().setData(object));
	}
	public String success() {
		return response(new ResponseData());
	}

	public String error(String errorInfor) {
		return response(new ResponseData().setResult(ResponseData.ERROR).setDetail(errorInfor));
	}
	public String error(Exception e) {
		return error(e, ResponseData.ERROR);
	}
	public String warning(Exception e) {
		return error(e, ResponseData.WARNING);
	}
	public String error(Exception e, int resultStatus) {
		// TODO logger.error(e);
		ResponseData rd = new ResponseData(resultStatus)
				.setDetail(e.getMessage())
				.setStacktrace(getStackTrace(e));
		return response(rd);
	}
	/**
	 * 获取异常的堆栈信息
	 * @param t 异常信息
	 * @return 字符串异常信息
	 */
	private static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			String s = sw.toString();
			// 限制长度为4000
			if (s != null && s.length() > 4000) {
				s = s.substring(0, 4000);
			}
			return s;
		} finally {
			pw.close();
		}
	}

	public static HttpServletRequest getRequest() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attrs.getRequest();
	}

	public static HttpServletResponse getResponse() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attrs.getResponse();
	}

	public static HttpSession getSession() {
		return getRequest().getSession();
	}
	
	public <T> T getService(Class<T> interfaceClazz) {
		return null; // TODO
		// return ServiceFactory.getInstance().getService(interfaceClazz);
	}

	/**
	 * 统一拦截所有异常
	 * @param e 异常对象
	 * @return 返回给前端的Json信息
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public String exceptionHanlder(Exception e) {
		return error(e);
	}
	
}
