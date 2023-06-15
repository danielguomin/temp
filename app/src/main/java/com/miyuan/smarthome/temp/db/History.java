package com.miyuan.smarthome.temp.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(primaryKeys = {"devicesID", "memberID", "time"})
public class History {

    private long time;

    @NonNull
    private String devicesID;

    private int memberID;

    private String temps;

    @Ignore
    private String name;

    private boolean updated = false;

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

    @NonNull
    public String getDevicesID() {
        return devicesID;
    }

    public void setDevicesID(@NonNull String devicesID) {
        this.devicesID = devicesID;
    }

    public String getTemps() {
        return temps;
    }

    public void setTemps(String temps) {
        this.temps = temps;
    }


    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "History{" +
                "time=" + time +
                ", devicesID='" + devicesID + '\'' +
                ", memberID=" + memberID +
                ", temps='" + temps + '\'' +
                ", name='" + name + '\'' +
                ", updated=" + updated +
                '}';
    }
}

