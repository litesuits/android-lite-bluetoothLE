package com.litesuits.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import com.litesuits.bluetooth.conn.ConnectError;
import com.litesuits.bluetooth.conn.ConnectListener;
import com.litesuits.bluetooth.conn.ConnectState;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;
import com.litesuits.bluetooth.utils.HandlerUtil;

import java.util.UUID;

/**
 * @author MaTianyu
 * @date 2015-01-16
 */
public class LiteBluetooth {
    private static final String TAG = "LiteBluetooth";
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;


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

    public boolean connect(final String mac, final ConnectListener listener) {
        return connect(mac, false, listener);
    }

    public boolean connect(final String mac, final boolean autoConnect, final ConnectListener listener) {
        if (mac == null || mac.split(":").length != 6) {
            listener.onFailed(ConnectError.Invalidmac);
            return false;
        }
        listener.stateChanged(ConnectState.Scanning);
        startScan(new PeriodMacScanCallback(mac, 30000, null) {

            @Override
            public void onScanTimeout() {
                listener.onFailed(ConnectError.ScanTimeout);
                listener.stateChanged(ConnectState.Initialed);
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                this.stopScanAndNotify();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LiteBluetoothDevice liteDevice = new LiteBluetoothDevice(device);
                        listener.stateChanged(ConnectState.Connecting);
                        liteDevice.connect(context, autoConnect, new LiteBluetoothGatCallback(20000, 10000) {
                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                super.onConnectionStateChange(gatt, status, newState);
                            }

                            @Override
                            public void onConnectSuccess(BluetoothGatt gatt) {
                                discoverServices(gatt, DEFAULT_DISCOVER_TIMEOUT);
                                listener.stateChanged(ConnectState.ServiceDiscovering);
                            }

                            @Override
                            public void onConnectTimeout(BluetoothGatt gatt) {
                                BleLog.e(TAG, " onConnectTimeout gatt: " + gatt);
                                closeBluetoothGatt(gatt);
                                listener.onFailed(ConnectError.ConnectTimeout);
                                listener.stateChanged(ConnectState.Initialed);
                            }


                            @Override
                            public void onServicesDiscoveSuccess(BluetoothGatt gatt) {
                                listener.stateChanged(ConnectState.ServiceDiscovered);
                                listener.onServicesDiscovered(gatt);
                            }

                            @Override
                            public void onServicesDiscoverTimeout(BluetoothGatt gatt) {
                                closeBluetoothGatt(gatt);
                                listener.onFailed(ConnectError.ServiceDiscoverTimeout);
                            }

                            @Override
                            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                                super.onCharacteristicWrite(gatt, characteristic, status);
                                listener.onCharacteristicWrite(gatt, characteristic, status);
                            }

                            @Override
                            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                                //super.onCharacteristicChanged(gatt, characteristic);
                                listener.onCharacteristicChanged(gatt, characteristic);
                            }

                            @Override
                            public void onOtherTimeout(BluetoothGatt gatt, String msg) {

                            }

                            @Override
                            public void onDisConnected(BluetoothGatt gatt) {
                                listener.stateChanged(ConnectState.DisConnected);
                            }

                        });

                    }
                });
            }

        });
        return true;
    }


    public void connectDirectly(final BluetoothDevice device, final ConnectListener listener) {
        connectDirectly(device, false, listener);
    }

    public void connectDirectly(final BluetoothDevice device, final boolean autoConnect, final ConnectListener listener) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiteBluetoothDevice liteDevice = new LiteBluetoothDevice(device);
                listener.stateChanged(ConnectState.Connecting);
                liteDevice.connect(context, autoConnect, new LiteBluetoothGatCallback(20000, 10000) {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                    }

                    @Override
                    public void onConnectSuccess(BluetoothGatt gatt) {
                        discoverServices(gatt, DEFAULT_DISCOVER_TIMEOUT);
                        listener.stateChanged(ConnectState.ServiceDiscovering);
                    }

                    @Override
                    public void onConnectTimeout(BluetoothGatt gatt) {
                        BleLog.e(TAG, " onConnectTimeout gatt: " + gatt);
                        BluetoothUtil.closeBluetoothGatt(gatt);
                        listener.onFailed(ConnectError.ConnectTimeout);
                        listener.stateChanged(ConnectState.Initialed);
                    }


                    @Override
                    public void onServicesDiscoveSuccess(BluetoothGatt gatt) {
                        listener.stateChanged(ConnectState.ServiceDiscovered);
                        listener.onServicesDiscovered(gatt);
                    }

                    @Override
                    public void onServicesDiscoverTimeout(BluetoothGatt gatt) {
                        BluetoothUtil.closeBluetoothGatt(gatt);
                        listener.onFailed(ConnectError.ServiceDiscoverTimeout);
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        listener.onCharacteristicWrite(gatt, characteristic, status);
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
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

                });

            }
        });
    }

    public void startScan(PeriodScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
        callback.setAdapter(bluetoothAdapter);
        callback.notifyScanStarted();
    }

    public void startScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.startLeScan(callback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter.stopLeScan(callback);
    }

    //public void discoverServices(BluetoothDevice device) {
    //
    //}

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
