package com.scn.devicemanagement.devicerepository;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceType;

/**
 * Created by steve on 2017. 11. 19..
 */

@Entity(tableName = "devices", primaryKeys = { "type", "address" })
public class DeviceEntity {

    @NonNull public DeviceType type;
    @NonNull public String name;
    @NonNull public String address;

    public DeviceEntity(@NonNull DeviceType type, @NonNull String name, @NonNull String address) {
        this.type = type;
        this.name = name;
        this.address = address;
    }

    public static DeviceEntity fromDevice(Device device) {
        return new DeviceEntity(device.getType(), device.getName(), device.getAddress());
    }
}
