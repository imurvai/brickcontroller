package com.scn.devicemanagement.devicerepository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceFactory;
import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final DeviceDao deviceDao;
    private final Map<String, Device> deviceMap = new HashMap<>();
    private final MutableLiveData<List<Device>> deviceListLiveData = new MutableLiveData<>();


    //
    // Constructor
    //

    @Inject
    public DeviceRepository(@NonNull Context context) {
        Logger.i(TAG, "constructor...");

        DeviceDatabase database = Room.databaseBuilder(context, DeviceDatabase.class, "brickcontroller_device_db").build();
        deviceDao = database.deviceDao();

        deviceListLiveData.setValue(getDeviceList());
    }

    //
    // API
    //

    @WorkerThread
    public synchronized void loadDevices(@NonNull DeviceFactory deviceFactory) {
        Logger.i(TAG, "loadDevices...");

        deviceMap.clear();
        List<DeviceEntity> deviceEntities = deviceDao.getAll();

        for (DeviceEntity deviceEntity : deviceEntities) {
            Device device = deviceFactory.createDevice(deviceEntity.type, deviceEntity.name, deviceEntity.address, deviceEntity.outputLevel);
            if (device != null) {
                deviceMap.put(device.getId(), device);
            }
        }

        deviceListLiveData.postValue(getDeviceList());
    }

    @WorkerThread
    public synchronized void storeDevice(@NonNull Device device) {
        Logger.i(TAG, "storeDevice - " + device);

        if (deviceMap.containsKey(device.getId())) {
            Logger.w(TAG, "  There is already a device with the same ID.");
            return;
        }

        deviceDao.insert(DeviceEntity.fromDevice(device));
        deviceMap.put(device.getId(), device);
        deviceListLiveData.postValue(getDeviceList());
    }

    @WorkerThread
    public synchronized void updateDevice(@NonNull Device device, @NonNull String newName) {
        Logger.i(TAG, "updateDeviceAsync - " + device);
        Logger.i(TAG, "  new name: " + newName);

        deviceDao.update(new DeviceEntity(device.getType(), newName, device.getAddress(), device.getOutputLevel()));
        device.setName(newName);
        deviceListLiveData.postValue(getDeviceList());
    }

    @WorkerThread
    public synchronized void updateDevice(@NonNull Device device, @NonNull Device.OutputLevel newOutputLevel) {
        Logger.i(TAG, "updateDeviceAsync - " + device);
        Logger.i(TAG, "  new output level: " + newOutputLevel);

        deviceDao.update(new DeviceEntity(device.getType(), device.getName(), device.getAddress(), newOutputLevel));
        device.setOutputLevel(newOutputLevel);
        deviceListLiveData.postValue(getDeviceList());
    }

    @WorkerThread
    public synchronized void deleteDevice(@NonNull Device device) {
        Logger.i(TAG, "deleteDevice - " + device);

        deviceDao.delete(DeviceEntity.fromDevice(device));
        deviceMap.remove(device.getId());
    }

    @WorkerThread
    public synchronized void deleteAllDevices() {
        Logger.i(TAG, "deleteAllDevices...");
        deviceDao.deleteAll();
        deviceMap.clear();
        deviceListLiveData.postValue(getDeviceList());
    }

    public synchronized Device getDevice(@NonNull String deviceId) {
        Logger.i(TAG, "getDevice - " + deviceId);

        if (!deviceMap.containsKey(deviceId)) {
            Logger.w(TAG, "  No device with id: " + deviceId);
            return null;
        }

        return deviceMap.get(deviceId);
    }

    public synchronized LiveData<List<Device>> getDeviceListLiveData() {
        Logger.i(TAG, "getDeviceListLiveData...");
        return deviceListLiveData;
    }

    //
    // Private methods
    //

    private List<Device> getDeviceList() {
        List<Device> deviceList = new ArrayList<>(deviceMap.values());
        Collections.sort(deviceList);
        return deviceList;
    }
}
