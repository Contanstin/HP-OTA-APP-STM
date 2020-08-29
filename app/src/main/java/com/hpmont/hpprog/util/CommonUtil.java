package com.hpmont.hpprog.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.hpmont.hpprog.base.MyApplication;

import java.util.Random;

public class CommonUtil {
    private static MyApplication application = (MyApplication) MyApplication.getInstance();
    /**
     * 消息提示
     * @param text 提示文本
     */
    public static void toast(String text) {
        Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取versionName
     * @param context
     * @return versionName
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取versionCode
     * @param context
     * @return versionCode
     */
    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 功能：将字节数组转化为16进制字符串
     * @param bytes
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase());
        }
        return stringBuilder.toString();
    }

    /**
     * 获取DSP解锁密钥
     * @param line
     * @return
     */
    public static String getSecretKey(String line) {
        String secretKeyStr = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        secretKeyStr = line.substring(0, 32);
        return secretKeyStr;
    }

    /**
     * 数组合并
     * @param first
     * @param second
     * @return
     */
    public static byte[] mergerArray(byte[] first, byte[] second){
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * long转byte数组（高位在前，低位在后）
     * @param value
     * @param length
     * @return
     */
    public static byte[] longToBytes(long value,int length) {
        if (length <= 0 || length > 4) {
            return null;
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i ++) {
            bytes[length - i - 1] = (byte)((value >> i * 8) & 0xFF);
        }
        return bytes;
    }

    /**
     * 循环右移数组
     * @param data
     * @param n
     * @return
     */
    public static byte[] moveArrayR(byte[] data, int n) {
        int length = data.length;
        byte[] data2 = new byte[length];
        for (int i = 0; i < length; i++) {
            data2[(i + n) % length] = data[i];
        }
        return data2;
    }

    /**
     * 循环左移数组
     * @param data
     * @param n
     * @return
     */
    public static byte[] moveArrayL(byte[] data, int n){
        int length = data.length;
        byte[] data2 = new byte[length];
        for (int i = 0; i < length; i ++) {
            data2[i] = data[(i + n) % length];
        }
        return data2;
    }

    /**
     * 数据加密
     * @param bytes
     * @param A1
     * @param A2
     * @param B
     * @return
     */
    public static byte[] dataEncryption(byte[] bytes, long A1, long A2, long B){
        if(bytes == null){
            return null;
        }
        byte[] data = new byte[bytes.length];
        byte tempB = longToBytes(B, 1)[0];
        byte[] tempA1 = longToBytes(A1,2);
        if (bytes.length < 5) {
            for (int i = 0; i < bytes.length; i ++) {
                data[i] = (byte)(bytes[i] ^ tempB);
            }
            return data;
        }
        else {
            for (int i = 0; i < bytes.length; i ++) {
                if (i % 2 == 0) {
                    data[i] = (byte)(bytes[i] ^ tempA1[0]);
                }
                else {
                    data[i] = (byte)(bytes[i] ^ tempA1[1]);
                }
            }
            int A2H = (int)((A2 >> 4) & 0x0F);
            int A2L = (int)(A2 & 0x0F);
            int times = bytes.length / 16;
            for (int i = 0; i < times; i++) {
                byte[] tempBytes = new byte[16];
                for (int j = 0; j < 16; j++) {
                    tempBytes[j] = data[16 * i + j];
                }
                for (int j = 0; j < A2L; j++) {
                    byte tempByte = tempBytes[A2H];
                    tempBytes[A2H] = tempBytes[0];
                    tempBytes[0] = tempByte;
                    tempBytes = moveArrayR(tempBytes, 3);
                }
                for (int j = 0; j < 16; j++) {
                    data[16 * i + j] = tempBytes[j];
                }
            }
            return data;
        }
    }

    /**
     * 数据解密
     * @param bytes
     * @param A1
     * @param A2
     * @param B
     * @return
     */
    public static byte[] dataDecryption(byte[] bytes, long A1, long A2, long B){
        if(bytes == null){
            return null;
        }
        byte[] data = bytes;
        byte tempB = longToBytes(B, 1)[0];
        byte[] tempA1 = longToBytes(A1, 2);
        if (bytes.length < 5) {
            for (int i = 0; i < bytes.length; i++) {
                data[i] = (byte)(bytes[i] ^ tempB);
            }
            return data;
        }
        else {
            int A2H = (int)((A2 >> 4) & 0x0F);
            int A2L = (int)(A2 & 0x0F);
            int times = bytes.length / 16;
            for (int i = 0; i < times; i++) {
                byte[] tempBytes = new byte[16];
                for (int j = 0; j < 16; j++) {
                    tempBytes[j] = data[16 * i + j];
                }
                for (int j = 0; j < A2L; j++) {
                    tempBytes = moveArrayL(tempBytes, 3);
                    byte tempByte = tempBytes[A2H];
                    tempBytes[A2H] = tempBytes[0];
                    tempBytes[0] = tempByte;
                }
                for (int j = 0; j < 16; j++) {
                    data[16 * i + j] = tempBytes[j];
                }
            }
            for (int i = 0; i < bytes.length; i++) {
                if (i % 2 == 0) {
                    data[i] = (byte) (bytes[i] ^ tempA1[0]);
                } else {
                    data[i] = (byte) (bytes[i] ^ tempA1[1]);
                }
            }
            return data;
        }
    }

    /**
     * 生成加密密钥
     * @param C
     * @return
     */
    public static byte[] buildKey(MyApplication application, long C){
        application.A1 = random(1, 65535, true);
        application.A2 = (random(2, 14, false) << 4) | random(2, 5, false);
        application.B = random(1, 255, true);
        long A1a, A2a, Key;
        A1a = application.A1 ^ ((application.B << 8) | application.B);
        A2a = application.A2 ^ application.B;
        long Ab = (A1a << 8) | A2a;
        Key = ((application.B ^ C) << 24) | (((Ab << 19) | (Ab >> 5)) & 0xFFFFFF);
        return longToBytes(Key, 4);
    }

    /**
     * 生成随机整数
     * @param min
     * @param max
     * @param condition
     * @return
     */
    public static long random(int min, int max, boolean condition){
        Random rd = new Random();
        if(!condition){
            long data = (long)(min + Math.random() * (max - min));
            return data;
        }
        int binaryLength = 0;
        if(max < 255){
            binaryLength = 8;
        }
        else if(255 < max && max <= 65535){
            binaryLength = 16;
        }
        else {
            binaryLength = 32;
        }
        long startTime = System.currentTimeMillis();
        while (true) {
            long data = (long)(min + Math.random() * (max - min));
            String dataStr = Long.toBinaryString(data);//.substring(0, binaryLength);
            int count_1 = 0;
            int count_0 = binaryLength - dataStr.length();
            char[] charAsrry = dataStr.toCharArray();
            for (int i = 0; i < charAsrry.length; i++) {
                if (charAsrry[i] == '0') {
                    count_0++;
                }
                else {
                    count_1++;
                }
            }
            if (count_0 > 1 && count_1 > 1) {
                return data;
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime - startTime > 10000) {
                return 3;
            }
        }
    }

    /**
     * 密钥转byte数组
     * @param secretKey
     * @return
     */
    public static byte[] secretKeyToByte(String secretKey){
        if(secretKey == null || secretKey.length() == 0){
            return null;
        }
        int length = secretKey.length();
        byte[] bytes = new byte[length / 2];
        for(int i = 0; i < length; i += 2){
            int temp = Integer.parseInt(secretKey.substring(i, i + 2), 16);
            bytes[i / 2] = (byte)temp;//Byte.parseByte(secretKey.substring(i, i + 2), 16);
        }
        return bytes;
    }

    public static Bitmap getBitmap(Activity activity){
        View bgView = activity.getWindow().getDecorView();
        bgView.setDrawingCacheEnabled(true);
        bgView.buildDrawingCache(true);
        /**
         * 获取当前窗口快照，相当于截屏
         */
        Bitmap bmp = bgView.getDrawingCache();
        return bmp;
    }

    public static Bitmap getBlurBitmap(int scaleFactor, int radius, Bitmap bitmap){
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (bitmap.getWidth() / scaleFactor),
                (int) (bitmap.getHeight() / scaleFactor),
                false);
        scaledBitmap = FastBlur.doBlur(scaledBitmap, (int) radius, true);
        return scaledBitmap;
    }
}
