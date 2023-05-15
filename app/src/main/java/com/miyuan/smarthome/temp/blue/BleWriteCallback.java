package com.miyuan.smarthome.temp.blue;


public interface BleWriteCallback {

    void onWriteSuccess(byte[] justWrite);

    void onWriteFailure(byte[] bytes);

}
