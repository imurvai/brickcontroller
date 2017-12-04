package com.scn.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.DialogInterface;
import android.os.Bundle;
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

    protected void showMessageBox(String message) {
        Logger.i(TAG, "showMessageBox - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(this.getString(R.string.ok), null)
                .create();
        dialog.show();
    }

    protected void showMessageBox(String message, final DialogInterface.OnClickListener onClickListener) {
        Logger.i(TAG, "showMessageBox - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(this.getString(R.string.ok), onClickListener)
                .create();
        dialog.show();
    }

    protected void showMessageBox(String title, String message, int iconId, final DialogInterface.OnClickListener onClickListener) {
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

    protected void showQuestionDialog(String question, String positiveButtonText, String negativeButtonText, final DialogInterface.OnClickListener onPositiveListener, final DialogInterface.OnClickListener onNegativeListener) {
        Logger.i(TAG, "showQuestionDialog - " + question);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(question)
                .setPositiveButton(positiveButtonText, onPositiveListener)
                .setNegativeButton(negativeButtonText, onNegativeListener)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(String message) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .create();
        dialog.show();
    }

    protected void showAlertDialog(String title, String message, String positiveButtonText, final DialogInterface.OnDismissListener dismissListener) {
        Logger.i(TAG, "showAlertDialog - " + message);

        dismissDialog();
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, null)
                .setOnDismissListener(dismissListener)
                .create();
        dialog.show();
    }

    protected void showProgressDialog(String message) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        dialog = new ProgressDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .create();
        dialog.show();
    }

    protected void showProgressDialog(String message, final DialogInterface.OnClickListener onClickListener) {
        Logger.i(TAG, "showProgressDialog - " + message);

        dismissDialog();
        dialog = new ProgressDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(this.getString(R.string.cancel), onClickListener)
                .create();
        dialog.show();
    }
}
