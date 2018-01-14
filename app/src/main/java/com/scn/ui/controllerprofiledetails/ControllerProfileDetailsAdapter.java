package com.scn.ui.controllerprofiledetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.ui.DefaultRecyclerViewViewHolder;
import com.scn.ui.OnListItemClickListener;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private OnListItemClickListener<ControllerEvent> controllerEventOnListItemClickListener = null;
    private OnListItemClickListener<ControllerAction> controllerActionOnListItemClickListener = null;
    private Map<String, String> deviceIdNameMap = null;

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
                return new ControllerEventItemViewHolder(parent, view, deviceIdNameMap);
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
        switch (holder.getItemViewType()) {
            case VIEWTYPE_CONTROLLER_EVENT:
                ((ControllerEventItemViewHolder)holder).bind(controllerEventList.get(position), controllerEventOnListItemClickListener, controllerActionOnListItemClickListener);
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

    public void setControllerEventOnListItemClickListener(@NonNull OnListItemClickListener<ControllerEvent> controllerEventOnListItemClickListener) {
        this.controllerEventOnListItemClickListener = controllerEventOnListItemClickListener;
        notifyDataSetChanged();
    }

    public void setControllerActionOnListItemClickListener(@NonNull OnListItemClickListener<ControllerAction> controllerActionOnListItemClickListener) {
        this.controllerActionOnListItemClickListener = controllerActionOnListItemClickListener;
        notifyDataSetChanged();
    }

    public void setDeviceIdNameMap(@NonNull Map<String, String> deviceIdNameMap) {
        this.deviceIdNameMap = deviceIdNameMap;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class ControllerEventItemViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup viewGroup;
        private Map<String, String> deviceIdNameMap;

        @BindView(R.id.controller_event_name) TextView controllerEventNameTextView;
        @BindView(R.id.remove_controller_event) Button removeControllerEventButton;
        @BindView(R.id.controller_action_container) LinearLayout controllerActionContainer;

        public ControllerEventItemViewHolder(ViewGroup viewGroup, View itemView, Map<String, String> deviceIdNameMap) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.viewGroup = viewGroup;
            this.deviceIdNameMap = deviceIdNameMap;
        }

        public void bind(@NonNull final ControllerEvent controllerEvent,
                         final OnListItemClickListener<ControllerEvent> controllerEventOnListItemClickListener,
                         final OnListItemClickListener<ControllerAction> controllerActionOnListItemClickListener) {
            controllerEventNameTextView.setText(controllerEvent.getEventText());

            removeControllerEventButton.setOnClickListener(view -> {
                if (controllerEventOnListItemClickListener != null) controllerEventOnListItemClickListener.onClick(controllerEvent, OnListItemClickListener.ItemClickAction.REMOVE, null);
            });

            controllerActionContainer.removeAllViews();
            for (ControllerAction controllerAction : controllerEvent.getControllerActions()) {
                View controllerActionView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_controller_action, viewGroup, false);
                TextView textView = controllerActionView.findViewById(R.id.controller_action);
                Button removeButton = controllerActionView.findViewById(R.id.remove_controller_action);

                if (deviceIdNameMap != null && deviceIdNameMap.containsKey(controllerAction.getDeviceId())) {
                    String deviceName = deviceIdNameMap.get(controllerAction.getDeviceId());
                    textView.setText(deviceName + " - ch: " + (controllerAction.getChannel() + 1) + (controllerAction.getIsInvert() ? " - inv." : ""));
                }
                else {
                    String deviceName = controllerAction.getDeviceId();
                    textView.setText(deviceName + " - ch: " + (controllerAction.getChannel() + 1) + (controllerAction.getIsInvert() ? " - inv." : ""));
                }

                controllerActionView.setOnClickListener(view -> {
                    if (controllerActionOnListItemClickListener != null) controllerActionOnListItemClickListener.onClick(controllerAction, OnListItemClickListener.ItemClickAction.CLICK, null);
                });

                removeButton.setOnClickListener(view -> {
                    if (controllerActionOnListItemClickListener != null) controllerActionOnListItemClickListener.onClick(controllerAction, OnListItemClickListener.ItemClickAction.REMOVE, null);
                });

                controllerActionContainer.addView(controllerActionView);
            }
        }
    }
}
