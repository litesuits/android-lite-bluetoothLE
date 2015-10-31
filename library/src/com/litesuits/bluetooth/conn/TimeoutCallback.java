package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGatt;

public abstract class TimeoutCallback implements Runnable {
    private Object tag;
    private long delayMillis = 3000;
    private BluetoothGatt bluetoothGatt;

    public TimeoutCallback() {
    }

    public TimeoutCallback(BluetoothGatt bluetoothGatt, long delayMillis) {
        this.bluetoothGatt = bluetoothGatt;
        this.delayMillis = delayMillis;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public TimeoutCallback setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public TimeoutCallback setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
        return this;
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
        onTimeout(bluetoothGatt);
    }
}