package com.scn.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-31.
 */

public final class IndeterministicProgressDialog extends Dialog {

    //
    // Members
    //

    private static final String TAG = QuestionDialog.class.getSimpleName();

    @BindView(R.id.message) TextView messageTextView;
    @BindView(R.id.cancel_button) Button cancelButton;

    //
    // Constructor
    //

    public IndeterministicProgressDialog(@NonNull Context context,
                                         @NonNull final String message,
                                         final OnDismissListener dismissListener) {
        super(context);

        setContentView(R.layout.dialog_indeterministic_progress);
        ButterKnife.bind(this);

        setOnDismissListener(dismissListener);

        messageTextView.setText(message);
        cancelButton.setOnClickListener(view -> IndeterministicProgressDialog.this.dismiss());
    }
}
