package com.apps.lore_f.domoticcontroller;

import java.util.HashMap;

/**
 * Created by lore_f on 08/10/2017.
 */

public class VSCameraDevice {

    private String ThreadID;
    private String OwnerDevice;
    private String MoDetStatus;

    private HashMap<String, Object> LastShotData;

    // empty constructor
    public VSCameraDevice() {
    }

    public String getThreadID() {
        return ThreadID;
    }

    public String getOwnerDevice() {
        return OwnerDevice;
    }

    public String getMoDetStatus() {
        return MoDetStatus;
    }

    public HashMap<String, Object> getLastShotData() { return LastShotData;}

}
