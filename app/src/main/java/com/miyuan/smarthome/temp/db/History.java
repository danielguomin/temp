package com.miyuan.smarthome.temp.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"deviceId", "memberId", "time"})
public class History {

    private long time;

    @NonNull
    private String deviceId;

    private int memberId;

    private String temps;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getTemps() {
        return temps;
    }

    public void setTemps(String temps) {
        this.temps = temps;
    }

    @Override
    public String toString() {
        return "History{" +
                "time=" + time +
                ", deviceId='" + deviceId + '\'' +
                ", memberId=" + memberId +
                ", temps='" + temps + '\'' +
                '}';
    }
}
