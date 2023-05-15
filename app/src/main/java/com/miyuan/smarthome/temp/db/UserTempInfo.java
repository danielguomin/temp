package com.miyuan.smarthome.temp.db;

import androidx.room.Entity;

@Entity(primaryKeys = {"userId", "deviceid"})
public class UserTempInfo {
    private int userId;
    private int deviceId;
}
