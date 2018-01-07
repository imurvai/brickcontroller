package com.scn.ui.controllertester;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.ui.DefaultRecyclerViewViewHolder;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2018-01-07.
 */

final class ControllerTesterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //
    // Private members
    //

    private static final int VIEWTYPE_CONTROLLER_EVENT = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<Pair<ControllerEvent, Float>> controllerEventValueList = new ArrayList<>();

    //
    // RecyclerView.Adapter overrides
    //


    @Override
    public int getItemViewType(int position) {
        return controllerEventValueList == null || controllerEventValueList.size() == 0 ?
                VIEWTYPE_DEFAULT :
                VIEWTYPE_CONTROLLER_EVENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_CONTROLLER_EVENT: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_controller_tester, parent, false);
                return new ControllerEventViewHolder(view);
            }

            case VIEWTYPE_DEFAULT: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_default, parent, false);
                return new DefaultRecyclerViewViewHolder(view, parent.getContext().getString(R.string.press_buttons_on_controller));
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEWTYPE_CONTROLLER_EVENT:
                ((ControllerEventViewHolder)holder).bind(controllerEventValueList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return controllerEventValueList == null || controllerEventValueList.size() == 0 ?
                1 :
                controllerEventValueList.size();
    }

    //
    // API
    //

    void setControllerEventMap(Map<ControllerEvent, Float> controllerEventMap) {
        controllerEventValueList.clear();
        for (Map.Entry<ControllerEvent, Float> controllerEventValue : controllerEventMap.entrySet()) {
            controllerEventValueList.add(new Pair<>(controllerEventValue.getKey(), controllerEventValue.getValue()));
        }
        notifyDataSetChanged();
    }

    //
    // ViewHolder
    //

    public class ControllerEventViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.controller_event_name) TextView controllerEventNameTextView;
        @BindView(R.id.controller_event_value) TextView controllerEventValueTextView;

        public ControllerEventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final Pair<ControllerEvent, Float> controllerEventValue) {
            controllerEventNameTextView.setText(controllerEventValue.first.getEventText());
            if (controllerEventValue.first.getEventType() == ControllerEvent.ControllerEventType.MOTION) {
                controllerEventValueTextView.setText(String.format("%.2f", controllerEventValue.second));
            }
            else {
                controllerEventValueTextView.setText("");
            }
        }
    }
}
