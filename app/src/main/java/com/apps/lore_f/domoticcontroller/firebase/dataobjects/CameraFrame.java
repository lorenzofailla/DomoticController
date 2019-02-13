package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

/**
 * Created by lore_f on 04/03/2018.
 */

public class CameraFrame {

    private long Date;
    private String Time;
    private String ImgData;

    public CameraFrame(){};

    public long getDate() {
        return Date;
    }

    public String getTime() {
        return Time;
    }

    public String getImgData() {
        return ImgData;
    }
}
