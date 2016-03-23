## LiteBle: Android Bluetooth Framework
Extremely simple! Based on callback.
Communication with BluetoothLE(BLE) device as easy as HTTP communication.
One Device, One connection, One LiteBluetooth Instance.

But One connection can has many callback:
One LiteBluetooth Instance can add many BluetoothGattCallback.

##Usage

###1. scan device
```java
private static int TIME_OUT_SCAN = 10000;
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
```

###2. scan and connect 
```java
private static String MAC = "00:00:00:AA:AA:AA";
liteBluetooth.scanAndConnect(MAC, false, new BleGattCallback() {

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
```


###3. get state of litebluetooth
```java
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
```

###4. add(remove) new callback to an existing connection.
```java
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
```

###5. refresh bluetooth device cache 
```java
liteBluetooth.refreshDeviceCache();
```

###6. close connection
```java
if (liteBluetooth.isConnectingOrConnected()) {
    liteBluetooth.closeBluetoothGatt();
}
```


###7. write data to characteritic 
```java
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
```

###8. write data to descriptor 
```java
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
```

###9. read data from characteritic 
```java
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
```

###10. enable notification of characteristic
```java
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
```

###11. enable notification of descriptor
```java
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
```

###12. read RSSI of device
```java
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
```

##More Detail, See The Sample

---
Website : http://litesuits.com

Email   : litesuits@qq.com

QQgroup : [42960650][1] , [47357508][2] 

[1]: http://shang.qq.com/wpa/qunwpa?idkey=19bf15b9c85ec15c62141dd00618f725e2983803cd2b48566fa0e94964ae8370
[2]: http://shang.qq.com/wpa/qunwpa?idkey=492d63aaffb04b23d8dc4df21f6b594008cbe1a819978659cddab2dbc397684e
