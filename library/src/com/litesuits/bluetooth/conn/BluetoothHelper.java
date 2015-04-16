package com.litesuits.bluetooth.conn;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.utils.HexUtil;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author MaTianyu
 * @date 2015-01-29
 */
public abstract class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private Handler handler = new Handler(Looper.getMainLooper());
    private TimeoutCallback timerTask;
    private long writeTimeout = 3000;
    private long readTimeout = 3000;

    /*------------ getter and setter  ------------ */

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    /*------------ TimerTask  ------------ */
    public synchronized void notifyTimerTaskStart(final BluetoothGatt gatt, long timeoutMillis, final TimeoutCallback callback) {
        notifyTimerTaskRemove();
        timerTask = callback;
        if (timerTask != null) {
            timerTask.setGatt(gatt);
            handler.postDelayed(timerTask, timeoutMillis);
        }
    }

    public void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public synchronized void notifyTimerTaskRemove() {
        if (timerTask != null) {
            handler.removeCallbacks(timerTask);
        }
    }

    public void notifyTimerTaskRemove(TimeoutCallback callback) {
        if (callback != null) {
            handler.removeCallbacks(callback);
        }
    }

    /*------------  BluetoothGatt  ------------ */
    public void closeBluetoothGatt(BluetoothGatt gatt) {
        if (gatt != null) {
            gatt.disconnect();
            refreshDeviceCache(gatt);
            gatt.close();
            Log.i(TAG, "closed BluetoothGatt ");
        }
    }

    /*------------  Service  ------------ */
    public BluetoothGattService getService(BluetoothGatt gatt, String serviceUUID) {
        return gatt.getService(UUID.fromString(serviceUUID));
    }

    /*------------  Characteristic服务  ------------ */
    public BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, String serviceUUID, String charactUUID) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

    public boolean characteristicWrite(BluetoothGatt gatt, String serviceUUID, String charactUUID, String hex, TimeoutCallback callback) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, serviceUUID, charactUUID);
        return characteristicWrite(gatt, characteristic, hex, callback);
    }

    public boolean characteristicWrite(BluetoothGatt gatt, String serviceUUID, String charactUUID, byte[] data, TimeoutCallback callback) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, serviceUUID, charactUUID);
        return characteristicWrite(gatt, characteristic, data, callback);
    }

    public boolean characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic charact, String hex, TimeoutCallback callback) {
        if (charact == null || hex == null) {
            return false;
        }
        BleLog.e(TAG, charact.getUuid() + "写入：" + hex);
        return characteristicWrite(gatt, charact, HexUtil.decodeHex(hex.toCharArray()), callback);
    }

    public boolean characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic charact, byte[] data, TimeoutCallback callback) {
        if (charact != null) {
            notifyTimerTaskStart(gatt, getWriteTimeout(), callback);
            charact.setValue(data);
            gatt.writeCharacteristic(charact);
            return true;
        } else {
            refreshDeviceCache(gatt);
            BleLog.e(TAG, "Characteristic 为空");
            return false;
        }
    }

    public boolean enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic cha2App,
                                                    String descriptorUUID) {
        return enableCharacteristicNotification(gatt, cha2App, descriptorUUID, true);
    }

    public boolean disableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic cha2App,
                                                     String descriptorUUID) {
        return enableCharacteristicNotification(gatt, cha2App, descriptorUUID, false);
    }

    private boolean enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic cha2App,
                                                     String descriptorUUID, boolean enable) {
        if (cha2App != null) {
            BleLog.i(TAG, "cha2APP enable notification : " + cha2App.getUuid());
            if ((cha2App.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                BleLog.i(TAG, "支持通知 readCharacteristic ");
                gatt.setCharacteristicNotification(cha2App, enable);
                BluetoothGattDescriptor descriptor = cha2App.getDescriptor(UUID.fromString(descriptorUUID));
                if (descriptor != null) {
                    BleLog.i(TAG, "开启通知 readCharacteristic ");
                    if (enable) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    return gatt.writeDescriptor(descriptor);
                }
            } else {
                BleLog.i(TAG, "该通道无内容读取！");
            }
        }
        return false;
    }


    /**
     * Clears the device cache. After uploading new hello4 the DFU target will have other services than before.
     */
    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        /*
         * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
		 */
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
                Log.i(TAG, "Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device", e);
        }
        return false;
    }
}
