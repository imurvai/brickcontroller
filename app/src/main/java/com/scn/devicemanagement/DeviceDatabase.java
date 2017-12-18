package com.scn.devicemanagement;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by steve on 2017. 11. 19..
 */

@Database(entities = { DeviceEntity.class }, version = 1)
@TypeConverters({
        DeviceTypeTypeConverter.class,
        DeviceOutputLevelTypeConverter.class
})
abstract class DeviceDatabase extends RoomDatabase {

    public static final String DatabaseName = "brickcontroller_device_db";

    public abstract DeviceDao deviceDao();
}
