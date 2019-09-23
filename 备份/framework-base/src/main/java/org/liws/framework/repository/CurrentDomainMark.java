package org.liws.framework.repository;

import com.yonyou.bq.framework.shiro.UserManager;
import com.yonyou.bq.framework.vo.UserVO;

/**
 * Created by zuoym on 2017/7/14.
 */
public class CurrentDomainMark {
    private final static ThreadLocal<String> defaultMark = new ThreadLocal<>();

    private final static ThreadLocal<String> primaryMark = new ThreadLocal<>();

    public static String getCurrentDomainMark() {
        if (primaryMark.get() != null) {
            return primaryMark.get();
        }
        UserVO user = null;
        try {
            user = UserManager.getLoginUser();
        } catch (Exception ex) {
            //eat it
        }
        if (user != null && user.getDomainMark() != null) {
            return user.getDomainMark();
        } else {
            return defaultMark.get() == null ? "00000" : defaultMark.get();
        }
    }

    public static boolean isSysAdmin() {
        return SysadminConst.DOMAIN_MARK.equals(getCurrentDomainMark())
                && UserManager.getLoginUser() != null
                && SysadminConst.ROLE_NAME.equalsIgnoreCase(UserManager.getLoginUser().getRoleNames());
    }


    public static void setDefaultCurrentDomainMark(String defaultCurrentDomainMark) {
        if (defaultCurrentDomainMark == null) {
            defaultMark.remove();
        } else {
            defaultMark.set(defaultCurrentDomainMark);
        }

    }

    public static void setPrimaryCurrentDomainMark(String primaryCurrentDomainMark) {
        if (primaryCurrentDomainMark == null) {
            primaryMark.remove();
        } else {
            primaryMark.set(primaryCurrentDomainMark);
        }

    }

    public static String getPrimaryCurrentDomainMark() {
        return primaryMark.get();

    }
}
