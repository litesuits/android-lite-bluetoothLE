package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * callback of {@link BluetoothGattCharacteristic} operation.
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public abstract class BleCharactCallback extends BleCallback {
    public abstract void onSuccess(BluetoothGattCharacteristic characteristic);
}