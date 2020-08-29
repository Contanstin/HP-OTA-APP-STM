package com.hpmont.hpprog.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import com.pgyersdk.crash.PgyCrashManager;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyApplication extends Application {

    public String vkey = "";
    public JSONArray productInfo = null;
    public Map<String,String> bootMap= new HashMap<>();
    public boolean connectState;
    private List<Activity> activityList;
    private static MyApplication instance;

    //接收数据类型（0：0x41；1：boot；2：order）
    public int receiveType = 0;
    //Boot文件缓存
    public byte[] bootBytes;
    public byte[] bootBytes_1;
    public byte[] bootBytes_2;
    public byte[] sendBoot;
    //Boot文件类型
    public int f_bootType = 0;
    //版本号
    public int versionCode = 0;
    //硬件编号
    public static byte hardwareCode=0;
    //中止下载标志位
    public boolean f_stopdownload = false;
    //ID
    public String softwareId = null;
    //Hex文件名
    public String hexFileName = null;
    //Hex版本号
    public String hexFileVersion = null;
    //软件版本发生改变标志位（true：改变；false：未改变）
    public boolean f_hexChange = false;
    //解锁密钥地址（Hex文件中）
    public String unlockKeyAddr = null;
    //Hex文件缓存
    public HexStruct[] hexStructs;
    //bin文件缓存
    public HexStruct[] hexStructs_STM32_bin;
    //Flash空间
    public static long[] flashRoom;
    //Flash起始地址
    public static long flashStart=0;
    //Flash结束地址
    public static long flashEnd=0;
    //Hex文件长度
    public int hexLength = 0;
    //Hex校验值
    public byte[] flashCheck = new byte[4];
    //bin文件校验值
    public byte[] flashCheckOut=new byte[4];
    //Flash解锁密钥
    public String secretKeyStr = null;
    //随机数A1, A2, B, C
    public long A1, A2, B, C;
    //通信数据加密KEY值
    public byte[] encryptionKey;
    //芯片类型（0为DSP）芯片
    public static int chipType=0;
    @Override
    public void onCreate(){
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
        activityList = new LinkedList<Activity>();
        initPgyCrash();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    public static Application getInstance() {
        return instance;
    }

    /**
     * 添加Activity
     * @param activity
     */
    public void addActivity(Activity activity){
        if(!activityList.contains(activity)){
            activityList.add(activity);
        }
    }

    /**
     * 销毁单个Activity
     * @param activity
     */
    public void removeActivity(Activity activity){
        if(activityList.contains(activity)){
            activityList.remove(activity);
            activity.finish();
        }
    }

    /**
     * 销毁所有Activity
     */
    public void removeAllActivity(){
        for(Activity activity : activityList){
            activity.finish();
        }
    }

    /**
     * 蒲公英Crash启动
     */
    private void initPgyCrash(){
        PgyCrashManager.register();
    }

    /**
     * 退出程序
     */
    public void exit(){
        removeAllActivity();
        //关闭蓝牙服务
        System.exit(0);
    }
}
