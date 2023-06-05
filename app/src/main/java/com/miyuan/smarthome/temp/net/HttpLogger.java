package com.miyuan.smarthome.temp.net;

import com.miyuan.smarthome.temp.log.Log;

import okhttp3.logging.HttpLoggingInterceptor;

public class HttpLogger implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(String message) {
        Log.d(message);
    }
}
