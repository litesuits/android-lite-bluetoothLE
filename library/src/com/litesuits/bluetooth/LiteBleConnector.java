package com.litesuits.bluetooth;

import android.bluetooth.*;
import android.os.Handler;
import android.os.Looper;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.utils.HexUtil;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author MaTianyu @http://litesuits.com
 * @date 2015-10-31
 */
public class LiteBleConnector {
    private static final String TAG = LiteBleConnector.class.getSimpleName();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattDescriptor descriptor;
    private LiteBluetooth liteBluetooth;
    private Handler handler;
    private int timeOutMillis = 30000;


    public LiteBleConnector(LiteBluetooth liteBluetooth) {
        this.liteBluetooth = liteBluetooth;
        this.bluetoothGatt = liteBluetooth.getBluetoothGatt();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public LiteBleConnector(LiteBluetooth liteBluetooth, BluetoothGattService service,
                            BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor) {
        this(liteBluetooth);
        this.service = service;
        this.characteristic = characteristic;
        this.descriptor = descriptor;
    }

    public LiteBleConnector(LiteBluetooth liteBluetooth,
                            UUID serviceUUID, UUID charactUUID, UUID descriptorUUID) {
        this(liteBluetooth);
        withUUID(serviceUUID, charactUUID, descriptorUUID);
    }

    public LiteBleConnector(LiteBluetooth liteBluetooth,
                            String serviceUUID, String charactUUID, String descriptorUUID) {
        this(liteBluetooth);
        withUUIDString(serviceUUID, charactUUID, descriptorUUID);
    }

    public LiteBleConnector withUUID(UUID serviceUUID, UUID charactUUID, UUID descriptorUUID) {
        if (serviceUUID != null && bluetoothGatt != null) {
            service = bluetoothGatt.getService(serviceUUID);
        }
        if (service != null && charactUUID != null) {
            characteristic = service.getCharacteristic(charactUUID);
        }
        if (characteristic != null && descriptorUUID != null) {
            descriptor = characteristic.getDescriptor(descriptorUUID);
        }
        return this;
    }

    public LiteBleConnector withUUIDString(String serviceUUID, String charactUUID, String descriptorUUID) {
        return withUUID(formUUID(serviceUUID), formUUID(charactUUID), formUUID(descriptorUUID));
    }

    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    public static abstract class Callback {
        public void onInitiatedSuccess() {}

        public abstract void onSuccess();

        public abstract void onFailure();
    }

    // _____________________ main operation _____________________
    public boolean writeCharacteristic(byte[] data, Callback callback) {
        return writeCharacteristic(getCharacteristic(), data, callback);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic charact, byte[] data, final Callback callback) {
        if (BleLog.isPrint) {
            BleLog.i(TAG, charact.getUuid() + " characteristic write bytes: "
                          + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        }
        if (callback != null) {
            final Runnable timeOut = new Runnable() {
                @Override
                public void run() {
                    callback.onFailure();
                    liteBluetooth.removeGattCallback(call);
                }
            };
            final BluetoothGattCallback call = new BluetoothGattCallback() {
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    handler.removeCallbacks(timeOut);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure();
                    }
                    v p
                }
            };
            liteBluetooth.addGattCallback(call);
            handler.postDelayed(timeOut, timeOutMillis);
        }
        charact.setValue(data);
        return handleAfterInitialed(getBluetoothGatt().writeCharacteristic(charact), callback);
    }

    public boolean writeDescriptor(byte[] data, Callback callback) {
        return writeDescriptor(getDescriptor(), data, callback);
    }

    public boolean writeDescriptor(BluetoothGattDescriptor descriptor, byte[] data, Callback callback) {
        if (BleLog.isPrint) {
            BleLog.i(TAG, descriptor.getUuid() + " descriptor write bytes: "
                          + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        }
        descriptor.setValue(data);
        return handleAfterInitialed(getBluetoothGatt().writeDescriptor(descriptor), callback);
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic charact, Callback callback) {
        return handleAfterInitialed(getBluetoothGatt().readCharacteristic(charact), callback);
    }

    public boolean readDescriptor(Callback callback) {
        return readDescriptor(getDescriptor(), callback);
    }

    public boolean readDescriptor(BluetoothGattDescriptor descriptor, Callback callback) {
        return handleAfterInitialed(getBluetoothGatt().readDescriptor(descriptor), callback);
    }

    public boolean readRemoteRssi(Callback callback) {
        return handleAfterInitialed(getBluetoothGatt().readRemoteRssi(), callback);
    }

    private boolean handleBeforeOperation(BluetoothGattCharacteristic characteristic,
                                          byte[] bytes, Callback callback) {
        if (callback == null) {
            return characteristic != null && bytes != null;
        } else {
            if (characteristic == null) {
                callback.onFailure();
                return false;
            } else if (bytes == null) {
                callback.onFailure();
                return false;
            }
        }
        return true;
    }

    private boolean handleAfterInitialed(boolean initiated, Callback callback) {
        if (callback != null) {
            if (initiated) {
                callback.onInitiatedSuccess();
            } else {
                callback.onFailure();
            }
        }
        return initiated;
    }

    public boolean setNotification(boolean enable) {
        return setNotification(getBluetoothGatt(), getCharacteristic(), getDescriptor(), enable);
    }

    public boolean setNotification(BluetoothGatt gatt,
                                   BluetoothGattCharacteristic characteristic,
                                   BluetoothGattDescriptor descriptor, boolean enable) {
        return setCharacteristicNotification(gatt, characteristic, enable)
               && setDescriptorNotification(gatt, descriptor, enable);
    }

    public boolean setCharacteristicNotification(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean enable) {
        if (gatt != null && characteristic != null) {
            if ((characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                BleLog.i(TAG, "characteristic support notification");
                gatt.setCharacteristicNotification(characteristic, enable);
            } else {
                BleLog.e(TAG, "characteristic do not support notification");
            }
        }
        return false;
    }

    public boolean setDescriptorNotification(BluetoothGatt gatt,
                                             BluetoothGattDescriptor descriptor,
                                             boolean enable) {
        if (gatt != null && descriptor != null) {
            BleLog.i(TAG, "开启通知 readCharacteristic ");
            if (enable) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    // _____________________ getter and setter _____________________
    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public LiteBleConnector setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
        return this;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public LiteBleConnector setService(BluetoothGattService service) {
        this.service = service;
        return this;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public LiteBleConnector setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        return this;
    }

    public BluetoothGattDescriptor getDescriptor() {
        return descriptor;
    }

    public LiteBleConnector setDescriptor(BluetoothGattDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public int getTimeOutMillis() {
        return timeOutMillis;
    }

    public LiteBleConnector setTimeOutMillis(int timeOutMillis) {
        this.timeOutMillis = timeOutMillis;
        return this;
    }
}
