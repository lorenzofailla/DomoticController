package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

/**
 * Created by lore_f on 08/10/2017.
 */

public class MotionEvent {

    private String CameraFullID;
    private String Date;
    private String Time;
    private String Timestamp;
    private String Device;
    private String ThreadID;
    private String VideoID;
    private String CameraName;
    private String ThumbnailID;
    private String LockedItem;
    private String NewItem;

    // empty constructor
    public MotionEvent() {
    }

    public String getCameraFullID() {
        return CameraFullID;
    }

    public String getDate() {
        return Date;
    }

    public String getTime() {
        return Time;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public String getDevice() {
        return Device;
    }

    public String getThreadID() {
        return ThreadID;
    }

    public String getVideoID() {
        return VideoID;
    }

    public String getCameraName() {
        return CameraName;
    }

    public String getThumbnailID() {
        return ThumbnailID;
    }

    public String isLockedItem() {
        return LockedItem;
    }

    public String isNewItem() {
        return NewItem;
    }

}
