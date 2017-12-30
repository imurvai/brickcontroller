package com.scn.devicemanagement;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by steve on 2017. 11. 19..
 */

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceEntity deviceEntity);

    @Query("SELECT * FROM devices")
    List<DeviceEntity> getAll();

    @Update
    void update(DeviceEntity deviceEntity);

    @Delete
    void delete(DeviceEntity deviceEntity);

    @Query("DELETE FROM devices")
    void deleteAll();
}
