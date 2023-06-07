package com.miyuan.smarthome.temp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Remind.class, History.class, Nurse.class}, version = 1, exportSchema = false)
public abstract class TempDataBase extends RoomDatabase {

    public abstract RemindDao getRemindDao();

    public abstract HistoryDao getHistoryDao();

    public abstract NurseDao getNurseDao();
}
