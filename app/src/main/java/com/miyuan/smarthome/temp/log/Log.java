package com.miyuan.smarthome.temp.log;

import android.os.Environment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

public class Log {
    public final static String TAG = "TEMP_BLUE";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        Timber.plant(new Timber.DebugTree());
        Timber.plant(new FileLoggingTree(Environment.getExternalStorageDirectory().getPath() + "/temp_blue"));
    }

    public static void d(String message) {
        Timber.tag(TAG);
        Timber.d(simpleDateFormat.format(new Date()) + "   " + message + "\n");
    }

    public static String toString(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();
        return ex.toString();
    }
}
