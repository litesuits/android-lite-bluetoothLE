package com.litesuits.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;
import com.litesuits.bluetooth.conn.BluetoothHelper;

/**
 * @author MaTianyu
 * @date 2015-01-22
 */
public abstract class PeriodScanCallback extends BluetoothHelper implements BluetoothAdapter.LeScanCallback {
    private Handler handler = new Handler(Looper.getMainLooper());
    private long             timeoutMillis;
    private BluetoothAdapter adapter;

    protected PeriodScanCallback(final long timeoutMillis, final BluetoothAdapter adapter) {
        this.timeoutMillis = timeoutMillis;
        this.adapter = adapter;
    }

    public abstract void onScanTimeout();

    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            notifyScanStoped();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanAndNotify();
                    onScanTimeout();

                }
            }, timeoutMillis);
        }
    }

    public void stopScanAndNotify() {
        if (adapter == null) throw new IllegalArgumentException("Scan callback has no BluetoothAdapter！");
        notifyScanStoped();
        adapter.stopLeScan(PeriodScanCallback.this);
    }

    public void notifyScanStoped() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }
}
