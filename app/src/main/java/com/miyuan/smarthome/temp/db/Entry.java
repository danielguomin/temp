package com.miyuan.smarthome.temp.db;

public class Entry {
    private long time;
    private float temp;

    public Entry(long time, float temp) {
        this.time = time;
        this.temp = temp;
    }

    public long getTime() {
        return time;
    }

    public float getTemp() {
        return temp;
    }
}
