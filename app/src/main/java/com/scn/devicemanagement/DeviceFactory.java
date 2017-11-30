package com.scn.devicemanagement;

/**
 * Created by imurvai on 2017-11-30.
 */

public interface DeviceFactory {

    //
    // API
    //

    Device createDevice(DeviceType type, String name, String address);
}
