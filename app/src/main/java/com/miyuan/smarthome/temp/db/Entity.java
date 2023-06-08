package com.miyuan.smarthome.temp.db;

public class Entity {
    private long time;
    private float temp;

    public Entity(long time, float temp) {
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
