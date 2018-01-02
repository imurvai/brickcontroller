package com.scn.devicemanagement;

import android.bluetooth.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Created by steve on 2017. 09. 07..
 */

@Singleton
final class BluetoothDeviceManager extends SpecificDeviceManager {

    //
    // Private members
    //

    private static final String TAG = BluetoothDeviceManager.class.getSimpleName();

    private final BluetoothAdapter bluetoothAdapter;
    private ObservableEmitter<Device> deviceEmitter = null;
    private final Object deviceEmitterLock = new Object();

    //
    // Constructor
    //

    @Inject
    public BluetoothDeviceManager(Context context) {
        super(context);
        Logger.i(TAG, "constructor...");

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;
        }
        else {
            bluetoothAdapter = null;
        }
    }

    //
    // API
    //

    @MainThread
    boolean isBluetoothLESupported() {
        Logger.i(TAG, "isBluetoothLESupported...");

        boolean isSupported = bluetoothAdapter != null;

        Logger.i(TAG, "  Bluetooth is " + (isSupported ? "" : "NOT ") + "supported.");
        return isSupported;
    }

    @MainThread
    boolean isBluetoothOn() {
        Logger.i(TAG, "isBluetoothOn...");

        boolean isOn = bluetoothAdapter != null && bluetoothAdapter.isEnabled();

        Logger.i(TAG, "  Bluetooth is " + (isOn ? "on" : "off") + ".");
        return isOn;
    }

    BluetoothAdapter getBluetoothAdapter() {
        Logger.i(TAG, "getBluetoothAdapter...");
        return bluetoothAdapter;
    }

    //
    // SpecificDeviceManager overrides
    //

    @MainThread
    @Override
    public synchronized Observable<Device> startScan() {
        Logger.i(TAG, "startScan...");

        if (!isBluetoothLESupported()) {
            return Observable.empty();
        }

        synchronized (deviceEmitterLock) {
            if (deviceEmitter != null) {
                Logger.i(TAG, "  Already scanning.");
                return null;
            }

            return Observable.create(emitter -> {
                Logger.i(TAG, "Subscribe on the scan observable...");

                synchronized (deviceEmitterLock) {
                    if (deviceEmitter == null) {
                        bluetoothAdapter.startLeScan(scanCallback);
                        deviceEmitter = emitter;
                    }
                }
            });
        }
    }

    @MainThread
    @Override
    public synchronized void stopScan() {
        Logger.i(TAG, "stopScan...");

        synchronized (deviceEmitterLock) {
            if (deviceEmitter == null) {
                Logger.i(TAG, "  Not scanning.");
                return;
            }

            try {
                bluetoothAdapter.stopLeScan(scanCallback);
            }
            finally {
                deviceEmitter.onComplete();
                deviceEmitter = null;
            }
        }
    }

    @Override
    public Device createDevice(@NonNull Device.DeviceType type, @NonNull  String name, @NonNull  String address, String deviceSpecificDataJSon) {
        Logger.i(TAG, "createDevice...");

        if (!isBluetoothLESupported()) {
            return null;
        }

        switch (type) {
            case SBRICK:
                return new SBrickDevice(context, name, address, BluetoothDeviceManager.this);
            case BUWIZZ:
                return new BuWizzDevice(context, name, address, deviceSpecificDataJSon, BluetoothDeviceManager.this);
            case BUWIZZ2:
                return new BuWizz2Device(context, name, address, deviceSpecificDataJSon, BluetoothDeviceManager.this);
        }

        Logger.i(TAG, "  Not bluetooth device.");
        return null;
    }

    //
    // Private
    //

    private BluetoothAdapter.LeScanCallback scanCallback = (bluetoothDevice, i, scanRecord) -> {
        Logger.i(TAG, "onLeScan...");

        if (bluetoothDevice == null) {
            Logger.i(TAG, "  bluetoothDevice is null.");
            return;
        }

        Logger.i(TAG, "  name: " + bluetoothDevice.getName());
        Logger.i(TAG, "  address: " + bluetoothDevice.getAddress());

        if (scanRecord == null || scanRecord.length == 0) {
            Logger.i(TAG, "  No scanrecord.");
        }

            Map<String, String> scanRecordMap = processScanRecord(scanRecord);
            if (!scanRecordMap.containsKey("FF")) {
                Logger.i(TAG, "  No manufacturer data in scan record.");
                return;
            }

            String manufacturerData = scanRecordMap.get("FF");
            Logger.i(TAG, "  Manufacturer data: " + manufacturerData);

            synchronized (deviceEmitterLock) {
                if (deviceEmitter != null) {
                    if (manufacturerData.startsWith("98 01")) {
                        deviceEmitter.onNext(createDevice(Device.DeviceType.SBRICK, bluetoothDevice.getName(), bluetoothDevice.getAddress(), null));
                    }
                    else if (manufacturerData.startsWith("48 4D")) {
                        deviceEmitter.onNext(createDevice(Device.DeviceType.BUWIZZ, bluetoothDevice.getName(), bluetoothDevice.getAddress(), null));
                    }
                    else if (manufacturerData.startsWith("4E 05")) {
                        deviceEmitter.onNext(createDevice(Device.DeviceType.BUWIZZ2, bluetoothDevice.getName(), bluetoothDevice.getAddress(), null));
                    }
                    else {
                        Logger.i(TAG, "  Unknown bluetooth device.");
                    }
                }
            }
    };

    private Map<String, String> processScanRecord(byte scanRecord[]) {
        Logger.i(TAG, "processScanRecord...");

        Map<String, String> scanRecordMap = new HashMap<>();

        if (scanRecord == null || scanRecord.length == 0) {
            Logger.i(TAG, "  empty scan record.");
            return scanRecordMap;
        }

        boolean isLength = true;
        int length = 0;
        String type = "";
        byte index = 0;
        StringBuilder sb = new StringBuilder();

        for (byte b : scanRecord) {
            if (isLength) {
                length = b;
                Logger.i(TAG, "  length: " + length);
                if (length == 0) {
                    return scanRecordMap;
                }

                isLength = false;
                index = 0;
            } else {
                if (index == 0) {
                    type = String.format("%02X", b);
                    Logger.i(TAG, "  type: " + type);

                    sb.setLength(0);
                    index++;
                } else {
                    sb.append(String.format("%02X", b));

                    if (index < length - 1) {
                        sb.append(" ");
                        index++;
                    }
                    else {
                        String data = sb.toString();
                        Logger.i(TAG, "  data: " + data);

                        scanRecordMap.put(type, data);
                        isLength = true;
                    }
                }
            }
        }

        return scanRecordMap;
    }
}
