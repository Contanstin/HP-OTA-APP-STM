package com.hpmont.hpprog.base;

import java.util.ArrayList;
import java.util.List;

public class RxData_Struct {
    private static int RX_BUFSIZE = 500;
    public static int[] rxBuffer = new int[RX_BUFSIZE];
    public static List<Integer> bootList = new ArrayList<>();
    public int f_RxOver;
    public int rxLength;
    public int rxIndex;
    public long rxStart;

    private static RxData_Struct rxData_Struct;

    /**
     * 单例模式（接收数据）
     * @return
     */
    public static RxData_Struct getInstance() {
        if (rxData_Struct == null) {
            rxData_Struct = new RxData_Struct();
        }
        return rxData_Struct;
    }

    /**
     * 数据缓存
     * @param rxData_Struct
     * @param rxData
     */
    public static void rxDataCaching(RxData_Struct rxData_Struct, int[] rxData){
        for (int i = 0; i < rxData.length; i++ ){
            int DataRx = rxData[i];
            if (rxData_Struct.rxIndex >= RX_BUFSIZE) {
                break;
            }
            rxData_Struct.rxBuffer[rxData_Struct.rxIndex] = DataRx;
            rxData_Struct.rxIndex ++;
        }
    }
}
