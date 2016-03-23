package com.litesuits.bluetooth.exception.hanlder;

import android.content.Context;
import android.widget.Toast;
import com.litesuits.bluetooth.exception.*;

/**
 * Toast exception.
 *
 * @author MaTianyu @http://litesuits.com
 * @date 2015-11-21
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
    private Context context;

    public DefaultBleExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onConnectException(ConnectException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onGattException(GattException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onOtherException(OtherException e) {
        Toast.makeText(context, e.getDescription(), Toast.LENGTH_LONG).show();
    }
}
