package com.litesuits.bluetooth.conn;

public enum ConnectError {

    Invalidmac(-1, "非法的mac地址"),
    ScanTimeout(-2, "扫描超时:未发现设备"),
    ConnectTimeout(-3, "蓝牙连接超时"),
    ServiceDiscoverTimeout(-4, "服务发现超时"),
    CharNotFound(-7, "未发现指定通道"),
    InvalidStatus(-6, "错误的蓝牙状态"),
    WriteTimeout(-7, "写入超时"),
    ReadTimeout(-8, "读取超时");


    ConnectError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public boolean hasConnected() {
        return this.getCode() > CharNotFound.getCode();
    }
}