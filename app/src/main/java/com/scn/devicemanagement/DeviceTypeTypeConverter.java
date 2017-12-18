package com.scn.devicemanagement;

import android.arch.persistence.room.TypeConverter;

/**
 * Created by steve on 2017. 11. 20..
 */

final class DeviceTypeTypeConverter {

    @TypeConverter
    public static int deviceTypeToInt(Device.DeviceType deviceType) {
        switch (deviceType) {
            case BUWIZZ: return 0;
            case SBRICK: return 1;
            case INFRARED: return 2;
            default: throw new IllegalArgumentException("Invalid device type.");
        }
    }

    @TypeConverter
    public static Device.DeviceType intToDeviceType(int type) {
        switch (type) {
            case 0: return Device.DeviceType.BUWIZZ;
            case 1: return Device.DeviceType.SBRICK;
            case 2: return Device.DeviceType.INFRARED;
            default: throw new IllegalArgumentException("Invalid device type.");
        }
    }
}
