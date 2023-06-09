package com.miyuan.smarthome.temp.db;

import java.io.Serializable;
import java.util.Arrays;

public class HistoryTemp implements Serializable {
    private int status;
    private long startTime;
    private int memberId;
    private int step;
    private int tempCount;
    private float[] temps;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getTempCount() {
        return tempCount;
    }

    public void setTempCount(int tempCount) {
        this.tempCount = tempCount;
    }

    public float[] getTemps() {
        return temps;
    }

    public void setTemps(float[] temps) {
        this.temps = temps;
    }

    @Override
    public String toString() {
        return "HistoryTemp{" +
                "status=" + status +
                ", startTime=" + startTime +
                ", memberId=" + memberId +
                ", step=" + step +
                ", tempCount=" + tempCount +
                ", temps=" + Arrays.toString(temps) +
                '}';
    }
}
