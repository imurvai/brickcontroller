package com.scn.ui.controllerprofiledetails;

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

final class ControllerEventDialog extends Dialog {

    public interface OnControllerEventDialogDismissListener {
        void onDismiss(ControllerEvent.ControllerEventType eventType, int eventCode);
    }

    //
    // Members
    //

    private static final String TAG = ControllerEventDialog.class.getSimpleName();

    private OnControllerEventDialogDismissListener dismissListener;

    //
    // Dialog overrides
    //

    public ControllerEventDialog(@NonNull Context context, @NonNull OnControllerEventDialogDismissListener dismissListener) {
        super(context);
        Logger.i(TAG, "constructor...");

        this.setContentView(R.layout.dialog_controller_event);
        this.dismissListener = dismissListener;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        Logger.i(TAG, "onKeyUp - keyCode: " + keyCode);

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {
            Logger.i(TAG, "  Key code: " + keyCode + " - (" + KeyEvent.keyCodeToString(keyCode) + ")");

            if (dismissListener != null) dismissListener.onDismiss(ControllerEvent.ControllerEventType.KEY, keyCode);
            dismiss();
            return true;
        }

        return false;
    }

    @Override
    public boolean onGenericMotionEvent(@NonNull MotionEvent event) {
        Logger.i(TAG, "onGenericMotionEvent...");

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
            for (int axis = 0; axis < 64; axis++) {
                if (Math.abs(event.getAxisValue(axis)) > 0.8) {
                    Logger.i(TAG, "  Axis code: " + axis + " - (" + MotionEvent.axisToString(axis) + ")");

                    if (dismissListener != null) dismissListener.onDismiss(ControllerEvent.ControllerEventType.MOTION, axis);
                    dismiss();
                    return true;
                }
            }
        }

        return false;
    }
}
