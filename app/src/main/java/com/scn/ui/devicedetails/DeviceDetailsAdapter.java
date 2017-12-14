package com.scn.ui.devicedetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceType;
import com.scn.logger.Logger;
import com.scn.ui.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-07.
 */

final class DeviceDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnDeviceChannelOutputChangedListener {
        void onOutputChanged(Device device, int channel, int value);
    }

    public interface OnDeviceOutputLevelChangedListener {
        void onOutputLevelChanged(Device device, Device.OutputLevel outputLevel);
    }

    //
    // Private members
    //

    private static final String TAG = DeviceDetailsAdapter.class.getSimpleName();

    private static final int VIEWTYPE_HEADER = 1;
    private static final int VIEWTYPE_OUTPUT_SEEKBAR = 2;
    private static final int VIEWTYPE_DEVICE_INFO = 3;
    private static final int VIEWTYPE_OUTPUT_LEVEL = 4;

    private Device device;
    private OnDeviceChannelOutputChangedListener outputChangedListener = null;
    private OnDeviceOutputLevelChangedListener outputLevelChangedListener = null;

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

        switch (device.getType()) {
            case BUWIZZ:
                if (position == 1) return VIEWTYPE_OUTPUT_LEVEL;
                else if (position < device.getNumberOfChannels() + 2) return VIEWTYPE_OUTPUT_SEEKBAR;

            case SBRICK:
            case INFRARED:
                if (position < device.getNumberOfChannels() + 1) return VIEWTYPE_OUTPUT_SEEKBAR;
        }

        return VIEWTYPE_DEVICE_INFO;
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
                return new OutputViewHolder(view);
            }

            case VIEWTYPE_OUTPUT_LEVEL: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_device_details_output_level, parent, false);
                return new OutputLevelViewHolder(view);
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
                int channel = device.getType() == DeviceType.BUWIZZ ? position - 2 : position -1;
                ((OutputViewHolder)holder).bind(device, channel, outputChangedListener);
                break;

            case VIEWTYPE_OUTPUT_LEVEL:
                ((OutputLevelViewHolder)holder).bind(device, outputLevelChangedListener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (device == null) {
            return 0;
        }
        else {
            if (device.getType() == DeviceType.BUWIZZ)
                return device.getNumberOfChannels() + 2;
            else
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

    public void setOutputChangedListener(OnDeviceChannelOutputChangedListener outputChangedListener) {
        this.outputChangedListener = outputChangedListener;
        notifyDataSetChanged();
    }

    public void setOutputLevelChangedListener(OnDeviceOutputLevelChangedListener outputLevelChangedListener) {
        this.outputLevelChangedListener = outputLevelChangedListener;
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
                    vendorImage.setImageResource(R.drawable.buwizz_list_image);
                    deviceType.setText("BuWizz");
                    break;
                case SBRICK:
                    vendorImage.setImageResource(R.drawable.sbrick_list_image);
                    deviceType.setText("SBrick");
                    break;
                case INFRARED:
                    vendorImage.setImageResource(R.drawable.infra_list_image);
                    deviceType.setText("PF Infra");
                    break;
            }

            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());
        }
    }

    public class OutputViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.output_level) AppCompatSeekBar outputSeekBar;

        public OutputViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Device device, final int channel, final OnDeviceChannelOutputChangedListener listener) {
            if (device == null) return;
            if (listener == null) return;

            final int halfMax = outputSeekBar.getMax() / 2;
            outputSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (listener != null) {
                        int value = ((i - halfMax) * 255) / halfMax;
                        listener.onOutputChanged(device, channel, value);
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

    public class OutputLevelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.radio_group) RadioGroup radioGroup;

        public OutputLevelViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Device device, final OnDeviceOutputLevelChangedListener listener) {
            if (device == null) return;
            switch (device.getOutputLevel()) {
                case LOW:
                    radioGroup.check(R.id.radio_low);
                    break;

                case NORMAL:
                    radioGroup.check(R.id.radio_normal);
                    break;

                case HIGH:
                    radioGroup.check(R.id.radio_high);
                    break;
            }

            if (listener == null) return;
            radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                switch (i) {
                    case R.id.radio_low:
                        listener.onOutputLevelChanged(device, Device.OutputLevel.LOW);
                        break;

                    case R.id.radio_normal:
                        listener.onOutputLevelChanged(device, Device.OutputLevel.NORMAL);
                        break;

                    case R.id.radio_high:
                        listener.onOutputLevelChanged(device, Device.OutputLevel.HIGH);
                        break;
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
