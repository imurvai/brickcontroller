package com.scn.devicemanagement.dagger;

import android.content.Context;

import com.scn.devicemanagement.BluetoothDeviceManager;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.devicemanagement.InfraRedDeviceManager;
import com.scn.devicemanagement.devicerepository.DeviceRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 2017. 11. 25..
 */

@Module
public class DeviceManagementModule {

    @Provides
    @Singleton
    BluetoothDeviceManager provideBluetoothDeviceManager(Context context) {
        return new BluetoothDeviceManager(context);
    }

    @Provides
    @Singleton
    InfraRedDeviceManager provideInfraredDeviceManager(Context context) {
        return new InfraRedDeviceManager(context);
    }

    @Provides
    @Singleton
    DeviceRepository provideDeviceRepository(Context context) {
        return new DeviceRepository(context);
    }

    @Provides
    @Singleton
    DeviceManager provideDeviceManager(Context context,
                                       DeviceRepository deviceRepository,
                                       BluetoothDeviceManager bluetoothDeviceManager,
                                       InfraRedDeviceManager infraRedDeviceManager) {
        return new DeviceManager(context, deviceRepository, bluetoothDeviceManager, infraRedDeviceManager);
    }
 }
