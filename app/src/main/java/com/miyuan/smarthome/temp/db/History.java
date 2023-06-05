package com.miyuan.smarthome.temp.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"deviceID", "memberID", "time"})
public class History {

    private long time;

    @NonNull
    private String deviceID;

    private int memberID;

    private String temps;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getMemberID() {
        return memberID;
    }

    public void setMemberID(int memberID) {
        this.memberID = memberID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceID() {
        return deviceID;
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
                ", deviceID='" + deviceID + '\'' +
                ", memberID=" + memberID +
                ", temps='" + temps + '\'' +
                '}';
    }
}

