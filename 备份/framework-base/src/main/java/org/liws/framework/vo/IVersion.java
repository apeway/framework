package org.liws.framework.vo;

/**
 * 用于VO的修改的并发控制，使用乐观锁的方式
 * 使用时VO实现改接口，在表中增加对应的类型是char(36)字段
 */
public interface IVersion {
	
	String getLockedVersion();
	
	void setLockedVersion(String lockedVersion);
}
