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

import java.util.HashMap;
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
    private HashMap<BluetoothGatt, String> gattMap = new HashMap<BluetoothGatt, String>();

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
            listener.failed(ConnectError.Invalidmac);
            return false;
        }
        listener.stateChanged(ConnectState.Scanning);
        startScan(new PeriodMacScanCallback(mac, 30000, null) {

            @Override
            public void onScanTimeout() {
                listener.failed(ConnectError.ScanTimeout);
                listener.stateChanged(ConnectState.Initialed);
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                connectDirectly(device, autoConnect, listener);
            }
        });
        return true;
    }


    public void connectDirectly(final BluetoothDevice device, final ConnectListener listener) {
        connectDirectly(device, false, listener);
    }

    public synchronized void connectDirectly(final BluetoothDevice device, final boolean autoConnect,
                                             final ConnectListener listener) {
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
        Log.i(TAG, "连接：" + device.getName() + " mac:" + device.getAddress() + "  autoConnect:" + autoConnect);
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiteBluetoothDevice liteDevice = new LiteBluetoothDevice(device);
                listener.stateChanged(ConnectState.Connecting);
                BluetoothGatt gatt = liteDevice.connect(context, autoConnect,
                        new LiteBluetoothGatCallback(20000, 30000) {

                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                super.onConnectionStateChange(gatt, status, newState);
                            }

                            @Override
                            public void onConnectSuccess(final BluetoothGatt gatt) {
                                gattMap.put(gatt, System.currentTimeMillis() + "");
                                listener.setBluetoothGatt(gatt);
                                listener.stateChanged(ConnectState.Connected);

                        /*
                     *  The onConnectionStateChange callback is called just after establishing connection and before sending Encryption Request BLE event in case of a paired device.
					 *  In that case and when the Service Changed CCCD is enabled we will get the indication after initializing the encryption, about 1600 milliseconds later.
					 *  If we discover services right after connecting, the onServicesDiscovered callback will be called immediately, before receiving the indication and the following
					 *  service discovery and we may end up with old, application's services instead.
					 *
					 *  This is to support the buttonless switch from application to bootloader mode where the DFU bootloader notifies the master about service change.
					 *  Tested on Nexus 4 (Android 4.4.4 and 5), Nexus 5 (Android 5), Samsung Note 2 (Android 4.4.2). The time after connection to end of service discovery is about 1.6s
					 *  on Samsung Note 2.
					 *
					 *  NOTE: We are doing this to avoid the hack with calling the hidden gatt.refresh() method, at least for bonded devices.
					 */
                                //if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                                //    try {
                                //        synchronized (this) {
                                //            Log.i(TAG, "Waiting 1600 ms for a possible Service Changed indication...");
                                //            wait(1600);
                                //
                                //            // After 1.6s the services are already discovered so the following gatt.discoverServices() finishes almost immediately.
                                //
                                //            // NOTE: This also works with shorted waiting time. The gatt.discoverServices() must be called after the indication is received which is
                                //            // about 600ms after establishing connection. Values 600 - 1600ms should be OK.
                                //        }
                                //    } catch (InterruptedException e) {
                                //        // do nothing
                                //    }
                                //}
                                discoverServices(gatt);
                                listener.stateChanged(ConnectState.ServiceDiscovering);
                                //HandlerUtil.runOnUiThreadDelay(new Runnable() {
                                //    @Override
                                //    public void run() {
                                //    }
                                //}, 2000);
                            }

                            @Override
                            public void onConnectTimeout(BluetoothGatt gatt) {
                                BleLog.e(TAG, " onConnectTimeout gatt: " + gatt);
                                BluetoothUtil.closeBluetoothGatt(gatt);
                                listener.failed(ConnectError.ConnectTimeout);
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
                                listener.failed(ConnectError.ServiceDiscoverTimeout);
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

                            @Override
                            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                                listener.onDescriptorRead(gatt, descriptor, status);
                            }

                            @Override
                            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                                listener.onDescriptorWrite(gatt, descriptor, status);
                            }
                        });
                gattMap.put(gatt, System.currentTimeMillis() + "");
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
