package com.scn.ui.devicelist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.scn.devicemanagement.Device;
import com.scn.ui.DefaultRecyclerViewViewHolder;
import com.scn.ui.OnListItemClickListener;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 2017. 11. 26..
 */

final class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //
    // Private members
    //

    private static final int VIEWTYPE_DEVICEITEM = 1;
    private static final int VIEWTYPE_DEFAULTITEM = 2;

    private List<Device> deviceList = new ArrayList<>();
    private OnListItemClickListener<Device> listItemClickListener = null;

    //
    // Constructor
    //

    @Inject
    DeviceListAdapter() {}

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public int getItemViewType(int position) {
        if (deviceList == null || deviceList.size() == 0) {
            return VIEWTYPE_DEFAULTITEM;
        }

        return VIEWTYPE_DEVICEITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_DEVICEITEM: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_device, parent, false);
                return new DeviceItemViewHolder(view);
            }

            case VIEWTYPE_DEFAULTITEM: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_default, parent, false);
                return new DefaultRecyclerViewViewHolder(view, parent.getContext().getString(R.string.scan_devices));
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEWTYPE_DEVICEITEM:
                ((DeviceItemViewHolder)holder).bind(deviceList.get(position), listItemClickListener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (deviceList == null || deviceList.size() == 0) {
            return 1;
        }

        return deviceList.size();
    }

    //
    // Api
    //

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
    }

    public void setListItemClickListener(OnListItemClickListener<Device> listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class DeviceItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vendor_image) ImageView vendorImage;
        @BindView(R.id.device_name) TextView deviceName;
        @BindView(R.id.device_address) TextView deviceAddress;
        @BindView(R.id.remove_device) Button removeDeviceButton;

        public DeviceItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Device device, final OnListItemClickListener<Device> listItemClickListener) {
            switch (device.getType()) {
                case BUWIZZ:
                    vendorImage.setImageResource(R.drawable.buwizz_image);
                    break;
                case SBRICK:
                    vendorImage.setImageResource(R.drawable.sbrick_image);
                    break;
                case INFRARED:
                    vendorImage.setImageResource(R.drawable.infra_image);
                    break;
            }

            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());

            itemView.setOnClickListener(view -> {
                if (listItemClickListener != null) listItemClickListener.onClick(device, OnListItemClickListener.ItemClickAction.CLICK, null);
            });

            removeDeviceButton.setOnClickListener(view -> {
                if (listItemClickListener != null) listItemClickListener.onClick(device, OnListItemClickListener.ItemClickAction.REMOVE, null);
            });
        }
    }
}