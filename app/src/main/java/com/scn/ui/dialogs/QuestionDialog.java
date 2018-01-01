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

public final class QuestionDialog extends Dialog {

    //
    // Members
    //

    private static final String TAG = QuestionDialog.class.getSimpleName();

    @BindView(R.id.message) TextView messageTextView;
    @BindView(R.id.yes_button) Button yesButton;
    @BindView(R.id.no_button) Button noButton;

    //
    // Constructor
    //

    public QuestionDialog(@NonNull final Context context,
                          @NonNull final String message,
                          final OnClickListener okClickListener,
                          final OnClickListener noClickListener,
                          final OnDismissListener dismissListener) {
        super(context);

        setContentView(R.layout.dialog_question);
        ButterKnife.bind(this);

        setOnDismissListener(dismissListener);

        messageTextView.setText(message);

        yesButton.setOnClickListener(view -> {
            dismiss();
            if (okClickListener != null) okClickListener.onClick(this, 0);
        });

        noButton.setOnClickListener(view -> {
            dismiss();
            if (noClickListener != null) noClickListener.onClick(this, 0);
        });
    }
}
