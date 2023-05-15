package com.miyuan.smarthome.temp;

import android.app.Application;
import android.content.Context;

import com.tencent.mmkv.MMKV;

public class TempApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);
    }
}
