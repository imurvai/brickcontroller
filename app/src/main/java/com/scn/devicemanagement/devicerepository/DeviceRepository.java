package com.scn.devicemanagement.devicerepository;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by steve on 2017. 11. 19..
 */

@Singleton
public class DeviceRepository {

    //
    // Private members
    //

    private static final String TAG = DeviceRepository.class.getSimpleName();

    private DeviceDao deviceDao;

    //
    // Constructor
    //

    @Inject
    public DeviceRepository(@NonNull Context context) {
        Logger.i(TAG, "constructor...");

        DeviceDatabase database = Room.databaseBuilder(context, DeviceDatabase.class, "brickcontroller_device_db").build();
        deviceDao = database.deviceDao();
    }

    //
    // API
    //

    public synchronized void saveDevice(DeviceEntity deviceEntity) {
        Logger.i(TAG, "saveDevice - " + deviceEntity.name);
        deviceDao.insert(deviceEntity);
    }

    public synchronized void saveDevices(List<DeviceEntity> deviceEntities) {
        Logger.i(TAG, "saveDevices...");
        deviceDao.deleteAll();
        deviceDao.insert(deviceEntities);
    }

    public synchronized List<DeviceEntity> loadDevices() {
        Logger.i(TAG, "loadDevices...");
        return deviceDao.getAll();
    }

    public synchronized void updateDevice(DeviceEntity deviceEntity) {
        Logger.i(TAG, "updateDevice - " + deviceEntity.name);
        deviceDao.update(deviceEntity);
    }

    public synchronized void deleteDevice(DeviceEntity deviceEntity) {
        Logger.i(TAG, "deleteDevice - " + deviceEntity.name);
        deviceDao.delete(deviceEntity);
    }

    public synchronized void deleteAllDevices() {
        Logger.i(TAG, "deleteAllDevices...");
        deviceDao.deleteAll();
    }
}
