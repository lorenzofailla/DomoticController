package com.apps.lore_f.domoticcontroller.generic.classes;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class DeviceStatus {

    private JSONObject jsonData;

    public DeviceStatus(String jsonSource) {

        try {

            jsonData = new JSONObject(jsonSource);

        } catch (JSONException e) {

            jsonData = null;

        }

    }

    protected long getLongValue(String key) {
        try {
            long result = jsonData.equals(null) ? Long.MIN_VALUE : jsonData.getLong(key);
            return result;
        } catch (JSONException e) {
            return Long.MIN_VALUE;
        }
    }

    protected String getStringValue(String key) {
        try {
            String result = jsonData.equals(null) ? "" : jsonData.getString(key);
            return result;
        } catch (JSONException e) {
            return "";
        }

    }

    protected float getFloatValue(String key) {
        try {
            float result = jsonData.equals(null) ? Float.MIN_VALUE : Float.parseFloat(jsonData.getString(key));
            return result;
        } catch (JSONException e) {
            return Float.MIN_VALUE;
        }

    }

}
