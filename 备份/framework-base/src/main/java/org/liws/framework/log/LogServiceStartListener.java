package org.liws.framework.log;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 目的使BQLogger更早生效
 */
public class LogServiceStartListener  implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		BQLogger.info("BQLogger has been destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		BQLogger.info("BQLogger has been inited");
	}

}
