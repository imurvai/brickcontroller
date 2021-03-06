package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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

    private static final int SCAN_INTERVAL_SECS = 10;

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

    private final DeviceRepository deviceRepository;
    private final BluetoothDeviceManager bluetoothDeviceManager;
    private final InfraRedDeviceManager infraRedDeviceManager;

    private final MutableLiveData<StateChange<DeviceManager.State>> stateChangeLiveData = new MutableLiveData<>();

    private final List<Device.DeviceType> supportedDeviceTypes = Arrays.asList(
            Device.DeviceType.INFRARED,
            Device.DeviceType.SBRICK,
            Device.DeviceType.BUWIZZ,
            Device.DeviceType.BUWIZZ2
    );

    //
    // Constructor
    //

    @Inject
    public DeviceManager(@NonNull DeviceRepository deviceRepository,
                         @NonNull BluetoothDeviceManager bluetoothDeviceManager,
                         @NonNull InfraRedDeviceManager infraRedDeviceManager) {
        Logger.i(TAG, "constructor...");

        this.deviceRepository = deviceRepository;
        this.bluetoothDeviceManager = bluetoothDeviceManager;
        this.infraRedDeviceManager = infraRedDeviceManager;

        stateChangeLiveData.setValue(new StateChange(State.OK, State.OK, false));
    }

    //
    // DeviceFactory methods
    //

    public Device createDevice(Device.DeviceType type, @NonNull String name, @NonNull String address, @NonNull String deviceSpecificDataJSon) {
        Logger.i(TAG, "createDevice - type: " + type.toString() + ", name: " + name + ", address: " + address + ", device specific data: " + deviceSpecificDataJSon);

        Device device = null;

        switch (type) {
            case INFRARED:
                device = infraRedDeviceManager.createDevice(type, name, address, deviceSpecificDataJSon);
                break;

            case BUWIZZ:
            case BUWIZZ2:
            case SBRICK:
                device = bluetoothDeviceManager.createDevice(type, name, address, deviceSpecificDataJSon);
                break;
        }

        return device;
    }

    //
    // API
    //

    public List<Device.DeviceType> getSupportedDeviceTypes() {
        return supportedDeviceTypes;
    }

    @MainThread
    public boolean isBluetoothLESupported() {
        Logger.i(TAG, "isBluetoothLESupported...");
        return bluetoothDeviceManager.isBluetoothLESupported();
    }

    @MainThread
    public boolean isBluetoothOn() {
        Logger.i(TAG, "isBluetoothOn...");
        return bluetoothDeviceManager.isBluetoothOn();
    }

    @MainThread
    public boolean isInfraSupported() {
        Logger.i(TAG, "isInfraSupported...");
        return infraRedDeviceManager.isInfraRedSupported();
    }

    @MainThread
    public LiveData<StateChange<DeviceManager.State>> getStateChangeLiveData() {
        Logger.i(TAG, "getStateChangeLiveData...");
        return stateChangeLiveData;
    }

    @MainThread
    public boolean loadDevicesAsync(boolean forceLoad) {
        Logger.i(TAG, "loadDevicesAsync...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.LOADING, false);

        Single.fromCallable(() -> {
                deviceRepository.loadDevices(this, forceLoad);
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
            setState(State.SCANNING, false, new ScanProgress(0));

            final Disposable timerDisposable = Observable.interval(1, TimeUnit.SECONDS)
                    .take(SCAN_INTERVAL_SECS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            time -> {
                                int intTime = time.intValue();
                                Logger.i(TAG, "Device timer scan onNext - " + intTime);
                                setState(State.SCANNING, false, new ScanProgress(intTime));
                            },
                            error -> {
                                Logger.e(TAG, "Device timer scan onError...", error);
                                stopDeviceScan();
                            },
                            () -> {
                                Logger.i(TAG, "Device timer scan onComplete...");
                                stopDeviceScan();
                            }
                    );

            Observable.merge(deviceScanObservables)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnNext(deviceRepository::storeDevice)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            device -> {
                                Logger.i(TAG, "Device scan onNext - " + device);
                            },
                            error -> {
                                Logger.e(TAG, "Device scan onError...", error);
                                timerDisposable.dispose();
                                stopDeviceScan();
                                setState(State.OK, true);
                            },
                            () -> {
                                Logger.i(TAG, "Device scan onComplete...");
                                timerDisposable.dispose();
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
    public boolean updateDeviceNameAsync(@NonNull final Device device, @NonNull final String newName) {
        Logger.i(TAG, "updateDeviceNameAsync - " + device);
        Logger.i(TAG, "  new name: " + newName);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            deviceRepository.updateDeviceName(device, newName);
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                x -> {
                    Logger.i(TAG, "updateDeviceNameAsync onSuccess - " + device);
                    setState(State.OK, false);
                },
                e -> {
                    Logger.e(TAG, "updateDeviceNameAsync onError - " + device, e);
                    setState(State.OK, true);
                });

        return true;
    }

    @MainThread
    public boolean updateDeviceSpecificDataAsync(@NonNull final Device device, @NonNull final String deviceSpecificDataJSon) {
        Logger.i(TAG, "updateDeviceSpecificDataAsync - " + device);
        Logger.i(TAG, "  new device specific data: " + deviceSpecificDataJSon);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState());
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            deviceRepository.updateDeviceSpecificData(device, deviceSpecificDataJSon);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "updateDeviceSpecificDataAsync onSuccess - " + device);
                            setState(State.OK, false);
                        },
                        e -> {
                            Logger.e(TAG, "updateDeviceSpecificDataAsync onError - " + device, e);
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

    @MainThread
    private void setState(State newState, boolean isError, Object data) {
        Logger.i(TAG, "setState with data - " + getCurrentState() + " -> " + newState);
        State currentState = getCurrentState();
        stateChangeLiveData.setValue(new StateChange(currentState, newState, isError, data));
    }

    //
    // ScanProgress
    //

    public static class ScanProgress {
        public int maxProgress;
        public int progress;
        public ScanProgress(int progress) {
            this.maxProgress = SCAN_INTERVAL_SECS;
            this.progress = progress;
        }
    }
}
