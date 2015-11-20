package com.litesuits.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.litesuits.bluetooth.conn.ConnectError;
import com.litesuits.bluetooth.conn.ConnectListener;
import com.litesuits.bluetooth.conn.ConnectState;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author MaTianyu
 * @date 2015-01-16
 */
public class LiteBluetooth {
    private static final String TAG = "LiteBluetooth";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_SCANNING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_SERVICES_DISCOVERING = 4;
    private static final int STATE_SERVICES_DISCOVERED = 5;

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ArrayList<BluetoothGattCallback> callbackList = new ArrayList<>();
    private ConcurrentHashMap<BluetoothGatt, String> gattMap = new ConcurrentHashMap<BluetoothGatt, String>();


    public LiteBluetooth(Context context) {
        this.context = context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startLeScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
    }

    public void startLeScan(PeriodScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
        callback.setAdapter(bluetoothAdapter);
        callback.notifyScanStarted();
    }

    public void startLeScan(PeriodMacScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
        callback.setAdapter(bluetoothAdapter);
        callback.notifyScanStarted();
    }

    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.stopLeScan(callback);
        if (callback instanceof PeriodScanCallback) {
            ((PeriodScanCallback) callback).notifyScanStoped();
        }
    }

    public LiteBleConnector newBleConnector() {
        return new LiteBleConnector(this);
    }

    public boolean addGattCallback(BluetoothGattCallback callback) {
        return callbackList.add(callback);
    }

    public boolean removeGattCallback(BluetoothGattCallback callback) {
        return callbackList.remove(callback);
    }

    public static abstract class LiteGattCallback extends BluetoothGattCallback {

        public abstract void onConnectSuccess(BluetoothGatt gatt, int status, int newState);

        @Override
        public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

        public abstract void onConnectFailure(BluetoothGatt gatt, int status, int newState);
    }


    private LiteGattCallback coreGattCallback = new LiteGattCallback() {

        @Override
        public void onConnectFailure(BluetoothGatt gatt, int status, int newState) {
            bluetoothGatt = null;
            for (BluetoothGattCallback call : callbackList) {
                if (call instanceof LiteGattCallback) {
                    ((LiteGattCallback) call).onConnectFailure(gatt, status, newState);
                }
            }
        }

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status, int newState) {
            bluetoothGatt = gatt;
            for (BluetoothGattCallback call : callbackList) {
                if (call instanceof LiteGattCallback) {
                    ((LiteGattCallback) call).onConnectSuccess(gatt, status, newState);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (BleLog.isPrint) {
                BleLog.i(TAG, "onConnectionStateChange  status: " + status
                              + " ,newState: " + newState + "  ,thread: " + Thread.currentThread().getId());
            }
            for (BluetoothGattCallback call : callbackList) {
                call.onConnectionStateChange(gatt, status, newState);
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                onConnectSuccess(gatt, status, newState);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                onConnectFailure(gatt, status, newState);
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                connectionState = STATE_CONNECTING;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
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

    public synchronized void connect(final BluetoothDevice device,
                                     final boolean autoConnect,
                                     final LiteGattCallback callback) {
        connect(device, autoConnect, callback);
    }

    public synchronized void connect(final BluetoothDevice device,
                                     final boolean autoConnect,
                                     final BluetoothGattCallback callback) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "connect device：" + device.getName()
                           + " mac:" + device.getAddress()
                           + " autoConnect ------> " + autoConnect);
                callbackList.add(callback);
                device.connectGatt(context, autoConnect, coreGattCallback);
            }
        });
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

    public synchronized boolean connect(final String mac, final boolean autoConnect, final ConnectListener listener) {
        if (mac == null || mac.split(":").length != 6) {
            listener.failed(ConnectError.Invalidmac);
            return false;
        }
        listener.stateChanged(ConnectState.Scanning);
        startLeScan(new PeriodMacScanCallback(mac, 5000) {

            @Override
            public void onScanTimeout() {
                listener.failed(ConnectError.ScanTimeout);
                listener.stateChanged(ConnectState.Initialed);
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                connect(device, autoConnect, listener);
            }
        });
        return true;
    }


    public synchronized void connect(final BluetoothDevice device, final boolean autoConnect,
                                     final ConnectListener listener) {
        closeAllConnects();
        Log.i(TAG, "连接：" + device.getName() + " mac:" + device.getAddress()
                   + "  autoConnect ---------------> " + autoConnect);

        handler.post(new Runnable() {
            @Override
            public void run() {
                LiteBluetoothDevice liteDevice = new LiteBluetoothDevice(device);
                listener.stateChanged(ConnectState.Connecting);
                final BluetoothGatt bluetoothGatt = liteDevice.connect(context, autoConnect,
                        new LiteBluetoothGatCallback(10000, 7000) {

                            final int MAX_RETRY = 1;
                            int connectTry = 0;
                            int discorverTry = 0;

                            @Override
                            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                                super.onReadRemoteRssi(gatt, rssi, status);
                                listener.onReadRemoteRssi(gatt, rssi, status);
                            }

                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                super.onConnectionStateChange(gatt, status, newState);
                            }

                            @Override
                            public void onConnectSuccess(final BluetoothGatt gatt) {
                                gattMap.put(gatt, System.currentTimeMillis() + "");
                                listener.setBluetoothGatt(gatt);
                                listener.stateChanged(ConnectState.Connected);

                                discoverServices(gatt);
                                listener.stateChanged(ConnectState.ServiceDiscovering);
                            }

                            @Override
                            public void onConnectTimeout(final BluetoothGatt gatt) {
                                if (connectTry++ < MAX_RETRY) {
                                    BluetoothDevice dev = device;
                                    if (gatt != null) {
                                        BluetoothUtil.closeBluetoothGatt(gatt);
                                        gattMap.remove(gatt);
                                        dev = gatt.getDevice();
                                    }
                                    retryConnectDirectly(this, dev);
                                } else {
                                    if (gatt != null) {
                                        BluetoothUtil.closeBluetoothGatt(gatt);
                                        gattMap.remove(gatt);
                                    }
                                    listener.failed(ConnectError.ConnectTimeout);
                                    listener.stateChanged(ConnectState.Initialed);
                                }
                            }


                            @Override
                            public void onServicesDiscoveSuccess(BluetoothGatt gatt) {
                                listener.stateChanged(ConnectState.ServiceDiscovered);
                                listener.onServicesDiscovered(gatt);
                            }

                            @Override
                            public void onServicesDiscoverTimeout(final BluetoothGatt gatt, int status) {
                                BleLog.e(TAG, "onConnectTimeout gatt: " + gatt);
                                if (gatt != null) {
                                    if (discorverTry++ < MAX_RETRY) {
                                        notifyDiscoverServicesStart(gatt);
                                        gatt.discoverServices();
                                    } else {
                                        BluetoothUtil.closeBluetoothGatt(gatt);
                                        gattMap.remove(gatt);
                                        listener.failed(ConnectError.ServiceDiscoverTimeout);
                                        listener.stateChanged(ConnectState.Initialed);
                                    }
                                } else {
                                    listener.failed(ConnectError.ServiceDiscoverTimeout);
                                    listener.stateChanged(ConnectState.Initialed);
                                }
                            }

                            @Override
                            public void onCharacteristicWrite(BluetoothGatt gatt,
                                                              BluetoothGattCharacteristic characteristic, int status) {
                                super.onCharacteristicWrite(gatt, characteristic, status);
                                listener.onCharacteristicWrite(gatt, characteristic, status);
                            }

                            @Override
                            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                                BluetoothGattCharacteristic characteristic) {
                                super.onCharacteristicChanged(gatt, characteristic);
                                listener.onCharacteristicChanged(gatt, characteristic);
                            }

                            @Override
                            public void onOtherTimeout(BluetoothGatt gatt, String msg) {

                            }

                            @Override
                            public void onDisConnected(BluetoothGatt gatt) {
                                listener.stateChanged(ConnectState.DisConnected);
                            }

                            @Override
                            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                                         int status) {
                                listener.onDescriptorRead(gatt, descriptor, status);
                            }

                            @Override
                            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                                          int status) {
                                listener.onDescriptorWrite(gatt, descriptor, status);
                            }
                        });
                if (bluetoothGatt != null) {
                    gattMap.put(bluetoothGatt, System.currentTimeMillis() + "");
                }
            }
        });
    }


    private BluetoothGatt retryConnectDirectly(LiteBluetoothGatCallback callback, BluetoothDevice device) {
        if (device != null) {
            BleLog.e(TAG, "BluetoothGatt retried connectGatt autoConnect ------------> false");
            callback.notifyConnectStart(null);
            BluetoothGatt gatt = device.connectGatt(context, false, callback);
            if (gatt != null) {
                gattMap.put(gatt, System.currentTimeMillis() + "");
            }
            return gatt;
        }
        return null;
    }


    public synchronized void closeAllConnects() {
        if (gattMap.size() > 0) {
            for (BluetoothGatt gatt : gattMap.keySet()) {
                if (gatt != null) {
                    try {
                        gatt.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        gatt.close();
                    }
                }
            }
            gattMap.clear();
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

    public void setContext(Context context) {
        this.context = context;
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

    public int getConnectionState() {
        return connectionState;
    }
}
