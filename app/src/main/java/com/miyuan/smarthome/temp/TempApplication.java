package com.miyuan.smarthome.temp;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import androidx.lifecycle.LiveData;

import com.miyuan.smarthome.temp.db.Member;
import com.miyuan.smarthome.temp.utils.SingleLiveData;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TempApplication extends Application {

    public static SingleLiveData<Member> _currentMemberLiveData = new SingleLiveData<>();
    public static LiveData<Member> currentLiveData = _currentMemberLiveData;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static String toString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    static void recordErrorToFile(String error) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = sdf.format(new Date(System.currentTimeMillis()));
        String parent = Environment.getExternalStorageDirectory() + File.separator + "temp_blue";
        final File file = new File(parent, fileName + "_error.log");
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(error.getBytes(), 0, error.getBytes().length);
        } catch (Exception e) {
            try {
                if (null != fos) {
                    fos.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);
        registerUncaughtException();
    }

    public void registerUncaughtException() {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                pw.flush();
                recordErrorToFile(sw.toString());
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }
}
