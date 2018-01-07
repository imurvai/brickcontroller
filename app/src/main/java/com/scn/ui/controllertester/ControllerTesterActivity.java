package com.scn.ui.controllertester;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2018-01-07.
 */

public final class ControllerTesterActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = ControllerTesterActivity.class.getSimpleName();

    private Map<ControllerEvent, Float> controllerEventMap = new HashMap<>();

    @Inject ControllerTesterAdapter controllerTesterAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    //
    // Activity overrides
    //


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_tester);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(ControllerTesterActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ControllerTesterActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(controllerTesterAdapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD && event.getRepeatCount() == 0) {
            ControllerEvent controllerEvent = new ControllerEvent(ControllerEvent.ControllerEventType.KEY, keyCode);
            controllerEventMap.put(controllerEvent, 0.0F);
            controllerTesterAdapter.setControllerEventMap(controllerEventMap);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD && event.getRepeatCount() == 0) {
            ControllerEvent controllerEvent = new ControllerEvent(ControllerEvent.ControllerEventType.KEY, keyCode);
            if (controllerEventMap.containsKey(controllerEvent)) {
                controllerEventMap.remove(controllerEvent);
                controllerTesterAdapter.setControllerEventMap(controllerEventMap);
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE) {

            for (int motionCode = 0; motionCode < 64; motionCode++) {
                float axisValue = event.getAxisValue(motionCode);
                ControllerEvent controllerEvent = new ControllerEvent(ControllerEvent.ControllerEventType.MOTION, motionCode);

                if (Math.abs(axisValue) > 0.1) {
                    controllerEventMap.put(controllerEvent, axisValue);
                }
                else {
                    if (controllerEventMap.containsKey(controllerEvent)) {
                        controllerEventMap.remove(controllerEvent);
                    }
                }
            }

            controllerTesterAdapter.setControllerEventMap(controllerEventMap);
            return true;
        }

        return super.onGenericMotionEvent(event);
    }
}
