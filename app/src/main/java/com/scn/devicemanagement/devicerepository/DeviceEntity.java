package com.scn.devicemanagement.devicerepository;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceType;

/**
 * Created by steve on 2017. 11. 19..
 */

@Entity(tableName = "devices", primaryKeys = { "type", "address" })
final class DeviceEntity {

    @NonNull public DeviceType type;
    @NonNull public String name;
    @NonNull public String address;
    @NonNull public Device.OutputLevel outputLevel;

    public DeviceEntity(@NonNull DeviceType type, @NonNull String name, @NonNull String address, @NonNull Device.OutputLevel outputLevel) {
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
