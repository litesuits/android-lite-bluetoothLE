package com.litesuits.bluetooth;

import android.bluetooth.*;
import android.os.Handler;
import android.os.Looper;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.utils.HexUtil;

import java.util.TimerTask;
import java.util.UUID;

public abstract class LiteBluetoothGatCallback extends BluetoothGattCallback {
    private static final String TAG = "LiteBluetoothGatCallback";
    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final int DEFAULT_DISCOVER_TIMEOUT = 5000;

    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private long discoverServicesTimeout = DEFAULT_DISCOVER_TIMEOUT;
    //private              Timer  timer                   = new Timer();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable connectTimeoutTask;
    private Runnable discoverServicesTimeoutTask;
    private Runnable otherTimeoutTask;
    private BluetoothGatt bluetoothGatt;

    protected LiteBluetoothGatCallback(long connectTimeout, long discoverServicesTimeout) {
        if (connectTimeout > 0) {
            this.connectTimeout = connectTimeout;
        }
        if (discoverServicesTimeout > 0) {
            this.discoverServicesTimeout = discoverServicesTimeout;
        }
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public abstract void onConnectSuccess(BluetoothGatt gatt);


    public abstract void onConnectTimeout(BluetoothGatt gatt);


    public abstract void onServicesDiscoveSuccess(BluetoothGatt gatt);

    public abstract void onServicesDiscoverTimeout(BluetoothGatt gatt, int status);

    public abstract void onOtherTimeout(BluetoothGatt gatt, String msg);

    public abstract void onDisConnected(BluetoothGatt gatt);

    protected void discoverServices(BluetoothGatt gatt) {
        notifyDiscoverServicesStart(gatt);
        gatt.discoverServices();
    }


    public BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, String serviceUUID, String charactUUID) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        //super.onConnectionStateChange(gatt, status, newState);
        if (BleLog.isPrint) {
            BleLog.i(TAG, "onConnectionStateChange  status: " + status + " ,newState: "+newState
                   + "  ,thread: " + Thread.currentThread().getId());
        }
        this.bluetoothGatt = gatt;
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            onDisConnected(gatt);
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                notifyConnectOver();
                onConnectSuccess(gatt);
            }
        } else {
            notifyConnectOver();
            if (connectTimeoutTask != null) {
                handler.post(connectTimeoutTask);
            }
            BleLog.e(TAG, "onConnectionStateChange status: " + status);
        }

    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        //super.onServicesDiscovered(gatt, status);
        if (BleLog.isPrint) {
            BleLog.i(TAG, "onServicesDiscovered  status: " + status + "  ,thread: " + Thread.currentThread().getId());
        }
        notifyDiscoverServicesOver();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onServicesDiscoveSuccess(gatt);
        } else {
            if (discoverServicesTimeoutTask != null) {
                handler.post(discoverServicesTimeoutTask);
            }
            BleLog.e(TAG, "onServicesDiscovered status: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        //super.onCharacteristicRead(gatt, characteristic, status);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "onCharacteristicRead  status: " + status +
                    " ,characteristic : " + characteristic.getUuid() +
                    " ,characteristic : " + HexUtil.encodeHexStr(characteristic.getValue()) +
                    " ,thread: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        //super.onCharacteristicWrite(gatt, characteristic, status);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "onCharacteristicWrite  status: " + status +
                    " ,characteristic : " + characteristic.getUuid() +
                    " ,characteristic : " + HexUtil.encodeHexStr(characteristic.getValue()) +
                    " ,thread: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //super.onCharacteristicChanged(gatt, characteristic);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "onCharacteristicChanged  uuid: " + characteristic.getUuid() +
                    " ,characteristic : " + HexUtil.encodeHexStr(characteristic.getValue()) +
                    " ,thread: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        //super.onDescriptorRead(gatt, descriptor, status);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "onDescriptorRead  status: " + status +
                    " ,characteristic : " + descriptor.getUuid() +
                    " ,characteristic : " + HexUtil.encodeHexStr(descriptor.getValue()) +
                    " ,thread: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        //super.onDescriptorWrite(gatt, descriptor, status);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "onDescriptorWrite  status: " + status +
                    " ,characteristic : " + descriptor.getUuid() +
                    " ,characteristic : " + HexUtil.encodeHexStr(descriptor.getValue()) +
                    " ,thread: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        //super.onReliableWriteCompleted(gatt, status);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "onReliableWriteCompleted  status: " + status + " ,thread: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        //super.onReadRemoteRssi(gatt, rssi, status);
        if (BleLog.isPrint) {
            BleLog.v(TAG, "connect  onReadRemoteRssi  status: " + status + " , rssi : " + rssi +
                    " ,thread: " + Thread.currentThread().getId());
        }
    }

    public void notifyConnectStart(final BluetoothGatt gatt) {
        notifyConnectOver();
        if (connectTimeoutTask == null) {
            connectTimeoutTask = new TimerTask() {
                @Override
                public void run() {
                    BleLog.e(TAG, "Bluetooth connect timeout. 蓝牙连接超时 ");
                    onConnectTimeout(gatt);
                }
            };
        }
        handler.postDelayed(connectTimeoutTask, connectTimeout);
    }

    public void notifyConnectOver() {
        if (connectTimeoutTask != null) {
            handler.removeCallbacks(connectTimeoutTask);
        }
    }

    public void notifyDiscoverServicesStart(final BluetoothGatt gatt) {
        notifyDiscoverServicesOver();
        if (discoverServicesTimeoutTask == null) {
            discoverServicesTimeoutTask = new TimerTask() {
                @Override
                public void run() {
                    BleLog.e(TAG, "Bluetooth discover services timeout. 蓝牙发现服务超时 ");
                    onServicesDiscoverTimeout(gatt, -1);
                }
            };
        }
        handler.postDelayed(discoverServicesTimeoutTask, discoverServicesTimeout);
    }

    public void notifyDiscoverServicesOver() {
        if (discoverServicesTimeoutTask != null) {
            handler.removeCallbacks(discoverServicesTimeoutTask);
        }
    }

    public void notifyOtherStart(final BluetoothGatt gatt, long timeoutMillis, final String msg) {
        notifyOtherOver();
        if (otherTimeoutTask == null) {
            otherTimeoutTask = new TimerTask() {
                @Override
                public void run() {
                    BleLog.e(TAG, "Bluetooth other things timeout. 蓝牙操作 超时： " + msg);
                    onOtherTimeout(gatt, msg);
                }
            };
        }
        handler.postDelayed(otherTimeoutTask, timeoutMillis);
    }

    public void notifyOtherOver() {
        if (otherTimeoutTask != null) {
            handler.removeCallbacks(otherTimeoutTask);
        }
    }

    public long getDiscoverServicesTimeout() {
        return discoverServicesTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }
}