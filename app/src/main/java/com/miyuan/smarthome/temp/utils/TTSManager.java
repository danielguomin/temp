package com.miyuan.smarthome.temp.utils;

import android.speech.tts.TextToSpeech;

import com.miyuan.smarthome.temp.TempApplication;

public class TTSManager {

    private TextToSpeech tts;

    private TTSManager() {
        tts = new TextToSpeech(TempApplication.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });
    }

    public static TTSManager getInstance() {
        return TTSManager.InstanceHolder.INSTANCE;
    }

    public void speek(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }

    public static class InstanceHolder {
        private static final TTSManager INSTANCE = new TTSManager();
    }

}
