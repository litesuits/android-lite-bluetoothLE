package com.litesuits.bluetooth.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import com.litesuits.bluetooth.conn.TimeoutCallback;
import com.litesuits.bluetooth.log.BleLog;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author MaTianyu
 * @date 2015-03-12
 */
public class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";

    public static void printServices(BluetoothGatt gatt) {
        if (gatt != null) {
            for (BluetoothGattService service : gatt.getServices()) {
                BleLog.i(TAG, "service: " + service.getUuid());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    BleLog.d(TAG, "  characteristic: " + characteristic.getUuid() + " value: " + Arrays.toString(characteristic.getValue()));
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        BleLog.v(TAG, "        descriptor: " + descriptor.getUuid() + " value: " + Arrays.toString(descriptor.getValue()));
                    }
                }
            }
        }
    }

    /*------------  BluetoothGatt  ------------ */
    public static void closeBluetoothGatt(BluetoothGatt gatt) {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    /*------------  Service  ------------ */
    public static BluetoothGattService getService(BluetoothGatt gatt, String serviceUUID) {
        return gatt.getService(UUID.fromString(serviceUUID));
    }

    /*------------  Characteristic服务  ------------ */
    public static BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String charactUUID) {
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

    public static BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, String serviceUUID, String charactUUID) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUUID));
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(charactUUID));
        }
        return null;
    }

    public static boolean characteristicWrite(BluetoothGatt gatt, String serviceUUID, String charactUUID, String hex, TimeoutCallback callback) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, serviceUUID, charactUUID);
        return characteristicWrite(gatt, characteristic, hex, callback);
    }

    public static boolean characteristicWrite(BluetoothGatt gatt, String serviceUUID, String charactUUID, byte[] data, TimeoutCallback callback) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, serviceUUID, charactUUID);
        return characteristicWrite(gatt, characteristic, data, callback);
    }

    public static boolean characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic charact, String hex, TimeoutCallback callback) {
        if (hex == null) {
            return false;
        }
        return characteristicWrite(gatt, charact, HexUtil.decodeHex(hex.toCharArray()), callback);
    }

    public static boolean characteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic charact, byte[] data, TimeoutCallback callback) {
        if (charact != null) {
            charact.setValue(data);
            gatt.writeCharacteristic(charact);
            return true;
        } else {
            BleLog.e(TAG, "Characteristic 为空");
            return false;
        }
    }

    public static boolean enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic cha2App,
                                                    String descriptorUUID) {
        if (cha2App != null) {
            BleLog.i(TAG, "cha2APP enable notification : " + cha2App.getUuid());
            //if ((cha2App.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            //    Log.i(TAG, "直接可读 readCharacteristic ");
            //    gatt.readCharacteristic(cha2App);
            //}
            if ((cha2App.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                BleLog.i(TAG, "支持通知 readCharacteristic ");
                gatt.setCharacteristicNotification(cha2App, true);
                BluetoothGattDescriptor descriptor = cha2App.getDescriptor(UUID.fromString(descriptorUUID));
                if (descriptor != null) {
                    BleLog.i(TAG, "开启通知 readCharacteristic ");
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    return gatt.writeDescriptor(descriptor);
                }
            } else {
                BleLog.i(TAG, "该通道无内容读取！");
            }
        }
        return false;
    }
}
