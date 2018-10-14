package com.scn.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.logger.Logger;
import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-21.
 */

public final class ControllerEventDialog extends Dialog {

    public interface OnControllerEventListener {
        void onEvent(ControllerEvent.ControllerEventType eventType, int eventCode);
    }

    //
    // Members
    //

    private static final String TAG = ControllerEventDialog.class.getSimpleName();

    @BindView(R.id.cancel_button) Button cancelButton;

    private OnControllerEventListener eventListener;

    //
    // Dialog overrides
    //

    public ControllerEventDialog(@NonNull final Context context,
                                 @NonNull final OnControllerEventListener eventListener,
                                 @NonNull final OnDismissListener dismissListener) {
        super(context);
        Logger.i(TAG, "constructor...");

        setContentView(R.layout.dialog_controller_event);
        ButterKnife.bind(this);

        setOnDismissListener(dismissListener);

        this.eventListener = eventListener;
        cancelButton.setOnClickListener(view -> dismiss());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        Logger.i(TAG, "onKeyUp - keyCode: " + keyCode);

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD && event.getRepeatCount() == 0) {
            Logger.i(TAG, "  Key code: " + keyCode + " - (" + KeyEvent.keyCodeToString(keyCode) + ")");

            dismiss();
            if (eventListener != null) eventListener.onEvent(ControllerEvent.ControllerEventType.KEY, keyCode);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(@NonNull MotionEvent event) {
        Logger.i(TAG, "onGenericMotionEvent...");

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE) {
            for (int axis = 0; axis < 64; axis++) {
                float rawAxisValue = event.getAxisValue(axis);

                if ((axis == MotionEvent.AXIS_RX || axis == MotionEvent.AXIS_RY) &&
                        event.getDevice().getVendorId() == 1356 &&
                        (event.getDevice().getProductId() == 2508 || event.getDevice().getProductId() == 1476))
                {
                    // DualShock 4 hack for the triggers
                    rawAxisValue = (rawAxisValue + 1) / 2;
                }

                if (Math.abs(rawAxisValue) > 0.8) {
                    Logger.i(TAG, "  Axis code: " + axis + " - (" + MotionEvent.axisToString(axis) + ")");

                    dismiss();
                    if (eventListener != null) eventListener.onEvent(ControllerEvent.ControllerEventType.MOTION, axis);
                    return true;
                }
            }
        }

        return super.onGenericMotionEvent(event);
    }
}
