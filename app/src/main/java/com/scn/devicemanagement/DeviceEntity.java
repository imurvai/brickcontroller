package com.scn.devicemanagement;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

/**
 * Created by steve on 2017. 11. 19..
 */

@Entity(tableName = "devices", primaryKeys = { "type", "address" })
final class DeviceEntity {

    @NonNull public Device.DeviceType type;
    @NonNull public String name;
    @NonNull public String address;
    public String deviceSpecificDataJSon;

    DeviceEntity(@NonNull Device.DeviceType type, @NonNull String name, @NonNull String address, String deviceSpecificDataJSon) {
        this.type = type;
        this.name = name;
        this.address = address;
        this.deviceSpecificDataJSon = deviceSpecificDataJSon;
    }

    static DeviceEntity fromDevice(@NonNull Device device) {
        return new DeviceEntity(device.getType(), device.getName(), device.getAddress(), device.getDeviceSpecificDataJSon());
    }

    @Override
    public String toString() {
        return "Name: " + name + ", type: " + type + ", address: " + address + ", device specific data: " + deviceSpecificDataJSon;
    }
}
