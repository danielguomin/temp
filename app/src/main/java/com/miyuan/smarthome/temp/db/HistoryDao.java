package com.miyuan.smarthome.temp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history")
    List<History> getAll();

    @Query("SELECT * FROM history WHERE devicesID = :devicesID AND memberId =:memberId ORDER BY time ASC")
    List<History> getAll(String devicesID, int memberId);

    @Query("SELECT * FROM history WHERE updated = :updated ORDER BY time ASC")
    List<History> getUpdateHistory(boolean updated);

    @Insert
    void insert(History history);

    @Insert
    void insert(List<History> histories);

    @Delete
    void delete(History history);

    @Update
    void update(History history);

    @Update
    void update(List<History> histories);
}
