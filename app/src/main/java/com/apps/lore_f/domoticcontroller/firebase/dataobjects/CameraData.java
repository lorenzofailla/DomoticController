package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

/**
 * Created by lore_f on 04/03/2018.
 */

public class CameraData {

    private String Name;
    private String ID;
    private String Owner;
    private long FrameTimestamp;

    public CameraData(){}

    public String getName() {
        return Name;
    }

    public String getID() {
        return ID;
    }

    public String getOwner() {
        return Owner;
    }

    public long getFrameTimestamp() {
        return FrameTimestamp;
    }
}
