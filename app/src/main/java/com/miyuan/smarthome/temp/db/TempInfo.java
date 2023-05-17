package com.miyuan.smarthome.temp.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class TempInfo implements Serializable {
    private String deviceId;
    private String deviceVersion;
    private int charging;
    private int memberId;
    private int memberCount;
    private List<Member> members;
    private byte[] orginal;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public int getCharging() {
        return charging;
    }

    public void setCharging(int charging) {
        this.charging = charging;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public byte[] getOrginal() {
        return orginal;
    }

    public void setOrginal(byte[] orginal) {
        this.orginal = orginal;
    }

    @Override
    public String toString() {
        return "TempInfo{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceVersion='" + deviceVersion + '\'' +
                ", charging=" + charging +
                ", memberId=" + memberId +
                ", memberCount=" + memberCount +
                ", members=" + members +
                ", orginal=" + Arrays.toString(orginal) +
                '}';
    }
}
