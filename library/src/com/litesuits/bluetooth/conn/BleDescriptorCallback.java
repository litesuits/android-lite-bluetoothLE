package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGattDescriptor;

/**
 * callback of {@link BluetoothGattDescriptor} operation.
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public abstract class BleDescriptorCallback extends BleCallback {
    public abstract void onSuccess(BluetoothGattDescriptor descriptor);
}