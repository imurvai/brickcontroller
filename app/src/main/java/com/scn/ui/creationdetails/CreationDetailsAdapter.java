package com.scn.ui.creationdetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerProfile;
import com.scn.ui.DefaultRecyclerViewViewHolder;
import com.scn.ui.OnListItemClickListener;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-18.
 */

final class CreationDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //
    // Private members
    //

    private static final int VIEWTYPE_CONTROLLER_PROFILE = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<ControllerProfile> controllerProfileList = new ArrayList<>();
    private OnListItemClickListener<ControllerProfile> listItemClickListener = null;

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public int getItemViewType(int position) {
        if (controllerProfileList == null || controllerProfileList.size() == 0) {
            return VIEWTYPE_DEFAULT;
        }

        return VIEWTYPE_CONTROLLER_PROFILE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_CONTROLLER_PROFILE: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_controller_profile, parent, false);
                return new ControllerProfileItemViewHolder(view);
            }

            case VIEWTYPE_DEFAULT: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_default, parent, false);
                return new DefaultRecyclerViewViewHolder(view, parent.getContext().getString(R.string.add_controller_profile));
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEWTYPE_CONTROLLER_PROFILE:
                ((ControllerProfileItemViewHolder)holder).bind(controllerProfileList.get(position), listItemClickListener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (controllerProfileList != null && controllerProfileList.size() > 0) {
            return controllerProfileList.size();
        }

        return 1;
    }

    //
    // API
    //

    public void setControllerProfile(@NonNull List<ControllerProfile> controllerProfileList) {
        this.controllerProfileList = controllerProfileList;
        notifyDataSetChanged();
    }

    public void setListItemClickListener(@NonNull OnListItemClickListener<ControllerProfile> listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class ControllerProfileItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.controller_profile_name) TextView controllerProfileNameTextView;
        @BindView(R.id.remove_controller_profile) Button removeControllerProfileButton;

        public ControllerProfileItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final ControllerProfile controllerProfile, final OnListItemClickListener<ControllerProfile> controllerProfileClickListener) {
            controllerProfileNameTextView.setText(controllerProfile.getName());

            itemView.setOnClickListener(view -> {
                if (controllerProfileClickListener != null) controllerProfileClickListener.onClick(controllerProfile, OnListItemClickListener.ItemClickAction.CLICK, null);
            });

            removeControllerProfileButton.setOnClickListener(view -> {
                if (controllerProfileClickListener != null) controllerProfileClickListener.onClick(controllerProfile, OnListItemClickListener.ItemClickAction.REMOVE, null);
            });
        }
    }
}
