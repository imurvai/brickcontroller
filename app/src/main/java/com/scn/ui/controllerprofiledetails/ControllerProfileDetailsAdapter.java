package com.scn.ui.controllerprofiledetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.ui.DefaultRecyclerViewViewHolder;
import com.scn.ui.OnListItemClickListener;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-20.
 */

final class ControllerProfileDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //
    // Members
    //

    private static final int VIEWTYPE_CONTROLLER_EVENT = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<ControllerEvent> controllerEventList = new ArrayList<>();
    private OnListItemClickListener<ControllerEvent> listItemClickListener = null;

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public int getItemViewType(int position) {
        if (controllerEventList != null && controllerEventList.size() > 0) {
            return VIEWTYPE_CONTROLLER_EVENT;
        }

        return VIEWTYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_CONTROLLER_EVENT: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_controller_event, parent, false);
                return new ControllerEventItemViewHolder(view);
            }

            case VIEWTYPE_DEFAULT: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_default, parent, false);
                return new DefaultRecyclerViewViewHolder(view, parent.getContext().getString(R.string.add_controller_event));
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEWTYPE_CONTROLLER_EVENT:
                ((ControllerEventItemViewHolder)holder).bind(controllerEventList.get(position), listItemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if (controllerEventList != null && controllerEventList.size() > 0) {
            return controllerEventList.size();
        }

        return 1;
    }

    //
    // API
    //

    public void setControllerEventList(@NonNull List<ControllerEvent> controllerEventList) {
        this.controllerEventList = controllerEventList;
        notifyDataSetChanged();
    }

    public void setListItemClickListener(@NonNull OnListItemClickListener<ControllerEvent> listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class ControllerEventItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.controller_event_name) TextView controllerEventNameTextView;
        @BindView(R.id.remove_controller_event) Button removeControllerEventButton;
        @BindView(R.id.controller_action_container) LinearLayout controllerActionContainer;

        public ControllerEventItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final ControllerEvent controllerEvent, final OnListItemClickListener<ControllerEvent> controllerEventOnListItemClickListener) {
            controllerEventNameTextView.setText(controllerEvent.getEventText());

            removeControllerEventButton.setOnClickListener(view -> {
                if (controllerEventOnListItemClickListener != null) controllerEventOnListItemClickListener.onClick(controllerEvent, OnListItemClickListener.ItemClickAction.REMOVE, null);
            });
        }
    }
}
