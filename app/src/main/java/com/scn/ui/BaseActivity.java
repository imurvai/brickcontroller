package com.scn.ui;

import android.app.Dialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;
import com.scn.ui.dialogs.DeterministicProgressDialog;
import com.scn.ui.dialogs.IndeterministicProgressDialog;
import com.scn.ui.dialogs.MessageBox;
import com.scn.ui.dialogs.QuestionDialog;
import com.scn.ui.dialogs.ValueEnterDialog;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Created by steve on 2017. 10. 29..
 */

public abstract class BaseActivity extends AppCompatActivity {

    //
    // Members
    //

    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int REQUEST_BT_ENABLE = 0x4201;

    protected static final String EXTRA_DEVICE_ID = "EXTRA_DEVICE_ID";
    protected static final String EXTRA_CREATION_NAME = "EXTRA_CREATION_NAME";
    protected static final String EXTRA_CONTROLLER_PROFILE_ID = "EXTRA_CONTROLLER_PROFILE_ID";
    protected static final String EXTRA_CONTROLLER_EVENT_ID = "EXTRA_CONTROLLER_EVENT_ID";
    protected static final String EXTRA_CONTROLLER_ACTION_ID = "EXTRA_CONTROLLER_ACTION_ID";

    @Inject ViewModelProvider.Factory viewModelFactory;
    @Inject DeviceManager deviceManager;

    private Dialog dialog;

    private BluetoothAdapterBroadcastReceiver bluetoothAdapterBroadcastReceiver = null;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Logger.i(TAG, "onResume...");
        super.onResume();

        if (!deviceManager.isBluetoothLESupported()) {
            return;
        }

        if (bluetoothAdapterBroadcastReceiver == null) {
            Logger.i(TAG, "  Registering to the Bluetooth broadcast receiver...");
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothAdapterBroadcastReceiver, filter);
        }

        if (!deviceManager.isBluetoothOn()) {
            startBluetoothRequestActivity();
        }
    }

    @Override
    protected void onPause() {
        Logger.i(TAG, "onPause...");

        if (bluetoothAdapterBroadcastReceiver != null) {
            unregisterReceiver(bluetoothAdapterBroadcastReceiver);
            bluetoothAdapterBroadcastReceiver = null;
        }

        if (dialog != null) {
            dialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.i(TAG, "onActivityResult - request code: " + requestCode + ", result code: " + resultCode);

        if (requestCode == REQUEST_BT_ENABLE) {
            Logger.i(TAG, "  REQUEST_BT_ENABLE");

            if (resultCode != RESULT_OK) {
                Logger.i(TAG, "  Not RESULT_OK, exiting...");
                BaseActivity.this.finishAffinity();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //
    // API
    //

    protected  <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
        Logger.i(TAG, "getViewModel - " + viewModelClass.getSimpleName());
        return viewModelFactory.create(viewModelClass);
    }

    protected void dismissDialog() {
        Logger.i(TAG, "dismissDialog...");

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    protected void showMessageBox(@NonNull final String message) {
        Logger.i(TAG, "showMessageBox - " + message);

        dismissDialog();
        dialog = new MessageBox(this, message, null);
        dialog.show();
    }

    protected void showQuestionDialog(@NonNull final String question,
                                      @NonNull final DialogInterface.OnClickListener onPositiveListener,
                                      final DialogInterface.OnClickListener onNegativeListener) {
        Logger.i(TAG, "showQuestionDialog - " + question);

        dismissDialog();
        dialog = new QuestionDialog(this, question, onPositiveListener, onNegativeListener, null);
        dialog.show();
    }

    protected void showAlertDialog(@NonNull final String message) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new MessageBox(this, message, null);
        dialog.show();
    }

    protected void showAlertDialog(@NonNull final  String message,
                                   @NonNull final DialogInterface.OnDismissListener dismissListener) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new MessageBox(this, message, dismissListener);
        dialog.show();
    }

    protected void showProgressDialog(@NonNull final String message) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        dialog = new IndeterministicProgressDialog(this, message, null, null);
        dialog.show();
    }

    protected void showProgressDialog(@NonNull final String message,
                                      @NonNull final DialogInterface.OnClickListener cancelClickListener) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        dialog = new IndeterministicProgressDialog(this, message, cancelClickListener, null);
        dialog.show();
    }

    protected void showProgressDialog(@NonNull final String message,
                                      final int maxProgress,
                                      final int progress,
                                      final DialogInterface.OnClickListener cancelClickListener) {
        Logger.i(TAG, "showProgressDialog - " + message);

        if (dialog != null && dialog instanceof DeterministicProgressDialog) {
            DeterministicProgressDialog progressDialog = (DeterministicProgressDialog)dialog;
            progressDialog.setProgress(progress);
        }
        else {
            dismissDialog();
            dialog = new DeterministicProgressDialog(this, message, maxProgress, progress, cancelClickListener, null);
            dialog.show();
        }
    }

    protected void showValueEnterDialog(@NonNull final String message,
                                        @NonNull final String initialValue,
                                        @NonNull final ValueEnterDialog.OnValueEnterDialogListener valueEnterListener) {
        Logger.i(TAG, "showValueEnterDialog - " + message);
        ValueEnterDialog dialog = new ValueEnterDialog(this, message, initialValue, valueEnterListener, null);
        dialog.show();
    }

    //
    // Private methods and classes
    //

    private void startBluetoothRequestActivity() {
        Logger.i(TAG, "startBluetoothRequestActivity...");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_BT_ENABLE);
    }

    private class BluetoothAdapterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i(TAG, "bluetoothAdapterBroadcastReceiver.onReceive");

            final String action = intent.getAction();

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                Logger.i(TAG, "  BluetoothAdapter.ACTION_STATE_CHANGED");

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    startBluetoothRequestActivity();
                }
            }
        }
    }
}
