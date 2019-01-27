package com.apps.lore_f.domoticcontroller.generic.dataobjects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 105053228 on 30/mar/2017.
 */

public class WOLDeviceInfo {

    private static final String TAG="WOLDeviceInfo";

    public static final String STATUS_STOPPED="Stopped";

    private String id="";
    private String name="";
    private String macAddress="";

    public WOLDeviceInfo(String dataJSON){

        try {

            JSONObject info = new JSONObject(dataJSON);

            id=info.getString("ID");
            macAddress=info.getString("Address");
            name=info.getString("Name");

        } catch (JSONException e) {

            Log.e(TAG, "Unable to parse json data: " + dataJSON);

        }

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress(){ return macAddress; }

}
