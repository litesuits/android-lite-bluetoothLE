package com.litesuits.bluetooth.conn;

public enum ConnectState {

    Initialed(0, "初始化状态：连接未建立"),
    Scanning(1, "扫描中..."),
    Connecting(2, "设备连接中..."),
    ServiceDiscovering(3, "服务发现中..."),
    ServiceDiscovered(4, "设备连接成功..."),
    DisConnected(6, "连接已断开");

    public boolean isInConnecting() {
        return code > 0 && code < 6;
    }

    ConnectState(int code, String message) {
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
}