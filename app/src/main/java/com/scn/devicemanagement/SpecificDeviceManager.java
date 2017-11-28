package com.scn.devicemanagement;

import io.reactivex.Observable;

/**
 * Created by steve on 2017. 11. 22..
 */

abstract class SpecificDeviceManager {

    abstract Observable<Device> startScan();
    abstract void stopScan();

    abstract Device createDevice(DeviceType type, String name, String address);
}
