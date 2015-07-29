package com.litesuits.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.litesuits.bluetooth.conn.ConnectError;
import com.litesuits.bluetooth.conn.ConnectListener;
import com.litesuits.bluetooth.conn.ConnectState;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;
import com.litesuits.bluetooth.utils.HandlerUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author MaTianyu
 * @date 2015-01-16
 */
public class LiteBluetooth {
    private static final String TAG = "LiteBluetooth";
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private ConcurrentHashMap<BluetoothGatt, String> gattMap = new ConcurrentHashMap<BluetoothGatt, String>();

    public LiteBluetooth(Context context) {
        this.context = context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startScan(PeriodMacScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
        callback.setAdapter(bluetoothAdapter);
        callback.notifyScanStarted();
    }

    public BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, String serviceUUID, String charactUUID) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

    public synchronized boolean connect(final String mac, final boolean autoConnect, final ConnectListener listener) {
        if (mac == null || mac.split(":").length != 6) {
            listener.failed(ConnectError.Invalidmac);
            return false;
        }
        listener.stateChanged(ConnectState.Scanning);
        startScan(new PeriodMacScanCallback(mac, 5000, null) {

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
        HandlerUtil.runOnUiThread(new Runnable() {
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

    public BluetoothGatt retryConnectDirectly(LiteBluetoothGatCallback callback, BluetoothDevice device) {
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

    public void startScan(PeriodScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
        callback.setAdapter(bluetoothAdapter);
        callback.notifyScanStarted();
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

    public void startScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.stopLeScan(callback);
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

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }
}
