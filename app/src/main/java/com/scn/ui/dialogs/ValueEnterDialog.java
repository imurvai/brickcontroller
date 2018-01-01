package com.scn.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-31.
 */

public final class ValueEnterDialog extends Dialog {

    public interface OnValueEnterDialogListener {
        void onValueEntered(String value);
    }

    //
    // Members
    //

    private static final String TAG = ValueEnterDialog.class.getSimpleName();

    @BindView(R.id.message) TextView messageTextView;
    @BindView(R.id.value) EditText valueEditText;
    @BindView(R.id.ok_button) Button okButton;
    @BindView(R.id.cancel_button) Button cancelButton;

    //
    // Constructor
    //

    public ValueEnterDialog(@NonNull final Context context,
                            @NonNull final String message,
                            @NonNull final String initialValue,
                            final OnValueEnterDialogListener valueEnterDialogListener,
                            final OnDismissListener dismissListener) {
        super(context);

        setContentView(R.layout.dialog_value_enter);
        ButterKnife.bind(this);

        setOnDismissListener(dismissListener);

        messageTextView.setText(message);
        valueEditText.setText(initialValue);

        okButton.setOnClickListener(view -> {
            dismiss();
            if (valueEnterDialogListener != null) valueEnterDialogListener.onValueEntered(valueEditText.getText().toString());
        });

        cancelButton.setOnClickListener(view -> dismiss());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        valueEditText.setSelection(valueEditText.getText().length());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
