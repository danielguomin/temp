package com.miyuan.smarthome.temp.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"deviceId", "memberId", "time"})
public class Nurse {

    private long time;

    @NonNull
    private String deviceId;

    private int memberId;

    private int type;

    private String content;


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @NonNull
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
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

    @Override
    public String toString() {
        return "Nurse{" +
                "time=" + time +
                ", deviceId='" + deviceId + '\'' +
                ", memberId=" + memberId +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
