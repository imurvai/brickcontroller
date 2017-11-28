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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Created by steve on 2017. 09. 07..
 */

@Singleton
public final class BluetoothDeviceManager extends SpecificDeviceManager {

    //
    // Private members
    //

    private static final String TAG = BluetoothDeviceManager.class.getSimpleName();

    private Context context = null;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private ObservableEmitter<Device> deviceEmitter;
    private Object deviceEmitterLock = new Object();

    //
    // Constructor
    //

    @Inject
    public BluetoothDeviceManager(Context context) {
        Logger.i(TAG, "constructor...");

        if (context == null)
            throw new IllegalArgumentException("context is null.");

        this.context = context;

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
    static boolean isBluetoothLESupported(Context context) {
        Logger.i(TAG, "isBluetoothLESupported...");

        boolean isSupported = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        Logger.i(TAG, "  Bluetooth is " + (isSupported ? "" : "not") + " supported.");
        return isSupported;
    }

    @MainThread
    static boolean isBluetoothOn() {
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

        if (!isBluetoothLESupported(context)) {
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
    Device createDevice(DeviceType type, String name, String address) {
        Logger.i(TAG, "createDevice...");

        if (!isBluetoothLESupported(context)) {
            return null;
        }

        switch (type) {
            case SBRICK:
                return new SBrickDevice(name, address, this);
            case BUWIZZ:
                return new BuWizzDevice(name, address, this);
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

            String manufacturerData = getManufacturerData(scanRecord.getBytes());
            Logger.i(TAG, "  manufacturer data: " + manufacturerData);

            synchronized (deviceEmitterLock) {
                if (deviceEmitter != null) {
                    if (manufacturerData.startsWith("98 01")) {
                        deviceEmitter.onNext(createDevice(DeviceType.SBRICK, result.getDevice().getName(), result.getDevice().getAddress()));
                    }
                    else if (manufacturerData.startsWith("48 4D")) {
                        deviceEmitter.onNext(createDevice(DeviceType.BUWIZZ, result.getDevice().getName(), result.getDevice().getAddress()));
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

    private String getManufacturerData(byte scanRecord[]) {
        Logger.i(TAG, "getManufacturerData...");

        if (scanRecord == null || scanRecord.length == 0) {
            Logger.i(TAG, "  empty scan record.");
            return "";
        }

        int flag = 0;
        int length = 0;
        byte type = 0;
        byte index = 0;
        StringBuilder sb = new StringBuilder();
        String manufacturerData = "";

        for (byte b : scanRecord) {
            switch (flag) {
                case 0:
                    length = b;
                    if (length == 0) return manufacturerData;
                    Logger.i(TAG, "  length: " + length);
                    flag = 1;
                    break;
                case 1:
                    type = b;
                    Logger.i(TAG, "  type: " + type);
                    flag = 1 < length ? 2 : 0;
                    index = 0;
                    break;
                case 2:
                    if (index == 0) sb.setLength(0);
                    sb.append(String.format("%02X", b));
                    if (index < length - 2) {
                        sb.append(" ");
                        index++;
                    }
                    else {
                        String dataString = sb.toString();
                        Logger.i(TAG, "  data: " + dataString);
                        if (type == -1) manufacturerData = dataString;
                        flag = 0;
                    }
                    break;
            }
        }

        return manufacturerData;
    }
}
