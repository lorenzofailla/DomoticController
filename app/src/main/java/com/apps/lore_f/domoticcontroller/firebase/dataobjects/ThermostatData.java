package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

import java.util.HashMap;

public class ThermostatData {

    private String IPAddress;
    private String MACAddress;
    private String Name;

    //private HashMap<String, Object> TimeHistory;
    private HashMap<String, Object> LastStatus;

    public ThermostatData() {
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public String getMACAddress(){
        return MACAddress;
    }

    public String getName() {
        return Name;
    }

    /*
    public HashMap<String, Object> getTimeHistory() {
        return TimeHistory;
    }
    */

    public HashMap<String, Object> getLastStatus() {
        return LastStatus;
    }

    public double getSetTemperature() {

        try {
            return Float.parseFloat(getLastStatus().get("thermostat_temp").toString());
        } catch (NumberFormatException e) {
            return -99.9;
        }
    }

    public double getRoomTemperature() {

        try {
            return Float.parseFloat(getLastStatus().get("room_temp").toString());
        } catch (NumberFormatException e) {
            return -99.9;
        }
    }

    public boolean isHeating() {

            return Integer.parseInt(getLastStatus().get("active").toString())==1;
    }

    public boolean isActive() {

        return Integer.parseInt(getLastStatus().get("power").toString())==1;
    }

    public int getMode() {

        return Integer.parseInt(getLastStatus().get("auto_mode").toString());

    }

    public boolean isFreezeActive(){
        return Integer.parseInt(getLastStatus().get("fre").toString())==1;
    }

}
