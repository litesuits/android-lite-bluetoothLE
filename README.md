# LiteBle: Android Bluetooth Framework

Extremely simple! Based on callback.

Communication with BluetoothLE(BLE) device as easy as HTTP communication.

##Usage

1. scan device
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

2. scan and connect 
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


3. write data to characteritic 
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

4. write data to descriptor 
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

5. read data from characteritic 
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

6. enable notification of characteristic
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

7. enable notification of descriptor
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

8. read RSSI of device
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


##More Detail

---
Website : http://litesuits.com

Email   : litesuits@qq.com

QQgroup : [47357508][1] , [42960650][2]


[1]: http://shang.qq.com/wpa/qunwpa?idkey=492d63aaffb04b23d8dc4df21f6b594008cbe1a819978659cddab2dbc397684e
[2]: http://shang.qq.com/wpa/qunwpa?idkey=19bf15b9c85ec15c62141dd00618f725e2983803cd2b48566fa0e94964ae8370
  