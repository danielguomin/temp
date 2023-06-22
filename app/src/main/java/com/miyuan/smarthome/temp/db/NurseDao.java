package com.miyuan.smarthome.temp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NurseDao {

    @Query("SELECT * FROM nurse")
    List<Nurse> getAll();

    @Query("SELECT * FROM nurse WHERE devicesID = :deviceId AND memberId =:memberId ORDER BY time ASC")
    List<Nurse> getAll(String deviceId, int memberId);

    @Query("SELECT * FROM nurse WHERE updated = :updated ORDER BY time ASC")
    List<Nurse> getAll(boolean updated);

    @Insert
    void insert(Nurse nurse);

    @Insert
    void insert(List<Nurse> nurses);

    @Delete
    void delete(Nurse nurse);

    @Update
    void update(Nurse nurse);

}
