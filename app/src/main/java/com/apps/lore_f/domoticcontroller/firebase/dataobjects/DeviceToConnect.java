package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by lore_f on 26/08/2017.
 */

public class DeviceToConnect {

    private final static String TAG=DeviceToConnect.class.getName();

    private String DeviceName;
    private boolean Online;
    private String StaticData;
    private String NetworkData;
    private String StatusData;

    private JSONObject dataJSON;

    // empty constructor
    public DeviceToConnect() {
    }

    public DeviceToConnect(String data) {

        try {

            this.dataJSON = new JSONObject(data);

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());

        }

    }

    public String getDeviceName() {
        return this.DeviceName;
    }

    public String getStaticData() {
        return this.StaticData;
    }

    public String getNetworkData() {
        return this.NetworkData;
    }

    public String getStatusData() {
        return this.StatusData;
    }

    public boolean getOnline() {
        return this.Online;
    }

}


