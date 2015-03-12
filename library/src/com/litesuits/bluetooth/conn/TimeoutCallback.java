package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGatt;

public abstract class TimeoutCallback extends BluetoothHelper implements Runnable {
    private Object tag;
    private BluetoothGatt gatt;

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public abstract void onTimeout(BluetoothGatt gatt);

    @Override
    public void run() {
        onTimeout(gatt);
    }
}