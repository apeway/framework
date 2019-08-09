package org.liws.framework.util.generator;

import java.util.Random;

public class SecurityCodeGenerator {

	private static final Random rand = new Random(47);
	private static final char[] charArray = "0123456789".toCharArray();
	
	/**
	 * 生成6位数字的随机验证码
	 * @return
	 */
	public static String generate() {
		StringBuilder securityCode = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			securityCode.append(generateRandomChar());
		}
		return securityCode.toString();
	}
	
	/**
	 * 生成一个0~9中的随机字符
	 * @return
	 */
	private static char generateRandomChar() {
		// 增加随机性
		if(System.nanoTime() % 2 == 0) {
			rand.nextInt(charArray.length);
		}
		return charArray[rand.nextInt(charArray.length)];
	}
	
}
