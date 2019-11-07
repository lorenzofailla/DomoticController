package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

import java.util.HashMap;

/**
 * Created by lore_f on 26/08/2017.
 */

public class DeviceData {

    private final static String TAG = DeviceData.class.getName();

    private String DeviceName;
    private boolean Online;
    private HashMap<String, Object> StaticData;
    private HashMap<String, Object> NetworkData;
    private HashMap<String, Object> StatusData;

    // empty constructor
    public DeviceData() {
    }

    public String getDeviceName() {
        return this.DeviceName;
    }

    public HashMap<String, Object> getStaticData() {
        return this.StaticData;
    }

    public HashMap<String, Object> getNetworkData() {
        return this.NetworkData;
    }

    public HashMap<String, Object> getStatusData() {
        return this.StatusData;
    }

    public boolean getOnline() {
        return this.Online;
    }

    public String getLocalIPAddresses() {

        HashMap<String, Object> network = (HashMap<String, Object>) getNetworkData().get("NetworkStatus");
        return network.get("LocalIP").toString();

    }

    public String getPublicIPAddress() {

        HashMap<String, Object> network = (HashMap<String, Object>) getNetworkData().get("NetworkStatus");
        return network.get("PublicIP").toString();

    }

    public float getTotalDiskSpace() {
        return Float.parseFloat(getGeneralStatus().get("TotalSpace").toString());
    }

    public float getAvailableDiskSpace() {
        return Float.parseFloat(getGeneralStatus().get("FreeSpace").toString());
    }

    public long getRunningSince() {
        return Long.parseLong(getGeneralStatus().get("RunningSince").toString());
    }

    public long getLastUpdate() {
        return Long.parseLong(getGeneralStatus().get("LastUpdate").toString());
    }

    public int getConnectedUsersCount() {

        String[] data = getUptime();

        if (data.length == 2) {

            try {
                return Integer.parseInt(data[0].split(",")[2].trim().split(" ")[0]);
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                return -1;
            }

        } else {
            return -1;
        }

    }

    public int getAverageLoad() {

        String[] data = getUptime();

        if (data.length == 2) {

            try {
                return (int) (Double.parseDouble(data[1].split(", ")[0].replaceAll(",", ".")) * 100.0);
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                return -1;
            }

        } else {
            return -1;
        }
    }

    private HashMap<String, Object> getGeneralStatus() {
        return (HashMap<String, Object>) getStatusData().get("GeneralStatus");
    }

    private HashMap<String, Object> getNetworkStatus() {
        return (HashMap<String, Object>) getNetworkData().get("NetworkStatus");
    }

    private String[] getUptime() {
        return getGeneralStatus().get("Uptime").toString().split("load average: ");
    }

    private HashMap<String, Object> getTransmissionParameters() {
        return (HashMap<String, Object>) getStaticData().get("Transmission");
    }

    private HashMap<String, Object> getWakeOnLANParameters() {
        return (HashMap<String, Object>) getStaticData().get("WakeOnLAN");
    }

    private HashMap<String, Object> getVideoSurveillanceParameters() {
        return (HashMap<String, Object>) getStaticData().get("VideoSurveillance");
    }

    private HashMap<String, Object> getFileManagerParameters() {
        return (HashMap<String, Object>) getStaticData().get("FileManager");
    }

    public boolean hasTransmission() {
        if (getTransmissionParameters() != null) {
            return (boolean) getTransmissionParameters().get("Enabled");
        } else {
            return false;
        }
    }

    public boolean hasWakeOnLAN() {
        if( getWakeOnLANParameters() != null) {
            return (boolean) getWakeOnLANParameters().get("Enabled");
        } else {
            return false;
        }
    }

    public boolean hasVideoSurveillance() {
        if( getVideoSurveillanceParameters() != null) {
            return (boolean) getVideoSurveillanceParameters().get("Enabled");
        } else {
            return false;
        }
    }

    public boolean hasFileManager() {
        if( getFileManagerParameters() != null) {
            return (boolean) getFileManagerParameters().get("Enabled");
        } else {
            return false;
        }
    }

}