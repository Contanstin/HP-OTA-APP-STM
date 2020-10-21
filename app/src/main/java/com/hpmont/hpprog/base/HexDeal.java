package com.hpmont.hpprog.base;

import android.util.Log;

import com.hpmont.hpprog.config.Config;
import com.hpmont.hpprog.util.CommonUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.hpmont.hpprog.util.EncryptHelper.RSA_Decrypt;
import static java.lang.Math.abs;


public class HexDeal<Products>{
    private static MyApplication application = (MyApplication) MyApplication.getInstance();
    private static List<HexStruct> hexStructs = new ArrayList<>();
    private static String[] productsInfo=null ;
//    private static ArrayList<Bin>binStructs=new ArrayList<>();
//    private static ChipType chiptypeStructs=new ChipType();
//    /**判断芯片类型是DSP或者STM_32
//     * @param chiptype
//     * @return
//     */
//    public static boolean chipType(String[] chiptype) throws Exception{
//      int cptype=0;
//      ChipType chip=new ChipType();
//      for(int i=0;i<chiptype.length;i++){
//          if(chiptype[i].substring(0,1).equals("&")){
//              cptype=1;
//          }
//          break;
//      }
//      chip.setChipType(cptype);
//      chiptypeStructs=chip;
//     return true;
//    }
//    /**
//     * @param binfile
//     * @return
//     */
//    public static boolean binFile(String[] binfile) throws  Exception{
//        binStructs.clear();
//        for(int i=0;i<binfile.length;i++){
//            if(binfile[i].substring(0,1).equals("&")){
//                String bin=new String();
//                Bin binStruct=new Bin();
//                bin=binfile[i].substring(1);
//                binStruct.getData(bin);
//                binStructs.add(binStruct);
//            }
//        }
//        return true;
//    }
    /**
     * @param products 加密的字符串
     * @return
     */
    public static boolean productsReload(String[] products) throws Exception {
        productsInfo=null;
        for (int i = 0; i < products.length; i++) {
            if (products[i].substring(0, 1).equals("*")) {
                String str = "";

                str=products[i].substring(1);
                String item[]=str.split("/");
                productsInfo=item;
            }
        }
        return true;
    }
    /**
     * 获取Hex文件
     *
     * @param hexArray
     * @return
     */
    public static boolean getHex(String[] hexArray) throws Exception {
        hexStructs.clear();
        if (application.unlockKeyAddr!=null&&application.unlockKeyAddr/*解锁密钥地址（Hex文件中）*/.equals("0X33FFF8")) {
            HexStruct hexstructkey = new HexStruct();
            byte[] tempAddrkey = new byte[4];
            tempAddrkey[0] = 0;
            tempAddrkey[1] = 51;
            tempAddrkey[2] = (byte) 255;
            tempAddrkey[3] = (byte) 248;
            hexstructkey.setAddr(tempAddrkey);
            hexStructs = hexReload(hexArray, "0033", "FFF8", hexstructkey);

        } else if (application.unlockKeyAddr!=null&&application.unlockKeyAddr.equals("0X3F7FF8")) {
            HexStruct hexstructkey = new HexStruct();
            byte[] tempAddrkey = new byte[4];
            tempAddrkey[0] = 0;
            tempAddrkey[1] = 63;
            tempAddrkey[2] = 127;
            tempAddrkey[3] = (byte) 248;
            hexstructkey.setAddr(tempAddrkey);
            hexStructs = hexReload(hexArray, "003F", "7FF8", hexstructkey);
        } else if(application.chipType==1){
            hexStructs=hexReload_STM32(hexArray);
        } else{
            return false;
        }
        HexStruct[] tempStruct = new HexStruct[hexStructs.size()];
        application.hexStructs = hexStructs.toArray(tempStruct);
        application.hexLength = application.hexStructs.length;
        return true;
    }

    //hexReload_STM32芯片
    public static List<HexStruct> hexReload_STM32(String[] lines) throws Exception {
        List<HexStruct> hexStructs = new ArrayList<>();
        String[] hexLines = lines;
        for (int i = 0; i < hexLines.length; i++) {
            if (hexLines[i].substring(0, 1).equals("#")) {
                long rd = Long.parseLong(hexLines[i].substring(1, 9), 16);
                long check = Long.parseLong(hexLines[i].substring(9, 17), 16) ^ rd;
                byte[] checkValue = new byte[4];
                checkValue[0] = (byte) ((check & 0xFF000000) >> 24);
                checkValue[1] = (byte) ((check & 0x00FF0000) >> 16);
                checkValue[2] = (byte) ((check & 0x0000FF00) >> 8);
                checkValue[3] = (byte) (check & 0x000000FF);
                application.flashCheck/*Hex校验值*/ = checkValue;
                String str = hexLines[i].substring(17, 49);
                String tempStr = "";
                for (int j = 0; j < str.length(); j += 8) {
                    long tempValue = Long.parseLong(str.substring(j, j + 8), 16) ^ rd;
                    tempStr += String.format("%08x", tempValue).toUpperCase();
                }

            } else if (hexLines[i].substring(0, 1).equals("+")) {
                HexStruct hexStruct = new HexStruct();
                long rd = Long.parseLong(hexLines[i].substring(1, 9), 16);
                Log.d("hexReload", hexLines[i].substring(9, 17));
                long ad = Long.parseLong(hexLines[i].substring(9, 17), 16);
                byte length = (byte) Integer.parseInt(hexLines[i].substring(17, 19), 16);
                byte[] recordLength = new byte[1];
                recordLength[0] = (byte) ((length ^ rd) & 0xFF);
                hexStruct.setRecordLength(recordLength);
                String tempad = String.format("%08x"/*转化为16进制*/, (rd ^ ad)).toUpperCase/*将字符串小写字符转换为大写*/();
                byte[] tempaddr = new byte[4];
                tempaddr[0] = (byte) Integer.parseInt(tempad.substring(0, 2), 16);
                tempaddr[1] = (byte) Integer.parseInt(tempad.substring(2, 4), 16);
                tempaddr[2] = (byte) Integer.parseInt(tempad.substring(4, 6), 16);
                tempaddr[3] = (byte) Integer.parseInt(tempad.substring(6, 8), 16);
                hexStruct.setAddr(tempaddr);
                String data = hexLines[i].substring(19, 19 + recordLength[0] * 4);
                if (data.length() < 8) {
                    String tempStr = data;
                    long last = Long.parseLong(tempStr, 16);
                    long bit;
                    if (data.length() == 6) {
                        bit = 0xFFFFFF;
                    } else if (data.length() == 4) {
                        bit = 0xFFFF;
                    } else if (data.length() == 2) {
                        bit = 0xFF;
                    } else {
                        bit = 0xFFFFFFFF;
                    }
                    long temp = last ^ (rd & bit);
                    String xor = String.format("%08x", temp).toUpperCase();
                    StringBuffer buffer = new StringBuffer(data);
                    buffer.delete(0, data.length());
                    buffer.append(xor);
                    data = buffer.toString();
                } else {
                    String tempStr = data.substring(data.length() - 8, data.length());
                    long last = Long.parseLong(tempStr, 16);
                    long temp = last ^ rd;
                    String xor = String.format("%08x", temp).toUpperCase();
                    StringBuffer buffer = new StringBuffer(data);
                    buffer.delete(data.length() - 8, data.length());
                    buffer.append(xor);
                    data = buffer.toString();
                }
                if (MyApplication.chipType == 1) {
                    StringBuilder str = new StringBuilder();
                    for (int j = 0; j < data.length(); j += 4) {
                        long tmp = Long.parseLong(data.substring(j, j + 4), 16);
                        if (tmp != 0 && tmp != 0x5B38) {
                            tmp ^= 0x5B38;
                        }
                        String s = String.format("%04x", tmp).toUpperCase();
                        str.append(s);
                    }
                    data = str.toString();
                }
                byte[] tempdata = new byte[data.length() / 2];
                for (int j = 0; j < data.length(); j += 2) {
                    tempdata[j / 2] = (byte) Integer.parseInt/*转化为整形*/(data.substring(j, j + 2), 16);
                }
                hexStruct.setData(tempdata);
                hexStructs.add(hexStruct);
            }
        }
        application.flashStart = 0x8002000;
        if(productsInfo!=null) {
            int result = Integer.parseInt(productsInfo[1].substring(2), 16);
            application.flashRoom = new long[result];
            for (int i = 0; i < application.flashRoom.length; i++) {
                application.flashRoom[i] = 0xFF;
            }
            long[] bytes = FlashRoom(hexStructs);
            application.flashCheck = CheckHex(bytes);
        }
        return hexStructs;
    }

//hexReload DSP芯片
    public static List<HexStruct> hexReload(String[] lines, String offSetAddr, String addr, HexStruct hexstruckkey) throws Exception {
        List<HexStruct> hexStructs = new ArrayList<>();
        String[] hexLines = lines;
        for (int i = 0; i < hexLines.length; i++) {
            if (hexLines[i].substring(0, 1).equals("#")) {
                long rd = Long.parseLong(hexLines[i].substring(1, 9), 16);
                long check = Long.parseLong(hexLines[i].substring(9, 17), 16) ^ rd;
                byte[] checkValue = new byte[4];
                checkValue[0] = (byte) ((check & 0xFF000000) >> 24);
                checkValue[1] = (byte) ((check & 0x00FF0000) >> 16);
                checkValue[2] = (byte) ((check & 0x0000FF00) >> 8);
                checkValue[3] = (byte) (check & 0x000000FF);
                application.flashCheck/*Hex校验值*/ = checkValue;
                String str = hexLines[i].substring(17, 49);
                String tempStr = "";
                for (int j = 0; j < str.length(); j += 8) {
                    long tempValue = Long.parseLong(str.substring(j, j + 8), 16) ^ rd;
                    tempStr += String.format("%08x", tempValue).toUpperCase();
                }
                application.secretKeyStr/*Flash解锁密钥*/ = CommonUtil.getSecretKey(tempStr);
                byte[] recordLengthKey = new byte[1];
                recordLengthKey[0] = (byte) (application.secretKeyStr.length() / 4);
                hexstruckkey.setRecordLength(recordLengthKey);
                hexstruckkey.setData(CommonUtil.secretKeyToByte(application.secretKeyStr));
                hexStructs.add(0, hexstruckkey);
            } else if (hexLines[i].substring(0, 1).equals("+")) {

                    HexStruct hexStruct = new HexStruct();
                    long rd = Long.parseLong(hexLines[i].substring(1, 9), 16);
                    Log.d("hexReload", hexLines[i].substring(9, 17));
                    long ad = Long.parseLong(hexLines[i].substring(9, 17), 16);
                    byte length = (byte) Integer.parseInt(hexLines[i].substring(17, 19), 16);
                    byte[] recordLength = new byte[1];
                    recordLength[0] = (byte) ((length ^ rd) & 0xFF);
                    hexStruct.setRecordLength(recordLength);
                    String tempad = String.format("%08x"/*转化为16进制*/, (rd ^ ad)).toUpperCase/*将字符串小写字符转换为大写*/();
                    byte[] tempaddr = new byte[4];
                    tempaddr[0] = (byte) Integer.parseInt(tempad.substring(0, 2), 16);
                    tempaddr[1] = (byte) Integer.parseInt(tempad.substring(2, 4), 16);
                    tempaddr[2] = (byte) Integer.parseInt(tempad.substring(4, 6), 16);
                    tempaddr[3] = (byte) Integer.parseInt(tempad.substring(6, 8), 16);
                    hexStruct.setAddr(tempaddr);
                    String data = hexLines[i].substring(19, 19 + recordLength[0] * 4);
                    if (data.length() < 8) {
                        String tempStr = data;
                        long last = Long.parseLong(tempStr, 16);
                        long bit;
                        if (data.length() == 6) {
                            bit = 0xFFFFFF;
                        } else if (data.length() == 4) {
                            bit = 0xFFFF;
                        } else if (data.length() == 2) {
                            bit = 0xFF;
                        } else {
                            bit = 0xFFFFFFFF;
                        }
                        long temp = last ^ (rd & bit);
                        String tmp1="%0"+data.length()+"x";
                        String xor = String.format(tmp1, temp).toUpperCase();
                        StringBuffer buffer = new StringBuffer(data);
                        buffer.delete(0, data.length());
                        buffer.append(xor);
                        data = buffer.toString();
                    } else {
                        String tempStr = data.substring(data.length() - 8, data.length());
                        long last = Long.parseLong(tempStr, 16);
                        long temp = last ^ rd;
                        String xor = String.format("%08x", temp).toUpperCase();
                        StringBuffer buffer = new StringBuffer(data);
                        buffer.delete(data.length() - 8, data.length());
                        buffer.append(xor);
                        data = buffer.toString();
                    }
                    if (application.chipType == 1) {
                        StringBuilder str = new StringBuilder();
                        for (int j = 0; j < data.length(); j += 4) {
                            long tmp = Long.parseLong(data.substring(j, j + 4), 16);
                            if (tmp != 0 && tmp != 0x5B38) {
                                tmp ^= 0x5B38;
                            }
                            String s = String.format("%04x", tmp).toUpperCase();
                            str.append(s);
                        }
                        data = str.toString();
                    }

                    byte[] tempdata = new byte[data.length() / 2];
                    for (int j = 0; j < data.length(); j += 2) {
                        tempdata[j / 2] = (byte) Integer.parseInt(data.substring(j, j + 2), 16);
                    }

                    hexStruct.setData(tempdata);
                    hexStructs.add(hexStruct);

            }
        }

            return hexStructs;

        }

    public static long[] FlashRoom(List<HexStruct> hex)
    {
        for (int i = 0; i < hex.size(); i++)
        {
            long l1 = (long) ((hex.get(i).getAddr()[0] & 0xFF) << 24);
            long l2 = (long) ((hex.get(i).getAddr()[1] & 0xFF) << 16);
            long l3 = (long) ((hex.get(i).getAddr()[2] & 0xFF) << 8);
            long l4 = (long) (hex.get(i).getAddr()[3] & 0xFF);
            long flashAddr=l1+l2+l3+l4;
            String str= byteToHexStr(hex.get(i).getData()).toUpperCase();
            for (int j = 0; j<str.length(); j+=2) {
                long tmp= Long.parseLong(str.substring(j,j+2), 16);
                application.flashRoom[(int)(flashAddr  - application.flashStart + j / 2)]=tmp;

            }
        }
        return application.flashRoom;
    }
    /// <summary>
    /// 字节数组转16进制字符串
    /// </summary>
    /// <param name="bytes"></param>
    /// <returns></returns>
    public static String byteToHexStr(byte[] bytes)
    {
        String returnStr = "";
        if (bytes != null)
        {
            for (int i = 0; i < bytes.length; i++)
            {
                returnStr +=String.format("%02x",bytes[i]);
            }
        }
        return returnStr;
    }
    /// <summary>
    /// 计算Hex文件校验值
    /// </summary>
    /// <param name="hexData"></param>
    /// <returns></returns>
    public static byte[] CheckHex(long[] hexData)
    {
        long CRC = 0xFFFF;
        long SUM = 0;
        byte[] bytes = new byte[4];
        int length = hexData.length / 2;
        for (int i = 0; i < length; i++)
        {
            long temp = (hexData[2 * i + 1] << 8) + hexData[2 * i];
            SUM += temp;
            CRC ^= temp;
            for (int j = 0; j < 8; j++)
            {
                if ((CRC & 0x0001) > 0)
                {
                    CRC = (CRC >> 1) ^ 0xA001;
                }
                else
                {
                    CRC = CRC >> 1;
                }
            }
        }
        bytes[0] = (byte)((SUM >> 8) & 0xFF);
        bytes[1] = (byte)(SUM & 0xFF);
        bytes[2] = (byte)((CRC >> 8) & 0xFF);
        bytes[3] = (byte)(CRC & 0xFF);
        return bytes;
    }
}
