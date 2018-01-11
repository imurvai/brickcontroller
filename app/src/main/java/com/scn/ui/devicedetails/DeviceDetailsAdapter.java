package com.scn.ui.devicedetails;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.scn.devicemanagement.BuWizz2Device;
import com.scn.devicemanagement.BuWizzDevice;
import com.scn.devicemanagement.Device;
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

    public interface OnDeviceSpecificDataChangedListener {
        void onDeviceSpecificDataChanged(Device device, String deviceSpecificDataJSon);
    }

    public interface OnEditDeviceNameListener {
        void onEdit(Device device);
    }

    //
    // Private members
    //

    private static final String TAG = DeviceDetailsAdapter.class.getSimpleName();

    private static final int VIEWTYPE_HEADER = 1;
    private static final int VIEWTYPE_OUTPUT_SEEKBAR = 2;
    private static final int VIEWTYPE_DEVICE_INFO = 3;
    private static final int VIEWTYPE_BUWIZZ_SPECIFIC_DATA = 4;
    private static final int VIEWTYPE_BUWIZZ2_SPECIFIC_DATA = 5;

    private Device device;
    private OnDeviceChannelOutputChangedListener outputChangedListener = null;
    private OnDeviceSpecificDataChangedListener deviceSpecificDataChangedListener = null;
    private OnEditDeviceNameListener editDeviceNameListener = null;

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
                if (position == 1) return VIEWTYPE_BUWIZZ_SPECIFIC_DATA;
                else if (position < device.getNumberOfChannels() + 2) return VIEWTYPE_OUTPUT_SEEKBAR;

            case BUWIZZ2:
                if (position == 1) return VIEWTYPE_BUWIZZ2_SPECIFIC_DATA;
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

            case VIEWTYPE_BUWIZZ_SPECIFIC_DATA: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_device_details_buwizz_specific, parent, false);
                return new BuWizzSpecificDataViewHolder(view);
            }

            case VIEWTYPE_BUWIZZ2_SPECIFIC_DATA: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_device_details_buwizz2_specific, parent, false);
                return new BuWizz2SpecificDataViewHolder(view);
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
        switch (holder.getItemViewType()) {
            case VIEWTYPE_HEADER:
                ((HeaderViewHolder)holder).bind(device, editDeviceNameListener);
                break;

            case VIEWTYPE_OUTPUT_SEEKBAR:
                Device.DeviceType deviceType = device.getType();
                boolean isBuWizz = deviceType == Device.DeviceType.BUWIZZ || deviceType == Device.DeviceType.BUWIZZ2;
                int channel = isBuWizz ? position - 2 : position -1;
                ((OutputViewHolder)holder).bind(device, channel, outputChangedListener);
                break;

            case VIEWTYPE_BUWIZZ_SPECIFIC_DATA:
                ((BuWizzSpecificDataViewHolder)holder).bind(device, deviceSpecificDataChangedListener);
                break;

            case VIEWTYPE_BUWIZZ2_SPECIFIC_DATA:
                ((BuWizz2SpecificDataViewHolder)holder).bind(device, deviceSpecificDataChangedListener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (device == null) {
            return 0;
        }
        else {
            Device.DeviceType deviceType = device.getType();
            if (deviceType == Device.DeviceType.BUWIZZ || deviceType == Device.DeviceType.BUWIZZ2)
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

    void setOutputChangedListener(OnDeviceChannelOutputChangedListener outputChangedListener) {
        this.outputChangedListener = outputChangedListener;
        notifyDataSetChanged();
    }

    void setDeviceSpecificDataChangedListener(OnDeviceSpecificDataChangedListener deviceSpecificDataChangedListener) {
        this.deviceSpecificDataChangedListener = deviceSpecificDataChangedListener;
        notifyDataSetChanged();
    }

    void setEditDeviceNameListener(OnEditDeviceNameListener editDeviceNameListener) {
        this.editDeviceNameListener = editDeviceNameListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vendor_image) ImageView vendorImage;
        @BindView(R.id.device_name) TextView deviceName;
        @BindView(R.id.edit) Button editDeviceName;
        @BindView(R.id.device_type) TextView deviceType;
        @BindView(R.id.device_address) TextView deviceAddress;

        HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final Device device, final OnEditDeviceNameListener editDeviceNameListener) {
            switch (device.getType()) {
                case BUWIZZ:
                    vendorImage.setImageResource(R.drawable.buwizz_image);
                    deviceType.setText("BuWizz");
                    break;
                case BUWIZZ2:
                    vendorImage.setImageResource(R.drawable.buwizz_image);
                    deviceType.setText("BuWizz 2");
                    break;
                case SBRICK:
                    vendorImage.setImageResource(R.drawable.sbrick_image);
                    deviceType.setText("SBrick");
                    break;
                case INFRARED:
                    vendorImage.setImageResource(R.drawable.infra_image);
                    deviceType.setText("PF Infra");
                    break;
            }

            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());

            editDeviceName.setOnClickListener(view -> {
                if (editDeviceNameListener != null) editDeviceNameListener.onEdit(device);
            });
        }
    }

    public class OutputViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.output_level) AppCompatSeekBar outputSeekBar;

        OutputViewHolder(View itemView) {
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

    public class BuWizzSpecificDataViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.radio_group) RadioGroup radioGroup;

        BuWizzSpecificDataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Device device, final OnDeviceSpecificDataChangedListener listener) {
            if (device == null) return;

            BuWizzDevice.BuWizzData buWizzData = new Gson().fromJson(device.getDeviceSpecificDataJSon(), BuWizzDevice.BuWizzData.class);
            switch (buWizzData.outputLevel) {
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
                    case R.id.radio_low: {
                            String data = new Gson().toJson(new BuWizzDevice.BuWizzData(BuWizzDevice.BuWizzOutputLevel.LOW));
                            listener.onDeviceSpecificDataChanged(device, data);
                        }
                        break;

                    case R.id.radio_normal: {
                        String data = new Gson().toJson(new BuWizzDevice.BuWizzData(BuWizzDevice.BuWizzOutputLevel.NORMAL));
                        listener.onDeviceSpecificDataChanged(device, data);
                    }
                    break;

                    case R.id.radio_high: {
                        String data = new Gson().toJson(new BuWizzDevice.BuWizzData(BuWizzDevice.BuWizzOutputLevel.HIGH));
                        listener.onDeviceSpecificDataChanged(device, data);
                    }
                    break;
                }
            });
        }
    }

    public class BuWizz2SpecificDataViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.radio_group) RadioGroup radioGroup;

        BuWizz2SpecificDataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Device device, final OnDeviceSpecificDataChangedListener listener) {
            if (device == null) return;

            BuWizz2Device.BuWizz2Data buWizz2Data = new Gson().fromJson(device.getDeviceSpecificDataJSon(), BuWizz2Device.BuWizz2Data.class);
            switch (buWizz2Data.outputLevel) {
                case LOW:
                    radioGroup.check(R.id.radio_low);
                    break;

                case NORMAL:
                    radioGroup.check(R.id.radio_normal);
                    break;

                case HIGH:
                    radioGroup.check(R.id.radio_high);
                    break;

                case LUDICROUS:
                    radioGroup.check(R.id.radio_ludicrous);
                    break;
            }

            if (listener == null) return;
            radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                switch (i) {
                    case R.id.radio_low: {
                        String data = new Gson().toJson(new BuWizz2Device.BuWizz2Data(BuWizz2Device.BuWizz2OutputLevel.LOW));
                        listener.onDeviceSpecificDataChanged(device, data);
                    }
                    break;

                    case R.id.radio_normal: {
                        String data = new Gson().toJson(new BuWizz2Device.BuWizz2Data(BuWizz2Device.BuWizz2OutputLevel.NORMAL));
                        listener.onDeviceSpecificDataChanged(device, data);
                    }
                    break;

                    case R.id.radio_high: {
                        String data = new Gson().toJson(new BuWizz2Device.BuWizz2Data(BuWizz2Device.BuWizz2OutputLevel.HIGH));
                        listener.onDeviceSpecificDataChanged(device, data);
                    }
                    break;

                    case R.id.radio_ludicrous: {
                        String data = new Gson().toJson(new BuWizz2Device.BuWizz2Data(BuWizz2Device.BuWizz2OutputLevel.LUDICROUS));
                        listener.onDeviceSpecificDataChanged(device, data);
                    }
                    break;
                }
            });
        }
    }

    public class DeviceInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.key) TextView key;
        @BindView(R.id.value) TextView value;

        DeviceInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(@NonNull final String key, @NonNull final String value) {
            this.key.setText(key);
            this.value.setText(value);
        }
    }
}
