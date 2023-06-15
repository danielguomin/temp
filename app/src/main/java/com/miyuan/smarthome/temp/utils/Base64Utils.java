package com.miyuan.smarthome.temp.utils;

import static android.util.Base64.DEFAULT;

import android.util.Base64;


public class Base64Utils {

    /**
     * 对给定的字符串进行base64解码操作
     */
    public static String decodeData(String inputData) {
        if (null == inputData) {
            return null;
        }
        return new String(Base64.decode(inputData.getBytes(), DEFAULT));
    }

    /**
     * 对给定的字符串进行base64加密操作
     */
    public static String encodeData(String inputData) {
        if (null == inputData) {
            return null;
        }
        return new String(Base64.encodeToString(inputData.getBytes(), DEFAULT));
    }

}
