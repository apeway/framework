package org.liws.framework.i18n;

/**
 * 服务端国际化接口,国际化文件放在resources/messages下
 */
public interface I18MessageHandler {

	/**
	 * 根据编码获取多语，参数占位使用{数字}，第一个从0开始，
	 * 如果没有发现国际化多语，返回是：!code!
	 * @param code
	 * @param args
	 * @return
	 */
	String getMessage(String code, Object... args);
}
