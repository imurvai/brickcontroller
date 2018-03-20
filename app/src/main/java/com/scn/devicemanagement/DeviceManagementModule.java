package com.scn.devicemanagement;

import android.content.Context;

import com.scn.app.AppPreferences;

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
    InfraRedDeviceManager provideInfraredDeviceManager(Context context, AppPreferences appPreferences) {
        return new InfraRedDeviceManager(context, appPreferences);
    }

    @Provides
    @Singleton
    DeviceRepository provideDeviceRepository(Context context) {
        return new DeviceRepository(context);
    }

    @Provides
    @Singleton
    DeviceManager provideDeviceManager(DeviceRepository deviceRepository,
                                       BluetoothDeviceManager bluetoothDeviceManager,
                                       InfraRedDeviceManager infraRedDeviceManager) {
        return new DeviceManager(deviceRepository, bluetoothDeviceManager, infraRedDeviceManager);
    }
 }
