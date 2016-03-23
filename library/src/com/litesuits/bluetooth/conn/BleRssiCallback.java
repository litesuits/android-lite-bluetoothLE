package com.litesuits.bluetooth.conn;

/**
 * callback of RSSI read.
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public abstract class BleRssiCallback extends BleCallback {
    public abstract void onSuccess(int rssi);
}