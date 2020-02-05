package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

import java.util.HashMap;

/**
 * Created by lore_f on 26/08/2017.
 */

public class DeviceData {

    private final static String TAG = DeviceData.class.getName();

    private String DeviceName;
    private boolean Online;
    private HashMap<String, Object> ExposedServices;
    private HashMap<String, Object> StaticData;
    private HashMap<String, Object> NetworkData;
    private HashMap<String, Object> StatusData;

    // empty constructor
    public DeviceData() {
    }

    public DeviceData(String DeviceName, boolean Online, HashMap<String, Object> ExposedServices, HashMap<String, Object> StaticData,HashMap<String, Object> NetworkData, HashMap<String, Object> StatusData){
        this.DeviceName=DeviceName;
        this.Online=Online;
        this.StaticData=StaticData;
        this.NetworkData=NetworkData;
        this.StatusData=StatusData;
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

    public HashMap<String, Object> getExposedServices() {
        return this.ExposedServices;
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

    private HashMap<String, Object> getCameraParameters() {
        return (HashMap<String, Object>) getStaticData().get("Camera");
    }

    private HashMap<String, Object> getFileManagerParameters() {
        return (HashMap<String, Object>) getStaticData().get("FileManager");
    }

    private HashMap<String, Object> getThermostatData() {
        return (HashMap<String, Object>) getStaticData().get("Thermostat");
    }

    private String getTCPService() {
        return getExposedServices().get("TCP").toString();
    }

    public String getTCPServiceHost(){
        if( getTCPService() != null){
            return getTCPService().split("//")[1].split(":")[0];
        } else {
            return null;
        }
    }

    public int getTCPServicePort(){
        if( getTCPService() != null){
            return Integer.parseInt (getTCPService().split("//")[1].split(":")[1]);
        } else {
            return -1;
        }
    }


    public boolean hasThermostat() {
        if (getThermostatData() != null) {
            return (boolean) getThermostatData().get("Enabled");
        } else {
            return false;
        }
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

    public boolean hasCamera() {
        if( getCameraParameters() != null) {
            return (boolean) getCameraParameters().get("Enabled");
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