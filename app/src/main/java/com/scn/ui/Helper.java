package com.scn.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Created by steve on 2017. 10. 29..
 */

public final class Helper {

    //
    // Private members
    //

    //
    // Constructor
    //

    private Helper() {}

    //
    // API
    //

    /**
     * Pops up a message box.
     * @param context is the current context.
     * @param message is the text to show.
     * @return the Dialog instance.
     */
    public static Dialog showMessageBox(Context context, String message) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), null)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Pops up a message box.
     * @param context is the current context.
     * @param message is the text to show.
     * @param onClickListener is the listener when the one and only button is clicked.
     * @return the Dialog instance.
     */
    public static Dialog showMessageBox(Context context, String message, final DialogInterface.OnClickListener onClickListener) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), onClickListener)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Pops up a message box.
     * @param context is the current context.
     * @param title is the dialog title.
     * @param message is the text to show.
     * @param iconId is the id of the icon.
     * @param onClickListener is the listener when the one and only button is clicked.
     * @return the Dialog instance.
     */
    public static Dialog showMessageBox(Context context, String title, String message, int iconId, final DialogInterface.OnClickListener onClickListener) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .setPositiveButton(context.getString(R.string.ok), onClickListener)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Pops up a question dialog.
     * @param context is the context.
     * @param question is the question text.
     * @param positiveButtonText is the text of the positive button.
     * @param negativeButtonText is the text of the negative button.
     * @param onPositiveListener is the listener for positive button.
     * @param onNegativeListener is the listener for negative button.
     * @return the dialog.
     */
    public static Dialog showQuestionDialog(Context context, String question, String positiveButtonText, String negativeButtonText, final DialogInterface.OnClickListener onPositiveListener, final DialogInterface.OnClickListener onNegativeListener) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setMessage(question)
                .setPositiveButton(positiveButtonText, onPositiveListener)
                .setNegativeButton(negativeButtonText, onNegativeListener)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Pops up an alert dialog.
     * @param context is the context.
     * @param title is the title.
     * @param message is the alert message.
     * @param positiveButtonText is the text of the positive button.
     * @param dismissListener is the listener for dismissing the dialog.
     * @return the dialog.
     */
    public static Dialog showAlertDialog(Context context, String title, String message, String positiveButtonText, final DialogInterface.OnDismissListener dismissListener) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, null)
                .setOnDismissListener(dismissListener)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Shows a progress dialog with the given message.
     * @param context The context the dialog is shown in.
     * @param message The message shown on the dialog.
     * @return The ProgressDialog instance.
     */
    public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    /**
     * Shows a progress dialog with the given message and a Cancel button.
     * @param context The context the dialog is shown in.
     * @param message The message shown on the dialog.
     * @param onClickListener Listener to handle the Cancel button event.
     * @return The ProgressDialog instance.
     */
    public static ProgressDialog showProgressDialog(Context context, String message, final DialogInterface.OnClickListener onClickListener) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), onClickListener);
        dialog.show();
        return dialog;
    }

    /**
     * Shows a progress dialog with the given message and a Cancel button.
     * @param context The context the dialog is shown in.
     * @param message The message shown on the dialog.
     * @param maxProgress The Max progrees value.
     * @param onClickListener Listener to handle the Cancel button event.
     * @return The ProgressDialog instance.
     */
    public static ProgressDialog showProgressDialog(Context context, String message, int maxProgress, final DialogInterface.OnClickListener onClickListener) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgressNumberFormat(null);
        dialog.setMax(maxProgress);
        dialog.setProgress(0);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), onClickListener);
        dialog.show();
        return dialog;
    }
}
