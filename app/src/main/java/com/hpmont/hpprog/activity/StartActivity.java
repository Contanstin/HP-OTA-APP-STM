package com.hpmont.hpprog.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.hpmont.hpprog.R;
import com.hpmont.hpprog.base.BaseActivity;
import com.hpmont.hpprog.config.Config;
import com.hpmont.hpprog.util.CommonUtil;
import com.hpmont.hpprog.util.EncryptHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class StartActivity extends BaseActivity {
    private ImageView img_start;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_start);
        init();
        initBoot();
        bootMD5();
    }

    /**
     * 初始化
     */
    private void init(){
        img_start = this.findViewById(R.id.startImg);

        mSharedPreferences = getSharedPreferences(Config.FILE_NAME, Context.MODE_PRIVATE);

        //旧语言码
        String oldlanguage = mSharedPreferences.getString(Config.LANGUAGE,"");

        //版本号
        int versionCode = mSharedPreferences.getInt(Config.VERSION_CODE, 0);

        // 获得当前语言码
        String language = getResources().getConfiguration().locale.getLanguage();

        //保存语言
        mSharedPreferences.edit()
                .putString(Config.LANGUAGE, language).commit();

        //保存最新版本号
        mSharedPreferences
                .edit()
                .putInt(Config.VERSION_CODE, CommonUtil.getAppVersionCode(this))
                .commit();

        alphaAnimation(2000);
    }

    /**
     * Boot文件初始化
     */
    private void initBoot(){
        File file = new File(Config.BOOT_PATH);
        if(!file.exists()){
            file.mkdirs();
            String[] fileList = null;
            try {
                fileList = this.getResources().getAssets().list("data");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if(fileList == null){
                return;
            }
            else{
                for(int i = 0; i < fileList.length; i ++){
                    String path = Config.BOOT_PATH + fileList[i];
                    File f = new File(path);
                    if(!f.exists()){
                        try{
                            InputStream inputStream = getResources().getAssets().open("data/" + fileList[i]);
                            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while ((len = inputStream.read(buffer, 0, 1024)) != -1){
                                fileOutputStream.write(buffer,0, len);
                            }
                            inputStream.close();
                            fileOutputStream.close();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Log.e("boot_error", e.toString());
                        }
                    }
                }
            }
        }
    }

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
     * APP启动动画
     * @param delayMillis 动画时间
     */
    private void alphaAnimation(long delayMillis){
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);
        //AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.5f);
        // 设定动画时间
        alphaAnimation.setDuration(delayMillis);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        img_start.setAnimation(alphaAnimation);
        alphaAnimation.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //发送消息到主线程
                handler.sendEmptyMessage(1);
            }
        }, delayMillis); //延时delayMillis毫秒
    }

    //定时器
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 切换到登陆页面
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
}
