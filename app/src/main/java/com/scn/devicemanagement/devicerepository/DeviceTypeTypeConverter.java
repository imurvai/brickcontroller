package com.scn.devicemanagement.devicerepository;

import android.arch.persistence.room.TypeConverter;

import com.scn.devicemanagement.DeviceType;

/**
 * Created by steve on 2017. 11. 20..
 */

final class DeviceTypeTypeConverter {

    @TypeConverter
    public static int deviceTypeToInt(DeviceType deviceType) {
        switch (deviceType) {
            case BUWIZZ: return 0;
            case SBRICK: return 1;
            case INFRARED: return 2;
            default: throw new IllegalArgumentException("Invalid device type.");
        }
    }

    @TypeConverter
    public static DeviceType intToDeviceType(int type) {
        switch (type) {
            case 0: return DeviceType.BUWIZZ;
            case 1: return DeviceType.SBRICK;
            case 2: return DeviceType.INFRARED;
            default: throw new IllegalArgumentException("Invalid device type.");
        }
    }
}
