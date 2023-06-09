package com.miyuan.smarthome.temp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RemindDao {

    @Query("SELECT * FROM remind")
    List<Remind> getAll();

    @Insert
    void insert(Remind remind);

    @Insert
    void insert(List<Remind> reminds);

    @Delete
    void delete(List<Remind> reminds);

    @Delete
    void delete(Remind remind);

    @Update
    void update(Remind remind);

}
