package com.miyuan.smarthome.temp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NurseDao {

    @Query("SELECT * FROM nurse")
    List<Nurse> getAll();

    @Query("SELECT * FROM nurse WHERE deviceId = :deviceId AND memberId =:memberId ORDER BY time ASC")
    List<Nurse> getAll(String deviceId, int memberId);

    @Insert
    void insert(Nurse nurse);

    @Delete
    void delete(Nurse nurse);

}
