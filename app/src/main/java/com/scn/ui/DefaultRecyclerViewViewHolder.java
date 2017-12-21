package com.scn.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-21.
 */

public final class DefaultRecyclerViewViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.default_text) TextView defaultTextView;

    public DefaultRecyclerViewViewHolder(View itemView, String defaulText) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        defaultTextView.setText(defaulText);
    }
}

