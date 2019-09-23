package org.liws.framework.util.web;

import javax.servlet.http.HttpServletRequest;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

public class DeviceUtil {

	/**
     * 判断请求是否手机端
     * @param req
     * @return
     */
    public static boolean isMobile(HttpServletRequest req) {
        UserAgent ua = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
        OperatingSystem os = ua.getOperatingSystem();
        if(DeviceType.MOBILE.equals(os.getDeviceType())) {
            return true;
        }
        return false;
    }
}
