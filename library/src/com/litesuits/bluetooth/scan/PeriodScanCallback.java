package com.litesuits.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;
import com.litesuits.bluetooth.LiteBluetooth;

/**
 * @author MaTianyu
 * @date 2015-01-22
 */
public abstract class PeriodScanCallback implements BluetoothAdapter.LeScanCallback {
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected long timeoutMillis;
    protected LiteBluetooth liteBluetooth;

    public PeriodScanCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public abstract void onScanTimeout();

    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            removeHandlerMsg();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    liteBluetooth.stopScan(PeriodScanCallback.this);
                    onScanTimeout();
                }
            }, timeoutMillis);
        }
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public PeriodScanCallback setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public LiteBluetooth getLiteBluetooth() {
        return liteBluetooth;
    }

    public PeriodScanCallback setLiteBluetooth(LiteBluetooth liteBluetooth) {
        this.liteBluetooth = liteBluetooth;
        return this;
    }
}
