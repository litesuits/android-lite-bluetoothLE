package com.litesuits.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import com.litesuits.bluetooth.exception.BleException;

/**
 * LiteBleGattCallback is an abstract extension of BluetoothGattCallback.
 */
public abstract class LiteBleGattCallback extends BluetoothGattCallback {

    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);

    @Override
    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    public abstract void onConnectFailure(BleException exception);
}