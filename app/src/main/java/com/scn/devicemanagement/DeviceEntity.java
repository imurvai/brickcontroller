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
    @NonNull public Device.OutputLevel outputLevel;

    public DeviceEntity(@NonNull Device.DeviceType type, @NonNull String name, @NonNull String address, @NonNull Device.OutputLevel outputLevel) {
        this.type = type;
        this.name = name;
        this.address = address;
        this.outputLevel = outputLevel;
    }

    public static DeviceEntity fromDevice(@NonNull Device device) {
        return new DeviceEntity(device.getType(), device.getName(), device.getAddress(), device.getOutputLevel());
    }

    @Override
    public String toString() {
        return "Name: " + name + ", type: " + type + ", address: " + address + ", outputLevel: " + outputLevel;
    }
}
