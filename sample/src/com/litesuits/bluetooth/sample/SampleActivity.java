package com.litesuits.bluetooth.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.os.Bundle;
import android.widget.Toast;
import com.litesuits.bluetooth.LiteBleGattCallback;
import com.litesuits.bluetooth.LiteBluetooth;
import com.litesuits.bluetooth.conn.BleCharactCallback;
import com.litesuits.bluetooth.conn.BleDescriptorCallback;
import com.litesuits.bluetooth.conn.BleRssiCallback;
import com.litesuits.bluetooth.conn.LiteBleConnector;
import com.litesuits.bluetooth.exception.BleException;
import com.litesuits.bluetooth.exception.hanlder.DefaultBleExceptionHandler;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;

import java.util.Arrays;

public class SampleActivity extends Activity {

    private static final String TAG = SampleActivity.class.getSimpleName();

    /**
     * mac和服务uuid纯属测试，测试时请替换真实参数。
     */
    public String UUID_SERVICE = "6e400000-0000-0000-0000-000011112222";

    public String UUID_CHAR_WRITE = "6e400001-0000-0000-0000-000011112222";
    public String UUID_CHAR_READ = "6e400002-0000-0000-0000-000011112222";

    public String UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    public String UUID_DESCRIPTOR_WRITE = "00002902-0000-1000-8000-00805f9b34fb";
    public String UUID_DESCRIPTOR_READ = "00002902-0000-1000-8000-00805f9b34fb";

    private static int TIME_OUT_SCAN = 10000;
    private static int TIME_OUT_OPERATION = 5000;
    private Activity activity;
    /**
     * 蓝牙主要操作对象，建议单例。
     */
    private static LiteBluetooth liteBluetooth;
    /**
     * 默认异常处理器
     */
    private DefaultBleExceptionHandler bleExceptionHandler;
    /**
     * mac和服务uuid纯属测试，测试时请替换真实参数。
     */
    private static String MAC = "00:00:00:AA:AA:AA";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity = this;
        if (liteBluetooth == null) {
            liteBluetooth = new LiteBluetooth(activity);
        }
        liteBluetooth.enableBluetoothIfDisabled(activity, 1);
        bleExceptionHandler = new DefaultBleExceptionHandler(this);
    }

    /**
     * scan devices for a while
     */
    private void scanDevicesPeriod() {
        liteBluetooth.startLeScan(new PeriodScanCallback(TIME_OUT_SCAN) {
            @Override
            public void onScanTimeout() {
                dialogShow(TIME_OUT_SCAN + " Millis Scan Timeout! ");
            }

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                BleLog.i(TAG, "device: " + device.getName() + "  mac: " + device.getAddress()
                              + "  rssi: " + rssi + "  scanRecord: " + Arrays.toString(scanRecord));
            }
        });
    }

    /**
     * scan a specified device for a while
     */
    private void scanSpecifiedDevicePeriod() {
        liteBluetooth.startLeScan(new PeriodMacScanCallback(MAC, TIME_OUT_SCAN) {

            @Override
            public void onScanTimeout() {
                dialogShow(TIME_OUT_SCAN + " Millis Scan Timeout!  Device Not Found! ");
            }

            @Override
            public void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
                dialogShow(" Device Found " + device.getName() + " MAC: " + device.getAddress()
                           + " \n RSSI: " + rssi + " records:" + Arrays.toString(scanRecord));
            }
        });
    }

    /**
     * scan and connect to device
     */
    private void scanAndConnect() {
        liteBluetooth.scanAndConnect(MAC, false, new LiteBleGattCallback() {

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                // discover services !
                gatt.discoverServices();
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothUtil.printServices(gatt);
                dialogShow(MAC + " Services Discovered SUCCESS !");
            }

            @Override
            public void onConnectFailure(BleException exception) {
                bleExceptionHandler.handleException(exception);
                dialogShow(MAC + " Services Discovered FAILURE !");
            }
        });
    }

    /**
     * scan first, then connect
     */
    private void scanThenConnect() {
        liteBluetooth.startLeScan(new PeriodMacScanCallback(MAC, TIME_OUT_SCAN) {

            @Override
            public void onScanTimeout() {
                dialogShow(TIME_OUT_SCAN + "毫秒扫描结束，未发现设备");
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                Toast.makeText(activity, "发现 " + device.getAddress() + " 正在连接...", Toast.LENGTH_LONG).show();
                liteBluetooth.connect(device, false, new LiteBleGattCallback() {

                    @Override
                    public void onConnectSuccess(BluetoothGatt gatt, int status) {
                        gatt.discoverServices();
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        BluetoothUtil.printServices(gatt);
                    }

                    @Override
                    public void onConnectFailure(BleException exception) {
                        bleExceptionHandler.handleException(exception);
                        dialogShow(device.getAddress() + " 设备连接失败");
                    }
                });

            }
        });
    }

    /**
     * get state
     */
    private void getBluetoothState() {
        BleLog.i(TAG, "liteBluetooth.getConnectionState: " + liteBluetooth.getConnectionState());
        BleLog.i(TAG, "liteBluetooth isInScanning: " + liteBluetooth.isInScanning());
        BleLog.i(TAG, "liteBluetooth isConnected: " + liteBluetooth.isConnected());
        BleLog.i(TAG, "liteBluetooth isServiceDiscoered: " + liteBluetooth.isServiceDiscoered());
        if (liteBluetooth.getConnectionState() >= LiteBluetooth.STATE_CONNECTING) {
            BleLog.i(TAG, "lite bluetooth is in connecting or connected");
        }
        if (liteBluetooth.getConnectionState() == LiteBluetooth.STATE_SERVICES_DISCOVERED) {
            BleLog.i(TAG, "lite bluetooth is in connected, services have been found");
        }
    }

    /**
     * add(remove) new callback to an existing connection.
     * One Device, One {@link LiteBluetooth}.
     * But one device( {@link LiteBluetooth}) can add many callback {@link BluetoothGattCallback}
     *
     * {@link LiteBleGattCallback} is a extension of {@link BluetoothGattCallback}
     */
    private void addNewCallbackToOneConnection() {
        BluetoothGattCallback liteCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {}

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {}
        };

        if (liteBluetooth.isConnectingOrConnected()) {
            liteBluetooth.addGattCallback(liteCallback);
            liteBluetooth.removeGattCallback(liteCallback);
        }
    }

    /**
     * refresh bluetooth device cache
     */
    private void refreshDeviceCache() {
        liteBluetooth.refreshDeviceCache();
    }


    /**
     * close connection
     */
    private void closeBluetoothGatt() {
        if (liteBluetooth.isConnectingOrConnected()) {
            liteBluetooth.closeBluetoothGatt();
        }
    }

    /**
     * write data to characteristic
     */
    private void writeDataToCharacteristic() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_WRITE, null)
                 .writeCharacteristic(new byte[]{1, 2, 3}, new BleCharactCallback() {
                     @Override
                     public void onSuccess(BluetoothGattCharacteristic characteristic) {
                         BleLog.i(TAG, "Write Success, DATA: " + Arrays.toString(characteristic.getValue()));
                     }

                     @Override
                     public void onFailure(BleException exception) {
                         BleLog.i(TAG, "Write failure: " + exception);
                         bleExceptionHandler.handleException(exception);
                     }
                 });
    }

    /**
     * write data to descriptor
     */
    private void writeDataToDescriptor() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_WRITE, UUID_DESCRIPTOR_WRITE)
                 .writeDescriptor(new byte[]{1, 2, 3}, new BleDescriptorCallback() {
                     @Override
                     public void onSuccess(BluetoothGattDescriptor descriptor) {
                         BleLog.i(TAG, "Write Success, DATA: " + Arrays.toString(descriptor.getValue()));
                     }

                     @Override
                     public void onFailure(BleException exception) {
                         BleLog.i(TAG, "Write failure: " + exception);
                         bleExceptionHandler.handleException(exception);
                     }
                 });
    }

    /**
     * read data from characteristic
     */
    private void readDataFromCharacteristic() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_READ, null)
                 .readCharacteristic(new BleCharactCallback() {
                     @Override
                     public void onSuccess(BluetoothGattCharacteristic characteristic) {
                         BleLog.i(TAG, "Read Success, DATA: " + Arrays.toString(characteristic.getValue()));
                     }

                     @Override
                     public void onFailure(BleException exception) {
                         BleLog.i(TAG, "Read failure: " + exception);
                         bleExceptionHandler.handleException(exception);
                     }
                 });
    }

    /**
     * read data from descriptor
     */
    private void readDataFromDescriptor() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_READ, UUID_DESCRIPTOR_READ)
                 .readDescriptor(new BleDescriptorCallback() {
                     @Override
                     public void onSuccess(BluetoothGattDescriptor descriptor) {
                         BleLog.i(TAG, "Read Success, DATA: " + Arrays.toString(descriptor.getValue()));
                     }

                     @Override
                     public void onFailure(BleException exception) {
                         BleLog.i(TAG, "Read failure : " + exception);
                         bleExceptionHandler.handleException(exception);
                     }
                 });
    }

    /**
     * enble notification of characteristic
     */
    private void enableNotificationOfCharacteristic() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_READ, null)
                 .enableCharacteristicNotification(new BleCharactCallback() {
                     @Override
                     public void onSuccess(BluetoothGattCharacteristic characteristic) {
                         BleLog.i(TAG, "Notification characteristic Success, DATA: " + Arrays
                                 .toString(characteristic.getValue()));
                     }

                     @Override
                     public void onFailure(BleException exception) {
                         BleLog.i(TAG, "Notification characteristic failure: " + exception);
                         bleExceptionHandler.handleException(exception);
                     }
                 });
    }

    /**
     * enable notification of descriptor
     */
    private void enableNotificationOfDescriptor() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_READ, UUID_DESCRIPTOR_READ)
                 .enableDescriptorNotification(new BleDescriptorCallback() {
                     @Override
                     public void onSuccess(BluetoothGattDescriptor descriptor) {
                         BleLog.i(TAG,
                                 "Notification descriptor Success, DATA: " + Arrays.toString(descriptor.getValue()));
                     }

                     @Override
                     public void onFailure(BleException exception) {
                         BleLog.i(TAG, "Notification descriptor failure : " + exception);
                         bleExceptionHandler.handleException(exception);
                     }
                 });
    }


    /**
     * read RSSI of device
     */
    public void readRssiOfDevice() {
        liteBluetooth.newBleConnector()
                     .readRemoteRssi(new BleRssiCallback() {
                         @Override
                         public void onSuccess(int rssi) {
                             BleLog.i(TAG, "Read Success, rssi: " + rssi);
                         }

                         @Override
                         public void onFailure(BleException exception) {
                             BleLog.i(TAG, "Read failure : " + exception);
                             bleExceptionHandler.handleException(exception);
                         }
                     });
    }

    public void dialogShow(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Lite BLE");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
