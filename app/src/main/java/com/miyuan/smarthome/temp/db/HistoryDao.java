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

    @Query("SELECT * FROM history WHERE deviceId = :deviceId AND memberId =:memberId ORDER BY time ASC")
    List<History> getAll(String deviceId, int memberId);

    @Query("SELECT * FROM history WHERE updated = :updated ORDER BY time ASC")
    List<History> getUpdateHistory(boolean updated);

    @Insert
    void insert(History history);

    @Delete
    void delete(History history);

    @Update
    void update(History history);
}
