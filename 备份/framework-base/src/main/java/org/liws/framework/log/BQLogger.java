package org.liws.framework.log;


import org.liws.framework.spring.SpringContextHolder;
import org.liws.framework.util.UtilTools;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志框架,配置文件log4j2.xml
 * @author jiwenlong
 *
 */
public class BQLogger {

	private static Logger rootLogger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
	private static String hostAddress = "unknown host";
	private volatile static ConcurrentHashMap<ClassLoader, String> LoaderMap = new ConcurrentHashMap<ClassLoader, String>();
	private static final String AE_NOT_WEB = "AENotWeb";
	
	static{
			try {
				hostAddress = InetAddress.getLocalHost().getHostAddress();
//				ClassLoader classLoader1 = ContextClassLoaderHelper.findClassLoaderByContext("/console");
//				if(classLoader1 != null){
//					getContextLogger(classLoader1);
//				}
//				ClassLoader classLoader2 = ContextClassLoaderHelper.findClassLoaderByContext("/bq_self");
//				if(classLoader2 != null){
//					getContextLogger(classLoader2);
//				}
//				ClassLoader classLoader3 = ContextClassLoaderHelper.findClassLoaderByContext("/report");
//				if(classLoader3 != null){
//					getContextLogger(classLoader3);
//				}
//				ClassLoader classLoader4 = ContextClassLoaderHelper.findClassLoaderByContext("/di");
//				if(classLoader4 != null){
//					getContextLogger(classLoader4);
//				}
			} catch (UnknownHostException e) {
				error("unknown Host", e);
			}
	}
	
	public static boolean isDebug(){
		return getContextLogger().isDebugEnabled();
	}
	
	public static boolean isInfo(){
		return getContextLogger().isInfoEnabled();
	}
	
	/**
	 * @param msg 打印的消息
	 */
	public static void debug(Object msg) {
		if (msg instanceof Throwable) {
			Throwable e = (Throwable) msg;
			debug(e.getMessage(), e);
			return;
		}
		getContextLogger().debug(wrapMsg(msg));
	}
	
	/**
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 */
	public static void debug(String msg, Object... params){
		getContextLogger().debug(wrapMsg(msg), params);
	}

	/**
	 * @param msg 消息
	 * @param t 异常
	 */
	public static void debug(Object msg, Throwable t) {
		getContextLogger().debug(wrapMsg(msg), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param t 消息
	 * @since 1.0.0.06
	 */
	public static void debug(LogMarker marker, Throwable t) {
		debug(marker, t.getMessage(), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 * @since 1.0.0.06
	 */
	public static void debug(LogMarker marker, String msg, Object... params){
		getContextLogger(marker).debug(wrapMsg(msg), params);
	}

	/**
	 * @param marker 另输出文件的标识
	 * @param msg
	 * @param t
	 * @since 1.0.0.06
	 */
	public static void debug(LogMarker marker, Object msg, Throwable t) {
		getContextLogger(marker).debug(wrapMsg(msg), t);
	}

	/**
	 * @param msg 打印的消息
	 */
	public static void info(Object msg) {
		if (msg instanceof Throwable) {
			Throwable e = (Throwable) msg;
			info(e.getMessage(), e);
			return;
		}
		getContextLogger().info(wrapMsg(msg));
	}
	
	/**
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 */
	public static void info(String msg, Object... params){
		getContextLogger().info(wrapMsg(msg), params);
	}
	
	/**
	 * @param msg 消息
	 * @param t 异常
	 */
	public static void info(Object msg, Throwable t) {
		getContextLogger().info(wrapMsg(msg), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param t 打印的消息
	 * @since 1.0.0.06
	 */
	public static void info(LogMarker marker, Throwable t) {
		info(marker, t.getMessage(), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 * @since 1.0.0.06
	 */
	public static void info(LogMarker marker, String msg, Object... params){
		getContextLogger(marker).info(wrapMsg(msg), params);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param msg 消息
	 * @param t 异常
	 * @since 1.0.0.06
	 */
	public static void info(LogMarker marker, Object msg, Throwable t) {
		getContextLogger(marker).info(wrapMsg(msg), t);
	}

	/**
	 * @param msg 打印的消息
	 */
	public static void warn(Object msg) {
		if (msg instanceof Throwable) {
			Throwable e = (Throwable) msg;
			warn(e.getMessage(), e);
			return;
		}
		getContextLogger().warn(wrapMsg(msg));
	}
	
	/**
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 */
	public static void warn(String msg, Object... params){
		getContextLogger().debug(msg, params);
	}
	
	/**
	 * @param msg 消息
	 * @param t 异常
	 */
	public static void warn(Object msg, Throwable t) {
		getContextLogger().warn(wrapMsg(msg), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param t 打印的消息
	 * @since 1.0.0.06
	 */
	public static void warn(LogMarker marker, Throwable t) {
		warn(marker, t.getMessage(), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 * @since 1.0.0.06
	 */
	public static void warn(LogMarker marker, String msg, Object... params){
		getContextLogger(marker).debug(msg, params);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param msg 消息
	 * @param t 异常
	 * @since 1.0.0.06
	 */
	public static void warn(LogMarker marker, Object msg, Throwable t) {
		getContextLogger(marker).warn(wrapMsg(msg), t);
	}

	/**
	 * @param msg 打印的消息
	 */
	public static void error(Object msg) {
		if (msg instanceof Throwable) {
			Throwable e = (Throwable) msg;
			error(e.getMessage(), e);
			return;
		}
		getContextLogger().error(wrapMsg(msg));
	}
	
	/**
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 */
	public static void error(String msg, Object... params){
		getContextLogger().error(wrapMsg(msg), params);
	}

	/**
	 * @param msg 消息
	 * @param t 异常
	 */
	public static void error(Object msg, Throwable t) {
		getContextLogger().error(wrapMsg(msg), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param t 打印的消息
	 * @since 1.0.0.06
	 */
	public static void error(LogMarker marker, Throwable t) {
		error(marker, t.getMessage(), t);
	}
	
	/**
	 * @param marker 另输出文件的标识
	 * @param msg 中用{}占位,用参数替换
	 * @param params 参数的个数等于msg中{}的个数
	 * @since 1.0.0.06
	 */
	public static void error(LogMarker marker, String msg, Object... params){
		getContextLogger(marker).error(wrapMsg(msg), params);
	}

	/**
	 * @param marker 另输出文件的标识
	 * @param msg 消息
	 * @param t 异常
	 * @since 1.0.0.06
	 */
	public static void error(LogMarker marker, Object msg, Throwable t) {
		getContextLogger(marker).error(wrapMsg(msg), t);
	}

	/**
	 * 消息包装
	 * @param msg 消息
	 * @return
	 */
	private static String wrapMsg(Object msg) {
		StringBuffer sb = new StringBuffer();
		sb.append("$$host=");
		sb.append(hostAddress);
		sb.append(" $$msg=");
		sb.append(msg);
		return sb.toString();
	}
	
	/**
	 * 返回对应产品的logger
	 * @return Logger
	 */
	private static Logger getContextLogger(){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return getContextLogger(loader);

	}

	private static Logger getContextLogger(ClassLoader loader) {
		if(LoaderMap.containsKey(loader)){
			return LogManager.getLogger(LoaderMap.get(loader));
		}else{
			String className = loader.getClass().getName();
			if (className.startsWith("org.apache.catalina.loader")){
				if(!LoaderMap.containsKey(loader)){
					synchronized (Thread.currentThread().getContextClassLoader()){
						if (LoaderMap.get(loader) == null){
							try {
								Class<?> webappClassLoader = loader.getClass();
								Method contextMethod = webappClassLoader.getMethod("getContextName");
								Object contextName = contextMethod.invoke(loader);
								if (contextName != null){
									// 判断contextName是否有对应的Logger,若没有生成默认
									String context = contextName.toString().replace("/", "");
									if(StringUtils.isEmpty(context)){
										context = "bq";
									}
									addDefaultLoggerIfAbsent(context, context);
									LoaderMap.put(loader, context);
								}
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException t) {
								rootLogger.error("when get context logger occur error and return rootLogger",t);
								return rootLogger;
							}
						}
					}
				}
				return LogManager.getLogger(LoaderMap.get(loader));
			}else{
				// 如设计器等，输出到ae.log中  other中全是第三方的
				return LogManager.getLogger(AE_NOT_WEB);
			}
		}
	}

	/**
	 * 获取Marker对应的Logger
	 * @param marker
	 * @return
	 * @since 1.0.0.06
	 */
	private static Logger getContextLogger(LogMarker marker){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String contextName = LoaderMap.get(loader);
		//如果contextName为空，调用getContextLogger初始化一下
		if (StringUtils.isEmpty(contextName)){
			getContextLogger();
		}
		//构建对应的日志输出
		if (!StringUtils.isEmpty(contextName)){
			String loggerName = marker.getName();
			addDefaultLoggerIfAbsent(contextName, loggerName);
			return LogManager.getLogger(loggerName);
		}
		return rootLogger;
	}
	
	/**
	 * 为Context增加一个默认Logger
	 * @param contextName
	 * @since 1.0.0.06
	 */
	private static void addDefaultLoggerIfAbsent(String contextName, String loggerName){
//		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//		Configuration config = ctx.getConfiguration();
//		Map<String, LoggerConfig> mapConf = config.getLoggers();
//		if (mapConf.containsKey(loggerName)){
//			return;
//		}
//		String fileDir = contextName;
//		String filePre = loggerName;
//        PatternLayout.Builder builder = PatternLayout.newBuilder();
//        builder.withCharset(Charset.defaultCharset())
//                .withPattern("[%-5p] %d{yyyy-MM-dd HH:mm:ss.SSS} $$thread=[%t] %c{15}:%M(%L) %m%n")
//                .withConfiguration(config).withAlwaysWriteExceptions(true).withDisableAnsi(false);
//        PatternLayout layout = builder.build();
////        PatternLayout layout = PatternLayout.createLayout("[%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS} $$thread=[%t] %c{15}:%M(%L) %m%n", null, config, null, null,
////				true, false, null, null);
//		StringBuilder fnSb = new StringBuilder(UtilTools.getBQHome());
//		fnSb.append(File.separator).append("logs")
//				.append(File.separator)
//				.append(SpringContextHolder.getBean(Environment.class).getProperty("server.port"))
//				.append(File.separator)
//				.append(fileDir).append(File.separator).append(filePre).append(".log");
//		String fileName = fnSb.toString();
//		String filePattern = fileName.substring(0, fileName.length() - 4) + "-%i.log";
//		TriggeringPolicy tp = SizeBasedTriggeringPolicy.createPolicy("10MB");
//
//		RolloverStrategy rs = DefaultRolloverStrategy.newBuilder()
//				.withMin(null)
//				.withMax("500")
//				.withFileIndex(null)
//				.withCompressionLevelStr(null)
//				.withCustomActions(null)
//				.withStopCustomActionsOnError(true)
//				.withConfig(config)
//				.build();
//        Appender appender = null;
//        if(UtilTools.isDevelopMode()){
//            appender = ConsoleAppender.newBuilder()
//                    .withName(loggerName + "_log")
//                    .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
//                    .setDirect(true)
//                    .withLayout(layout)
//                    .setConfiguration(config)
//                    .build();
//        }else{
//            appender = RollingFileAppender.newBuilder()
//                    .withFileName(fileName)
//                    .withFilePattern(filePattern)
//                    .withName(loggerName + "_log")
//                    .withAppend(true)
//                    .withLayout(layout)
//                    .withStrategy(rs)
//                    .withPolicy(tp)
//                    .setConfiguration(config)
//                    .build();
//        }
//        appender.start();
//        config.addAppender(appender);
//		AppenderRef ref = AppenderRef.createAppenderRef(loggerName + "_log", null, null);
//		AppenderRef[] refs = new AppenderRef[] { ref };
//		Level level = Level.INFO;
//		if (mapConf.containsKey(loggerName)){
//			level = mapConf.get(loggerName).getLevel();
//		}
//		LoggerConfig loggerConfig = LoggerConfig.createLogger(true, level, loggerName, "true", refs, null,
//				config, null);
//		loggerConfig.addAppender(appender, level, null);
//		config.addLogger(loggerName, loggerConfig);
//		ctx.updateLoggers();
	}
	
}
