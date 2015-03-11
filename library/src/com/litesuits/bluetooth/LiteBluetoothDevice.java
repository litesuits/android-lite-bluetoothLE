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
    //private Context         context;
    //private BluetoothGatt   gatt;


    public LiteBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        //this.context = context.getApplicationContext();
    }

    public void connect(Context context, LiteBluetoothGatCallback callback) {
        BluetoothGatt gatt = bluetoothDevice.connectGatt(context, false, callback);
        callback.notifyConnectStart(gatt);
    }


    public void discoverServices(LiteBluetoothGatCallback callback, BluetoothGatt gatt) {
        gatt.discoverServices();
        callback.notifyDiscoverServicesStart(gatt);
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}
