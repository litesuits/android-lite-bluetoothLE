package com.litesuits.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

/**
 * @author MaTianyu
 * @date 2015-01-22
 */
public class LiteBluetoothDevice {
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private byte[] scanRecord;
    //private Context         context;
    //private BluetoothGatt   gatt;


    public LiteBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        //this.context = context.getApplicationContext();
    }

    public BluetoothGatt connect(Context context, LiteBluetoothGatCallback callback) {
        callback.notifyConnectStart(null);
        BluetoothGatt gatt = bluetoothDevice.connectGatt(context, false, callback);
        return gatt;
    }


    public BluetoothGatt connect(Context context, boolean autoConnect, LiteBluetoothGatCallback callback) {
        callback.notifyConnectStart(null);
        BluetoothGatt gatt = bluetoothDevice.connectGatt(context, autoConnect, callback);
        return gatt;
    }


    public void discoverServices(LiteBluetoothGatCallback callback, BluetoothGatt gatt) {
        callback.notifyDiscoverServicesStart(gatt);
        gatt.discoverServices();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
