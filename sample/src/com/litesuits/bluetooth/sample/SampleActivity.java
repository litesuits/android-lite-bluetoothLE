package com.litesuits.bluetooth.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import com.litesuits.bluetooth.BluetoothHelper;
import com.litesuits.bluetooth.LiteBluetooth;
import com.litesuits.bluetooth.conn.ConnectError;
import com.litesuits.bluetooth.conn.ConnectListener;
import com.litesuits.bluetooth.conn.ConnectState;
import com.litesuits.bluetooth.conn.TimeoutCallback;
import com.litesuits.bluetooth.scan.PeriodMacScanCallback;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.HandlerUtil;

public class SampleActivity extends Activity {

    /**
     * 蓝牙主要操作对象，建议单例。
     */
    private static LiteBluetooth liteBluetooth;
    private static int TIME_OUT = 30000;
    private static String mac = "00:00:00:AA:AA:AA";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (liteBluetooth == null) {
            liteBluetooth = new LiteBluetooth(this);
        }

        final BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            }
        };
        liteBluetooth.startScan(callback);
        HandlerUtil.runOnUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                liteBluetooth.stopScan(callback);
            }
        }, 30000);

        liteBluetooth.startScan(new PeriodScanCallback(30000) {
            @Override
            public void onScanTimeout() {

            }

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            }
        });

        liteBluetooth.startScan(new PeriodMacScanCallback("00:00:00:AA:AA:AA", 30000) {
            @Override
            public void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {

            }

            @Override
            public void onScanTimeout() {

            }
        });

        ConnectListener connectListener = new ConnectListener() {

            @Override
            public void onStateChanged(ConnectState state) {

            }

            @Override
            public void onFailed(ConnectError error) {

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt) {
                bluetoothHelper = new BluetoothHelper(bluetoothGatt);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              int status) {

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {

            }
        };
        // mac和服务uuid纯属测试，测试时请替换真实参数。
        liteBluetooth.connect("00:00:00:AA:AA:AA", false, connectListener);


    }

    BluetoothHelper bluetoothHelper;

    // mac和服务uuid纯属测试，测试时请替换真实参数。
    public String UUID_SERVICE = "6e400000-0000-0000-0000-000011112222";
    public String UUID_CHAR_SEND = "6e400001-0000-0000-0000-000011112222";
    public String UUID_CHAR_RECEIVE = "6e400002-0000-0000-0000-000011112222";
    public String UUID_DESCRIPTOR = "00002902-0000-1000-8000-000011112222";
    protected BluetoothGattCharacteristic charSend;
    protected BluetoothGattCharacteristic charReceive;

    public void sendDataToDevice() {
        if (bluetoothHelper != null) {
            BluetoothGatt gatt = bluetoothHelper.getBluetoothGatt();
            charSend = bluetoothHelper.getCharacteristic(UUID_SERVICE, UUID_CHAR_SEND);
            bluetoothHelper.characteristicWrite(charSend, "00EECC88", new TimeoutCallback(gatt, 3000) {
                @Override
                public void onTimeout(BluetoothGatt gatt) {

                }
            });
            charReceive = bluetoothHelper.getCharacteristic(gatt, UUID_SERVICE, UUID_CHAR_RECEIVE);
        }
    }

    public void readDataOfDevice() {

    }

    public void readRssiOfDevice() {
        BluetoothGatt bluetoothGatt = bluetoothHelper.getBluetoothGatt();
    }
}
