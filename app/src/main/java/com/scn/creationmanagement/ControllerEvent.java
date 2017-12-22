package com.scn.creationmanagement;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imurvai on 2017-12-17.
 */

@Entity(tableName = "controller_events",
        foreignKeys = @ForeignKey(
                entity = ControllerProfile.class,
                parentColumns = { "id" },
                childColumns = { "controller_profile_id" }
        ))
public final class ControllerEvent {

    //
    // Public types
    //

    public enum ControllerEventType {
        KEY,
        MOTION
    }

    //
    // Private members
    //

    @Ignore
    private static final String TAG = ControllerEvent.class.getSimpleName();

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "controller_profile_id")
    private long controllerProfileId;

    @ColumnInfo(name = "event_type")
    private ControllerEventType eventType;

    @ColumnInfo(name = "event_code")
    private int eventCode;

    @Ignore
    private List<ControllerAction> controllerActions = new ArrayList<>();

    //
    // Constructor
    //

    ControllerEvent(long id, long controllerProfileId, @NonNull ControllerEventType eventType, int eventCode) {
        Logger.i(TAG, "constructor - eventType: " + eventType + ", eventCode: " + eventCode);
        this.id = id;
        this.controllerProfileId = controllerProfileId;
        this.eventType = eventType;
        this.eventCode = eventCode;
    }

    //
    // API
    //

    public long getId() { return id; }
    void setId(long value) { id = value; }

    public long getControllerProfileId() { return controllerProfileId; }
    void setControllerProfileId(long value) { controllerProfileId = value; }

    public ControllerEventType getEventType() { return eventType; }
    void setEventType(ControllerEventType value) { eventType = value; }

    public int getEventCode() { return eventCode; }
    void setEventCode(int value) { eventCode = value; }

    public List<ControllerAction> getControllerActions() { return controllerActions; }

    ControllerAction getControllerAction(@NonNull String deviceId, int channel) {
        Logger.i(TAG, "checkControllerAction - event type: " + eventType + ", event code: " + eventCode);

        for (ControllerAction controllerAction : controllerActions) {
            if (controllerAction.getDeviceId().equals(deviceId) && controllerAction.getChannel() == channel) {
                return controllerAction;
            }
        }

        return null;
    }

    boolean addControllerAction(ControllerAction controllerAction) {
        Logger.i(TAG, "addControllerAction - " + controllerAction);

        if (controllerActions.contains(controllerAction)) {
            Logger.w(TAG, "  Controller action with the same device and channel already exists.");
        }

        controllerActions.add(controllerAction);
        return true;
    }

    boolean removeControllerAction(ControllerAction controllerAction) {
        Logger.i(TAG, "removeControllerAction - " + controllerAction);

        if (!controllerActions.contains(controllerAction)) {
            Logger.w(TAG, "  No such controller action.");
            return false;
        }

        controllerActions.remove(controllerAction);
        return true;
    }

    public String getEventText() {
        switch (eventType) {
            case KEY:
                switch (eventCode) {
                    case KeyEvent.KEYCODE_DPAD_UP: return "D-Pad Up";
                    case KeyEvent.KEYCODE_DPAD_DOWN: return "D-Pad Down";
                    case KeyEvent.KEYCODE_DPAD_RIGHT: return "D-Pad Right";
                    case KeyEvent.KEYCODE_DPAD_LEFT: return "D-Pad Left";

                    case KeyEvent.KEYCODE_BUTTON_THUMBL: return "Left Joy Button";
                    case KeyEvent.KEYCODE_BUTTON_THUMBR: return "Right Joy Button";

                    case KeyEvent.KEYCODE_BUTTON_X: return "Button X";
                    case KeyEvent.KEYCODE_BUTTON_Y: return "Button Y";
                    case KeyEvent.KEYCODE_BUTTON_A: return "Button A";
                    case KeyEvent.KEYCODE_BUTTON_B: return "Button B";

                    case KeyEvent.KEYCODE_BUTTON_L1: return "Left Trigger Button 1";
                    case KeyEvent.KEYCODE_BUTTON_L2: return "Left Trigger Button 2";
                    case KeyEvent.KEYCODE_BUTTON_R1: return "Right Trigger Button 1";
                    case KeyEvent.KEYCODE_BUTTON_R2: return "Right Trigger Button 2";

                    case KeyEvent.KEYCODE_BUTTON_START: return "Start";
                    case KeyEvent.KEYCODE_BUTTON_SELECT: return "Select";

                    default: return "Key code: " + eventCode;
                }

            case MOTION:
                switch (eventCode) {
                    case MotionEvent.AXIS_HAT_X: return "D-Pad Horizontal";
                    case MotionEvent.AXIS_HAT_Y: return "D-Pad Vertical";

                    case MotionEvent.AXIS_X: return "Left Joy Horizontal";
                    case MotionEvent.AXIS_Y: return "Left Joy Vertical";

                    case MotionEvent.AXIS_Z: return "Right Joy Horizontal";
                    case MotionEvent.AXIS_RZ: return "Right Joy Vertical";

                    case MotionEvent.AXIS_LTRIGGER: return "Left Trigger";
                    case MotionEvent.AXIS_THROTTLE: return "Throttle";

                    case MotionEvent.AXIS_RTRIGGER: return "Right Trigger";
                    case MotionEvent.AXIS_BRAKE: return "Brake";

                    default: return "Motion code: " + eventCode;
                }
        }

        return "Unknown";
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControllerEvent))
            return false;

        ControllerEvent other = (ControllerEvent)obj;
        return other.eventType == eventType && other.eventCode == eventCode;
    }

    @Override
    public String toString() {
        return "EventType: " + eventType + ", EventCode: " + getEventText();
    }
}
