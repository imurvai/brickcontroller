package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.TypeConverter;

import com.scn.creationmanagement.ControllerEvent;

/**
 * Created by imurvai on 2017-12-17.
 */

final class ControllerEventTypeTypeContverer {

    @TypeConverter
    public static int ControllerEventTypeToInt(ControllerEvent.ControllerEventType eventType) {
        switch (eventType) {
            case KEY: return 0;
            case MOTION: return 1;
            default: throw new IllegalArgumentException("Illegal event type.");
        }
    }

    @TypeConverter
    public static ControllerEvent.ControllerEventType IntToControllerEventType(int eventTypeValue) {
        switch (eventTypeValue) {
            case 0: return ControllerEvent.ControllerEventType.KEY;
            case 1: return ControllerEvent.ControllerEventType.MOTION;
            default: throw new IllegalArgumentException("Illegal event type value: " + eventTypeValue);
        }
    }
}
