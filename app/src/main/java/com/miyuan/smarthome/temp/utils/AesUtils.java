package com.miyuan.smarthome.temp.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {

    private static final String pass = "06E81ac8BE275800";

    /**
     * 解密参数
     *
     * @param params
     * @return
     */
//    public static <T> T decodeParams(String params, Class<T> elementType) {
//        try {
//            String content = AesUtils.decrypt(params);
//            return JacksonUtils.getObjectMapper().readValue(content, elementType);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 解密参数
     *
     * @param str
     * @return
     */
    public static String decodeParam(String str) {
        try {
            if (str.length() > 0) {
                return AesUtils.decrypt(str);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     * @throws
     * @method encrypt
     * @since v1.0
     */
    public static String encrypt(String content) {
        try {
            IvParameterSpec ips = new IvParameterSpec(pass.getBytes());
            SecretKeySpec sks = new SecretKeySpec(pass.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sks, ips);
            byte[] encryptedData = cipher.doFinal(content.getBytes());
            String res = parseByte2HexStr(encryptedData);

            return Base64Utils.encodeData(res); // 加密  
        } catch (Exception e) {
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
     * @throws
     * @method decrypt
     * @since v1.0
     */
    public static String decrypt(String content) {
        try {
            String res = Base64Utils.decodeData(content);

            byte[] decryptFrom = parseHexStr2Byte(res);
            IvParameterSpec zeroIv = new IvParameterSpec(pass.getBytes());
            SecretKeySpec key1 = new SecretKeySpec(pass.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key1, zeroIv);

            byte[] result = cipher.doFinal(decryptFrom);
            return new String(result); // 解密  
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     * @throws
     * @method parseByte2HexStr
     * @since v1.0
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
     * @throws
     * @method parseHexStr2Byte
     * @since v1.0
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
}
