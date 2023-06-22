package com.miyuan.smarthome.temp.blue;


import com.miyuan.smarthome.temp.log.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by guomin on 2018/6/2.
 */

public class ProtocolUtils {

    public static final int PROTOCOL_HEAD_TAIL = 0x7e;
    private static final int PROTOCAL_COMMON_00 = 0x80;
    private static final int PROTOCAL_COMMON_01 = 0x81;
    private static final int PROTOCAL_COMMON_02 = 0x82;
    private static final int PROTOCAL_COMMON_03 = 0x83;
    private static final int PROTOCAL_COMMON_05 = 0x85;
    private static final int PROTOCAL_COMMON_06 = 0x86;
    private static final int PROTOCAL_COMMON_08 = 0x88;

    public static byte[] getTempStatus(long time) {
        Log.d("Protocol getTempStatus ==");
        byte[] result = new byte[13];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_00;
        result[2] = 01;
        byte[] bytes = HexUtils.longToByte(time);
        result[3] = bytes[0];
        int cr = result[1] ^ result[2] ^ result[3];
        for (int i = 1; i < bytes.length; i++) {
            result[3 + i] = bytes[i];
            cr = cr ^ bytes[i];
        }
        result[11] = (byte) cr;
        result[12] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] updateMember(boolean add, int id, String name) {
        Log.d("Protocol updateMember ==");
        byte[] result = new byte[1];
        try {
            byte[] bytes = name.getBytes("GBK");
            result = new byte[bytes.length + 5 + 3];
            result[0] = PROTOCOL_HEAD_TAIL;
            result[1] = (byte) PROTOCAL_COMMON_00;
            result[2] = 05;
            result[3] = (byte) (add ? 01 : 04);
            result[4] = (byte) id;
            result[5] = (byte) bytes.length;
            System.arraycopy(bytes, 0, result, 6, bytes.length);
            int cr = result[1];
            for (int i = 2; i < result.length - 2; i++) {
                cr = cr ^ result[i];
            }
            result[result.length - 2] = (byte) cr;
            result[result.length - 1] = PROTOCOL_HEAD_TAIL;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;

    }

    public static byte[] getCurrentTemp() {
        Log.d("Protocol getCurrentTemp ==");
        byte[] result = new byte[13];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_00;
        result[2] = 02;
        result[3] = 0;
        int cr = result[1] ^ result[2] ^ result[3];
        result[4] = (byte) cr;
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getHistoryTemp() {
        Log.d("Protocol getHistoryTemp ==");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_00;
        result[2] = 03;
        result[3] = 0;
        int cr = result[1] ^ result[2] ^ result[3];
        result[4] = (byte) cr;
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }
}

