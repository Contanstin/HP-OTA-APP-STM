package com.hpmont.hpprog.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.hpmont.hpprog.BuildConfig;
import com.hpmont.hpprog.R;
import com.hpmont.hpprog.base.BaseActivity;
import com.hpmont.hpprog.config.Config;
import com.hpmont.hpprog.util.CommonUtil;
import com.hpmont.hpprog.util.EncryptHelper;
import com.hpmont.hpprog.util.GetDeviceUUID;
import com.hpmont.hpprog.util.JsonUtils;
import com.hpmont.hpprog.util.MyDialog;
import com.hpmont.hpprog.util.http.HttpClient;
import com.loopj.android.http.RequestParams;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.zhy.m.permission.MPermissions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private TextView txt_version;
    private TextView txt_save;
    private EditText edt_username;
    private EditText edt_password;
    private Button btn_login;
    private Button btn_delete;
    private Button btn_deletePSW;
    private Button btn_call;
    private ImageView img_save;
    private RadioButton radio_serial;
    private RadioButton radio_bluetooth;

    private boolean isSavePsw;
    private SharedPreferences mSharedPreferences;
    private AlertDialog builder = null;
    Map<String,String> downMap = new HashMap<>();
    private Thread downThread = null;
    volatile boolean f_login = false;
    private String communicationType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_login);
        checkPermissins();
        init();
    }

    private void init(){
        txt_version = (TextView)findViewById(R.id.login_version);
        txt_save = (TextView)findViewById(R.id.login_saveText);
        edt_username = (EditText)findViewById(R.id.login_usenameEdit);
        edt_password = (EditText)findViewById(R.id.login_pswEdit);
        btn_login = (Button)findViewById(R.id.login_entryBtn);
        btn_delete = (Button)findViewById(R.id.login_deleteBtn);
        btn_deletePSW = (Button)findViewById(R.id.login_deletePSWBtn);
        btn_call = (Button)findViewById(R.id.login_callBtn);
        img_save = (ImageView)findViewById(R.id.login_saveImg);
        radio_serial = (RadioButton)findViewById(R.id.serial);
        radio_bluetooth = (RadioButton)findViewById(R.id.bluetooth);

        //注册单击监听
        btn_login.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_deletePSW.setOnClickListener(this);
        btn_call.setOnClickListener(this);
        txt_save.setOnClickListener(this);
        img_save.setOnClickListener(this);

        txt_version.setText(getResources().getString(R.string.version_title)+ "V" + CommonUtil.getAppVersionName(this));

        mSharedPreferences = getSharedPreferences(Config.FILE_NAME, Context.MODE_PRIVATE);
        String psw = mSharedPreferences.getString(Config.PASSWORD, "");
        String username = mSharedPreferences.getString(Config.USER_NAME, "");
        isSavePsw = mSharedPreferences.getBoolean(Config.IS_SAVE_PSW, false);
        edt_username.setText(username);
        if (isSavePsw) {
            edt_password.setText(psw);
            img_save.setImageResource(R.drawable.login_save_psw);
        }
        else {
            edt_password.setText("");
            img_save.setImageResource(R.drawable.login_notsave_psw);
        }
        edt_username.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    String username = edt_username.getText().toString();
                    if(username.length() > 0){
                        btn_delete.setVisibility(View.VISIBLE);
                    }
                    else{
                        btn_delete.setVisibility(View.GONE);
                    }
                }
                else {
                    // 此处为失去焦点时的处理内容
                    btn_delete.setVisibility(View.GONE);
                }
            }
        });
        edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btn_delete.setVisibility(View.VISIBLE);
                }
                else {
                    btn_delete.setVisibility(View.GONE);
                }
            }
        });

        edt_password.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    String password = edt_password.getText().toString();
                    if(password.length() > 0){
                        btn_deletePSW.setVisibility(View.VISIBLE);
                    }
                    else{
                        btn_deletePSW.setVisibility(View.GONE);
                    }
                }
                else {
                    // 此处为失去焦点时的处理内容
                    btn_deletePSW.setVisibility(View.GONE);
                }
            }
        });
        edt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btn_deletePSW.setVisibility(View.VISIBLE);
                }
                else {
                    btn_deletePSW.setVisibility(View.GONE);
                }
            }
        });

        String type = mSharedPreferences.getString(Config.TYPE, "bluetooth");
        if(type.equals("bluetooth")){
            radio_bluetooth.setChecked(true);
            radio_bluetooth.setButtonTintList(getResources().getColorStateList(R.color.radio_checked));
            radio_serial.setButtonTintList(getResources().getColorStateList(R.color.radio_unchecked));
            communicationType = "bluetooth";
        }
        else if(type.equals("serial")){
            radio_serial.setChecked(true);
            radio_serial.setButtonTintList(getResources().getColorStateList(R.color.radio_checked));
            radio_bluetooth.setButtonTintList(getResources().getColorStateList(R.color.radio_unchecked));
            communicationType = "serial";
        }
        else{
            mSharedPreferences.edit().putString(Config.TYPE, "bluetooth").commit();
            radio_bluetooth.setChecked(true);
            radio_bluetooth.setButtonTintList(getResources().getColorStateList(R.color.radio_checked));
            radio_serial.setButtonTintList(getResources().getColorStateList(R.color.radio_unchecked));
            communicationType = "bluetooth";
        }
        radio_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mSharedPreferences.edit().putString(Config.TYPE, "bluetooth").commit();
                    radio_bluetooth.setButtonTintList(getResources().getColorStateList(R.color.radio_checked));
                    radio_serial.setButtonTintList(getResources().getColorStateList(R.color.radio_unchecked));
                    communicationType = "bluetooth";
                }
            }
        });
        radio_serial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mSharedPreferences.edit().putString(Config.TYPE, "serial").commit();
                    radio_serial.setButtonTintList(getResources().getColorStateList(R.color.radio_checked));
                    radio_bluetooth.setButtonTintList(getResources().getColorStateList(R.color.radio_unchecked));
                    communicationType = "serial";
                }
            }
        });
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.login_entryBtn:
                //登录
                loginEvent();
                break;
            case R.id.login_deleteBtn:
                //删除用户名
                edt_username.setText("");
                break;
            case R.id.login_deletePSWBtn:
                //删除密码
                edt_password.setText("");
                break;
            case R.id.login_callBtn:
                //拨电话
                String[] str = {};
                str = (btn_call.getText().toString()).split(":");
                final String phoneNum = str[1].replaceAll("-", "");

                MyDialog.showCallDialog(LoginActivity.this, getResources().getString(R.string.login_company_name), getResources().getString(R.string.login_service_phone) + phoneNum, new MyDialog.ClickCallback() {
                    @Override
                    public void clickConfirm() {
                        call(phoneNum);
                    }

                    @Override
                    public void clickCancel() {
                    }
                });
                break;
            case R.id.login_saveText:
            case R.id.login_saveImg:
                //保存密码
                if (isSavePsw) {
                    isSavePsw = false;
                    img_save.setImageResource(R.drawable.login_notsave_psw);
                }
                else {
                    isSavePsw = true;
                    img_save.setImageResource(R.drawable.login_save_psw);
                }
                break;
            default:
                break;

        }
    }

    /**
     * 登录
     */
    private void loginEvent(){
        //输入校验
        if(edt_username.getText().toString().length() == 0){
            CommonUtil.toast(getResources().getString(R.string.login_name_hint));
            return;
        }
        if(edt_password.getText().toString().length() == 0){
            CommonUtil.toast(getResources().getString(R.string.login_psw_hint));
            return;
        }
        loadStart(getResources().getString(R.string.login_landing));
        String username = "";
        String password = "";
        try{
            String userName = URLEncoder.encode(edt_username.getText().toString(), "UTF-8");
            String passWord = URLEncoder.encode(edt_password.getText().toString(), "UTF-8");
            username = EncryptHelper.RSA_Encrypt(userName);
            password = EncryptHelper.RSA_Encrypt(passWord);
//            username = EncryptHelper.RSA_Encrypt(edt_username.getText().toString());
//            password = EncryptHelper.RSA_Encrypt(edt_password.getText().toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        params.put("uuid", GetDeviceUUID.getUUID(LoginActivity.this));

        String url = Config.BaseUrl + "login";
        HttpClient.post(LoginActivity.this, url, params, "",
                new HttpClient.httpCallBack() {
                    @Override
                    public void success(String res) {
                        try{
                            JSONObject object = new JSONObject(res);
                            String status = JsonUtils.getJSONString(object,"status");
                            String vkey = JsonUtils.getJSONString(object,"vkey");
                            application.vkey = "";
                            switch (status){
                                case "1":
                                    //用户名、密码、是否保存
                                    mSharedPreferences.edit().putString(Config.USER_NAME, edt_username.getText().toString()).commit();
                                    mSharedPreferences.edit().putString(Config.PASSWORD, edt_password.getText().toString()).commit();
                                    mSharedPreferences.edit().putBoolean(Config.IS_SAVE_PSW, isSavePsw).commit();
                                    application.vkey = vkey;
                                    getBootInfo();
                                    break;
                                case "2":
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_err_1));
                                    break;
                                case "3":
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_err_2));
                                    break;
                                case "4":
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_err_3));
                                    break;
                                case "5":
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_err_4));
                                    break;
                                default:
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_err_5));
                                    break;
                            }

                        }
                        catch (JSONException e){
                            // TODO Auto-generated catch block
                            loadStop();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void fail(String error) {
                        loadStop();
                        CommonUtil.toast(error);
                    }
                });
    }

    /**
     * 获取Boot信息
     */
    private void getBootInfo(){
        String url = Config.BaseUrl + "findBootList";
        HttpClient.post(LoginActivity.this, url, null, "",
                new HttpClient.httpCallBack() {
                    @Override
                    public void success(String res) {
                        try{
                            JSONObject object = new JSONObject(res);
                            String status = JsonUtils.getJSONString(object,"status");
                            final JSONArray content = JsonUtils.getJsonArray(object,"content");
                            switch (status){
                                case "1":
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateBoot(content);
                                        }
                                    });
                                    break;
                                default:
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_boot_err));
                                    break;
                            }
                        }
                        catch (JSONException e){
                            // TODO Auto-generated catch block
                            loadStop();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void fail(String error) {
                        loadStop();
                        CommonUtil.toast(error);
                    }
                });
    }

    /**
     * 获取产品信息
     */
    private void getProductInfo(){
        RequestParams params = new RequestParams();
        params.put("vkey", application.vkey);
        String url = Config.BaseUrl + "findProductByPc";
        HttpClient.post(LoginActivity.this, url, params, "",
                new HttpClient.httpCallBack() {
                    @Override
                    public void success(String res) {
                        try{
                            JSONObject object = new JSONObject(res);
                            String status = JsonUtils.getJSONString(object,"status");
                            JSONArray content = JsonUtils.getJsonArray(object,"content");
                            switch (status){
                                case "1":
                                    loadStop();
                                    application.productInfo = content;
                                    Intent downIntent;
                                    if(communicationType.equals("bluetooth")){
                                        downIntent = new Intent(LoginActivity.this, DownloadBluetoothActivity.class);
                                    }
                                    else{
                                        downIntent = new Intent(LoginActivity.this, DownloadSerialActivity.class);
                                    }
                                    // 登陆页进入下载
                                    downIntent.putExtra("login_down", "1");
                                    startActivity(downIntent);
                                    finish();
                                    break;
                                case "2":
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_product_err_1));
                                    break;
                                case "4":
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_product_err_2));
                                    break;
                                default:
                                    loadStop();
                                    CommonUtil.toast(getResources().getString(R.string.login_product_err_3));
                                    break;
                            }
                        }
                        catch (JSONException e){
                            // TODO Auto-generated catch block
                            loadStop();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void fail(String error) {
                        loadStop();
                        CommonUtil.toast(error);
                    }
                });
    }

    /**
     * 登录开始
     * @param msg
     */
    private void loadStart(String msg){
        f_login = true;
        if(builder == null){
            builder = new AlertDialog.Builder(LoginActivity.this, R.style.WaitProgress).create();
        }
        View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.bar_progress, null);
        TextView txt_progress = (TextView) view.findViewById(R.id.progressText);
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        txt_progress.setText(msg);
        if (!msg.equals("")) {
            Activity a = LoginActivity.this;
            if (!a.isFinishing()) {
                builder.show();
                builder.getWindow().setContentView(view);
            }
        }
        builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                //处理监听事件
                try{
                    f_login = false;
                    if(downThread == null){
                        return;
                    }
                    downThread.interrupt();
                    downThread = null;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 登录结束
     */
    private void loadStop(){
        f_login = false;
        if(builder != null && builder.isShowing()){
            builder.dismiss();
        }
    }

    /**
     * 更新Boot文件
     */
    private void updateBoot(final JSONArray content){
        downMap.clear();
        for(int i = 0; i < content.length(); i ++){
            JSONObject object = null;
            try{
                object = content.getJSONObject(i);
            }
            catch (Exception e){
                e.printStackTrace();
                continue;
            }
            if(object == null){
                continue;
            }
            else{
                String url = JsonUtils.getJSONString(object,"bootUrl");
                String bootName = JsonUtils.getJSONString(object,"bootName");
                String MD5 = JsonUtils.getJSONString(object,"MD5").toUpperCase();
                if(url == "" || url == null){
                    continue;
                }
                if(application.bootMap.containsKey(bootName)){
                    if(!MD5.equals(application.bootMap.get(bootName))){
                        downMap.put(url,bootName);
                    }
                }
                else{
                    downMap.put(url,bootName);
                }
            }
        }
        //下载
        downThread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient.downBootFile(LoginActivity.this, downMap, new HttpClient.httpCallBack() {
                    @Override
                    public void success(String res) {
                        //发送消息到主线程
                        if(f_login){
                            bootMD5();
                            handler.sendEmptyMessage(1);
                        }
                    }

                    @Override
                    public void fail(String error) {
                        Log.d("downFail", "fail");
                    }
                });
            }
        });
        downThread.start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //获取产品信息
                    getProductInfo();
                    break;
            }
        }
    };

    /**
     * 计算Boot文件MD5值
     */
    private void bootMD5(){
        application.bootMap.clear();
        File file = new File(Config.BOOT_PATH);
        File[] files = file.listFiles();
        if(files != null){
            for(int i = 0; i < files.length; i ++){
                String path = Config.BOOT_PATH + files[i].getName();
                File f = new File(path);
                String MD5 = EncryptHelper.getFileMD5(f);
                application.bootMap.put(files[i].getName(), MD5);
            }
        }
    }

    /**
     * 拨打电话
     * @param phoneNum 电话号码
     */
    private void call(String phoneNum){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(LoginActivity.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                MPermissions.requestPermissions(LoginActivity.this, 0, android.Manifest.permission.CALL_PHONE);
            }
            else{
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
                startActivity(intent);
            }
        }
        else{
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
            startActivity(intent);
        }
    }

    /**
     * 权限设置
     */
    private void checkPermissins(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION};
            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(LoginActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            // 全部允许
            if (mPermissionList.isEmpty()) {
                pgyInit();
            }
            else {
                String[] permissionsArr = mPermissionList.toArray(new String[mPermissionList.size()]);
                MPermissions.requestPermissions(LoginActivity.this, 0, permissionsArr);
            }
        }
        else{
            pgyInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                boolean f_permission = false;
                for(int i = 0; i < permissions.length; i ++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        f_permission = true;
                    }
                }
                if(f_permission){
                    MyDialog.showNormalDialog(LoginActivity.this, getResources().getString(R.string.permission), getResources().getString(R.string.permission_settings), new MyDialog.ClickCallback() {
                        @Override
                        public void clickConfirm() {
                            setPermission();
                        }

                        @Override
                        public void clickCancel() {
                            application.exit();
                        }
                    });
                }
                else{
                    pgyInit();
                }
                break;
            default:
                break;

        }
    }

    /**
     * 蒲公英检测更新
     */
    private void pgyInit(){
        new PgyUpdateManager.Builder()
                .setForced(true)
                .setUserCanRetry(false)
                .setDeleteHistroyApk(false)
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        Log.d("pgyer", "there is no new version");
                    }
                    @Override
                    public void onUpdateAvailable(AppBean appBean) {
                        Log.d("pgyer", "there is new version can update"
                                + "new versionCode is " + appBean.getVersionCode());
                        String new_version = appBean.getVersionName();
                        final AppBean myAppBean = appBean;
                        MyDialog.showUpdataDialog(LoginActivity.this, new_version, new MyDialog.ClickCallback(){
                            @Override
                            public void clickConfirm(){
                                PgyUpdateManager.downLoadApk(myAppBean.getDownloadURL());
                            }
                            @Override
                            public void clickCancel(){

                            }
                        });
                    }
                    @Override
                    public void checkUpdateFailed(Exception e) {
                        Log.e("pgyer", "check update failed ", e);
                    }
                })
                .register();
    }

    /**
     * 打开权限设置
     */
    private void setPermission(){
        String brand = Build.BRAND;
        if(brand.toLowerCase().equals("redmi") || brand.toLowerCase().equals("xiaomi")){
            gotoMiuiPermission();
        }
        else if(brand.toLowerCase().equals("huawei") || brand.toLowerCase().equals("honor")){
            gotoHuaweiPermission();
        }
        else if(brand.toLowerCase().equals("meizu")){
            gotoMeizuPermission();
        }
        else{
            startActivity(getAppDetailSettingIntent());
        }
        application.exit();
    }


    private Intent getAppDetailSettingIntent(){
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private void gotoMiuiPermission() {
        // MIUI 8
        try {
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", LoginActivity.this.getPackageName());
            LoginActivity.this.startActivity(localIntent);
        } catch (Exception e) {
            // MIUI 5/6/7
            try {
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", LoginActivity.this.getPackageName());
                LoginActivity.this.startActivity(localIntent);
            } catch (Exception ex) { // 否则跳转到应用详情
                startActivity(getAppDetailSettingIntent());
            }
        }
    }

    /**
     * 华为的权限管理页面
     */
    private void gotoHuaweiPermission() {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private void gotoMeizuPermission() {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }
    }
}
