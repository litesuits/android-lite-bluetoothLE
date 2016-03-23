package com.litesuits.bluetooth.exception;

/**
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class TimeoutException extends BleException {
    public TimeoutException() {
        super(ERROR_CODE_TIMEOUT, "Timeout Exception Occurred! ");
    }
}
