package com.litesuits.bluetooth.exception;

/**
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class InitiatedException extends BleException {
    public InitiatedException() {
        super(ERROR_CODE_INITIAL, "Initiated Exception Occurred! ");
    }
}
