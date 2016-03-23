package com.litesuits.bluetooth.exception;

import android.bluetooth.BluetoothGatt;

/**
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class ConnectException extends BleException {
    private BluetoothGatt bluetoothGatt;
    private int gattStatus;

    public ConnectException(BluetoothGatt bluetoothGatt, int gattStatus) {
        super(ERROR_CODE_GATT, "Gatt Exception Occurred! ");
        this.bluetoothGatt = bluetoothGatt;
        this.gattStatus = gattStatus;
    }

    public int getGattStatus() {
        return gattStatus;
    }

    public ConnectException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public ConnectException setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
        return this;
    }

    @Override
    public String toString() {
        return "ConnectException{" +
               "gattStatus=" + gattStatus +
               ", bluetoothGatt=" + bluetoothGatt +
               "} " + super.toString();
    }
}
