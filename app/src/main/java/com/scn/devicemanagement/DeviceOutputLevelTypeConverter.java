package com.scn.devicemanagement;

import android.arch.persistence.room.TypeConverter;

/**
 * Created by imurvai on 2017-12-14.
 */

final class DeviceOutputLevelTypeConverter {

    @TypeConverter
    public static int outputLevelToInt(Device.OutputLevel outputLevel) {
        switch (outputLevel) {
            case LOW: return 0;
            case NORMAL: return 1;
            case HIGH: return 2;
            default: throw new IllegalArgumentException("Invalid device output level.");
        }
    }

    @TypeConverter
    public static Device.OutputLevel intToutputLevel(int level) {
        switch (level) {
            case 0: return Device.OutputLevel.LOW;
            case 1: return Device.OutputLevel.NORMAL;
            case 2: return Device.OutputLevel.HIGH;
            default: return Device.OutputLevel.NORMAL;
        }
    }
}
