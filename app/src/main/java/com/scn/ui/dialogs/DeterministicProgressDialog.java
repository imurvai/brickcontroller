package com.scn.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-31.
 */

public final class DeterministicProgressDialog extends Dialog {

    //
    // Members
    //

    private static final String TAG = QuestionDialog.class.getSimpleName();

    @BindView(R.id.message) TextView messageTextView;
    @BindView(R.id.progressbar) ProgressBar progressBar;
    @BindView(R.id.cancel_button) Button cancelButton;

    //
    // Constructor
    //

    public DeterministicProgressDialog(@NonNull final Context context,
                                       @NonNull final String message,
                                       final int maxProgress,
                                       final int initialProgress,
                                       final OnClickListener cancelClickListener,
                                       final OnDismissListener dismissListener) {
        super(context);

        setContentView(R.layout.dialog_deterministic_progress);
        ButterKnife.bind(this);

        setOnDismissListener(dismissListener);

        messageTextView.setText(message);
        progressBar.setMax(maxProgress);
        progressBar.setProgress(initialProgress);
        cancelButton.setOnClickListener(view -> {
            DeterministicProgressDialog.this.dismiss();
            if (cancelClickListener != null) cancelClickListener.onClick(this, 0);
        });
    }

    //
    // API
    //

    @MainThread
    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }
}
