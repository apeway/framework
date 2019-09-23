package org.liws.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 读取serverConfig.properties配置文件
 */
public class ServerConfigReader {
	
	private static Properties props = null;

	public static Properties getServerConfig() {
		synchronized (ServerConfigReader.class) {
			if (props == null) {
				readServerConfig();
			}
		}
		return props;
	}
	
	private static void readServerConfig() {
		props = new Properties();
		if(UtilTools.getBQConfig().startsWith("classpath")){ // classpath下找文件
			String configFile = "serverConfig.properties";
			
			// ???	<--
			StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
			String bootRoot = stacks[stacks.length-1].getClassName();
			if(bootRoot.startsWith("org.testng")) {
				configFile = "serverTestConfig.properties";
			} // -->
			
			// Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile)
			URL url = Thread.currentThread().getContextClassLoader().getResource(configFile);
			if(url != null){
				try (InputStream is = url.openStream()) {
					props.load(is);
				} catch (Exception e) {
					//BQLogger.error(e);
				}
			}

		}else { // bq_config指定的目录下找文件
			Path path = Paths.get(UtilTools.getBQConfig() + "/serverConfig.properties");
			if (Files.exists(path)) {
				try (InputStream is = Files.newInputStream(path)) {
					props.load(is);
				} catch (IOException e) {
					//BQLogger.error(e);
				}
			}
		}
		
		decryptRepoDbPassword("jdbc.mima");
		decryptRepoDbPassword("jdbc.password");
	}
	/** Encrypt:2be98afc86aa7f2e4b216a069d1878f8b ==> yonyou@1*/
	private static void decryptRepoDbPassword(String key){
		String start = "Encrypt:";
		if(props.containsKey(key) && props.getProperty(key) != null
				&& props.getProperty(key).startsWith(start)){
			props.setProperty(key, UtilTools.decryptPassword(props.getProperty(key).substring(start.length())));
		}
	}

	public static void refreshServerConfig() {
		synchronized (ServerConfigReader.class) {
			readServerConfig();
		}
	}
	
	
	
	public static void main(String[] args) {
		// -Dbq_config=files  	==>	files文件夹
		// 不带jvm参数 			==>	src-main-resource
		System.out.println(ServerConfigReader.getServerConfig().getProperty("location"));
	}
	
	
}
