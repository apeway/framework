package com.yonyou.bq.framework.util;

import com.yonyou.bq.framework.log.AELogger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.SecureRandom;

/**
 * DES加密和解密。
 *
 */
public class DESUtil {

    /**
     * 安全密钥
     */
    private String keyData = "ABCDEFGHIJKLMNOPQRSTWXYZabcdefghijklmnopqrstwxyz0123456789-_.";

    /**
     * 功能：构造
     *
     */
    public DESUtil() {
    }

    /**
     * 功能：构造
     * <p>
     * key
     */
    public DESUtil(String key) {
        this.keyData = key;
    }

    /**
     * 功能：加密
     *
     * @param source  源字符串
     * @return String
     */
    public String encrypt(String source) {
        String encrypt = null;
        byte[] ret = encrypt(source.getBytes());
        encrypt = java.util.Base64.getEncoder().encodeToString(ret);
        return encrypt;
    }

    /**
     * 功能：解密
     *
     * @param encryptedData 被加密后的字符串
     * @return String
     */
    public String decrypt(String encryptedData) {
        String descryptedData = null;
        byte[] ret = descrypt(java.util.Base64.getDecoder().decode(encryptedData));
        descryptedData = new String(ret);
        return descryptedData;
    }

    /**
     * 加密数据 用生成的密钥加密原始数据
     *
     * @param primaryData 原始数据
     * @return byte[]
     */
    public byte[] encrypt(byte[] primaryData) {

        /** DES算法要求有一个可信任的随机数源 */
        SecureRandom sr = new SecureRandom();

        /** 使用原始密钥数据创建DESKeySpec对象 */
        DESKeySpec dks = null;
        try {
            dks = new DESKeySpec(keyData.getBytes());
            /** 创建一个密钥工厂 */
            SecretKeyFactory keyFactory = null;

            keyFactory = SecretKeyFactory.getInstance("DES");


            /** 用密钥工厂把DESKeySpec转换成一个SecretKey对象 */
            SecretKey key = keyFactory.generateSecret(dks);


            /** Cipher对象实际完成加密操作 */
            Cipher cipher = null;

            cipher = Cipher.getInstance("DES");


            /** 用密钥初始化Cipher对象 */

            cipher.init(Cipher.ENCRYPT_MODE, key, sr);


            /** 正式执行加密操作 */
            byte encryptedData[] = cipher.doFinal(primaryData);


            /** 返回加密数据 */
            return encryptedData;

        } catch (Exception e) {
            AELogger.error(e);
            return null;
        }


    }

    /**
     * 用密钥解密数据
     *
     * @param encryptedData 加密后的数据
     * @return byte[]
     */
    public byte[] descrypt(byte[] encryptedData) {

        /** DES算法要求有一个可信任的随机数源 */
        SecureRandom sr = new SecureRandom();



        /** 使用原始密钥数据创建DESKeySpec对象 */
        try {
            DESKeySpec dks = new DESKeySpec(keyData.getBytes());
            /** 创建一个密钥工厂 */
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            /** 用密钥工厂把DESKeySpec转换成一个SecretKey对象 */
            SecretKey key = keyFactory.generateSecret(dks);
            /** Cipher对象实际完成加密操作 */
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key, sr);
            byte decryptedData[] = cipher.doFinal(encryptedData);
            return decryptedData;
        } catch (Exception e) {
            AELogger.error(e);
            return null;
        }


    }


}