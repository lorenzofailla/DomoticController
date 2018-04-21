package com.apps.lore_f.domoticcontroller;

/**
 * Created by lore_f on 08/10/2017.
 */

public class VSEvent {

    private String CameraFullID;
    private String Date;
    private String Time;
    private String Device;
    private String ThreadID;
    private String VideoLink;
    private String CameraName;

    // empty constructor
    public VSEvent() {
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

    public String getDevice() {
        return Device;
    }

    public String getThreadID() {
        return ThreadID;
    }

    public String getVideoLink() {        return VideoLink;    }

    public String getCameraName() {return CameraName; }
}
