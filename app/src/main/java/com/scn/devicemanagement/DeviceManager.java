package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.devicemanagement.devicerepository.DeviceEntity;
import com.scn.devicemanagement.devicerepository.DeviceRepository;
import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by steve on 2017. 09. 06..
 */

@Singleton
public final class DeviceManager {

    //
    // Constants
    //

    private static final String TAG = DeviceManager.class.getSimpleName();

    public enum State {
        OK,
        LOADING,
        REMOVING,
        UPDATING,
        SCANNING
    }

    //
    // Private members
    //

    private final Context context;
    private final DeviceRepository deviceRepository;
    private final BluetoothDeviceManager bluetoothDeviceManager;
    private final InfraRedDeviceManager infraRedDeviceManager;

    private Map<String, Device> deviceMap = new HashMap<>();
    private MutableLiveData<List<Device>> deviceListLiveData = new MutableLiveData<>();
    private MutableLiveData<StateChange<DeviceManager.State>> stateChangeLiveData = new MutableLiveData<>();

    //
    // Constructor
    //

    @Inject
    public DeviceManager(@NonNull Context context,
                         @NonNull DeviceRepository deviceRepository,
                         @NonNull BluetoothDeviceManager bluetoothDeviceManager,
                         @NonNull InfraRedDeviceManager infraRedDeviceManager) {
        Logger.i(TAG, "constructor...");

        this.context = context;
        this.deviceRepository = deviceRepository;
        this.bluetoothDeviceManager = bluetoothDeviceManager;
        this.infraRedDeviceManager = infraRedDeviceManager;

        deviceListLiveData.setValue(getDevices());
        stateChangeLiveData.setValue(new StateChange(State.OK, State.OK, false));
    }

    //
    // API
    //

    @MainThread
    public synchronized boolean isBluetoothLESupported() {
        Logger.i(TAG, "isBluetoothLESupported...");
        return BluetoothDeviceManager.isBluetoothLESupported(context);
    }

    @MainThread
    public synchronized boolean isBluetoothOn() {
        Logger.i(TAG, "isBluetoothOn...");
        return BluetoothDeviceManager.isBluetoothOn();
    }

    @MainThread
    public synchronized boolean isInfraSupported() {
        Logger.i(TAG, "isInfraSupported...");
        return InfraRedDeviceManager.isInfraRedSupported(context);
    }

    @MainThread
    public synchronized LiveData<StateChange<DeviceManager.State>> getStateChangeLiveData() {
        Logger.i(TAG, "getStateChangeLiveData...");
        return stateChangeLiveData;
    }

    @MainThread
    public synchronized boolean startLoadingDevices() {
        Logger.i(TAG, "startLoadingDevices...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.LOADING, false);

        deviceMap.clear();

        Single.fromCallable(() -> {
                List<DeviceEntity> deviceEntities = deviceRepository.loadDevices();
                List<Device> devices = new ArrayList<>();

                for (DeviceEntity deviceEntity : deviceEntities) {
                    Device device = createDevice(deviceEntity.type, deviceEntity.name, deviceEntity.address);
                    if (device != null) {
                        devices.add(device);
                    }
                }

                return devices;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    devices -> {
                        Logger.i(TAG, "Load devices onSuccess...");
                        addDevices(devices);
                        setState(State.OK, false);
                    },
                    error -> {
                        Logger.e(TAG, "Load devices onError...", error);
                        setState(State.OK, true);
                    });

        return true;
    }

    @MainThread
    public synchronized boolean startDeviceScan() {
        Logger.i(TAG, "startDeviceScan...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.SCANNING, false);

        Observable<Device> bluetoothDeviceObservable = bluetoothDeviceManager.startScan();
        Observable<Device> infraredDeviceObservable = infraRedDeviceManager.startScan();

        Observable.merge(infraredDeviceObservable, bluetoothDeviceObservable)
                .subscribeOn(Schedulers.io())
                .map(device -> {
                    Logger.i(TAG, "Saving device - " + device);
                    deviceRepository.saveDevice(DeviceEntity.fromDevice(device));
                    return device;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        device -> {
                            Logger.i(TAG, "Device scan onNext - " + device);
                            addDevice(device, true);
                        },
                        error -> {
                            Logger.e(TAG, "Device scan onError...", error);
                            setState(State.OK, true);
                        },
                        () -> {
                            Logger.i(TAG, "Device scan onComplete...");
                            setState(State.OK, false);
                        });

        return true;
    }

    @MainThread
    public synchronized void stopDeviceScan() {
        Logger.i(TAG, "stopDeviceScan...");

        if (getCurrentState() != State.SCANNING) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return;
        }

        bluetoothDeviceManager.stopScan();
        infraRedDeviceManager.stopScan();
    }

    @MainThread
    public synchronized List<Device> getDevices() {
        Logger.i(TAG, "getDevices...");

        List<Device> deviceList = new ArrayList<>(deviceMap.values());
        Collections.sort(deviceList);
        return deviceList;
    }

    @MainThread
    public synchronized LiveData<List<Device>> getDeviceListLiveData() {
        Logger.i(TAG, "getDeviceListLiveData...");
        return deviceListLiveData;
    }

    @MainThread
    public synchronized Device getDevice(@NonNull String deviceId) {
        Logger.i(TAG, "getDevice - " + deviceId);

        if (!deviceMap.containsKey(deviceId)) {
            Logger.i(TAG, "  No such device.");
            return null;
        }

        return deviceMap.get(deviceId);
    }

    @MainThread
    public synchronized boolean removeDevice(@NonNull final Device device) {
        Logger.i(TAG, "deleteDevice - " + device);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        if (!deviceMap.containsKey(device.getId())) {
            Logger.i(TAG, "  No such device.");
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            deviceRepository.deleteDevice(DeviceEntity.fromDevice(device));
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                x -> {
                    Logger.i(TAG, "deleteDevice onSuccess.");
                    deviceMap.remove(deviceMap.get(device.getId()));
                    deviceListLiveData.setValue(getDevices());
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "deleteDevice onError", e);
                    setState(State.OK, true);
                });

        return true;
    }

    @MainThread
    public synchronized boolean removeAllDevices() {
        Logger.i(TAG, "removeAllDevices...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            deviceRepository.deleteAllDevices();
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                x -> {
                    Logger.i(TAG, "removeAllDevice onSuccess.");
                    deviceMap.clear();
                    deviceListLiveData.setValue(getDevices());
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "removeAllDevice onError", e);
                    setState(State.OK, true);
                });

        return true;
    }

    @MainThread
    public synchronized boolean updateDevice(@NonNull final Device device, String newName) {
        Logger.i(TAG, "updateDevice - " + device);
        Logger.i(TAG, "  new name: " + newName);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        if (!deviceMap.containsKey(device.getId())) {
            Logger.i(TAG, "  No such device.");
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            DeviceEntity deviceEntity = new DeviceEntity(device.getType(), newName, device.getAddress());
            deviceRepository.updateDevice(deviceEntity);
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                x -> {
                    Logger.i(TAG, "updateDevice onSuccess - " + device);
                    device.setName(newName);
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "updateDevice onError - " + device, e);
                    setState(State.OK, true);
                });

        return true;
    }

    //
    // Private methods
    //

    @MainThread
    private State getCurrentState() {
        return stateChangeLiveData.getValue().getCurrentState();
    }

    @MainThread
    private void setState(State newState, boolean isError) {
        State currentState = getCurrentState();
        stateChangeLiveData.setValue(new StateChange(currentState, newState, isError));
    }

    private Device createDevice(DeviceType type, @NonNull String name, @NonNull String address) {
        Logger.i(TAG, "createDevice - type: " + type.toString() + ", name: " + name + ", address: " + address);

        Device device = bluetoothDeviceManager.createDevice(type, name, address);
        if (device == null) device = infraRedDeviceManager.createDevice(type, name, address);

        if (device == null)
            Logger.i(TAG, "  No manager for device type " + type.toString());

        return device;
    }

    @MainThread
    private boolean addDevice(@NonNull final Device device, boolean notify) {
        Logger.i(TAG, "addDevice - " + device);

        if (deviceMap.containsKey(device.getId())) {
            Logger.i(TAG, "  Device already added.");
            return false;
        }

        deviceMap.put(device.getId(), device);
        if (notify) deviceListLiveData.setValue(getDevices());

        return true;
    }

    @MainThread
    private void addDevices(@NonNull final List<Device> devices) {
        Logger.i(TAG, "addDevices - " + devices.size());
        for (Device device : devices) {
            addDevice(device, false);
        }

        deviceListLiveData.setValue(getDevices());
    }

//    @MainThread
//    private void saveDevices() {
//        Logger.i(TAG, "saveDevices...");
//
//        setState(State.SAVING, false);
//
//        Single.fromCallable(() -> {
//            List<DeviceEntity> deviceEntities = new ArrayList<>();
//            for (Device device : getDevices()) deviceEntities.add(DeviceEntity.fromDevice(device));
//            deviceRepository.saveDevices(deviceEntities);
//            return true;
//        })
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe(
//                x -> {
//                    Logger.i(TAG, "saveDevices onSuccess.");
//                    setState(State.OK, false);
//                },
//                e -> {
//                    Logger.e(TAG, "saveDevices onError", e);
//                    setState(State.OK, true);
//                });
//    }
}
