package com.bin.mylibrary.utils;
//暂时使用AES加密


import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MyCode {

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public static byte[] encrypt(String content, String password) {
        try {
            // 20171215 问题修改  Given final block not properly padded
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(password.getBytes());

            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] content, String password) {
        try {
            // 20171215 问题修改  Given final block not properly padded
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(password.getBytes());

            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 解密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public static byte[] encrypt2(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 加密处理
    public static String toEncrypt(String content, String password) {
        String result = "";
        byte[] encryptResult = encrypt(content, password);
        //System.out.println("加密后字符串为"+bs.encodeToString(encryptResult));
        // 特殊字符替换处理
        result = reSpec(Base64.encodeToString(encryptResult, Base64.DEFAULT), "1");
        return result;
    }

    // 解密处理
    public static String toDecrypt(String content, String password) throws UnsupportedEncodingException {
        // 特殊字符替换处理
        content = reSpec(content, "2");
        //System.out.println("待解密字符串为"+content);
        byte[] decryptResult = decrypt(Base64.decode(content.getBytes(), Base64.DEFAULT), password);
        return new String(decryptResult, "utf-8");
    }

    // 特殊字符替换处理
    private static String reSpec(String content, String flg) {
        // 加号和斜杠
        if ("1".equals(flg)) {
            // 特殊字符转标识码
            return content.replace("+", "zhaoloei1").replace("\\", "zhaolei2").replace("/", "zhaolei3");
        } else {
            // 标识码转特殊字符
            return content.replace("zhaoloei1", "+").replace("zhaolei2", "\\").replace("zhaolei3", "/");
        }
    }

    /**
     * 对人员编号进行加密
     *
     * @param encryptStringex
     * @return
     */
    public static String EncryptDES(String encryptStringex) {
        String encryptString = "";
        Random r = new Random();
        for (int i = 0; i < encryptStringex.length(); i++) {
            encryptString += encryptStringex.charAt(i);
            encryptString += String.valueOf(r.nextInt(9));

        }
        String encryptKey = "szhtkj12345";
        byte[] Keystr = {0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00};
        try {
            byte[] rgbKey = encryptKey.substring(0, 8).getBytes("UTF-8");
            byte[] rgbIV = Keystr;
            byte[] inputByteArray = encryptString.getBytes("UTF-8");
            Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec keySpec = new DESKeySpec(rgbKey);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key key = keyFactory.generateSecret(keySpec);
            IvParameterSpec iv = new IvParameterSpec(Keystr);
            enCipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] pasByte = enCipher.doFinal(inputByteArray);
            return Base64.encodeToString(pasByte, Base64.DEFAULT);
        } catch (Exception e) {
            return encryptString;
        }
    }
}
