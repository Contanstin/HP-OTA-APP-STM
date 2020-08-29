package com.hpmont.hpprog.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Ascii;
import com.google.common.primitives.Bytes;
import com.hpmont.hpprog.R;
import com.hpmont.hpprog.adapter.SpinnerAdapter;
import com.hpmont.hpprog.base.BaseActivity;
//import com.hpmont.hpprog.base.Bin;
import com.hpmont.hpprog.base.HexDeal;
//import com.hpmont.hpprog.base.ChipType;
import com.hpmont.hpprog.base.HexStruct;
import com.hpmont.hpprog.base.MyApplication;
//import com.hpmont.hpprog.base.ProductStruct;
import com.hpmont.hpprog.base.ReadBluetoothDataCallBack;
import com.hpmont.hpprog.base.RxData_Struct;
import com.hpmont.hpprog.config.Config;
import com.hpmont.hpprog.util.CRC16;
import com.hpmont.hpprog.util.CommonUtil;
import com.hpmont.hpprog.util.JsonUtils;
import com.hpmont.hpprog.util.http.HttpClient;
import com.hpmont.hpprog.widget.MyProgressDialog;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DownloadBluetoothActivity extends BaseActivity implements View.OnClickListener, ReadBluetoothDataCallBack.onHandleData {
    private String TAG = "DownloadActivityLog";
    private final static int REQUEST_CONNECT_DEVICE = 1;
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private boolean mConnectState = false;
    private ReadBluetoothDataCallBack callBack;
    private Map<String, JSONObject> productMap = new HashMap<>();
    private Map<String, String> nonstandardMap = new HashMap<>();
    private Thread downThread = null;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    private final static int CONNECT_SUCCESS = 1;
    private final static int CONNECT_FAIL = 0;

    //蓝牙设备
    BluetoothDevice _device = null;
    //蓝牙通信socket
    BluetoothSocket _socket = null;
    //输入流，用来接收蓝牙数据
    private InputStream inputStream;
    private boolean bThread = false;
    private boolean bRun = true;

    private TextView txt_title_center;
    private ImageView img_ble_state;
    private Spinner spinner_product;
    private Spinner spinner_nonstandard;
    private Button btn_update;
    private Button btn_cancel;
    private TextView txt_stephint;

    //发送HEX计数
    private int c_sendhex = 0;
    //完成标志位（0x41）
    private boolean f_check = false;
    //退出Boot标志
    private boolean f_exitboot = false;
    //Boot完成标志位
    private boolean f_boot = false;
    //擦除Flash标志位
    private boolean f_eraseflash = false;
    //数据错误标志位
    private boolean f_error = false;
    //Hex发送完成标志
    private boolean f_hexfinish = false;
    //数据重发标志位
    private boolean f_resend = false;
    //回复指令标志位（数据类型：01）
    private int orderType = 0;
    //回复加载密钥标志位（数据类型：02）
    private boolean f_unlock = false;
    //回复写入数据标志位（数据类型：03）
    private boolean f_hex = false;
    //回复波特率切换标志位（数据类型：04）
    private boolean f_bandrate = false;
    //回复硬件编号（数据类型：11）
    private boolean f_hardware=false;
    //回复随机数标志位（数据类型：05）
    private boolean f_random = false;
    //回复全片校验标志位（数据类型：06）
    private boolean f_hexcheck = false;
    //回复软件版本标志位（数据类型：10）
    private boolean f_version = false;
    //回复通信密码标志位（数据类型：20）
    private boolean f_password = false;
    //获取Hex文件标志位
    private boolean f_getHexFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_download);
        if (_bluetooth == null) {
            CommonUtil.toast(getResources().getString(R.string.devicesacn_bluetooth_not_supported));
            application.exit();
            return;

        }
        new Thread() {
            public void run() {
                if (_bluetooth.isEnabled() == false) {
                    _bluetooth.enable();
                }
            }
        }.start();
        callBack = ReadBluetoothDataCallBack.getInstance();
        callBack.setOnCallBack(this);
        init();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopDownload();
        callBack.removeCallBack();
        unregisterReceiver(mBroadcastReceiver);

        if (_socket != null) {
            try {
                _socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private AlertDialog bleConnect;

    private void bleConnectDialog(String msg) {
        if (bleConnect == null) {
            bleConnect = new AlertDialog.Builder(DownloadBluetoothActivity.this, R.style.WaitProgress).create();
        }
        View view = LayoutInflater.from(DownloadBluetoothActivity.this).inflate(R.layout.bar_progress, null);
        TextView txt_progress = (TextView) view.findViewById(R.id.progressText);
        bleConnect.setCancelable(false);
        bleConnect.setCanceledOnTouchOutside(false);
        txt_progress.setText(msg);
        if (!msg.equals("")) {
            Activity a = DownloadBluetoothActivity.this;
            if (!a.isFinishing()) {
                bleConnect.show();
                bleConnect.getWindow().setContentView(view);
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_SUCCESS:
                    try {
                        inputStream = _socket.getInputStream();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (bThread == false) {
                        readThread.start();
                        bThread = true;
                    } else {
                        bRun = true;
                    }
                    break;
                case CONNECT_FAIL:
                    if (_socket != null) {
                        try {
                            _socket.close();
                        } catch (Exception e) {

                        }
                        _socket = null;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    bleConnectDialog(getResources().getString(R.string.login_type_bluetooth));
                    final String address = data.getExtras().getString(BluetoothActivity.EXTRA_DEVICE_ADDRESS);

                    new Thread() {
                        public void run() {
                            _device = _bluetooth.getRemoteDevice(address);
                            try {
                                _socket = _device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                            } catch (Exception e) {
                                bleConnect.cancel();
                                handler.sendEmptyMessage(CONNECT_FAIL);
                                Looper.prepare();
                                CommonUtil.toast("Failed!");
                                Looper.loop();
                            }
                            try {
                                _socket.connect();
                                bleConnect.cancel();
                                handler.sendEmptyMessage(CONNECT_SUCCESS);
                                Looper.prepare();
                                CommonUtil.toast(getResources().getString(R.string.connect) + _device.getName() + getResources().getString(R.string.success));
                                Looper.loop();
                            } catch (Exception e) {
                                try {
                                    bleConnect.cancel();
                                    handler.sendEmptyMessage(CONNECT_FAIL);
                                    Looper.prepare();
                                    CommonUtil.toast("Failed!");
                                    Looper.loop();
                                    if (_socket != null) {
                                        _socket.close();
                                    }
                                    _socket = null;
                                } catch (Exception ex) {
                                    bleConnect.cancel();
                                    handler.sendEmptyMessage(CONNECT_FAIL);
                                    Looper.prepare();
                                    CommonUtil.toast("Failed!");
                                    Looper.loop();
                                }
                                return;
                            }
                        }
                    }.start();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ble_state:
                if (application.connectState) {
                    CommonUtil.toast(getResources().getString(R.string.download_ble_connect));
                } else {
                    if (_bluetooth.isEnabled() == false) {
                        CommonUtil.toast(getResources().getString(R.string.devicesacn_contecting_ble));
                        return;
                    }
                    if (_socket == null) {
                        Intent intent = new Intent(this, BluetoothActivity.class);
                        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                    }
                }
                break;

            case R.id.updateBtn:
                if (spinner_product.getCount() == 0 || spinner_nonstandard.getCount() == 0) {
                    CommonUtil.toast(getResources().getString(R.string.download_select_pro));
                    return;
                }
                if (!application.connectState) {
                    CommonUtil.toast(getResources().getString(R.string.download_ble_disconnect));
                    return;
                }
                showProgressDialog();
                txt_stephint.setText("");
                //重置标志位
                resetFlag();
                //禁用控件
                disableView();
                downThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if(application.chipType==0) {
                            if (application.f_bootType == 1) {
                                download_type1();
                            } else if (application.f_bootType == 2) {
                                download_type2();
                            }
                        }else if(application.chipType==1){
                            download_type3();
                    }
                        Looper.loop();
                    }
                });
                downThread.start();
                break;

            case R.id.cancelBtn:
                if (!application.f_stopdownload && downThread != null && downThread.isAlive()) {
                    refreshUI_step(getResources().getString(R.string.download_discontinuing), true);
                }
                application.f_stopdownload = true;
                break;

            default:
                break;
        }
    }

    private void sendBytes(byte[] bytes) {
        if (_socket == null) {
            CommonUtil.toast(getResources().getString(R.string.download_ble_disconnect));
            return;
        }
        if (bytes.length == 0) {
            return;
        }
        try {
            OutputStream outputStream = _socket.getOutputStream();
            outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Thread readThread = new Thread() {
        public void run() {
            bRun = true;
            int length = 0;
            byte[] buffer;
            //接收数据
            while (true) {
                try {
                    while (inputStream.available() == 0) {
                        while (bRun == false) {
                        }
                    }
                    while (true) {
                        if (!bThread) {
                            return;
                        }
                        buffer = new byte[inputStream.available()];
                        length = inputStream.read(buffer);
                        if (length > 0) {
                            byte[] bytes = new byte[length];
                            System.arraycopy(buffer, 0, bytes, 0, bytes.length);
                            if (callBack != null) {
                                String str = "";
                                int[] data = new int[bytes.length];
                                for (int i = 0; i < data.length; i++) {
                                    data[i] = (bytes[i] & 0xFF);
                                    str += (String.format("%02x", ((data[i] & 0xFF))).toUpperCase() + " ");
                                }
                                Log.d("receiveData", str);
                                callBack.invokeMethod(data, DownloadBluetoothActivity.this);
                            }
                        }
                        if (inputStream.available() == 0) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 中止下载线程
     */
    private void stopDownload() {
        if (downThread != null) {
            try {
                downThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                enableView();
            }
        }
        if (progressDialog != null) {
            progressDialog.cancel();
        }
        enableView();
    }

    /**
     * 禁用控件
     */
    private void disableView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner_product.setEnabled(false);
                spinner_nonstandard.setEnabled(false);
                btn_update.setEnabled(false);
            }
        });
    }

    /**
     * 启用控件
     */
    private void enableView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner_product.setEnabled(true);
                spinner_nonstandard.setEnabled(true);
                btn_update.setEnabled(true);
            }
        });
    }

    /**
     * UI刷新（下载步骤）
     *
     * @param step      当前步骤字符串
     * @param isNewLine 是否换行（true：换行；false：不换行）
     */
    private void refreshUI_step(final String step, final boolean isNewLine) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isNewLine) {
                    String txt = txt_stephint.getText().toString();
                    if (txt.equals("")) {
                        txt_stephint.append(step);
                        progressDialog.appendMsg(step);
                    } else {
                        txt_stephint.append("\n" + step);
                        progressDialog.appendMsg("\n" + step);
                    }
                } else {
                    txt_stephint.append(step);
                    progressDialog.appendMsg(step);
                }
            }
        });
    }

    /**
     * UI刷新（进度条）
     *
     * @param value 当前进度（0-100）
     */
    private void refreshUI_progress(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setProgress(value);
                }

            }
        });
    }

    private void refreshDialog_progress(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message != null && progressDialog != null) {
                    //progressDialog.setMessage(message);
                }
            }
        });
    }

    private void cancelDownload() {
        if (!application.f_stopdownload && downThread != null && downThread.isAlive()) {
            refreshUI_step(getResources().getString(R.string.download_discontinuing), true);
            refreshDialog_progress(getResources().getString(R.string.download_discontinuing));
        }
        application.f_stopdownload = true;
    }

    MyProgressDialog progressDialog;

    private void showProgressDialog() {
        progressDialog = new MyProgressDialog(this, R.style.MyProgress);
        progressDialog.setTitle(getResources().getString(R.string.update_progress));
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setCancelListener(getResources().getString(R.string.cancel), new MyProgressDialog.OnCancelClickListener() {
            @Override
            public void onCancelClick() {
                cancelDownload();
            }
        });
    }

    /**
     * 蓝牙状态广播
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                mConnectState = true;
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                mConnectState = false;
                try {
                    bRun = false;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (_socket != null) {
                        _socket.close();
                    }
                    _socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                mConnectState = false;
                try {
                    bRun = false;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (_socket != null) {
                        _socket.close();
                    }
                    _socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            application.connectState = mConnectState;
            showConnectStatus(application.connectState);
        }
    };

    /**
     * 广播过滤策略
     *
     * @return
     */
    private static IntentFilter stateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

    /**
     * 初始化
     */
    private void init() {
        txt_title_center = (TextView) findViewById(R.id.title_centerText);
        img_ble_state = (ImageView) findViewById(R.id.ble_state);
        spinner_product = (Spinner) findViewById(R.id.spinner_product);
        spinner_nonstandard = (Spinner) findViewById(R.id.spinner_nonstandard);
        btn_update = (Button) findViewById(R.id.updateBtn);
        btn_cancel = (Button) findViewById(R.id.cancelBtn);
        txt_stephint = (TextView) findViewById(R.id.stepHintText);
        txt_stephint.setMovementMethod(ScrollingMovementMethod.getInstance());

        //注册单击事件
        img_ble_state.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        txt_title_center.setVisibility(View.VISIBLE);
        txt_title_center.setText(getResources().getString(R.string.login_type_bluetooth));
        img_ble_state.setVisibility(View.VISIBLE);

        initProductSpinner();

        registerReceiver(mBroadcastReceiver, stateIntentFilter());
        showConnectStatus(application.connectState);
    }

    /**
     * 加载Boot
     *
     * @param productName 产品名称
     */
    private void loadBoot(String productName) {
        application.bootBytes = null;
        application.bootBytes_1 = null;
        application.bootBytes_2 = null;
        JSONObject object = productMap.get(productName);
        String productNum = JsonUtils.getJSONString(object, "productNum");
        switch (productNum) {
            case Config.PRODUCT_335:
                application.chipType=0;
                application.unlockKeyAddr = "0X33FFF8";
                break;
            case Config.PRODUCT_034:
                application.chipType=0;
                application.unlockKeyAddr = "0X3F7FF8";
                break;
            case Config.PRODUCT_062:
                application.chipType=0;
                application.unlockKeyAddr = "0X3F7FF8";
                break;
            case Config.PRODUCT_069:
                application.chipType=0;
                application.unlockKeyAddr = "0X3F7FF8";
                break;
            case Config.PRODUCT_STM:
                application.chipType=1;
                application.unlockKeyAddr=null;
                application.flashStart=0x8002000;
                application.flashEnd=0x8010000;
                application.flashRoom=new long[(int) (application.flashEnd-application.flashStart)];
                for(int i=0;i<application.flashRoom.length;i++){
                    application.flashRoom[i]=0xFF;
                }
                break;
            default:
                application.unlockKeyAddr = "0X33FFF8";
                break;
        }

        if(application.chipType==0) {
            JSONArray bootNameList = JsonUtils.getJsonArray(object, "bootNameList");

            try {
                if (bootNameList.length() == 1) {
                    String path = Config.BOOT_PATH + bootNameList.getString(0);
                    application.bootBytes = readFile(path);
                    application.f_bootType = 1;
                } else if (bootNameList.length() == 2) {
                    String path1 = Config.BOOT_PATH + bootNameList.getString(0);
                    String path2 = Config.BOOT_PATH + bootNameList.getString(1);
                    application.bootBytes_1 = readFile(path1);
                    application.bootBytes_2 = readFile(path2);
                    application.f_bootType = 2;

                } else {
                    application.bootBytes = null;
                    application.f_bootType = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                application.exit();
            }
        }
    }

    /**
     * 读取文件
     *
     * @param path 存储路径
     * @return byte数组
     */
    private byte[] readFile(String path) {
        byte[] bytes = null;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            if (inputStream != null) {
                bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化产品型号下拉框
     */
    private void initProductSpinner() {
        productMap.clear();
        ArrayList<String> productList = new ArrayList<String>();
        if (application.productInfo != null && application.productInfo.length() > 0) {
            for (int i = 0; i < application.productInfo.length(); i++) {
                JSONObject object = null;
                try {
                    object = application.productInfo.getJSONObject(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                if (object == null) {
                    continue;
                } else {
                    String name = JsonUtils.getJSONString(object, "name");
                    productMap.put(name, object);
                    productList.add(name);
                }
            }
        } else {
            productList.add(getResources().getString(R.string.download_null));
            CommonUtil.toast(getResources().getString(R.string.download_no_product));
        }

        SpinnerAdapter productAdapter = new SpinnerAdapter(DownloadBluetoothActivity.this, productList);
        spinner_product.setAdapter(productAdapter);
        spinner_product.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String productName = (String) spinner_product.getSelectedItem();
                initNonStandardSpinner(productName);
                loadBoot(productName);
                application.f_hexChange = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Another interface callback
            }
        });
    }

    /**
     * 初始化非标下拉框
     */
    private void initNonStandardSpinner(String productName) {
        nonstandardMap.clear();
        application.softwareId = null;
        ArrayList<String> nonstandstardList = new ArrayList<String>();
        JSONObject object = productMap.get(productName);
        if (object != null) {
            JSONArray nonstandard = JsonUtils.getJsonArray(object, "children");
            if (nonstandard != null && nonstandard.length() > 0) {
                for (int i = 0; i < nonstandard.length(); i++) {
                    JSONObject nonstandardobject = null;
                    try {
                        nonstandardobject = nonstandard.getJSONObject(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (nonstandardobject == null) {
                        continue;
                    }
                    String nonStandardName = JsonUtils.getJSONString(nonstandardobject, "authCode");
                    String nonStandardId = JsonUtils.getJSONString(nonstandardobject, "authId");
                    nonstandardMap.put(nonStandardName, nonStandardId);
                    nonstandstardList.add(nonStandardName);
                }
            } else {
                nonstandstardList.add(getResources().getString(R.string.download_null));
            }
        } else {
            nonstandstardList.add(getResources().getString(R.string.download_null));
        }

        SpinnerAdapter nonstandardAdapter = new SpinnerAdapter(DownloadBluetoothActivity.this, nonstandstardList);
        spinner_nonstandard.setAdapter(nonstandardAdapter);
        spinner_nonstandard.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nonStandardName = (String) spinner_nonstandard.getSelectedItem();
                application.softwareId = nonstandardMap.get(nonStandardName);
                application.f_hexChange = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Another interface callback
            }
        });
    }

    /**
     * 蓝牙连接状态处理
     *
     * @param connectState
     */
    private void showConnectStatus(final boolean connectState) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connectState) {
                    img_ble_state.setImageResource(R.drawable.ble_connect);
                } else {
                    img_ble_state.setImageResource(R.drawable.ble_disconnect);
                }

            }
        });
    }

    @Override
    public void handleData(int[] data) {
        if (application.receiveType == 0) {
            checkExplain(data);
        } else if (application.receiveType == 1) {
            bootExplain();
        } else {
            int[] tempRx = new int[data.length];
            for (int i = 0; i < data.length; i++) {
                tempRx[i] = data[i] & 0x00FF;
            }
            int CRCH = tempRx[data.length - 1];
            int CRCL = tempRx[data.length - 2];
            int CRCRx = (CRCH * 256) + CRCL;
            int CRCCalc = CRC16.crcCalc_Table(tempRx, data.length - 2);
            if (CRCCalc != CRCRx) {
                return;
            }
            if(application.chipType==0) {
                if (data[0] == Config.FRAME_HEAD && data[1] == Config.FRAME_LENGTH) {
                    byte[] bytes = new byte[data[1] - 2];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = (byte) data[i + 2];
                    }
                    if (f_password) {
                        bytes = CommonUtil.dataDecryption(bytes, application.A1, application.A2, application.B);
                    }
                    orderExplain(bytes);
                }
            }else if(application.chipType==1){
                if (data[0] == Config.FRAME_HEAD && data[2] == Config.FRAME_LENGTH) {
                    byte[] bytes = new byte[data[2] - 2];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = (byte) data[i + 3];
                    }
                    if (f_password) {
                        bytes = CommonUtil.dataDecryption(bytes, application.A1, application.A2, application.B);
                    }
                    orderExplain(bytes);
                }
            }
        }
    }

    /**
     * 连接状态解析（0x41）
     *
     * @param rxBuffer
     */
    private void checkExplain(int[] rxBuffer) {
        if (rxBuffer.length == 1) {
            if (rxBuffer[0] == 0x41) {
                f_check = true;
            }
        }
    }

    /**
     * Boot文件解析
     */
    private void bootExplain() {
        byte[] bytes = Bytes.toArray(RxData_Struct.getInstance().bootList);
        int length = application.sendBoot.length - 1;
        byte[] bytes1 = new byte[length];
        byte[] bytes2 = new byte[length];
        System.arraycopy(bytes, 0, bytes1, 0, bytes1.length);
        System.arraycopy(application.sendBoot, 0, bytes2, 0, bytes2.length);
        if (Arrays.equals(bytes1, bytes2)) {
            f_boot = true;
        }
    }

    /**
     * 指令解析
     *
     * @param data
     */
    private void orderExplain(byte[] data) {
        if (data[0] == Config.CMD) {
            if (data[1] == Config.SUCCESS) {
                //擦除Flash
                if (orderType == 1) {
                    f_eraseflash = true;
                    orderType = 0;
                }
                //退出Boot
                else if (orderType == 2) {
                    f_exitboot = true;
                    orderType = 0;
                }
            } else if (data[1] == Config.FAILED) {
                if (orderType == 1) {
                    f_error = true;
                    orderType = 0;
                } else if (orderType == 2) {
                    f_error = true;
                    orderType = 0;
                }
            }
        } else if (data[0] == Config.KEY) {
            if (data[1] == Config.SUCCESS) {
                f_unlock = true;
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        } else if (data[0] == Config.DAT) {
            if (data[1] == Config.SUCCESS) {
                f_hex = true;
                c_sendhex++;
                refreshUI_progress(50 + c_sendhex * 50 / application.hexLength);
                if (c_sendhex == application.hexLength) {
                    f_hexfinish = true;
                }
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        } else if (data[0] == Config.BAND) {
            if (data[1] == Config.SUCCESS) {
                f_bandrate = true;
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        } else if (data[0]==Config.Hardware){
            if(data[1]!=0) {
                f_hardware = true;
                application.hardwareCode = data[1];
            }
        }else if (data[0] == Config.CHECK) {
            if (data[1] == Config.SUCCESS) {
                f_hexcheck = true;
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        } else if (data[0] == Config.RAN) {
            application.C = data[1];
            application.encryptionKey = CommonUtil.buildKey(application, application.C);
            f_random = true;
        } else if (data[0] == Config.VER) {
            if (data[1] != 0) {
                f_version = true;
                application.versionCode = data[1];
            }
        } else if (data[0] == Config.PWD) {
            if (data[1] == Config.SUCCESS) {
                f_password = true;
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        } else if (data[0] == Config.IDLE) {
            if (data[1] == Config.SUCCESS) {
                f_resend = true;
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        } else if (data[0] == Config.DEF) {
            if (data[1] == Config.FAILED) {
                f_resend = true;
            } else if (data[1] == Config.FAILED) {
                f_error = true;
            }
        }
    }

    /**
     * 中断下载
     *
     * @param str
     */
    private void exitDownload(String str) {
        if (application.f_stopdownload) {
            application.f_stopdownload = false;
            if (application.versionCode > 0) {
                exit_Boot();
            }
            refreshUI_step(getResources().getString(R.string.download_OK), false);
            dealDownloadFail();
            stopDownload();
        } else {
            if (application.versionCode > 0) {
                exit_Boot();
            }
            if (str.length() > 0) {
                refreshUI_step(str, false);
            }
            dealDownloadFail();
            stopDownload();
        }
    }

    /**
     * 单Boot文件下载流程
     */
    private void download_type1() {
        refreshUI_progress(0);
        refreshUI_step(getResources().getString(R.string.download_start), true);
        refreshDialog_progress(getResources().getString(R.string.download_start));
        if (!getHexFile()) {
            if (application.f_stopdownload) {
                application.f_stopdownload = false;
                refreshUI_step(getResources().getString(R.string.download_OK), false);
            } else {
                refreshUI_step(getResources().getString(R.string.download_loadfile_fail), true);
            }
            application.f_hexChange = true;
            stopDownload();
            return;
        }
        refreshUI_progress(10);
        refreshUI_step(getResources().getString(R.string.download_OK) + "（" + application.hexFileName + "  " + application.hexFileVersion + "）", false);
        refreshUI_step(getResources().getString(R.string.download_connect), true);
        refreshDialog_progress(getResources().getString(R.string.download_connect));
        if (!send_Check()) {
            exitDownload(getResources().getString(R.string.download_check_ble));
            return;
        }
        refreshUI_progress(13);
        if (!send_Boot(application.bootBytes)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!wait_Version()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!get_Random()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!send_Encryption(application.encryptionKey)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!unlock(CommonUtil.secretKeyToByte(application.secretKeyStr))) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        refreshUI_progress(20);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_versioncode) + application.versionCode, true);
        refreshUI_step(getResources().getString(R.string.download_erase_flash), true);
        refreshDialog_progress(getResources().getString(R.string.download_erase_flash));
        if (!eraseFlash()) {
            exitDownload(getResources().getString(R.string.download_unlock_fail));
            return;
        }
        refreshUI_progress(50);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_downloading), true);
        refreshDialog_progress(getResources().getString(R.string.download_downloading));
        if (!send_Hex(application.hexStructs)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!hexCheck(application.flashCheck)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!exit_Boot()) {
            refreshUI_step(getResources().getString(R.string.download_download_fail), true);
            stopDownload();
            return;
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshUI_progress(100);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_success), true);
        refreshDialog_progress(getResources().getString(R.string.download_success));
        stopDownload();
    }

    /**
     * 双Boot文件下载流程
     */
    private void download_type2() {
        refreshUI_progress(0);
        refreshUI_step(getResources().getString(R.string.download_start), true);
        if (!getHexFile()) {
            if (application.f_stopdownload) {
                application.f_stopdownload = false;
                refreshUI_step(getResources().getString(R.string.download_OK), false);
            } else {
                refreshUI_step(getResources().getString(R.string.download_loadfile_fail), true);
            }
            application.f_hexChange = true;
            stopDownload();
            return;
        }
        refreshUI_progress(10);
        refreshUI_step(getResources().getString(R.string.download_OK) + "（" + application.hexFileName + "  " + application.hexFileVersion + "）", false);
        refreshUI_step(getResources().getString(R.string.download_connect), true);
        if (!send_Check()) {
            exitDownload(getResources().getString(R.string.download_check_ble));
            return;
        }
        refreshUI_progress(13);
        if (!send_Boot(application.bootBytes_1)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!wait_Version()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!get_Random()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!send_Encryption(application.encryptionKey)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!unlock(CommonUtil.secretKeyToByte(application.secretKeyStr))) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        refreshUI_progress(20);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_versioncode) + application.versionCode, true);
        refreshUI_step(getResources().getString(R.string.download_erase_flash), true);
        if (!eraseFlash()) {
            exitDownload(getResources().getString(R.string.download_unlock_fail));
            return;
        }
        if (!exit_Boot()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        resetFlag();
        if (!send_Check()) {
            exitDownload(getResources().getString(R.string.download_check_ble));
            return;
        }
        if (!send_Boot(application.bootBytes_2)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!wait_Version()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!get_Random()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!send_Encryption(application.encryptionKey)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        refreshUI_progress(50);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_downloading), true);
        if (!send_Hex(application.hexStructs)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!hexCheck(application.flashCheck)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!exit_Boot()) {
            refreshUI_step(getResources().getString(R.string.download_download_fail), true);
            stopDownload();
            return;
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshUI_progress(100);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_success), true);
        stopDownload();
    }


    /**
     * STM32的下载
     */
    private void download_type3() {
        refreshUI_progress(0);
        refreshUI_step(getResources().getString(R.string.download_start), true);
        refreshDialog_progress(getResources().getString(R.string.download_start));
        if (!getHexFile()) {
            if (application.f_stopdownload) {
                application.f_stopdownload = false;
                refreshUI_step(getResources().getString(R.string.download_OK), false);
            } else {
                refreshUI_step(getResources().getString(R.string.download_loadfile_fail), true);
            }
            application.f_hexChange = true;
            stopDownload();
            return;
        }
        refreshUI_progress(10);
        refreshUI_step(getResources().getString(R.string.download_OK) + "（" + application.hexFileName + "  " + application.hexFileVersion + "）", false);
        refreshUI_step(getResources().getString(R.string.download_connect), true);
        refreshDialog_progress(getResources().getString(R.string.download_connect));
        refreshUI_progress(13);
        if (!Send_Hardware()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!Send_Ver()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!get_Random()) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!send_Encryption(application.encryptionKey)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        refreshUI_progress(20);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_versioncode) + application.versionCode, true);
        refreshUI_step(getResources().getString(R.string.download_erase_flash), true);
        refreshDialog_progress(getResources().getString(R.string.download_erase_flash));
        if (!eraseFlash()) {
            exitDownload(getResources().getString(R.string.download_unlock_fail));
            return;
        }
        refreshUI_progress(50);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_downloading), true);
        refreshDialog_progress(getResources().getString(R.string.download_downloading));
        if (!send_Hex(application.hexStructs)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!hexCheck(application.flashCheck)) {
            exitDownload(getResources().getString(R.string.download_download_fail));
            return;
        }
        if (!exit_Boot()) {
            refreshUI_step(getResources().getString(R.string.download_download_fail), true);
            stopDownload();
            return;
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshUI_progress(100);
        refreshUI_step(getResources().getString(R.string.download_OK), false);
        refreshUI_step(getResources().getString(R.string.download_success), true);
        refreshDialog_progress(getResources().getString(R.string.download_success));
        stopDownload();
    }

    //发送硬件编号
    private boolean Send_Hardware() {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        byte[]bytes={};
        sendOrder_STM(bytes, Config.Hardware);
        while (!f_hardware) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendOrder_STM(bytes, Config.Hardware);
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 10000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
            if (f_resend) {
                f_resend = false;
                sendOrder_STM(bytes, Config.BAND);
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_hardware) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }
    /**
     * 发送版本号
     */
       private boolean Send_Ver() {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        byte[]bytes={};
        sendOrder_STM(bytes, Config.VER);
        while (!f_version) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sendOrder_STM(bytes, Config.VER);
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 1000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
            if (f_resend) {
                f_resend = false;
                sendOrder_STM(bytes, Config.BAND);
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_version) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }
    /**
     * 重置标志位
     */
    private void resetFlag() {
        c_sendhex = 0;
        orderType = 0;

        f_check = false;
        f_boot = false;
        f_unlock = false;
        f_eraseflash = false;
        f_error = false;
        f_hex = false;
        f_exitboot = false;
        f_version = false;
        f_hexfinish = false;
        f_bandrate = false;
        f_hardware=false;
        f_hexcheck = false;
        f_password = false;
        f_random = false;
        f_resend = false;
        f_getHexFile = false;

        application.receiveType = 0;
        application.versionCode = 0;
        application.sendBoot = null;
        application.f_stopdownload = false;
    }


    /**
     * 获取后台Hex文件
     *
     * @return
     */
    private boolean getHexFile() {
        RequestParams params = new RequestParams();
        params.put("vkey", application.vkey);
        params.put("authId", application.softwareId);
        String url = Config.BaseUrl + "getHexFileByPc";
        try {
            HttpClient.post_Syn(DownloadBluetoothActivity.this, url, params,
                    new HttpClient.synHttpCallBack() {
                        @Override
                        public void success(JSONObject object) {
                            try {
                                String status = JsonUtils.getJSONString(object, "status");
                                application.hexFileName = null;
                                application.hexFileVersion = null;
                                switch (status) {
                                    case "1":
                                        application.hexFileName = JsonUtils.getJSONString(object, "fileName");
                                        application.hexFileVersion = JsonUtils.getJSONString(object, "fileVersion");
                                        String url = JsonUtils.getJSONString(object, "hexUrl");
                                        if (application.f_hexChange) {
                                            application.f_hexChange = false;
                                            String[] hexArry = HttpClient.downHexFile(DownloadBluetoothActivity.this, url);
                                            try {
                                                if (MyApplication.chipType == 0) {
                                                    if (HexDeal.getHex(hexArry)) {
                                                        if (application.f_stopdownload) {
                                                            f_getHexFile = false;
                                                        } else {
                                                            f_getHexFile = true;
                                                        }
                                                    }
                                                } else {
//                                                    if (HexDeal.binFile(hexArry) &&HexDeal.productsReload(hexArry)&& HexDeal.getHex(hexArry)) {
                                                    if (HexDeal.getHex(hexArry)) {
                                                        if (application.f_stopdownload) {
                                                            f_getHexFile = false;
                                                        } else {
                                                            f_getHexFile = true;
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        break;
                                    case "2":
                                        CommonUtil.toast(getResources().getString(R.string.download_hex_err_1));
                                        break;
                                    case "3":
                                        CommonUtil.toast(getResources().getString(R.string.download_hex_err_2));
                                        break;
                                    case "4":
                                        CommonUtil.toast(getResources().getString(R.string.download_hex_err_3));
                                        break;
                                    case "5":
                                        CommonUtil.toast(getResources().getString(R.string.download_hex_err_4));
                                        break;
                                    default:
                                        CommonUtil.toast(getResources().getString(R.string.download_hex_err_5));
                                        break;
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(JSONObject error) {
                            CommonUtil.toast("Error");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            refreshUI_step(getResources().getString(R.string.check_network), true);
        }
        return f_getHexFile;
    }

    /**
     * 处理烧录异常
     */
    private void dealDownloadFail() {
        RequestParams params = new RequestParams();
        params.put("vkey", application.vkey);
        params.put("authId", application.softwareId);
        String url = Config.BaseUrl + "addBurnNum";
        try {
            HttpClient.post_Syn(DownloadBluetoothActivity.this, url, params,
                    new HttpClient.synHttpCallBack() {
                        @Override
                        public void success(JSONObject object) {
                            try {
                                String status = JsonUtils.getJSONString(object, "status");
                                if (status.equals("1")) {
                                    return;
                                } else {
                                    Log.d("dealDownloadFail", "Error");
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(JSONObject error) {
                            CommonUtil.toast("Error");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送指令
     */
    private void sendOrder(byte[] txBuffer, byte dataType) {
        if (txBuffer == null) {
            return;
        }
        int dataLength = txBuffer.length;
        byte frameLength = (byte) (1 + dataLength + 2);
        byte[] byteHead = new byte[2];
        byteHead[0] = (byte) 0xAA;
        byteHead[1] = frameLength;
        byte[] step1=byteHead;
        byte[] type = new byte[1];
        type[0] = dataType;
        byte[] data = CommonUtil.mergerArray(type, txBuffer);
        if (f_password) {
            data = CommonUtil.dataEncryption(data, application.A1, application.A2, application.B);
        }
        if(application.chipType==1) {
            step1 = CommonUtil.mergerArray(byteHead, Config.FRAME_ADDR);
        }
        byte[] byteNoCRC = CommonUtil.mergerArray(step1, data);
        int[] tempTx = new int[byteNoCRC.length];
        for (int j = 0; j < tempTx.length; j++) {
            tempTx[j] = byteNoCRC[j] & 0xFF;
        }
        int crcCalc = 0;
        byte[] byteCRC = new byte[2];
        crcCalc = CRC16.crcCalc_Table(tempTx, tempTx.length);
        byteCRC[0] = (byte) (crcCalc & 0x00FF);
        byteCRC[1] = (byte) (crcCalc >> 8);
        byte[] bytes = CommonUtil.mergerArray(byteNoCRC, byteCRC);
        sendBytes(bytes);
    }

    /**
     * 发送STM32指令
     */
    private void sendOrder_STM(byte[] txBuffer, byte dataType) {
        if (txBuffer == null) {
            return;
        }
        int dataLength = txBuffer.length;
        byte frameLength = (byte) (1 + dataLength + 2);
        byte[] byteHead = new byte[3];
        byteHead[0] = (byte) 0xAA;
        if(application.hardwareCode!=0){
            byteHead[1]=application.hardwareCode;
        }else {
            byteHead[1] = (byte) 0xFF;
        }
        byteHead[2] = frameLength;
        byte[] type = new byte[1];
        type[0] = dataType;
        byte[] data = CommonUtil.mergerArray(type, txBuffer);
        if (f_password) {
            data = CommonUtil.dataEncryption(data, application.A1, application.A2, application.B);
        }
        byte[] byteNoCRC = CommonUtil.mergerArray(byteHead, data);
        int[] tempTx = new int[byteNoCRC.length];
        for (int j = 0; j < tempTx.length; j++) {
            tempTx[j] = byteNoCRC[j] & 0xFF;
        }
        int crcCalc = 0;
        byte[] byteCRC = new byte[2];
        crcCalc = CRC16.crcCalc_Table(tempTx, tempTx.length);
        byteCRC[0] = (byte) (crcCalc & 0x00FF);
        byteCRC[1] = (byte) (crcCalc >> 8);
        byte[] bytes = CommonUtil.mergerArray(byteNoCRC, byteCRC);
        sendBytes(bytes);
    }
    /**
     * 发送0x41连接主板
     *
     * @return
     */
    private boolean send_Check() {
        application.receiveType = 0;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        byte[] bytes = {0x41};
        sendBytes(bytes);
        while (!f_check) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 1000)) {
                sendBytes(bytes);
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_check) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 发送Boot
     *
     * @param bootBytes
     * @return
     */
    private boolean send_Boot(byte[] bootBytes) {
        if (bootBytes == null) {
            refreshUI_step(getResources().getString(R.string.download_loadfile_fail), true);
            return false;
        }
        application.receiveType = 1;
        RxData_Struct.getInstance().bootList.clear();
        application.sendBoot = null;
        application.sendBoot = bootBytes;
        int count = (int) Math.ceil((double) bootBytes.length / 200);
        int remainder = bootBytes.length % 200;
        for (int i = 0; i < count; i++) {
            byte[] bytes;
            if (i == count - 1) {
                if (remainder == 0) {
                    bytes = new byte[200];
                } else {
                    bytes = new byte[remainder];
                }
            } else {
                bytes = new byte[200];
            }

            System.arraycopy(bootBytes, i * 200, bytes, 0, bytes.length);
            sendBytes(bytes);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long overTime = System.currentTimeMillis();
        while (!f_boot) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (overTime + 3000)) {
                return false;
            }
        }
        if (!f_boot) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 等待版本号
     *
     * @return
     */
    private boolean wait_Version() {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        while (!f_version) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 5000)) {
                return false;
            }
        }
        if (!f_version) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 发送波特率
     *
     * @param bytes
     * @return
     */
    private boolean send_BandRate(byte[] bytes) {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        sendOrder(bytes, Config.BAND);
        while (!f_bandrate) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 1000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
            if (f_resend) {
                f_resend = false;
                sendOrder(bytes, Config.BAND);
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_bandrate) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 获取随机数
     *
     * @return
     */
    private boolean get_Random() {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        byte[] bytes = {0x01};
        if(application.chipType==0) {
            sendOrder(bytes, Config.RAN);
        }else if(application.chipType==1){
            sendOrder_STM(bytes,Config.RAN);
        }
        while (!f_random) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 10000)) {
                return false;
            }
            if (f_resend) {
                f_resend = false;
                if(application.chipType==0) {
                    sendOrder(bytes, Config.RAN);
                }else if(application.chipType==1){
                    sendOrder_STM(bytes,Config.RAN);
                }
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_random) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 发送加密密钥
     *
     * @param bytes
     * @return
     */
    private boolean send_Encryption(byte[] bytes) {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        if (application.chipType == 0) {
            sendOrder(bytes, Config.PWD);
        }else if(application.chipType==1){
            sendOrder_STM(bytes,Config.PWD);
        }
        while (!f_password) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 1000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
            if (f_resend) {
                f_resend = false;
                if (application.chipType == 0) {
                    sendOrder(bytes, Config.PWD);
                }else if(application.chipType==1){
                    sendOrder_STM(bytes,Config.PWD);
                }
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_password) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 解锁
     *
     * @param bytes
     * @return
     */
    private boolean unlock(byte[] bytes) {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        sendOrder(bytes, Config.KEY);
        while (!f_unlock) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 1000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
            if (f_resend) {
                f_resend = false;
                sendOrder(bytes, Config.KEY);
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_unlock) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 擦除Flash
     *
     * @return
     */
    private boolean eraseFlash() {
        application.receiveType = 2;
        orderType = 1;
        long startTime = System.currentTimeMillis();
        byte[] bytes = {Config.CMD_CLRFLASH};
        if(application.chipType==1){
            sendOrder_STM(bytes,Config.CMD);
        }else if(application.chipType==0) {
            sendOrder(bytes, Config.CMD);
        }
        while (!f_eraseflash) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime < (startTime + 10000)) {
                refreshUI_progress(20 + 2 * (int) (nowTime - startTime) / 1000);
            }
            if (nowTime > (startTime + 60000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
        }
        if (!f_eraseflash) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 发送Hex
     *
     * @param hexStructs
     * @return
     */
    private boolean send_Hex(HexStruct[] hexStructs) {
        application.receiveType = 2;
        int length = hexStructs.length;
        c_sendhex = 0;
        for (int i = 0; i < length; i++) {
            byte[] bytes = CommonUtil.mergerArray(CommonUtil.mergerArray(hexStructs[i].getAddr(), hexStructs[i].getRecordLength()), hexStructs[i].getData());
            if(application.chipType==0){
                sendOrder(bytes, Config.DAT);
            }else if(application.chipType==1){
                sendOrder_STM(bytes,Config.DAT);
            }
            f_hex = false;
            long startTime = System.currentTimeMillis();
            int c_fail = 0;
            while (!f_hex) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long nowTime = System.currentTimeMillis();
                if (nowTime > (startTime + 10000)) {
                    return false;
                }
                if (f_error) {
                    f_error = false;
                    return false;
                }
                if (f_resend) {
                    f_resend = false;
                    if(application.chipType==0){
                        sendOrder(bytes, Config.DAT);
                    }else if(application.chipType==1){
                        sendOrder_STM(bytes,Config.DAT);
                    }
                    c_fail++;
                    startTime = System.currentTimeMillis();
                }
                if (c_fail > 3) {
                    return false;
                }
            }
            if (application.f_stopdownload) {
                return false;
            }
        }
        long waitTime = System.currentTimeMillis();
        while (!f_hexfinish) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (waitTime + 5000)) {
                return false;
            }
        }
        if (!f_hexfinish) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 全片校验
     *
     * @param bytes
     * @return
     */
    private boolean hexCheck(byte[] bytes) {
        application.receiveType = 2;
        long startTime = System.currentTimeMillis();
        int c_fail = 0;
        if(application.chipType==0){
            sendOrder(bytes, Config.CHECK);
        }else if(application.chipType==1){
            sendOrder_STM(bytes,Config.CHECK);
        }
        while (!f_hexcheck){
            try{
                Thread.sleep(1);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if(nowTime > (startTime + 60000)){
                return false;
            }
            if(f_error){
                f_error = false;
                return false;
            }
            if(f_resend){
                f_resend = false;
                if(application.chipType==0){
                    sendOrder(bytes, Config.CHECK);
                }else if(application.chipType==1){
                    sendOrder_STM(bytes,Config.CHECK);
                }
                c_fail ++;
                startTime = System.currentTimeMillis();
            }
            if(c_fail > 3){
                return false;
            }
        }
        if (!f_hexcheck) {
            return false;
        } else {
            if (application.f_stopdownload) {
                return false;
            }
            return true;
        }
    }

    /**
     * 退出Boot
     *
     * @return
     */
    private boolean exit_Boot() {
        application.receiveType = 2;
        orderType = 2;
        long startTime = System.currentTimeMillis();
        byte[] bytes = {Config.CMD_EXIT};
        int c_fail = 0;
        if(application.chipType==0){
            sendOrder(bytes, Config.CMD);
        }else if(application.chipType==1){
            sendOrder_STM(bytes,Config.CMD);
        }
        while (!f_exitboot) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime > (startTime + 1000)) {
                return false;
            }
            if (f_error) {
                f_error = false;
                return false;
            }
            if (f_resend) {
                f_resend = false;
                if(application.chipType==0){
                    sendOrder(bytes, Config.CMD);
                }else if(application.chipType==1){
                    sendOrder_STM(bytes,Config.CMD);
                }
                c_fail++;
                startTime = System.currentTimeMillis();
            }
            if (c_fail > 3) {
                return false;
            }
        }
        if (!f_exitboot) {
            return false;
        } else {
            return true;
        }
    }
}
