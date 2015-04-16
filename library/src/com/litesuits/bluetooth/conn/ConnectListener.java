package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import com.litesuits.bluetooth.log.BleLog;

public abstract class ConnectListener extends BluetoothHelper {
    private static final String TAG = "ConnectListener";
    private ConnectState connectState = ConnectState.Initialed;
    private BluetoothGatt bluetoothGatt;

    public ConnectState getConnectState() {
        return connectState;
    }
    public boolean isInConnected() {
        return connectState == ConnectState.ServiceDiscovered;
    }
    public boolean isInConnecting() {
        return getConnectState().isInConnecting();
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }


    public final void stateChanged(ConnectState state) {
        BleLog.i(TAG, "Ble stateChanged --------------------------> " + state);
        connectState = state;
        onStateChanged(state);
    }

    public abstract void onStateChanged(ConnectState state);

    public final void failed(ConnectError error) {
        BleLog.e(TAG, "Ble error -----------------------------> " + error);
        onFailed(error);
    }

    public abstract void onFailed(ConnectError error);

    //public abstract void onConnected(BluetoothGatt gatt);

    public abstract void onServicesDiscovered(BluetoothGatt gatt);

    public abstract void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    public abstract void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
    }
}