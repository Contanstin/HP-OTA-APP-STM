package com.hpmont.hpprog.base;

public class HexStruct {

    private byte[] Addr;
    private byte[] RecordLength;
    private byte[] Data;

    public byte[] getAddr() {
        return Addr;
    }

    public void setAddr(byte[] addr) {
        Addr = addr;
    }

    public byte[] getRecordLength() {
        return RecordLength;
    }

    public void setRecordLength(byte[] recordLength) {
        RecordLength = recordLength;
    }

    public byte[] getData() {
        return Data;
    }

    public void setData(byte[] data) {
        Data = data;
    }
}