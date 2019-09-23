package org.liws.framework.i18n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class I18MessageHandlerImp implements I18MessageHandler {

    @Autowired
    private BQResourceBundleMessageSource rbms;

    @Override
    public String getMessage(String code, Object... args) {
        if (args == null) {
            args = new Object[0];
        }

        try {
        	/*if (code != null) {
				code = code.trim();
				String regex = "\\{.*?\\}";
				Pattern pattern = Pattern.compile(regex);
				Matcher m = pattern.matcher(code);
				int start = 0;
				int end = 0;
				StringBuilder sb = new StringBuilder();
				while (m.find(end)) {
					start = m.start();
					sb.append(code.substring(end, start));
					end = m.end();
					String subCode = code.substring(start + 1, end - 1);
					Locale locale = null;
					try {
						locale = LocaleContextHolder.getLocale();
						if(UserManager.getLoginUser() != null && UserManager.getLoginUser().getLocaleLang() != null){
							locale = UserManager.getLoginUser().getLocaleLang();
						}
						sb.append(rbms.getMessage(subCode, args, locale));
					} catch (NoSuchMessageException e) {//NOSONAR
						try {
							if (!Locale.CHINA.equals(locale)) {
								locale = Locale.US;
							}
							sb.append(rbms.getMessage(subCode, args, locale));
						}catch (NoSuchMessageException ex){
							sb.append("!").append(subCode).append("!");
						}
					}

				}
				if (end < code.length()) {
					sb.append(code.substring(end));
				}
				return sb.toString();
			}else{
				return "";
			}*/
        	
            return rbms.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {// NOSONAR
            return "!" + code + "!";
        }
    }

}
