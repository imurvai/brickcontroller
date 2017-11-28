package com.scn.ui.devicelist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.scn.devicemanagement.Device;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 2017. 11. 26..
 */

final class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListAdapterViewHolder> {

    //
    // Private members
    //

    private List<Device> deviceList = new ArrayList<>();

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public DeviceListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
        return new DeviceListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceListAdapterViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.setView(device);
    }

    @Override
    public int getItemCount() {
        if (deviceList != null) {
            return deviceList.size();
        }

        return 0;
    }

    //
    // Api
    //

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
    }

    //
    // ViewHolder
    //

    public class DeviceListAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vendor_image) ImageView vendorImage;
        @BindView(R.id.device_name) TextView deviceName;
        @BindView(R.id.device_address) TextView deviceAddress;

        public DeviceListAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setView(Device device) {
            switch (device.getType()) {
                case BUWIZZ:
                    vendorImage.setImageResource(R.drawable.buwizz_logo);
                    break;
                case SBRICK:
                    vendorImage.setImageResource(R.drawable.sbrick_logo);
                    break;
                case INFRARED:
                    vendorImage.setImageResource(R.drawable.lego_logo);
                    break;
            }
            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());
        }
    }
}