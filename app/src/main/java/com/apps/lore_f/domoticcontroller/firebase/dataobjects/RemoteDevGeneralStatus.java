package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

/**
 * Created by lore_f on 09/09/2018.
 */

public class RemoteDevGeneralStatus {

    // required empty constructor
    public RemoteDevGeneralStatus(){};

    private String Uptime;
    private long RunningSince;
    private long LastUpdate;
    private float FreeSpace;
    private float TotalSpace;

    public String getUptime() {
        return Uptime;
    }

    public void setUptime(String uptime) {
        Uptime = uptime;
    }

    public long getRunningSince() {
        return RunningSince;
    }

    public void setRunningSince(long runningSince) {
        RunningSince = runningSince;
    }

    public long getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public float getFreeSpace() {
        return FreeSpace;
    }

    public void setFreeSpace(float freeSpace) {
        FreeSpace = freeSpace;
    }

    public float getTotalSpace() {
        return TotalSpace;
    }

    public void setTotalSpace(float totalSpace) {
        TotalSpace = totalSpace;
    }

    public String getSystemLoad(){

        String loadString;
        try {

            loadString = Uptime.split("[,]")[3].replaceAll("[ ]", "").split("[:]")[1];
            float load = Float.parseFloat(loadString);
            return String.format("%.1f %%", load*100.0);

        } catch (IndexOutOfBoundsException e) {

            return "-";

        }

    }

    public String getDiskStatus(){

        return String.format("%.1f MiB (%.1f %%)", FreeSpace, 100.0*FreeSpace/TotalSpace);

    }

}
