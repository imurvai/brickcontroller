package com.scn.devicemanagement;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.support.annotation.MainThread;

import com.scn.logger.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by steve on 2017. 09. 07..
 */

@Singleton
public final class InfraRedDeviceManager extends SpecificDeviceManager {

    //
    // Private members
    //

    private static final String TAG = InfraRedDeviceManager.class.getSimpleName();

    private Context context;

    private int numConnectedDevices = 0;
    private int outputValues[] = new int[8];

    //
    // Constructor
    //

    @Inject
    public InfraRedDeviceManager(Context context) {
        Logger.i(TAG, "constructor...");

        if (context == null)
            throw new IllegalArgumentException("context is null.");

        this.context = context;

        resetOutputs();
    }

    //
    // API
    //

    static boolean isInfraRedSupported(Context context) {
        Logger.i(TAG, "isInfraRedSupported...");

        ConsumerIrManager irManager = context.getSystemService(ConsumerIrManager.class);
        boolean isSupported = irManager != null && irManager.hasIrEmitter();

        Logger.i(TAG, "  Infra is " + (isSupported ? "" : "NOT") + " supported.");
        return isSupported;
    }

    @MainThread
    synchronized void connect(Device device) {
        Logger.i(TAG, "connect - " + device.getId());
        numConnectedDevices++;
    }

    @MainThread
    synchronized void disconnect(Device device) {
        Logger.i(TAG, "disconnect - " + device.getId());

        numConnectedDevices--;
        if (numConnectedDevices == 0)
            resetOutputs();
    }

    @MainThread
    synchronized void setOutput(String deviceAddress, int channel, int value) {
        Logger.i(TAG, "setOutput - address: " + deviceAddress + ", channel: " + channel + ", value: " + value);

        int address = convertAddress(deviceAddress);
        outputValues[address * 2 + channel] = value;
    }

    //
    // SpecificDeviceManager overrides
    //

    @MainThread
    @Override
    synchronized Observable<Device> startScan() {
        Logger.i(TAG, "startScan...");

        if (!isInfraRedSupported(context)) {
            return Observable.empty();
        }

        return Observable.fromArray(
                createDevice(DeviceType.INFRARED, "Lego PF 1", "1"),
                createDevice(DeviceType.INFRARED, "Lego PF 2", "2"),
                createDevice(DeviceType.INFRARED, "Lego PF 3", "3"),
                createDevice(DeviceType.INFRARED, "Lego PF 4", "4"));
    }

    @MainThread
    @Override
    synchronized void stopScan() {
        Logger.i(TAG, "stopScan...");
    }

    @Override
    Device createDevice(DeviceType type, String name, String address) {
        Logger.i(TAG, "createDevice...");

        if (!isInfraRedSupported(context)) {
            return null;
        }

        if (type != DeviceType.INFRARED) {
            Logger.i(TAG, "  Not infrared device.");
            return null;
        }

        return new InfraRedDevice(name, address, this);
    }

    //
    // Private methods
    //

    private void resetOutputs() {
        Logger.i(TAG, "resetOutputs");
        for (int i = 0; i < 8; i++) outputValues[i] = 0;
    }

    private int convertAddress(String address) {
        Logger.i(TAG, "convertAddress - " + address);

        switch (address) {
            case "1": return 0;
            case "2": return 1;
            case "3": return 2;
            case "4": return 3;
            default: throw new IllegalArgumentException("Invalid infra address.");
        }
    }
}
