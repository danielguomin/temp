package com.miyuan.smarthome.temp.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"devicesID", "memberID", "time"})
public class Nurse {

    private long time;

    @NonNull
    private String devicesID;

    private int memberID;

    private int type;

    private String content;

    private boolean updated = false;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @NonNull
    public String getDevicesID() {
        return devicesID;
    }

    public void setDevicesID(@NonNull String devicesID) {
        this.devicesID = devicesID;
    }

    public int getMemberID() {
        return memberID;
    }

    public void setMemberID(int memberID) {
        this.memberID = memberID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "Nurse{" +
                "time=" + time +
                ", devicesID='" + devicesID + '\'' +
                ", memberID=" + memberID +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", updated=" + updated +
                '}';
    }
}
