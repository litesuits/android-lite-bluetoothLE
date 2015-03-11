package com.litesuits.bluetooth;//package com.litesuits.ble;
//
//import android.com.litesuits.bluetooth.BluetoothAdapter;
//import android.com.litesuits.bluetooth.BluetoothDevice;
//import android.util.Log;
//import com.litesuits.ble.utils.ByteUtil;
//import com.litesuits.ble.utils.HexUtil;
//
//import java.util.Arrays;
//
///**
// * @author MaTianyu
// * @date 2015-01-15
// */
//public class BleScanner {
//    private static final String TAG = "BleScanner";
//
//    class Callback extends BluetoothAdapter.LeScanCallback {
//
//        @Override
//        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            Log.i(TAG, "device: " + device.getName() + " , rssi: " + "" + rssi + " ," +
//                    "\nscanRecord byt: " + Arrays.toString(scanRecord) + " , " +
//                    "\nscanRecord hex: " + HexUtil.encodeHexStr(scanRecord) + " , " +
//                    "\nscanRecord bit: " + ByteUtil.byteToBit(scanRecord) + " , " +
//                    "\nmac: " + device.getAddress() + ", " + "type: " + device.getType() + " ," +
//                    "bondState: " + device.getBondState() + " , " +
//                    " , uuids: " + Arrays.toString(device.getUuids()));
//            String hex = "";
//            if (scanRecord != null) for (byte b : scanRecord) {
//                hex += Integer.toHexString(b);
//            }
//            Log.i(TAG, "____scanRecord: " + hex);
//            if (!isConnecting && device.getName().equals("aaaa")) {
//                stopScan();
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        isConnecting = true;
//                        startConnect(device);
//                    }
//                }, 3000);
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        stopConnection();
//                    }
//                }, 15000);
//            }
//        }
//    }
//}
