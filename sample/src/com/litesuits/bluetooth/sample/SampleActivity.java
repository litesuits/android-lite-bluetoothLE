package com.litesuits.bluetooth.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.widget.Toast;
import com.litesuits.bluetooth.BluetoothHelper;
import com.litesuits.bluetooth.LiteBleConnector;
import com.litesuits.bluetooth.LiteBluetooth;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;

import java.util.Arrays;

public class SampleActivity extends Activity {

    /**
     * 蓝牙主要操作对象，建议单例。
     */
    private static LiteBluetooth liteBluetooth;
    private static int TIME_OUT = 10000;
    private static String MAC = "00:00:00:AA:AA:AA";
    private Activity activity;

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
        //final BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        //    @Override
        //    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //
        //    }
        //};
        //liteBluetooth.startLeScan(callback);
        //HandlerUtil.runOnUiThreadDelay(new Runnable() {
        //    @Override
        //    public void run() {
        //        liteBluetooth.stopScan(callback);
        //    }
        //}, 30000);
        //
        //liteBluetooth.startLeScan(new PeriodScanCallback(30000) {
        //    @Override
        //    public void onScanTimeout() {
        //
        //    }
        //
        //    @Override
        //    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //
        //    }
        //});
        //
        //liteBluetooth.startLeScan(new PeriodMacScanCallback("00:00:00:AA:AA:AA", 30000) {
        //    @Override
        //    public void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //
        //    }
        //
        //    @Override
        //    public void onScanTimeout() {
        //
        //    }
        //});
        //
        //ConnectListener connectListener = new ConnectListener() {
        //
        //    @Override
        //    public void onStateChanged(ConnectState state) {
        //
        //    }
        //
        //    @Override
        //    public void onFailed(ConnectError error) {
        //
        //    }
        //
        //    @Override
        //    public void onServicesDiscovered(BluetoothGatt gatt) {
        //        // 初始化BluetoothHelper
        //        bluetoothHelper = new BluetoothHelper(bluetoothGatt);
        //    }
        //
        //    @Override
        //    public void onCharacteristicWrite(BluetoothGatt gatt,
        //                                      BluetoothGattCharacteristic characteristic,
        //                                      int status) {
        //
        //    }
        //
        //    @Override
        //    public void onCharacteristicChanged(BluetoothGatt gatt,
        //                                        BluetoothGattCharacteristic characteristic) {
        //
        //    }
        //};
        //// mac和服务uuid纯属测试，测试时请替换真实参数。
        //liteBluetooth.connect("00:00:00:AA:AA:AA", false, connectListener);
    }

    BluetoothHelper bluetoothHelper;

    // mac和服务uuid纯属测试，测试时请替换真实参数。
    public String UUID_SERVICE = "6e400000-0000-0000-0000-000011112222";
    public String UUID_CHAR_SEND = "6e400001-0000-0000-0000-000011112222";
    public String UUID_CHAR_RECEIVE = "6e400002-0000-0000-0000-000011112222";
    public String UUID_DESCRIPTOR = "00002902-0000-1000-8000-000011112222";
    protected BluetoothGattCharacteristic charSend;
    protected BluetoothGattCharacteristic charReceive;

    public void readDataOfDevice() {

    }


    private void scanDevicesPeriod() {
        liteBluetooth.startLeScan(new PeriodScanCallback(TIME_OUT) {
            @Override
            public void onScanTimeout() {
                dialogShow(TIME_OUT + "毫秒 扫描结束");
            }

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            }
        });
    }

    private void scanSpecifiedDevicePeriod() {
        liteBluetooth.startLeScan(new PeriodMacScanCallback(MAC, TIME_OUT) {

            @Override
            public void onScanTimeout() {
                dialogShow(TIME_OUT + "毫秒扫描结束，未发现设备");
            }

            @Override
            public void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
                dialogShow("发现设备 " + device.getName() + " MAC: " + device.getAddress()
                           + " \n RSSI: " + rssi + " records:" + Arrays.toString(scanRecord));
            }
        });
    }

    private void scanAndConnect() {
        liteBluetooth.startLeScan(new PeriodMacScanCallback(MAC, TIME_OUT) {

            @Override
            public void onScanTimeout() {
                dialogShow(TIME_OUT + "毫秒扫描结束，未发现设备");
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                Toast.makeText(activity, "发现 " + device.getAddress() + " 正在连接...", Toast.LENGTH_LONG).show();
                liteBluetooth.connect(device, false, new LiteBluetooth.LiteGattCallback() {
                    @Override
                    public void onConnectSuccess(BluetoothGatt gatt, int status, int newState) {
                        gatt.discoverServices();
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        BluetoothUtil.printServices(gatt);
                    }

                    @Override
                    public void onConnectFailure(BluetoothGatt gatt, int status, int newState) {
                        dialogShow(device.getAddress() + " 设备连接失败");
                    }
                });

            }
        });
    }

    private void writeData() {
        LiteBleConnector connector = liteBluetooth.newBleConnector();
        connector.withUUIDString(UUID_SERVICE, UUID_CHAR_SEND, null)
                 .writeCharacteristic(new byte[]{1, 2, 3}, new LiteBleConnector.Callback() {

                     @Override
                     public void onSuccess() {

                     }

                     @Override
                     public void onFailure() {

                     }
                 });
    }

    private void readData() {
    }


    public void readRssiOfDevice() {
    }

    public void dialogShow(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Lite BLE");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
