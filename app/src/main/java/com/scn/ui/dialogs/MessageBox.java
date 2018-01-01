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

public final class MessageBox extends Dialog {

    //
    // Members
    //

    private static final String TAG = MessageBox.class.getSimpleName();

    @BindView(R.id.message) TextView messageTextView;
    @BindView(R.id.ok_button) Button okButton;

    //
    // Constructor
    //

    public MessageBox(@NonNull final Context context,
                      @NonNull final String message,
                      final OnDismissListener dismissListener) {
        super(context);

        setContentView(R.layout.dialog_messagebox);
        ButterKnife.bind(this);

        setOnDismissListener(dismissListener);
        messageTextView.setText(message);
        okButton.setOnClickListener(view -> MessageBox.this.dismiss());
    }
}
