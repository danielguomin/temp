package com.miyuan.smarthome.temp.db;

public class Member {
    private int memberId;
    private int length;
    private String name;
    private boolean choice;

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", length=" + length +
                ", name='" + name + '\'' +
                ", choice=" + choice +
                '}';
    }
}
