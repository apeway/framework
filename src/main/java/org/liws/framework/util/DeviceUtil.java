package org.liws.framework.util;

import javax.servlet.http.HttpServletRequest;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

public abstract class DeviceUtil {
	private DeviceUtil() {}
	
	public static boolean isMobileDevice(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		return userAgent != null && (userAgent.matches(".*Android.*") || userAgent.matches(".*iPhone.*")
				|| userAgent.matches(".*iPad.*"));
	}
	
    public static boolean isMobileDevice2(HttpServletRequest req) {
        UserAgent ua = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
        OperatingSystem os = ua.getOperatingSystem();
        if(DeviceType.MOBILE.equals(os.getDeviceType())) {
            return true;
        }
        return false;
    }
    
}
