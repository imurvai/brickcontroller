package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by imurvai on 2017-12-17.
 */

@Database(entities = {}, version = 1)
@TypeConverters({})
public abstract class CreationDatabase {

    public static final String DatabaseName = "brickcontroller_creation_db";

    public abstract CreationDao creationDao();
}
