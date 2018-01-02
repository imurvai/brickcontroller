package com.scn.devicemanagement;

/**
 * Created by imurvai on 2017-11-30.
 */

interface DeviceFactory {

    //
    // API
    //

    Device createDevice(Device.DeviceType type, String name, String address, String deviceSpecificDataJSon);
}
