package com.miyuan.smarthome.temp.db;

import androidx.room.Entity;

@Entity(primaryKeys = {"userId", "deviceid"})
public class UserTempInfo {
    private int userId;
    private int deviceId;
    private long startTime;
    private String temps;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getTemps() {
        return temps;
    }

    public void setTemps(String temps) {
        this.temps = temps;
    }
}
