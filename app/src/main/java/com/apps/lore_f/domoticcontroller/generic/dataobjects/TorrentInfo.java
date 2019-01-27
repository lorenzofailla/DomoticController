package com.apps.lore_f.domoticcontroller.generic.dataobjects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 105053228 on 30/mar/2017.
 */

public class TorrentInfo {

    private static final String TAG="TorrentInfo";

    public static final String STATUS_STOPPED="Stopped";

    private String id="";
    private String done="";
    private String have="";
    private String eta="";
    private double up=-9999.0;
    private double down=-9999.0;
    private String ratio="";
    private String status="";
    private String name="";

    public TorrentInfo(String dataJSON){

        try {

            JSONObject info = new JSONObject(dataJSON);

            id=info.getString("ID");
            done=info.getString("Done");
            have=info.getString("Have");
            eta=info.getString("ETA");
            up=info.getDouble("Up");
            down=info.getDouble("Down");
            ratio=info.getString("Ratio");
            status=info.getString("Status");
            name=info.getString("Name");

        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse json data: " + dataJSON);
        }

    }

    public String getId() {
        return id;
    }

    public String getDone() {
        return done;
    }

    public String getHave() {
        return have;
    }

    public String getEta() {
        return eta;
    }

    public double getUp() {
        return up;
    }

    public double getDown() {
        return down;
    }

    public String getRatio() {
        return ratio;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
