package com.scn.ui.controllerprofiledetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.scn.creationmanagement.ControllerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imurvai on 2017-12-20.
 */

final class ControllerProfileDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnControllerEventClickListener {
        void onClick(ControllerEvent controllerEvent);
        void onRemoveClick(ControllerEvent controllerEvent);
    }

    //
    // Members
    //

    private static final int VIEWTYPE_CONTROLLER_EVENT = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<ControllerEvent> controllerEventList = new ArrayList<>();
    private OnControllerEventClickListener controllerEventClickListener = null;

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    //
    // API
    //

    public void setControllerEventList(@NonNull List<ControllerEvent> controllerEventList) {
        this.controllerEventList = controllerEventList;
        notifyDataSetChanged();
    }

    public void setControllerEventClickListener(@NonNull OnControllerEventClickListener controllerEventClickListener) {
        this.controllerEventClickListener = controllerEventClickListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class ControllerEventItemViewHolder extends RecyclerView.ViewHolder {

        public ControllerEventItemViewHolder(View itemView) {
            super(itemView);
        }

        public void bind() {}
    }

    public class ControllerProfileDetailsDefaultViewHolder extends RecyclerView.ViewHolder {

        public ControllerProfileDetailsDefaultViewHolder(View itemView) {
            super(itemView);
        }

        public void bind() {}
    }
}
