package com.apps.lore_f.domoticcontroller;

/**
 * Created by lore_f on 26/08/2017.
 */

public class DeviceToConnect {

    private String deviceName;
    private String location;
    private boolean isUnix;
    private boolean hasDirectoryNavigation;
    private boolean hasTorrentManagement;
    private boolean hasCamera;

    // empty constructor
    public DeviceToConnect() {
    }

    public DeviceToConnect(String fileName, String requestor, String mediaLink, long size, int nOfDownloads) {
        this.deviceName = fileName;

    }

    public String getDeviceName() {
        return deviceName;
    }

}


