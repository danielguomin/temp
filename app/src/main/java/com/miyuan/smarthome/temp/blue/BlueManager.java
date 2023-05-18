package com.miyuan.smarthome.temp.blue;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;

import com.miyuan.smarthome.temp.db.CurrentTemp;
import com.miyuan.smarthome.temp.db.HistoryTemp;
import com.miyuan.smarthome.temp.db.Member;
import com.miyuan.smarthome.temp.db.TempInfo;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.utils.SingleLiveData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by guomin on 2018/3/8.
 */
public class BlueManager {

    public static final String KEY_WRITE_BUNDLE_STATUS = "write_status";
    public static final String KEY_WRITE_BUNDLE_VALUE = "write_value";
    private static final int STOP_SCAN_AND_CONNECT = 0;
    private static final int MSG_SPLIT_WRITE = 1;
    private static final int MSG_OBD_DISCONNECTED = 12;


    private static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String READ_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final int CONNECTED = 1; // 连接成功
    private static final int DISCONNECTED = 0; // 断开连接
    private static final long COMMAND_TIMEOUT = 15000;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mContext;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mWorkerThread;
    private Handler mHandler;

    private static SingleLiveData<Integer> _connectStatusLiveData = new SingleLiveData<>();
    public static LiveData<Integer> connectStatusLiveData = _connectStatusLiveData;
    private static SingleLiveData<TempInfo> _tempInfoLiveData = new SingleLiveData<>();
    public static LiveData<TempInfo> tempInfoLiveData = _tempInfoLiveData;
    private static SingleLiveData<CurrentTemp> _currentTempLiveData = new SingleLiveData<>();
    public static LiveData<CurrentTemp> currentTempLiveData = _currentTempLiveData;
    private static SingleLiveData<HistoryTemp> _historyTempLiveData = new SingleLiveData<>();
    public static LiveData<HistoryTemp> historyTempLiveData = _historyTempLiveData;
    private static SingleLiveData<Boolean> _membearLiveData = new SingleLiveData<>();
    public static LiveData<Boolean> membearLiveData = _membearLiveData;

    private int connectStatus;
    private boolean isScaning = false;
    private volatile boolean canGo = true;
    private ArrayList<String> scanResult = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        /**
         *
         * @param device    扫描到的设备
         * @param rssi
         * @param scanRecord
         */
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, final byte[] scanRecord) {
            if (null == device) {
                return;
            }
            String name = device.getName();
            if (scanResult.contains(name)) {
                return;
            }
            Log.d("device.getName()=    " + device.getName() + " device.getAddress()=" + device.getAddress());
            scanResult.add(name);
            if (name != null && name.startsWith("MYobd")) {
                Message msg = mHandler.obtainMessage();
                msg.what = STOP_SCAN_AND_CONNECT;
                msg.obj = device.getAddress();
                mHandler.sendMessage(msg);
            }
        }
    };
    private volatile boolean split;
    private byte[] mData;
    private int mCount = 20;
    private Queue<byte[]> mDataQueue;
    private TimeOutThread timeOutThread;
    private BleWriteCallback bleWriteCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] justWrite) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            write();
        }

        @Override
        public void onWriteFailure(byte[] date) {
            // 重新发送
            realWrite(date);
        }
    };
    private BluetoothManager bluetoothManager;
    private byte[] currentProtocol;
    private volatile int currentRepeat = 0;
    private int repeat = 0;
    private LinkedList<byte[]> instructList;
    /**
     * 待发送指令
     */
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue(1);
    /**
     * 上一包是否接受完成
     */
    private boolean unfinish = true;
    /**
     * 完整包
     */
    private byte[] full;
    private int currentIndex;
    private int count;
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("getConnectionState " + status + "   " + newState);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("onConnectionStateChange  STATE_CONNECTED");
                    canGo = true;
                    connectStatus = CONNECTED;
                    mBluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("onConnectionStateChange  STATE_DISCONNECTED");
                    connectStatus = DISCONNECTED;
                    Message message = new Message();
                    message.what = MSG_OBD_DISCONNECTED;
                    mHandler.sendMessage(message);
                    disconnect();
                }
            } else {
                connectStatus = DISCONNECTED;
                Message message = new Message();
                message.what = MSG_OBD_DISCONNECTED;
                mHandler.sendMessage(message);
                disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("onServicesDiscovered  " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                //拿到该服务 1,通过UUID拿到指定的服务  2,可以拿到该设备上所有服务的集合
                List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();

                //2.通过指定的UUID拿到设备中的服务也可使用在发现服务回调中保存的服务
                BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
//
                //3.通过指定的UUID拿到设备中的服务中的characteristic，也可以使用在发现服务回调中通过遍历服务中信息保存的Characteristic
                writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITE_UUID));
                readCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(NOTIFY_UUID));

                mBluetoothGatt.setCharacteristicNotification(readCharacteristic, true);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        _connectStatusLiveData.setValue(BoxEvent.BLUE_CONNECTED);
//                        notifyBleCallBackListener(BoxEvent.BLUE_CONNECTED, null);
                    }
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("OBD->APP  " + HexUtils.byte2HexStr(characteristic.getValue()));
            analyzeProtocol(characteristic.getValue());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("onCharacteristicRead  success " + Arrays.toString(characteristic.getValue()));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Message message = new Message();
            message.what = MSG_SPLIT_WRITE;
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_WRITE_BUNDLE_STATUS, status);
            bundle.putByteArray(KEY_WRITE_BUNDLE_VALUE, characteristic.getValue());
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    };

    private BlueManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sentToBox();
            }
        }, "sendMessage").start();
        timeOutThread = new TimeOutThread();
        timeOutThread.start();
    }

    public static BlueManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * is support ble?
     *
     * @return
     */
    boolean isSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && mContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @SuppressLint("ServiceCast")
    @MainThread
    public void init(Activity activity) {
        mContext = activity;
        if (isSupportBle()) {
            bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(mContext, "Device does not support Bluetooth", Toast.LENGTH_LONG);
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        mWorkerThread = new HandlerThread(BlueManager.class.getSimpleName());
        mWorkerThread.start();
        mHandler = new WorkerHandler(mWorkerThread.getLooper());

        startScan();
    }

    public synchronized void startScan() {
        if (null == mBluetoothAdapter || isScaning) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        isScaning = true;

        scanResult.clear();

        mBluetoothAdapter.startLeScan(leScanCallback);

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan(false);
            }
        }, 1000 * 60);

    }

    public synchronized void stopScan(boolean find) {
        if (null == mBluetoothAdapter || !isScaning) {
            return;
        }
        isScaning = false;
        if (!find) {
            _connectStatusLiveData.setValue(BoxEvent.BLUE_SCAN_FAILED);
        } else {
            _connectStatusLiveData.setValue(BoxEvent.BLUE_SCAN_FINISHED);
        }
//        notifyBleCallBackListener(BoxEvent.BLUE_SCAN_FINISHED, find);
        mBluetoothAdapter.stopLeScan(leScanCallback);
    }

    void connect(String address) {

        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);

        if (bluetoothDevice == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = bluetoothDevice.connectGatt(mContext,
                    false, bluetoothGattCallback, TRANSPORT_LE);
        } else {
            mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, bluetoothGattCallback);
        }
    }

    /**
     * 断开链接
     */
    public synchronized void disconnect() {

        timeOutThread.endCommand();

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mBluetoothGatt != null) {
                refresh.invoke(mBluetoothGatt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean isConnected() {
        return connectStatus == 1;
    }

    /**
     * 发送指令
     *
     * @param data
     */
    public synchronized void send(byte[] data) {
        // 判断该指令是否和待发送队列中最后一个指令相同，如果相同则不放入，不相同则加入，判断date中第2、3位是否一致既可

        if (instructList == null) {
            instructList = new LinkedList<>();
            queue.add(data);
            return;
        }

        if (queue.size() == 0 && canGo) {
            queue.add(data);
            return;
        }

        byte[] last = instructList.pollLast();
        if (last != null && data[1] == last[1] && data[2] == last[2]) {
            return;
        }
        instructList.addLast(data);
    }

    /**
     * 蓝牙通信
     */
    private void sentToBox() {
        while (true) {
            try {
                byte[] message = queue.take();
                write(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    private synchronized void write(byte[] data) {
        if (null == writeCharacteristic) {
            return;
        }

        boolean reset = true;

        if (null != currentProtocol && currentProtocol.length == data.length) {
            for (int i = 0; i < currentProtocol.length; i++) {
                if (currentProtocol[i] != data[i]) {
                    reset = false;
                    break;
                }
            }
            if (!reset) {
                repeat = 0;
            }
        } else {
            repeat = 0;
        }

        currentProtocol = new byte[data.length];

        System.arraycopy(data, 0, currentProtocol, 0, data.length);

        if (data.length > 20) {
            split = true;
            mData = data;
            splitWrite();
        } else {
            split = false;
            realWrite(data);
        }
    }

    private void realWrite(byte[] data) {
        canGo = false;
        Log.d("APP->OBD " + HexUtils.byte2HexStr(data));
        if (mBluetoothGatt == null) {
            return;
        }

        if (split || (data[1] == (byte) 0x89 && data[2] == 01) || (data[1] == (byte) 0x88 && data[2] == 03)) { // 未拆封包 或者 心跳包
        } else {
            timeOutThread.startCommand(true);
        }
        writeCharacteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    private Queue<byte[]> splitByte(byte[] data, int count) {
        Queue<byte[]> byteQueue = new LinkedList<>();
        if (data != null) {
            int index = 0;
            do {
                byte[] rawData = new byte[data.length - index];
                byte[] newData;
                System.arraycopy(data, index, rawData, 0, data.length - index);
                if (rawData.length <= count) {
                    newData = new byte[rawData.length];
                    System.arraycopy(rawData, 0, newData, 0, rawData.length);
                    index += rawData.length;
                } else {
                    newData = new byte[count];
                    System.arraycopy(data, index, newData, 0, count);
                    index += count;
                }
                byteQueue.offer(newData);
            } while (index < data.length);
        }
        return byteQueue;
    }

    private void splitWrite() {
        if (mData == null) {
            throw new IllegalArgumentException("data is Null!");
        }
        if (mCount < 1) {
            throw new IllegalArgumentException("split count should higher than 0!");
        }
        mDataQueue = splitByte(mData, mCount);
        write();
    }

    private void write() {
        if (mDataQueue.peek() == null) {
            timeOutThread.startCommand(true);
            return;
        } else {
            byte[] data = mDataQueue.poll();
            realWrite(data);
        }
    }

    /**
     * 接受数据
     *
     * @param data
     */
    public synchronized void analyzeProtocol(byte[] data) {
        Log.d(" analyzeProtocol");
        if (null != data && data.length > 0) {
            if (data[0] == ProtocolUtils.PROTOCOL_HEAD_TAIL && data.length != 1 && unfinish && data.length >= 7) {
                // 获取包长度
                byte[] len = new byte[]{data[4], data[3]};
                count = byteToShort(len);
                if (data.length == count + 7) {  //为完整一包
                    full = new byte[count + 5];
                    System.arraycopy(data, 1, full, 0, full.length);
                    validateAndNotify(full);
                } else if (data.length < count + 7) {
                    unfinish = false;
                    full = new byte[count + 5];
                    currentIndex = data.length - 1;
                    System.arraycopy(data, 1, full, 0, data.length - 1);
                } else if (data.length > count + 7) {
                    Log.d(" analyzeProtocol error one ");
                    currentIndex = 0;
                    unfinish = true;
                    full = new byte[]{};
                    return;
                }
            } else {
                if ((currentIndex + data.length - 1) == count + 5) { // 最后一包
                    unfinish = true;
                    System.arraycopy(data, 0, full, currentIndex, data.length - 1);
                    validateAndNotify(full);
                } else if ((currentIndex + data.length - 1) < count + 5) { // 包不完整
                    // 未完成
                    System.arraycopy(data, 0, full, currentIndex, data.length);
                    currentIndex += data.length;
                } else {
                    Log.d(" analyzeProtocol error two ");
                    currentIndex = 0;
                    unfinish = true;
                    full = new byte[]{};
                    return;
                }
            }
        }
    }

    public short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * @param res
     */
    private void validateAndNotify(byte[] res) {
        Log.d(" validateAndNotify");
        if (!(res[0] == (byte) 0x09 && res[1] == 01 || res[0] == (byte) 0x08 && res[1] == 03)) {
            timeOutThread.endCommand();
            byte[] msg = instructList.pollLast();
            canGo = true;
            if (msg != null && queue.size() == 0) {
                queue.add(msg);
            }
        }

        byte[] result = new byte[res.length];
        System.arraycopy(res, 0, result, 0, res.length);

        int cr = result[0];
        for (int i = 1; i < result.length - 1; i++) {
            cr = cr ^ result[i];
        }
        if (cr != result[result.length - 1]) {
            result = null;
        } else {
            try {
                byte[] content = new byte[result.length - 1];
                System.arraycopy(result, 0, content, 0, content.length); // 去掉校验码
                Log.d("content  " + HexUtils.formatHexString(content));
                if (content[0] == 00) {
                    if (content[1] == 01) { // Box状态
                        TempInfo info = new TempInfo();
                        info.setDeviceId(HexUtils.formatHexString(Arrays.copyOfRange(content, 4, 16)));
                        info.setDeviceVersion(new String(Arrays.copyOfRange(content, 16, 28)));
                        info.setCharging(HexUtils.byteToInt(content[28]));
                        info.setMemberId(HexUtils.byteToInt(content[29]));
                        int memberCount = HexUtils.byteToInt(content[30]);
                        info.setMemberCount(memberCount);
                        List<Member> members = new ArrayList<>();
                        int start = 31;
                        for (int i = 0; i < memberCount; i++) {
                            Member member = new Member();
                            member.setMemberId(HexUtils.byteToInt(content[start++]));
                            int length = HexUtils.byteToInt(content[start++]);
                            member.setLength(length);
                            member.setName(new String(Arrays.copyOfRange(content, start, start + length), "GBK"));
                            start += length;
                            members.add(member);
                        }
                        info.setMembers(members);
                        info.setOrginal(content);
                        _tempInfoLiveData.postValue(info);
                    } else if (content[1] == 02) { // 实时温度
                        CurrentTemp temp = new CurrentTemp();
                        temp.setCharging(HexUtils.byteToInt(content[4]));
                        temp.setStatus(HexUtils.byteToInt(content[5]));
                        temp.setMemberId(HexUtils.byteToInt(content[6]));
                        temp.setTemp((HexUtils.byteToInt(content[7]) + 170) / 10.0f);
                        _currentTempLiveData.postValue(temp);
                    } else if (content[1] == 03) { // 历史温度
                        HistoryTemp historyTemp = new HistoryTemp();
                        historyTemp.setStatus(HexUtils.byteToInt(content[4]));
                        historyTemp.setStartTime(HexUtils.byteToLong(Arrays.copyOfRange(content, 5, 9)));
                        historyTemp.setMemberId(HexUtils.byteToInt(content[9]));
                        historyTemp.setStep(HexUtils.byteToInt(content[10]));
                        int count = HexUtils.byteToInt(content[11]);
                        historyTemp.setTempCount(count);
                        float[] temps = new float[count];
                        for (int i = 12; i < count; i++) {
                            temps[i - 12] = (HexUtils.byteToInt(content[6]) + 170) / 10.0f;
                        }
                        historyTemp.setTemps(temps);
                        _historyTempLiveData.postValue(historyTemp);
                    } else if (content[1] == 05) { // 修改成员信息
                        _membearLiveData.postValue(HexUtils.byteToInt(content[4]) == 1);
                    }
                }
            } catch (Exception e) {
                Log.d(Log.toString(e));
            }
        }
    }

    public static class InstanceHolder {
        private static final BlueManager INSTANCE = new BlueManager();
    }

    private final class WorkerHandler extends Handler {

        WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            switch (msg.what) {
                case STOP_SCAN_AND_CONNECT:
                    final String address = (String) msg.obj;
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            stopScan(true);
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            connect(address);
                        }
                    });
                    break;
                case MSG_SPLIT_WRITE:
                    final int status = bundle.getInt(KEY_WRITE_BUNDLE_STATUS);
                    final byte[] value = bundle.getByteArray(KEY_WRITE_BUNDLE_VALUE);
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (split && bleWriteCallback != null) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    bleWriteCallback.onWriteSuccess(value);
                                } else {
                                    bleWriteCallback.onWriteFailure(value);
                                }
                            }
                        }
                    });
                    break;
                case MSG_OBD_DISCONNECTED:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            _connectStatusLiveData.setValue(BoxEvent.TEMP_DISCONNECTED);
                        }
                    });
                    break;
            }
        }
    }


    private class TimeOutThread extends Thread {

        private static final String TIMEOUTSYNC = "MTIMEOUTSYNC";

        private boolean needStop = false;

        private boolean waitForCommand = false;

        private boolean needRewire = false;

        @Override
        public synchronized void start() {
            needStop = false;
            super.start();
        }

        @Override
        public void run() {
            super.run();
            while (!needStop) {
                synchronized (TIMEOUTSYNC) {
                    if (needStop) {
                        return;
                    }
                    if (waitForCommand) {
                        try {
                            Log.d("TimeOutThread wait ");
                            TIMEOUTSYNC.wait(COMMAND_TIMEOUT);
                            if (needRewire) {
                                Log.d("TimeOutThread needRewire ");
                                if (currentRepeat > 2) {
                                    currentRepeat = 0;
                                    mMainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("连续2次响应超时，断开连接！");
//                                            waitForCommand = false;
//                                            needRewire = false;
//                                            currentRepeat = 0;
                                            disconnect();
                                            _connectStatusLiveData.setValue(BoxEvent.TEMP_DISCONNECTED);
//                                            notifyBleCallBackListener(BoxEvent.TEMP_DISCONNECTED, null);
                                        }
                                    });
                                } else {
                                    Log.d("响应超时，重新发送！");
                                    if (mDataQueue != null) {
                                        mDataQueue.clear();
                                    }
                                    queue.add(currentProtocol);
                                    currentRepeat++;
                                }
                            }
                            Log.d("TimeOutThread notifyAll ");
                            TIMEOUTSYNC.notifyAll();
//                            TIMEOUTSYNC.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

        public void startCommand(boolean reset) {
            if (!waitForCommand) {
                synchronized (TIMEOUTSYNC) {
                    Log.d("TimeOutThread startCommand ");
                    waitForCommand = true;
                    if (reset) {
                        needRewire = true;
                    }
                    TIMEOUTSYNC.notifyAll();
                }
            }
        }

        public void endCommand() {
            if (waitForCommand) {
                synchronized (TIMEOUTSYNC) {
                    Log.d("TimeOutThread endCommand ");
                    waitForCommand = false;
                    needRewire = false;
                    currentRepeat = 0;
                    TIMEOUTSYNC.notifyAll();
//                    try {
//                        TIMEOUTSYNC.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }

        public void cancel() {
            Log.d("TimeOutThread cancel ");
            needStop = true;
            interrupt();
        }
    }
}