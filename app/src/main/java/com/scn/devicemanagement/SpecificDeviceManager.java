package com.scn.devicemanagement;

import android.content.Context;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import io.reactivex.Observable;

/**
 * Created by steve on 2017. 11. 22..
 */

abstract class SpecificDeviceManager implements DeviceFactory {

    //
    // Private, protected members
    //

    private static final String TAG = SpecificDeviceManager.class.getSimpleName();

    protected Context context;

    //
    // Constructor
    //

    SpecificDeviceManager(@NonNull Context context) {
        Logger.i(TAG, "constructor...");
        this.context = context;
    }

    //
    // API
    //

    abstract Observable<Device> startScan();
    abstract void stopScan();

    //
    // DeviceFactory override
    //

    @Override
    public abstract Device createDevice(@NonNull Device.DeviceType type, @NonNull String name, @NonNull String address, String deviceSpecificDataJSon);
}
