package org.liws.framework.log;

/**
 * 标记：可以生成新的子文件
 */
public class LogMarker {
	
	private String name;

	public String getName(){
		return this.name;
	}
	
	private LogMarker(String name){
		this.name = name;
	}
	
	/**
	 * 如 name : console.fs
	 * 日志路径:AE_HOME/logs/console/console.fs.log
	 * 日志级别:与console模块设置的日志界别相同
	 * @param name 全局唯一,建议：上下文名称.子模块名称
	 * @return
	 */
	public static LogMarker of(String name){
		return new LogMarker(name);
	}
}
