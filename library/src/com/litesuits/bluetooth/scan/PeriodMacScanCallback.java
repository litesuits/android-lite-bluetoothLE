package com.litesuits.bluetooth.scan;

import android.bluetooth.BluetoothDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author MaTianyu
 * @date 2015-01-22
 */
public abstract class PeriodMacScanCallback extends PeriodScanCallback {
    private String mac;
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    public PeriodMacScanCallback(String mac, long timeoutMillis) {
        super(timeoutMillis);
        if (mac == null) {
            throw new IllegalArgumentException("start scan, mac can not be null!");
        }
        this.mac = mac;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!hasFound.get()) {
            if (mac.equalsIgnoreCase(device.getAddress())) {
                hasFound.set(true);
                stopScanAndNotify();
                onDeviceFound(device, rssi, scanRecord);
            }
        }
    }

    public abstract void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);

}
