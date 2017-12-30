package com.scn.creationmanagement;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by imurvai on 2017-12-17.
 */

@Database(entities = {
        Creation.class,
        ControllerProfile.class,
        ControllerEvent.class,
        ControllerAction.class
}, version = 1)
@TypeConverters({ ControllerEventTypeTypeContverer.class })
abstract class CreationDatabase extends RoomDatabase {

    static final String DatabaseName = "brickcontroller_creation_db";

    public abstract CreationDao creationDao();
}
