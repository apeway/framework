package org.liws.framework.util.encode;

import java.math.BigInteger;

/**
 * 加密工具类
 */
public class EncryptUtil {

	private static final String MIMA_ENCRYPT_PRFIX = "Encrypt:";

	public static String encryptPassword(String password) {
		if (password == null || password.isEmpty()) {
			return password;
		}
		return MIMA_ENCRYPT_PRFIX + encrypt(password);
	}

	public static String decryptPassword(String encryptedPassword) {
		if (encryptedPassword == null || encryptedPassword.isEmpty()) {
			return encryptedPassword;
		}
		if (encryptedPassword.startsWith(MIMA_ENCRYPT_PRFIX)) {
			encryptedPassword = encryptedPassword.substring(MIMA_ENCRYPT_PRFIX.length());
		}
		return decrypt(encryptedPassword);
	}
	
	///////////////////////////////////////////////////////////
	
	private static final int RADIX = 16;
	private static final String SEED = "0933910847463829827159347601486730416058";
	
	public static final String encrypt(String password) {
		if (password == null || password.length() == 0) {
            return "";
        }
		BigInteger bi_passwd = new BigInteger(password.getBytes());
		BigInteger bi_seed = new BigInteger(SEED);
		BigInteger bi_encryptedPasswd = bi_seed.xor(bi_passwd);
		return bi_encryptedPasswd.toString(RADIX);
	}

	public static final String decrypt(String encryptedPasswd) {
		if (encryptedPasswd == null || encryptedPasswd.length() == 0) {
            return "";
        }
		BigInteger bi_seed = new BigInteger(SEED);
		BigInteger bi_encryptedPasswd = new BigInteger(encryptedPasswd, RADIX);
		BigInteger bi_passwd = bi_encryptedPasswd.xor(bi_seed);
		return new String(bi_passwd.toByteArray());
	}
	
}
