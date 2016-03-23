package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGattCallback;
import com.litesuits.bluetooth.exception.BleException;

/**
 * Base abstract callback of ble operation.
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public abstract class BleCallback {
    private BluetoothGattCallback bluetoothGattCallback;

    protected BleCallback setBluetoothGattCallback(BluetoothGattCallback bluetoothGattCallback) {
        this.bluetoothGattCallback = bluetoothGattCallback;
        return this;
    }

    protected BluetoothGattCallback getBluetoothGattCallback() {
        return bluetoothGattCallback;
    }

    public void onInitiatedSuccess() {}

    public abstract void onFailure(BleException exception);
}