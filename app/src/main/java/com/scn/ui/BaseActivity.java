package com.scn.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

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

    protected static final int REQUEST_BT_ENABLE = 0x0001;

    protected static final String EXTRA_DEVICE_ID = "EXTRA_DEVICE_ID";
    protected static final String EXTRA_CREATION_NAME = "EXTRA_CREATION_NAME";
    protected static final String EXTRA_CONTROLLER_PROFILE_ID = "EXTRA_CONTROLLER_PROFILE_ID";

    @Inject ViewModelProvider.Factory viewModelFactory;
    @Inject DeviceManager deviceManager;

    private Dialog dialog;

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

        if (!deviceManager.isBluetoothOn()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BT_ENABLE);
        }
    }

    @Override
    protected void onPause() {
        Logger.i(TAG, "onPause...");

        if (dialog != null) {
            dialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.i(TAG, "onActivityResult...");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BT_ENABLE && requestCode != RESULT_OK) {
            BaseActivity.this.finish();
        }
    }

    //
    // API
    //

    public <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
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
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(this.getString(R.string.ok), null)
                .create();
        dialog.show();
    }

    protected void showMessageBox(@NonNull final String message,
                                  @NonNull final DialogInterface.OnClickListener onClickListener) {
        Logger.i(TAG, "showMessageBox - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(this.getString(R.string.ok), onClickListener)
                .create();
        dialog.show();
    }

    protected void showQuestionDialog(@NonNull final String question,
                                      @NonNull final String positiveButtonText,
                                      @NonNull final String negativeButtonText,
                                      @NonNull final DialogInterface.OnClickListener onPositiveListener,
                                      final DialogInterface.OnClickListener onNegativeListener) {
        Logger.i(TAG, "showQuestionDialog - " + question);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(question)
                .setPositiveButton(positiveButtonText, onPositiveListener)
                .setNegativeButton(negativeButtonText, onNegativeListener)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(@NonNull final String message) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(@NonNull final  String message,
                                   @NonNull final DialogInterface.OnDismissListener dismissListener) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .setOnDismissListener(dismissListener)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(@NonNull final String message,
                                   @NonNull final String positiveButtonText,
                                   @NonNull final DialogInterface.OnDismissListener dismissListener) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, null)
                .setOnDismissListener(dismissListener)
                .create();
        dialog.show();
    }

    protected void showProgressDialog(@NonNull final String message) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog = progressDialog;
        dialog.show();
    }

    protected void showProgressDialog(@NonNull final String message,
                                      @NonNull final DialogInterface.OnClickListener onClickListener) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), onClickListener);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog = progressDialog;
        dialog.show();
    }

    protected interface OnValueEnterDialogListener {
        void onValueEntered(String value);
    }

    protected void showValueEnterDialog(@NonNull final String message,
                                        @NonNull final String initialValue,
                                        @NonNull final OnValueEnterDialogListener onValueEnterListener) {
        Logger.i(TAG, "showValueEnterDialog - " + message);

        View view = getLayoutInflater().inflate(R.layout.dialog_content_value_enter, null);
        EditText editText = view.findViewById(R.id.value);
        editText.setText(initialValue);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setView(view)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    if (onValueEnterListener != null) {
                        onValueEnterListener.onValueEntered(editText.getText().toString());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .create();
        alertDialog.show();
    }

    protected int convertDpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT;
    }
}
