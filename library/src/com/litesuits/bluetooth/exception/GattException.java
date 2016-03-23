package com.litesuits.bluetooth.exception;

/**
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class GattException extends BleException {
    private int gattStatus;

    public GattException(int gattStatus) {
        super(ERROR_CODE_GATT, "Gatt Exception Occurred! ");
        this.gattStatus = gattStatus;
    }


    public int getGattStatus() {
        return gattStatus;
    }

    public GattException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    @Override
    public String toString() {
        return "GattException{" +
               "gattStatus=" + gattStatus +
               "} " + super.toString();
    }
}
