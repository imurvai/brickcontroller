package com.scn.ui.creationdetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerProfile;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-18.
 */

final class CreationDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnControllerProfileClickListener {
        void onClick(ControllerProfile controllerProfile);
        void onRemoveClick(ControllerProfile controllerProfile);
    }

    //
    // Private members
    //

    private static final int VIEWTYPE_CONTROLLER_PROFILE = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<ControllerProfile> controllerProfileList = new ArrayList<>();
    private OnControllerProfileClickListener controllerProfileClickListener = null;

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
                        .inflate(R.layout.list_item_creation_details_default, parent, false);
                return new CreationDetailsDefaultViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEWTYPE_CONTROLLER_PROFILE:
                ((ControllerProfileItemViewHolder)holder).bind(controllerProfileList.get(position), controllerProfileClickListener);
                break;

            case VIEWTYPE_DEFAULT:
                ((CreationDetailsDefaultViewHolder)holder).bind();
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

    public void setControllerProfileClickListener(@NonNull OnControllerProfileClickListener controllerProfileClickListener) {
        this.controllerProfileClickListener = controllerProfileClickListener;
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

        public void bind(final ControllerProfile controllerProfile, final OnControllerProfileClickListener controllerProfileClickListener) {
            controllerProfileNameTextView.setText(controllerProfile.getName());

            itemView.setOnClickListener(view -> {
                if (controllerProfileClickListener != null) controllerProfileClickListener.onClick(controllerProfile);
            });

            removeControllerProfileButton.setOnClickListener(view -> {
                if (controllerProfileClickListener != null) controllerProfileClickListener.onRemoveClick(controllerProfile);
            });
        }
    }

    public class CreationDetailsDefaultViewHolder extends RecyclerView.ViewHolder {

        public CreationDetailsDefaultViewHolder(View itemView) {
            super(itemView);
        }

        public void bind() {}
    }
}
