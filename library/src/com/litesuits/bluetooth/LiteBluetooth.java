/*
 * Copyright (C) 2013 litesuits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.litesuits.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.litesuits.bluetooth.conn.LiteBleConnector;
import com.litesuits.bluetooth.exception.BleException;
import com.litesuits.bluetooth.exception.ConnectException;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One Device, One connection, One {@link LiteBluetooth} Instance.
 * One connection can has many callback.
 * <p/>
 * One {@link LiteBluetooth} Instance can add many {@link BluetoothGattCallback}
 * {@link LiteBleGattCallback} is an abstract extension of {@link BluetoothGattCallback}.
 * <p/>
 *
 * @author MaTianyu
 * @date 2015-01-16
 */
public class LiteBluetooth {
    private static final String TAG = LiteBluetooth.class.getSimpleName();
    public static final int DEFAULT_SCAN_TIME = 20000;
    public static final int DEFAULT_CONN_TIME = 10000;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_SERVICES_DISCOVERED = 4;

    private int connectionState = STATE_DISCONNECTED;
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Set<BluetoothGattCallback> callbackList = new LinkedHashSet<BluetoothGattCallback>();

    public LiteBluetooth(Context context) {
        this.context = context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public LiteBleConnector newBleConnector() {
        return new LiteBleConnector(this);
    }

    public boolean isInScanning() {

        return connectionState == STATE_SCANNING;
    }

    public boolean isConnectingOrConnected() {
        return connectionState >= STATE_CONNECTING;
    }

    public boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    public boolean isServiceDiscoered() {
        return connectionState == STATE_SERVICES_DISCOVERED;
    }

    public boolean addGattCallback(BluetoothGattCallback callback) {
        return callbackList.add(callback);
    }

    public boolean addGattCallback(LiteBleGattCallback callback) {
        return callbackList.add(callback);
    }

    public boolean removeGattCallback(BluetoothGattCallback callback) {
        return callbackList.remove(callback);
    }

    /**
     * Starts a scan for Bluetooth LE devices.
     * <p/>
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     *
     * @param callback the callback LE scan results are delivered
     * @return true, if the scan was started successfully
     */
    public boolean startLeScan(BluetoothAdapter.LeScanCallback callback) {
        boolean suc = bluetoothAdapter.startLeScan(callback);
        if (suc) {
            connectionState = STATE_SCANNING;
        }
        return suc;
    }

    public boolean startLeScan(PeriodScanCallback callback) {
        callback.setLiteBluetooth(this).notifyScanStarted();
        boolean suc = bluetoothAdapter.startLeScan(callback);
        if (suc) {
            connectionState = STATE_SCANNING;
        } else {
            callback.removeHandlerMsg();
        }
        return suc;
    }

    public void startLeScan(PeriodMacScanCallback callback) {
        startLeScan((PeriodScanCallback) callback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        if (callback instanceof PeriodScanCallback) {
            ((PeriodScanCallback) callback).removeHandlerMsg();
        }
        bluetoothAdapter.stopLeScan(callback);
        if (connectionState == STATE_SCANNING) {
            connectionState = STATE_DISCONNECTED;
        }
    }

    /**
     * Note: Be Sure Call This On Main(UI) Thread!
     * Note: Be Sure Call This On Main(UI) Thread!
     * Note: Be Sure Call This On Main(UI) Thread!
     * <p/>
     * Connect to GATT Server hosted by this device. Caller acts as GATT client.
     * The callback is used to deliver results to Caller, such as connection status as well
     * as any further GATT client operations.
     * The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct
     * GATT client operations.
     *
     * @param device      the device to be connected.
     * @param autoConnect Whether to directly connect to the remote device (false)
     *                    or to automatically connect as soon as the remote
     *                    device becomes available (true).
     * @param callback    GATT callback handler that will receive asynchronous callbacks.
     * @return BluetoothGatt instance. You can use BluetoothGatt to conduct GATT client operations.
     */
    public synchronized BluetoothGatt connect(final BluetoothDevice device,
                                              final boolean autoConnect,
                                              final LiteBleGattCallback callback) {
        Log.i(TAG, "connect device：" + device.getName()
                + " mac:" + device.getAddress()
                + " autoConnect ------> " + autoConnect);
        callbackList.add(callback);
        return device.connectGatt(context, autoConnect, coreGattCallback);
    }

    /**
     * Note: Be Sure Call This On Main(UI) Thread!
     * <p/>
     * Try to scan specified device. Connect to GATT Server hosted by this device. Caller acts as GATT client.
     * The callback is used to deliver results to Caller, such as connection status as well
     * as any further GATT client operations.
     *
     * @param mac         MAC of device
     * @param autoConnect Whether to directly connect to the remote device (false)
     *                    or to automatically connect as soon as the remote
     *                    device becomes available (true).
     * @param callback    GATT callback handler that will receive asynchronous callbacks.
     */
    public boolean scanAndConnect(String mac, final boolean autoConnect, final LiteBleGattCallback callback) {
        if (mac == null || mac.split(":").length != 6) {
            throw new IllegalArgumentException("Illegal MAC ! ");
        }
        startLeScan(new PeriodMacScanCallback(mac, DEFAULT_SCAN_TIME) {

            @Override
            public void onScanTimeout() {
                if (callback != null) {
                    callback.onConnectFailure(BleException.TIMEOUT_EXCEPTION);
                }
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(device, autoConnect, callback);
                    }
                });
            }
        });
        return true;
    }

    /**
     * Clears the device cache. After uploading new hello4 the DFU target will have other services than before.
     */
    public boolean refreshDeviceCache() {
        /*
         * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
		 */
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                Log.i(TAG, "Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device", e);
        }
        return false;
    }

    /**
     * disconnect, refresh and close bluetooth gatt.
     */
    public void closeBluetoothGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            refreshDeviceCache();
            bluetoothGatt.close();
            Log.i(TAG, "closed BluetoothGatt ");
        }
    }

    public void enableBluetoothIfDisabled(Activity activity, int requestCode) {
        if (!bluetoothAdapter.isEnabled()) {
            BluetoothUtil.enableBluetooth(activity, requestCode);
        }
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public void enableBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    public void enableBluetooth() {
        bluetoothAdapter.enable();
    }

    public void disableBluetooth() {
        bluetoothAdapter.disable();
    }

    public Context getContext() {
        return context;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    /**
     * return
     * {@link #STATE_DISCONNECTED}
     * {@link #STATE_SCANNING}
     * {@link #STATE_CONNECTING}
     * {@link #STATE_CONNECTED}
     * {@link #STATE_SERVICES_DISCOVERED}
     */
    public int getConnectionState() {
        return connectionState;
    }

    private LiteBleGattCallback coreGattCallback = new LiteBleGattCallback() {

        @Override
        public void onConnectFailure(BleException exception) {
            bluetoothGatt = null;
            for (BluetoothGattCallback call : callbackList) {
                if (call instanceof LiteBleGattCallback) {
                    ((LiteBleGattCallback) call).onConnectFailure(exception);
                }
            }
        }

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            bluetoothGatt = gatt;
            for (BluetoothGattCallback call : callbackList) {
                if (call instanceof LiteBleGattCallback) {
                    ((LiteBleGattCallback) call).onConnectSuccess(gatt, status);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (BleLog.isPrint) {
                BleLog.i(TAG, "onConnectionStateChange  status: " + status
                        + " ,newState: " + newState + "  ,thread: " + Thread.currentThread().getId());
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                onConnectSuccess(gatt, status);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                onConnectFailure(new ConnectException(gatt, status));
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                connectionState = STATE_CONNECTING;
            }
            for (BluetoothGattCallback call : callbackList) {
                call.onConnectionStateChange(gatt, status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            connectionState = STATE_SERVICES_DISCOVERED;
            for (BluetoothGattCallback call : callbackList) {
                call.onServicesDiscovered(gatt, status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (BluetoothGattCallback call : callbackList) {
                call.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (BluetoothGattCallback call : callbackList) {
                call.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            for (BluetoothGattCallback call : callbackList) {
                call.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (BluetoothGattCallback call : callbackList) {
                call.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (BluetoothGattCallback call : callbackList) {
                call.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            for (BluetoothGattCallback call : callbackList) {
                call.onReliableWriteCompleted(gatt, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            for (BluetoothGattCallback call : callbackList) {
                call.onReadRemoteRssi(gatt, rssi, status);
            }
        }
    };
}
