package com.scn.devicemanagement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Created by steve on 2017. 03. 18..
 */

abstract class BluetoothDevice extends Device {

    //
    // Private and Protected members
    //

    private static final String TAG = BluetoothDevice.class.getSimpleName();

    protected final Context context;
    private final android.bluetooth.BluetoothDevice bluetoothDevice;

    protected BluetoothGatt bluetoothGatt = null;
    private final Object bluetoothGattLock = new Object();

    //
    // Constructor
    //

    BluetoothDevice(@NonNull Context context, @NonNull String name, @NonNull String address, @NonNull BluetoothDeviceManager bluetoothDeviceManager) {
        super(name, address);

        this.context = context;
        this.bluetoothDevice = bluetoothDeviceManager.getBluetoothAdapter().getRemoteDevice(address);
    }

    //
    // API
    //

    @MainThread
    @Override
    public boolean connect() {
        Logger.i(TAG, "connectDevice - " + this);

        synchronized (bluetoothGattLock) {
            if (bluetoothGatt != null) {
                Logger.w(TAG, "  GATT is already been used.");
                return false;
            }

            bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
            if (bluetoothGatt == null) {
                Logger.w(TAG, "  Failed to connect GATT.");
                return false;
            }

            setState(State.CONNECTING, false);
            return true;
        }
    }

    @MainThread
    @Override
    public boolean disconnect() {
        Logger.i(TAG, "disconnectDevice - " + this);

        synchronized (bluetoothGattLock) {
            if (bluetoothGatt != null) {
                Logger.i(TAG, "  Disconnecting GATT...");
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
            }

            disconnectInternal();
            setState(State.DISCONNECTED, false);
            return true;
        }
    }

    //
    // Protected API
    //

    protected abstract boolean onServiceDiscovered(BluetoothGatt gatt);
    protected abstract boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    protected abstract boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    protected abstract void disconnectInternal();

    BluetoothGattCharacteristic getGattCharacteristic(@NonNull BluetoothGatt gatt, @NonNull String serviceUUID, @NonNull String characteristicUUID) {
        Logger.i(TAG, "getGattCharacteristic...");
        Logger.i(TAG, "  Service UUID       : " + serviceUUID);
        Logger.i(TAG, "  Characteristic UUID: " + characteristicUUID);

        BluetoothGattService service = getService(gatt, serviceUUID);
        if (service == null) {
            Logger.i(TAG, "  Service is null.");
            return null;
        }

        if (characteristicUUID.length() == 4) {
            Logger.i(TAG, "  Partial characteristic UUID");
            String partialUUID = "0000" + characteristicUUID;
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().startsWith(partialUUID))
                    return characteristic;
            }
        }
        else {
            Logger.i(TAG, "  Full characteristic UUID");
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
            if (characteristic != null) {
                return characteristic;
            }
        }

        Logger.w(TAG, "  No such characteristic found.");
        return null;
    }

    protected void logServices(@NonNull BluetoothGatt gatt) {
        Logger.i(TAG, "logServices...");

        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            Logger.i(TAG, "  Service UUID:" + service.getUuid().toString());

            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                Logger.i(TAG, "  Characteristic UUID: " + characteristic.getUuid().toString());
                logCharacteristicPermissions(characteristic.getPermissions());
                logCharacteristicProperties(characteristic.getProperties());

                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    Logger.i(TAG, "  Descriptor UUID: " + descriptor.getUuid().toString());
                }
            }
        }
    }

    //
    // Private methods
    //

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.i(TAG, "onConnectionStateChange - device: " + BluetoothDevice.this);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Logger.w(TAG, "  GATT status - " + status);
                setState(State.DISCONNECTED, true);
                return;
            }

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    Logger.i(TAG, "  Connecting.");
                    synchronized (bluetoothGattLock) {
                        if (bluetoothGatt == null) {
                            Logger.w(TAG, "  Disconnect has been called.");
                            return;
                        }

                        setState(State.CONNECTING, false);
                    }
                    break;

                case BluetoothProfile.STATE_CONNECTED:
                    Logger.i(TAG, "  Connected.");
                    synchronized (bluetoothGattLock) {
                        if (bluetoothGatt != null) {
                            Logger.i(TAG, "  Start service discovery...");
                            bluetoothGatt.discoverServices();
                        }
                        else {
                            Logger.w(TAG, "  Disconnect has been called, skipping service discovery.");
                        }
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    Logger.i(TAG, "  Disconnecting.");
                    synchronized (bluetoothGattLock) {
                        if (bluetoothGatt == null) {
                            Logger.w(TAG, "  Disconnect has been called.");
                            return;
                        }

                        setState(State.DISCONNECTING, false);
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Logger.i(TAG, "  Disconnected.");
                    synchronized (bluetoothGattLock) {
                        if (bluetoothGatt == null) {
                            Logger.w(TAG, "  Disconnect has been called.");
                            return;
                        }

                        setState(State.DISCONNECTED, false);
                    }
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.i(TAG, "onServicesDiscovered - device: " + BluetoothDevice.this);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Logger.w(TAG, "  GATT status - " + status);
                return;
            }

            synchronized (bluetoothGattLock) {
                if (bluetoothGatt == null) {
                    Logger.w(TAG, "  Disconnect has been called.");
                    return;
                }

                if (!BluetoothDevice.this.onServiceDiscovered(gatt)) {
                    Logger.w(TAG, "  Service discovery failed, trying to reconnect...");
                    gatt.disconnect();
                    gatt.connect();
                    return;
                }

                setState(State.CONNECTED, false);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.i(TAG, "onCharacteristicRead - device: " + BluetoothDevice.this);
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Logger.w(TAG, "  GATT status - " + status);
                return;
            }

            BluetoothDevice.this.onCharacteristicRead(gatt, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //Logger.i(TAG, "onCharacteristicWrite - device: " + BluetoothDevice.this);
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Logger.w(TAG, "  GATT status - " + status);
                return;
            }

            BluetoothDevice.this.onCharacteristicWrite(gatt, characteristic);
        }
    };

    private BluetoothGattService getService(BluetoothGatt gatt, String uuid) {
        Logger.i(TAG, "getService - " + uuid);

        if (gatt == null) {
            Logger.w(TAG, "  No GATT yet.");
            return null;
        }

        if (uuid.length() == 4) {
            Logger.i(TAG, "  Partial service UUID");
            String partialUUID = "0000" + uuid;
            for (BluetoothGattService service : gatt.getServices()) {
                if (service.getUuid().toString().startsWith(partialUUID))
                    return service;
            }
        }
        else {
            Logger.i(TAG, "  Full service UUID");
            BluetoothGattService service = gatt.getService(UUID.fromString(uuid));
            if (service != null)
                return service;
        }

        Logger.i(TAG, "  No such service found.");
        return null;
    }

    private void logCharacteristicPermissions(int permissions) {
        Logger.i(TAG, "logCharacteristicPermissions...");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_READ) != 0)
            Logger.i(TAG, "  PERMISSION_READ");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) != 0)
            Logger.i(TAG, "  PERMISSION_READ_ENCRYPTED");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) != 0)
            Logger.i(TAG, "  PERMISSION_READ_ENCRYPTED_MITM");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE) != 0)
            Logger.i(TAG, "  PERMISSION_WRITE");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) != 0)
            Logger.i(TAG, "  PERMISSION_WRITE_ENCRYPTED");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) != 0)
            Logger.i(TAG, "  PERMISSION_WRITE_ENCRYPTED_MITM");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) != 0)
            Logger.i(TAG, "  PERMISSION_WRITE_SIGNED");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) != 0)
            Logger.i(TAG, "  PERMISSION_WRITE_SIGNED_MITM");
    }

    private void logCharacteristicProperties(int properties) {
        Logger.i(TAG, "logCharacteristicProperties...");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0)
            Logger.i(TAG, "  PROPERTY_BROADCAST");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0)
            Logger.i(TAG, "  PROPERTY_EXTENDED_PROPS");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0)
            Logger.i(TAG, "  PROPERTY_INDICATE");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0)
            Logger.i(TAG, "  PROPERTY_NOTIFY");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
            Logger.i(TAG, "  PROPERTY_READ");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0)
            Logger.i(TAG, "  PROPERTY_SIGNED_WRITE");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0)
            Logger.i(TAG, "  PROPERTY_WRITE");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
            Logger.i(TAG, "  PROPERTY_WRITE_NO_RESPONSE");
    }
}
