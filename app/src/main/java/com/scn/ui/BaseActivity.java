package com.scn.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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

    @Inject
    ViewModelProvider.Factory viewModelFactory;

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
    protected void onPause() {
        Logger.i(TAG, "onPause...");

        if (dialog != null) {
            dialog.dismiss();
        }

        super.onPause();
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

    protected void showMessageBox(@NonNull String message) {
        Logger.i(TAG, "showMessageBox - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(this.getString(R.string.ok), null)
                .create();
        dialog.show();
    }

    protected void showMessageBox(@NonNull String message, @NonNull final DialogInterface.OnClickListener onClickListener) {
        Logger.i(TAG, "showMessageBox - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(this.getString(R.string.ok), onClickListener)
                .create();
        dialog.show();
    }

    protected void showMessageBox(@NonNull String title, @NonNull String message, int iconId, @NonNull final DialogInterface.OnClickListener onClickListener) {
        Logger.i(TAG, "showMessageBox - tile: " + title + ", message: " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .setPositiveButton(this.getString(R.string.ok), onClickListener)
                .create();
        dialog.show();
    }

    protected void showQuestionDialog(@NonNull String question, @NonNull String positiveButtonText, @NonNull String negativeButtonText, @NonNull final DialogInterface.OnClickListener onPositiveListener, final DialogInterface.OnClickListener onNegativeListener) {
        Logger.i(TAG, "showQuestionDialog - " + question);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(question)
                .setPositiveButton(positiveButtonText, onPositiveListener)
                .setNegativeButton(negativeButtonText, onNegativeListener)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(@NonNull String message) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(@NonNull String message, @NonNull final DialogInterface.OnDismissListener dismissListener) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .setOnDismissListener(dismissListener)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(@NonNull String message, @NonNull String positiveButtonText, @NonNull final DialogInterface.OnDismissListener dismissListener) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, null)
                .setOnDismissListener(dismissListener)
                .create();
        dialog.show();
    }

    protected void showProgressDialog(@NonNull String message) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog = progressDialog;
        dialog.show();
    }

    protected void showProgressDialog(@NonNull String message, @NonNull final DialogInterface.OnClickListener onClickListener) {
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
}
