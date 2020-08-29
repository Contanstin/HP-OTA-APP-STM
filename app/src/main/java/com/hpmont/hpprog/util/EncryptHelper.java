package com.hpmont.hpprog.util;

import android.content.Context;
import com.google.common.primitives.Bytes;
import com.hpmont.hpprog.util.apaches.binary.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import static com.hpmont.hpprog.util.CommonUtil.bytesToHexString;


public class EncryptHelper {
    private static String publicKey ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+hXNe+glE/YS6mmTRMmamntWxoMutzjIAvfd4umB4Fnm/0hs+5SeyOwplmHEia95nngJRwxngTdjqhm4zb5MBIqsphkaXqcVKg/0zPtlP8v+sB/WmkYzQQiraqMBbx+0qtltxVwmUR7JtN2lRmSkPTYAX2JijagQGOoeG3vX29QIDAQAB";
    private static String privateKey  ="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAL6Fc176CUT9hLqaZNEyZqae1bGgy63OMgC993i6YHgWeb/SGz7lJ7I7CmWYcSJr3meeAlHDGeBN2OqGbjNvkwEiqymGRpepxUqD/TM+2U/y/6wH9aaRjNBCKtqowFvH7Sq2W3FXCZRHsm03aVGZKQ9NgBfYmKNqBAY6h4be9fb1AgMBAAECgYBuozyuSPEdGUP8wTAOtAAcflyGPGRLWFR9TdPqTgE7e4HUPQUJbzWwMM0G1pHMdWvALGgYZv4d3dc7yjkwsXhhzW+HPyK3gTbckSsomV6lOVX5E/ZYQ4f6XsP+17p+8AcbGn3GvrmjEzgDF3E7Pjvks2Oo5snVc5/zHBrm3u2UAQJBAOrCo3TxC7k8aV0aQAeni3QxDbsDcQygYJ6/PCIYZv0oc3PQDtoeOx7MBKzQdWP9rn1R4DXFkXjIxfLNsjYcgoECQQDPwi15aHGqgjHbbf1sSR5EseSiUt5RFkM5eXAx+RMBGvu5yYsc9JeJSNfNeHXcFqdvAYmW8L8B3RL4N7tvoVJ1AkEAi3DHBDv02bbfYpSn+aPz+jT00eMub/CG02QFhL731WEEioLHf5k7RoSqNjevso/I59kNEwNh79tZcGnrc6algQJBALuqnCch+CLcTir3FMb+2U+WHX+fOWCnqnu9PWJ2qfsCo8XzzyyNqGCDIyRgHp56/C1ihyWIPBFz4BHFThOM9o0CQQCxAwumni2evwmgkgP0QIgNZC3vD7Jrxx0NLJQ6raewg1XpulG2vuSLoZ84Egp7RSDuw2WcziqvdYUcuz218MlD";
    private static String keyName="RSA";

    /**
     * RSA公钥加密
     * @param str 待加密字符
     * @return 密文
     * @throws Exception
     */
    public static String RSA_Encrypt( String str) throws Exception{
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(keyName).generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    /**
     * RSA私钥解密
     * @param str 密文
     * @return 解密字符
     * @throws Exception
     */
    public static String RSA_Decrypt(String str) throws Exception{
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(keyName).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }

    /**
     * 获取文件MD5值(assets)
     * @param fileName 文件名
     * @param context
     * @return
     */
    public static String GetFileMD5(String fileName, Context context){
        MessageDigest digest = null;
        InputStream  in;
        byte buffer[] = new byte[1024];
        int len = 0;
        try {
            in = context.getResources().getAssets().open("data/" + fileName);
            digest = MessageDigest.getInstance("MD5");
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    /**
     * 获取文件MD5值
     * @param file 文件
     * @return
     */
    public static String getFileMD5(File file){
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream fileInputStream = null;
        byte buffer[] = new byte[1024];
        int len = 0;
        try {
            digest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            while ((len = fileInputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            fileInputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    /**
     * Hex文件解密
     * @param bytes
     * @param opmode
     * @return
     * @throws Exception
     */
    private static byte[] hex_Decrypt(byte[] bytes, int opmode) throws Exception{
        String key = "hpmont_hpprog";
        byte[] keyArray = new byte[32];
        if(key.length() >= keyArray.length){
            key = key.substring(0,keyArray.length);
        }
        else{
            int length = key.length();
            for(int i = 0; i < (keyArray.length - length); i ++){
                key += " ";
            }
        }
        keyArray = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(keyArray, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        cipher.init(opmode, skeySpec);
        byte[] decrypted = cipher.doFinal(bytes, 0, bytes.length);
        return decrypted;
    }

    /**
     * byte数组格式转换
     * @param bytes
     * @return
     */
    public static byte[] getBytes(byte[] bytes){
        int count = (int)Math.ceil((double)bytes.length / 1040);
        ArrayList<Byte> hexList = new ArrayList<>();
        try{
            for(int i = 0; i < count; i ++){
                byte[] stepBytes;
                if((i + 1) * 1040 > bytes.length){
                    stepBytes = new byte[bytes.length - (count - 1) * 1040];
                }
                else {
                    stepBytes = new byte[1040];
                }
                for (int j = 0; j < stepBytes.length; j++) {
                    stepBytes[j] = bytes[i * 1040 + j];
                }
                byte[] tempByte = hex_Decrypt(stepBytes, Cipher.DECRYPT_MODE);
                for (int j = 0; j < tempByte.length; j++) {
                    hexList.add(tempByte[j]);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return Bytes.toArray(hexList);
    }
}
