package com.miyuan.smarthome.temp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Device.class, Remind.class}, version = 1, exportSchema = false)
public abstract class TempDataBase extends RoomDatabase {

    public abstract UserDao getUserDao();

    public abstract DeviceDao getDeviceDao();

    public abstract RemindDao getRemindDao();
}
