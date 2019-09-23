package org.liws.framework.i18n;

import java.util.Locale;

public class I18nUtils {

	private static BQResourceBundleMessageSource bundleMessageSource = null;

	@SuppressWarnings("unused")
	public static String getMessage(String code, Object... args) {
		I18MessageHandler messageHandler = null; // SpringContextHolder.getBean(I18MessageHandler.class);
		if (messageHandler != null) {
			return messageHandler.getMessage("{" + code + "}", args);
		} else {
			synchronized (I18nUtils.class) {
				if (bundleMessageSource == null)
					bundleMessageSource = new BQResourceBundleMessageSource();
			}
			return bundleMessageSource.getMessage(code, args, Locale.getDefault());
		}
	}
	
	public static void main(String[] args) {
//		System.out.println(I18nUtils.getMessage(code, args));
	}
}
