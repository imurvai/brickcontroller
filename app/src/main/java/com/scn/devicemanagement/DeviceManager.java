package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.devicemanagement.devicerepository.DeviceRepository;
import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

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
public final class DeviceManager implements DeviceFactory {

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

        stateChangeLiveData.setValue(new StateChange(State.OK, State.OK, false));
    }

    //
    // DeviceFactory methods
    //

    public Device createDevice(DeviceType type, @NonNull String name, @NonNull String address) {
        Logger.i(TAG, "createDevice - type: " + type.toString() + ", name: " + name + ", address: " + address);

        Device device = null;

        switch (type) {
            case INFRARED:
                if (InfraRedDeviceManager.isInfraRedSupported(context))
                    device = infraRedDeviceManager.createDevice(type, name, address);
                break;

            case BUWIZZ:
            case SBRICK:
                if (BluetoothDeviceManager.isBluetoothLESupported(context))
                    device = bluetoothDeviceManager.createDevice(type, name, address);
                break;
        }

        return device;
    }

    //
    // API
    //

    @MainThread
    public boolean isBluetoothLESupported() {
        Logger.i(TAG, "isBluetoothLESupported...");
        return BluetoothDeviceManager.isBluetoothLESupported(context);
    }

    @MainThread
    public boolean isBluetoothOn() {
        Logger.i(TAG, "isBluetoothOn...");
        return BluetoothDeviceManager.isBluetoothOn();
    }

    @MainThread
    public boolean isInfraSupported() {
        Logger.i(TAG, "isInfraSupported...");
        return InfraRedDeviceManager.isInfraRedSupported(context);
    }

    @MainThread
    public LiveData<StateChange<DeviceManager.State>> getStateChangeLiveData() {
        Logger.i(TAG, "getStateChangeLiveData...");
        return stateChangeLiveData;
    }

    @MainThread
    public boolean loadDevicesAsync() {
        Logger.i(TAG, "loadDevicesAsync...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.LOADING, false);

        Single.fromCallable(() -> {
                deviceRepository.loadDevices(this);
                return true;
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    x -> {
                        Logger.i(TAG, "Load devices onSuccess...");
                        setState(State.OK, false);
                    },
                    error -> {
                        Logger.e(TAG, "Load devices onError...", error);
                        setState(State.OK, true);
                    });

        return true;
    }

    @MainThread
    public boolean startDeviceScan() {
        Logger.i(TAG, "startDeviceScan...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        Observable<Device> bluetoothDeviceObservable = bluetoothDeviceManager.startScan();
        Observable<Device> infraredDeviceObservable = infraRedDeviceManager.startScan();

        List<Observable<Device>> deviceScanObservables = new ArrayList<>();
        if (bluetoothDeviceObservable != null) deviceScanObservables.add(bluetoothDeviceObservable);
        if (infraredDeviceObservable != null) deviceScanObservables.add(infraredDeviceObservable);

        if (deviceScanObservables.size() > 0) {
            setState(State.SCANNING, false);

            Observable.merge(deviceScanObservables)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(device -> deviceRepository.storeDevice(device)) // TODO: on background thread!!!
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            device -> {
                                Logger.i(TAG, "Device scan onNext - " + device);
                            },
                            error -> {
                                Logger.e(TAG, "Device scan onError...", error);
                                stopDeviceScan();
                                setState(State.OK, true);
                            },
                            () -> {
                                Logger.i(TAG, "Device scan onComplete...");
                                setState(State.OK, false);
                            });

            return true;
        }

        return false;
    }

    @MainThread
    public void stopDeviceScan() {
        Logger.i(TAG, "stopDeviceScan...");

        if (getCurrentState() != State.SCANNING) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return;
        }

        bluetoothDeviceManager.stopScan();
        infraRedDeviceManager.stopScan();
    }

    @MainThread
    public LiveData<List<Device>> getDeviceListLiveData() {
        Logger.i(TAG, "getDeviceListLiveData...");
        return deviceRepository.getDeviceListLiveData();
    }

    public Device getDevice(@NonNull String deviceId) {
        Logger.i(TAG, "getDevice - " + deviceId);
        return deviceRepository.getDevice(deviceId);
    }

    public boolean removeDeviceAsync(@NonNull final Device device) {
        Logger.i(TAG, "removeDeviceAsync - " + device);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            deviceRepository.deleteDevice(device);
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                x -> {
                    Logger.i(TAG, "deleteDevice onSuccess.");
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "deleteDevice onError", e);
                    setState(State.OK, true);
                });

        return true;
    }

    @MainThread
    public boolean removeAllDevicesAsync() {
        Logger.i(TAG, "removeAllDevicesAsync...");

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
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "removeAllDevice onError.", e);
                    setState(State.OK, true);
                });

        return true;
    }

    @MainThread
    public boolean updateDeviceAsync(@NonNull final Device device, String newName) {
        Logger.i(TAG, "updateDeviceAsync - " + device);
        Logger.i(TAG, "  new name: " + newName);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            deviceRepository.updateDevice(device, newName);
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                x -> {
                    Logger.i(TAG, "updateDeviceAsync onSuccess - " + device);
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "updateDeviceAsync onError - " + device, e);
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
        Logger.i(TAG, "setState - " + getCurrentState() + " -> " + newState);
        State currentState = getCurrentState();
        stateChangeLiveData.setValue(new StateChange(currentState, newState, isError));
    }
}
