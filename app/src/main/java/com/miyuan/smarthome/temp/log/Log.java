package com.miyuan.smarthome.temp.log;

import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

public class Log {
    public final static String TAG = "TEMP";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        Timber.plant(new Timber.DebugTree());
        Timber.plant(new FileLoggingTree(Environment.getExternalStorageDirectory().getPath() + "/obd"));
    }

    public static void d(String message) {
        Timber.tag(TAG);
        Timber.d(simpleDateFormat.format(new Date()) + "   " + message + "\n");
    }
}
