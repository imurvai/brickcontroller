package com.scn.ui.controller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerProfile;
import com.scn.ui.OnListItemClickListener;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-24.
 */

final class ControllerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnListItemClickListener<ControllerProfile> {

    //
    // Members
    //

    private List<ControllerProfile> controllerProfileList = new ArrayList<>();
    private ControllerProfile selectedControllerProfile = null;
    private OnListItemClickListener<ControllerProfile> listItemClickListener = null;

    //
    // RecyclerView adapter overrides
    //

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_simple_text, parent, false);
        return new ControllerProfileItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ControllerProfile controllerProfile = controllerProfileList.get(position);
        boolean isSelected = controllerProfile == selectedControllerProfile;
        ((ControllerProfileItemViewHolder)holder).bind(controllerProfile, isSelected, ControllerAdapter.this);
    }

    @Override
    public int getItemCount() {
        if (controllerProfileList == null) return 0;
        return controllerProfileList.size();
    }

    //
    // OnListItemClickListener override
    //

    @Override
    public void onClick(ControllerProfile item, ItemClickAction itemClickAction, Object data) {
        selectedControllerProfile = item;
        notifyDataSetChanged();

        if (listItemClickListener != null) listItemClickListener.onClick(item, itemClickAction, data);
    }

    //
    // API
    //

    public void setControllerProfileList(@NonNull List<ControllerProfile> controllerProfileList) {
        this.controllerProfileList = controllerProfileList;
        this.selectedControllerProfile = controllerProfileList.size() > 0 ? controllerProfileList.get(0) : null;
        notifyDataSetChanged();
    }

    public void setListItemClickListener(@NonNull OnListItemClickListener<ControllerProfile> listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolder
    //

    public class ControllerProfileItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) RelativeLayout relativeLayout;
        @BindView(R.id.text) TextView textView;

        public ControllerProfileItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final ControllerProfile controllerProfile, boolean isSelected, OnListItemClickListener<ControllerProfile> listItemClickListener) {
            relativeLayout.setBackgroundColor(isSelected ? 0xfff0f0f0 : 0xffffffff);
            textView.setText(controllerProfile.getName());

            itemView.setOnClickListener(view -> {
                if (listItemClickListener != null) listItemClickListener.onClick(controllerProfile, OnListItemClickListener.ItemClickAction.CLICK, null);
            });
        }
    }
}
