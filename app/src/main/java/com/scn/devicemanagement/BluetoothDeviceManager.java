package com.scn.devicemanagement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.MainThread;

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

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private ObservableEmitter<Device> deviceEmitter;
    private Object deviceEmitterLock = new Object();

    //
    // Constructor
    //

    @Inject
    public BluetoothDeviceManager(Context context) {
        super(context);
        Logger.i(TAG, "constructor...");

        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("Can't find bluetooth adapter.");

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null)
            throw new RuntimeException("Can't find bluetooth LE scanner.");
    }

    //
    // API
    //

    @MainThread
    public boolean isBluetoothLESupported() {
        Logger.i(TAG, "isBluetoothLESupported...");

        boolean isSupported = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        Logger.i(TAG, "  Bluetooth is " + (isSupported ? "" : "NOT ") + "supported.");
        return isSupported;
    }

    @MainThread
    public boolean isBluetoothOn() {
        Logger.i(TAG, "isBluetoothOn...");

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        boolean isOn = adapter != null && adapter.isEnabled();

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
                        bluetoothLeScanner.startScan(scanCallback);
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
                bluetoothLeScanner.stopScan(scanCallback);
            }
            finally {
                deviceEmitter.onComplete();
                deviceEmitter = null;
            }
        }
    }

    @Override
    Device createDevice(Device.DeviceType type, String name, String address, Device.OutputLevel outputLevel) {
        Logger.i(TAG, "createDevice...");

        if (!isBluetoothLESupported()) {
            return null;
        }

        switch (type) {
            case SBRICK:
                return new SBrickDevice(context, name, address, this);
            case BUWIZZ:
                return new BuWizzDevice(context, name, address, outputLevel, this);
        }

        Logger.i(TAG, "  Not bluetooth device.");
        return null;
    }

    //
    // Private
    //

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Logger.i(TAG, "onScanResult...");
            Logger.i(TAG, "  name: " + result.getDevice().getName());
            Logger.i(TAG, "  address: " + result.getDevice().getAddress());

            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) return;

            android.bluetooth.BluetoothDevice device = result.getDevice();
            if (device == null) return;

            Map<String, String> scanRecordMap = processScanRecord(scanRecord.getBytes());
            if (!scanRecordMap.containsKey("FF")) {
                Logger.i(TAG, "  No manufacturer data in scan record.");
                return;
            }

            String manufacturerData = scanRecordMap.get("FF");
            Logger.i(TAG, "  Manufacturer data: " + manufacturerData);

            synchronized (deviceEmitterLock) {
                if (deviceEmitter != null) {
                    if (manufacturerData.startsWith("98 01")) {
                        deviceEmitter.onNext(createDevice(Device.DeviceType.SBRICK, device.getName(), device.getAddress(), Device.OutputLevel.NORMAL));
                    }
                    else if (manufacturerData.startsWith("48 4D")) {
                        deviceEmitter.onNext(createDevice(Device.DeviceType.BUWIZZ, device.getName(), device.getAddress(), Device.OutputLevel.NORMAL));
                    }
                    else {
                        Logger.i(TAG, "  Unknown bluetooth device.");
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Logger.i(TAG, "onScanFailed...");

            synchronized (deviceEmitterLock) {
                if (deviceEmitter != null) {
                    deviceEmitter.onError(new RuntimeException("Bluetooth scan error - " + errorCode));
                    deviceEmitter = null;
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
