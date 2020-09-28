package com.hpmont.hpprog.config;

import android.os.Environment;

import java.util.UUID;

public class Config {
    //HTTP访问URL
    //public static final String BaseUrl = "https://www.hpmontserver.com/HPProg/pc/";
    //public static final String BaseUrl = "http://120.79.59.8:8080/HPProg/pc/";
//    public static final String BaseUrl = "http://113.108.115.34:3388/HPProg/pc/";
     //public static final String BaseUrl="http://120.79.59.8:8080/HPProg/pc/";
//   public static final String BaseUrl="http://192.168.2.232:8089/PCUser/";
    //public static final String BaseUrl= "http://120.79.59.8:8080/PCUser/";
    public static final String BaseUrl= "http://113.108.115.34:8081/PCUser/";
    public static final String BOOT_PATH = Environment.getExternalStorageDirectory() + "/HPProg/data/";
    //

    //SharedPreferences文件名
    public static final String FILE_NAME = "HPProg";

    //语言
    public static final String LANGUAGE = "Language";

    //版本号
    public static final String VERSION_CODE = "VersionCode";

    // 用户密码
    public static final String PASSWORD = "Passeword";

    // 用户名
    public static final String USER_NAME = "UserName";

    //是否保存密码
    public static final String IS_SAVE_PSW = "Save_PSW";

    //通信类型
    public static final String TYPE = "Type";

    //主板连接
    public static final byte CONNECT_BYTE = 0x41;

    //产品型号（28335）
    public static final String PRODUCT_335 = "TMS320F28335";

    //产品型号（28034）
    public static final String PRODUCT_034 = "TMS320F28034";

    //产品型号（28062）
    public static final String PRODUCT_062 = "TMS320F28062";

    //产品型号（28069）
    public static final String PRODUCT_069 = "TMS320F28069";
    //产品型号（STM32）
    public static final String PRODUCT_STM ="STM32";

    //帧头
    public static final int FRAME_HEAD = 0xAA;

    //地址

    public static final byte[] FRAME_ADDR = {(byte) 0xFF};

    //帧长
    public static final int FRAME_LENGTH = 0x04;

    public static final byte DEF   = (byte)0x00;   //NULL
    public static final byte CMD   = (byte)0x01;   //指令
    public static final byte KEY   = (byte)0x02;   //解锁密钥
    public static final byte DAT   = (byte)0x03;   //flash数据
    public static final byte BAND  = (byte)0x04;   //波特率切换
    public static final byte Hardware=(byte)0x11;  //询问硬件编号
    public static final byte RAN   = (byte)0x05;   //随机数
    public static final byte CHECK = (byte)0x06;   //全片校验
    public static final byte VER   = (byte)0x10;   //版本号
    public static final byte PWD   = (byte)0x20;   //动态密钥发送位
    public static final byte IDLE  = (byte)0xFF;   //空闲

    //CMD:
    public static final byte CMD_UNLOCK     = (byte)0x01;   //Flash解锁
    public static final byte CMD_CLRFLASH   = (byte)0x02;   //擦除Flash
    public static final byte CMD_CLRKEY     = (byte)0x03;   //清空Key缓存
    public static final byte CMD_EXIT       = (byte)0x04;   //退出Boot
    public static final byte CMD_RESEND     = (byte)0x05;   //重复上次的发送
    public static final byte CMD_LOCK       = (byte)0x06;   //写锁定
    public static final byte CMD_CLRDATE    = (byte)0x07;   //存储数据擦除
    public static final byte CMD_CLROUTFLASH= (byte)0x08;   //片外擦除FLASH
    public static final byte CMD_CLRFLASH_A = (byte)0x0A;   //擦除Flash A
    public static final byte CMD_CLRFLASH_B = (byte)0x0B;   //擦除Flash B
    public static final byte CMD_CLRFLASH_C = (byte)0x0C;   //擦除Flash C
    public static final byte CMD_CLRFLASH_D = (byte)0x0D;   //擦除Flash D
    public static final byte CMD_CLRFLASH_E = (byte)0x0E;   //擦除Flash E
    public static final byte CMD_CLRFLASH_F = (byte)0x0F;   //擦除Flash F
    public static final byte CMD_CLRFLASH_G = (byte)0x10;   //擦除Flash G
    public static final byte CMD_CLRFLASH_H = (byte)0x11;   //擦除Flash H

    public static final byte FAILED  = (byte)0x00;
    public static final byte SUCCESS = (byte)0x01;
}
