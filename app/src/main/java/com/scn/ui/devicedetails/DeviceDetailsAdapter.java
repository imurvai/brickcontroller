package com.scn.ui.devicedetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceType;
import com.scn.ui.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-07.
 */

final class DeviceDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnDeviceChannelSeekBarListener {
        void onSeekBarChanged(Device device, int channel, int value);
    }

    //
    // Private members
    //

    private static final int VIEWTYPE_HEADER = 1;
    private static final int VIEWTYPE_OUTPUT_SEEKBAR = 2;
    private static final int VIEWTYPE_DEVICE_INFO = 3;

    private Device device;
    private OnDeviceChannelSeekBarListener seekBarListener = null;

    //
    // Constructor
    //

    @Inject
    DeviceDetailsAdapter() {}

    //
    // RecyclerView.Adapter overrides
    //


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEWTYPE_HEADER;
        }
        else if (position <= device.getNumberOfChannels()) {
            return VIEWTYPE_OUTPUT_SEEKBAR;
        }
        else {
            return VIEWTYPE_DEVICE_INFO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_HEADER: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_device_details_header, parent, false);
                return new HeaderViewHolder(view);
            }

            case VIEWTYPE_OUTPUT_SEEKBAR: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_device_details_output_seekbar, parent, false);
                return new OutputSeekBarViewHolder(view);
            }

            case VIEWTYPE_DEVICE_INFO: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_key_value, parent, false);
                return new DeviceInfoViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEWTYPE_HEADER:
                ((HeaderViewHolder)holder).bind(device);
                break;

            case VIEWTYPE_OUTPUT_SEEKBAR:
                ((OutputSeekBarViewHolder)holder).bind(device, position - 1, seekBarListener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (device == null) {
            return 0;
        }
        else if (device.getType() == DeviceType.BUWIZZ) {
            // TODO: remove it when BUWIZZ becomes supported
            return 1;
        }
        else {
            return device.getNumberOfChannels() + 1;
        }
    }

    //
    // API
    //

    public void setDevice(Device device) {
        this.device = device;
        notifyDataSetChanged();
    }

    public void setSeekBarListener(OnDeviceChannelSeekBarListener seekBarListener) {
        this.seekBarListener = seekBarListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vendor_image) ImageView vendorImage;
        @BindView(R.id.device_name) TextView deviceName;
        @BindView(R.id.device_type) TextView deviceType;
        @BindView(R.id.device_address) TextView deviceAddress;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Device device) {
            switch (device.getType()) {
                case BUWIZZ:
                    vendorImage.setImageResource(R.drawable.buwizz_logo);
                    deviceType.setText("BuWizz");
                    break;
                case SBRICK:
                    vendorImage.setImageResource(R.drawable.sbrick_logo);
                    deviceType.setText("SBrick");
                    break;
                case INFRARED:
                    vendorImage.setImageResource(R.drawable.lego_logo);
                    deviceType.setText("Lego infra receiver");
                    break;
            }

            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());
        }
    }

    public class OutputSeekBarViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.output_level) AppCompatSeekBar outputLevel;

        public OutputSeekBarViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final Device device, final int channel, @NonNull final OnDeviceChannelSeekBarListener seekBarListener) {
            final int halfMax = outputLevel.getMax() / 2;
            outputLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (seekBarListener != null) {
                        int value = ((i - halfMax) * 255) / halfMax;
                        seekBarListener.onSeekBarChanged(device, channel, value);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBar.setProgress(halfMax);
                }
            });
        }
    }

    public class DeviceInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.key) TextView key;
        @BindView(R.id.value) TextView value;

        public DeviceInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final String key, @NonNull final String value) {
            this.key.setText(key);
            this.value.setText(value);
        }
    }
}
