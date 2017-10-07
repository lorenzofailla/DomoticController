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
    private boolean hasZoneMinderManagement;

    // empty constructor
    public DeviceToConnect() {
    }


    public String getDeviceName() {
        return deviceName;
    }

    public boolean getHasTorrentManagement() {
        return hasTorrentManagement;
    }

    public boolean getHasDirectoryNavigation() {
        return hasDirectoryNavigation;
    }

    public boolean getHasZoneMinderManagement() {return hasZoneMinderManagement; }

}


