package com.apps.lore_f.domoticcontroller.generic.classes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lore_f on 20/01/2019.
 */

public class DeviceDataParser {

    private final static String TAG = DeviceDataParser.class.getName();

    public final static String VPN_NOTCONNECTED_ENTRY = "<not-connected>";

    private JSONObject staticDataJSON;
    private JSONObject statusDataJSON;
    private JSONObject networkDataJSON;

    private boolean staticDataValidated = false;
    private boolean statusDataValidated = false;
    private boolean networkDataValidated = false;

    public boolean setStaticDataJSON(String staticDataJSON) {

        try {

            this.staticDataJSON = new JSONObject(staticDataJSON);
            return validateStaticData();

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            return false;

        }

    }

    public boolean setStatusDataJSON(String statusDataJSON) {
        try {

            this.statusDataJSON = new JSONObject(statusDataJSON);
            return validateStatusData();

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            return false;

        }
    }

    public boolean setNetworkDataJSON(String networkDataJSON) {
        try {

            this.networkDataJSON = new JSONObject(networkDataJSON);
            return validateNetworkData();

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            return false;

        }
    }

    private double totalDiskSpace = 0.0;
    private double availableDiskSpace = 0.0;
    private long runningSince = -1L;
    private long lastUpdate = -1L;
    private String uptimeMessage = "";

    private boolean hasTorrent = false;
    private boolean hasVideoSurveillance = false;
    private boolean hasWakeOnLan = false;
    private boolean hasFileManager = false;

    private String localIPAddressesList;
    private String publicIPAddress;
    private String vpnIPAddress;

    public DeviceDataParser(){};

    public DeviceDataParser(String statusDataJSON, String networkDataJSON, String staticDataJSON) {

        try {

            this.statusDataJSON = new JSONObject(statusDataJSON);
            this.networkDataJSON = new JSONObject(networkDataJSON);
            this.staticDataJSON = new JSONObject(staticDataJSON);
            statusDataValidated = this.validateStatusData();
            networkDataValidated = this.validateNetworkData();
            staticDataValidated = this.validateStaticData();

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            statusDataValidated = false;
            networkDataValidated = false;
            staticDataValidated = false;

        }

    }

    private boolean validateStatusData() {

        try {

            JSONObject generalStatus = statusDataJSON.getJSONObject("GeneralStatus");

            totalDiskSpace = generalStatus.getDouble("TotalSpace");
            availableDiskSpace = generalStatus.getDouble("FreeSpace");

            runningSince = generalStatus.getLong("RunningSince");
            lastUpdate = generalStatus.getLong("LastUpdate");
            uptimeMessage = generalStatus.getString("Uptime");

            return true;

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            return false;

        }

    }

    private boolean validateNetworkData() {

        try {

            JSONObject network = networkDataJSON.getJSONObject("NetworkStatus");

            localIPAddressesList = network.getString("LocalIP");
            publicIPAddress = network.getString("PublicIP");
            vpnIPAddress = network.getString("VPN");

            return true;

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            return false;

        }

    }

    private boolean validateStaticData() {

        try {

            JSONObject enabledInterfaces = staticDataJSON.getJSONObject("EnabledInterfaces");
            JSONObject videoSurveillance = staticDataJSON.getJSONObject("VideoSurveillance");
            JSONObject wakeOnLan = staticDataJSON.getJSONObject("WakeOnLan");

            hasTorrent = enabledInterfaces.getBoolean("TorrentManagement");
            hasVideoSurveillance = enabledInterfaces.getBoolean("VideoSurveillanceManagement");
            hasWakeOnLan = enabledInterfaces.getBoolean("WakeOnLanManagement");
            hasFileManager = enabledInterfaces.getBoolean("DirectoryNavigation");

            return true;

        } catch (JSONException e) {

            Log.e(TAG, e.getMessage());
            return false;

        }

    }

    public boolean isConnectedToVPN() {
        return !(vpnIPAddress.equals(VPN_NOTCONNECTED_ENTRY));
    }

    public String[] getIPAddressesForTCPConnection() {

        String result="";

        if (isConnectedToVPN()) {
            result += vpnIPAddress + " ";
        }

        result += localIPAddressesList + publicIPAddress;

        return result.split("[ ]");

    }

    public boolean isStaticDataValidated() {
        return staticDataValidated;
    }

    public boolean isStatusDataValidated() {
        return statusDataValidated;
    }

    public boolean isNetworkDataValidated() {
        return networkDataValidated;
    }

    public double getTotalDiskSpace() {
        return totalDiskSpace;
    }

    public double getAvailableDiskSpace() {
        return availableDiskSpace;
    }

    public long getRunningSince() {
        return runningSince;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getUptimeMessage() {
        return uptimeMessage;
    }

    public boolean isHasTorrent() {
        return hasTorrent;
    }

    public boolean isHasVideoSurveillance() {
        return hasVideoSurveillance;
    }

    public boolean isHasWakeOnLan() {
        return hasWakeOnLan;
    }

    public boolean isHasFileManager() {
        return hasFileManager;
    }

    public String getLocalIPAddressesList() {
        return localIPAddressesList;
    }

    public String getPublicIPAddress() {
        return publicIPAddress;
    }

    public String getVpnIPAddress() {
        return vpnIPAddress;
    }

}
