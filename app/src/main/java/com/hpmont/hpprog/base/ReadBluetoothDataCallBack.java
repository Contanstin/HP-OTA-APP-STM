package com.hpmont.hpprog.base;

import android.content.Context;
import com.hpmont.hpprog.config.Config;

import java.util.ArrayList;
import java.util.LinkedList;

public class ReadBluetoothDataCallBack {
    private static ReadBluetoothDataCallBack mbBack;
    private onHandleData mCallback;
    private Object rxLock = new Object();
    private RxData_Struct rxData_struct = new RxData_Struct();
    protected MyApplication application;

    public static ReadBluetoothDataCallBack getInstance() {
        if (mbBack == null) {
            mbBack = new ReadBluetoothDataCallBack();
        }
        return mbBack;
    }

    public void invokeMethod(int[] data, Context context) {
        application = (MyApplication)context.getApplicationContext();
        if(application.chipType==1){
            if (mCallback != null) {
                if (rxData_struct.rxStart == 0 || application.receiveType == 1) {
                    rxData_struct.rxStart = System.currentTimeMillis();
            }
                synchronized (rxLock) {
                    if (!application.connectState) {
                        return;
                    }
                    int dataLength = data.length;
                    if (dataLength <= 0) {
                        return;
                    } else {
                        if (application.receiveType == 1) {
                            for (int i = 0; i < dataLength; i++) {
                                rxData_struct.bootList.add(data[i]);
                            }
                        } else {
                            RxData_Struct.rxDataCaching(rxData_struct, data);
                        }
                    }
                    //接收数据类型（0:0x41；1：boot；2：order）
                    if (application.receiveType == 0) {
                        if (rxData_struct.rxBuffer[0] != Config.CONNECT_BYTE) {
                            rxData_struct.rxIndex = 0;
                            rxData_struct.rxStart = 0;
                            return;
                        }
                        int frameLength = 1;
                        if (rxData_struct.rxIndex < frameLength) {
                            long nowTime = System.currentTimeMillis();
                            if (nowTime > (rxData_struct.rxStart + 1000)) {
                                rxData_struct.rxIndex = 0;
                                rxData_struct.rxStart = 0;
                                return;
                            }
                        } else {
                            rxData_struct.f_RxOver = 1;
                            rxData_struct.rxLength = frameLength;
                            rxData_struct.rxIndex = 0;
                            rxData_struct.rxStart = 0;
                        }
                    } else if (application.receiveType == 1) {
                        int frameLength = application.sendBoot.length - 1;
                        if (rxData_struct.bootList.size() < frameLength) {
                            long nowTime = System.currentTimeMillis();
                            if (nowTime > (rxData_struct.rxStart + 1000)) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                rxData_struct.bootList.clear();
                                return;
                            }
                        } else {
                            rxData_struct.f_RxOver = 1;
                            rxData_struct.rxLength = frameLength;
                            rxData_struct.rxStart = 0;
                            rxData_struct.rxIndex = 0;
                        }
                    } else if (application.receiveType == 2) {
                        if (rxData_struct.rxIndex == 1 || rxData_struct.rxIndex == 2) {
                            if (rxData_struct.rxBuffer[0] != Config.FRAME_HEAD) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                return;
                            }
                        } else if (rxData_struct.rxIndex >= 2) {
                            if (rxData_struct.rxBuffer[0] != Config.FRAME_HEAD || rxData_struct.rxBuffer[2] != Config.FRAME_LENGTH) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                return;
                            }
                        }
                        int frameLength = 0;
                        if (application.chipType == 0) {
                            frameLength = 6;
                        } else if (application.chipType == 1) {
                            frameLength = 7;
                        }
                        if (rxData_struct.rxIndex < frameLength) {
                            long nowTime = System.currentTimeMillis();
                            if (nowTime > (rxData_struct.rxStart + 1000)) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                return;
                            }
                        } else {
                            rxData_struct.f_RxOver = 1;
                            rxData_struct.rxLength = frameLength;
                            rxData_struct.rxStart = 0;
                            rxData_struct.rxIndex = 0;
                        }
                    }
                }

                if (rxData_struct.f_RxOver == 1) {
                    if (application.receiveType == 1) {
                        mCallback.handleData(null);
                    } else {
                        int[] buffer = new int[rxData_struct.rxLength];
                        System.arraycopy(rxData_struct.rxBuffer, 0, buffer, 0, buffer.length);
                        mCallback.handleData(buffer);
                    }
                    rxData_struct.f_RxOver = 0;
                    rxData_struct.rxStart = 0;
                    rxData_struct.rxIndex = 0;
                }
            }
           }
            else if(application.chipType==0){
            if (mCallback != null) {
                if (rxData_struct.rxStart == 0 || application.receiveType == 1) {
                    rxData_struct.rxStart = System.currentTimeMillis();
                }
                synchronized (rxLock) {
                    if (!application.connectState) {
                        return;
                    }
                    int dataLength = data.length;
                    if (dataLength <= 0) {
                        return;
                    } else {
                        if (application.receiveType == 1) {
                            for (int i = 0; i < dataLength; i++) {
                                rxData_struct.bootList.add(data[i]);
                            }
                        } else {
                            RxData_Struct.rxDataCaching(rxData_struct, data);
                        }
                    }
                    //接收数据类型（0:0x41；1：boot；2：order）
                    if (application.receiveType == 0) {
                        if (rxData_struct.rxBuffer[0] != Config.CONNECT_BYTE) {
                            rxData_struct.rxIndex = 0;
                            rxData_struct.rxStart = 0;
                            return;
                        }
                        int frameLength = 1;
                        if (rxData_struct.rxIndex < frameLength) {
                            long nowTime = System.currentTimeMillis();
                            if (nowTime > (rxData_struct.rxStart + 1000)) {
                                rxData_struct.rxIndex = 0;
                                rxData_struct.rxStart = 0;
                                return;
                            }
                        } else {
                            rxData_struct.f_RxOver = 1;
                            rxData_struct.rxLength = frameLength;
                            rxData_struct.rxIndex = 0;
                            rxData_struct.rxStart = 0;
                        }
                    } else if (application.receiveType == 1) {
                        int frameLength = application.sendBoot.length - 1;
                        if (rxData_struct.bootList.size() < frameLength) {
                            long nowTime = System.currentTimeMillis();
                            if (nowTime > (rxData_struct.rxStart + 1000)) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                rxData_struct.bootList.clear();
                                return;
                            }
                        } else {
                            rxData_struct.f_RxOver = 1;
                            rxData_struct.rxLength = frameLength;
                            rxData_struct.rxStart = 0;
                            rxData_struct.rxIndex = 0;
                        }
                    } else if (application.receiveType == 2) {
                        if (rxData_struct.rxIndex == 1 ) {
                            if (rxData_struct.rxBuffer[0] != Config.FRAME_HEAD) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                return;
                            }
                        } else if (rxData_struct.rxIndex >= 2) {
                            if (rxData_struct.rxBuffer[0] != Config.FRAME_HEAD || rxData_struct.rxBuffer[1] != Config.FRAME_LENGTH) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                return;
                            }
                        }
                        int frameLength = 0;
                        if (application.chipType == 0) {
                            frameLength = 6;
                        } else if (application.chipType == 1) {
                            frameLength = 7;
                        }
                        if (rxData_struct.rxIndex < frameLength) {
                            long nowTime = System.currentTimeMillis();
                            if (nowTime > (rxData_struct.rxStart + 1000)) {
                                rxData_struct.rxStart = 0;
                                rxData_struct.rxIndex = 0;
                                return;
                            }
                        } else {
                            rxData_struct.f_RxOver = 1;
                            rxData_struct.rxLength = frameLength;
                            rxData_struct.rxStart = 0;
                            rxData_struct.rxIndex = 0;
                        }
                    }
                }

                if (rxData_struct.f_RxOver == 1) {
                    if (application.receiveType == 1) {
                        mCallback.handleData(null);
                    } else {
                        int[] buffer = new int[rxData_struct.rxLength];
                        System.arraycopy(rxData_struct.rxBuffer, 0, buffer, 0, buffer.length);
                        mCallback.handleData(buffer);
                    }
                    rxData_struct.f_RxOver = 0;
                    rxData_struct.rxStart = 0;
                    rxData_struct.rxIndex = 0;
                }
            }
        }
        }


    public void setOnCallBack(onHandleData callback) {
        mCallback = callback;
    }

    public void removeCallBack() {
        mCallback = null;
    }

    // 定义接口
    public interface onHandleData {
        public void handleData(int[] data);
    }
}
