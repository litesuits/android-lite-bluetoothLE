package com.litesuits.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * @author MaTianyu
 * @date 2015-01-22
 */
public abstract class PeriodMacScanCallback extends PeriodScanCallback {
    private String mac;
    private boolean hasConnect;

    protected PeriodMacScanCallback(String mac, long timeoutMillis, BluetoothAdapter adapter) {
        super(timeoutMillis, adapter);
        this.mac = mac;
        if (mac == null) {
            throw new IllegalArgumentException("start scan, mac can not be null!");
        }
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (hasConnect) {
            stopScanAndNotify();
        } else {
            if (mac.equalsIgnoreCase(device.getAddress())) {
                stopScanAndNotify();
                onDeviceFound(device, rssi, scanRecord);
                hasConnect = true;
            }
        }
    }

    public abstract void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);

}
