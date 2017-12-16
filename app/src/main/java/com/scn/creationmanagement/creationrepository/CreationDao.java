package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.scn.creationmanagement.Creation;

import java.util.List;

/**
 * Created by imurvai on 2017-12-17.
 */

@Dao
public interface CreationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void Insert(List<Creation> creations);

    @Query("SELECT * FROM creations")
    List<Creation> GetAll();
}
