package org.liws.framework.util.generator;

import java.util.UUID;

public class UUIDGenerator {

	private static UUIDGenerator instance = new UUIDGenerator();
	private UUIDGenerator() {
	}
	public static UUIDGenerator getInstance() {
		return instance;
	}

	/**
	 * 生成UUID
	 * @return
	 */
	public String generate() {
		return UUID.randomUUID().toString();
	}
}