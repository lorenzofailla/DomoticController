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

    private JSONObject wakeonlanDataJSON;
    private JSONObject videoSurveillanceDataJSON;

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

    public String getStaticData() {

        return this.staticDataJSON.toString();

    }

    public String getWOLData() {

        return wakeonlanDataJSON.toString();

    }

    public JSONObject getVideoSurveillanceJSON() {

        return videoSurveillanceDataJSON;

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

    public String getStatusData() {

        return this.statusDataJSON.toString();

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

    public String getNetworkData() {

        return this.networkDataJSON.toString();

    }

    private double totalDiskSpace = -9999.0;
    private double availableDiskSpace = -9999.0;
    private long runningSince = -1L;
    private long lastUpdate = -1L;
    private String uptimeMessage = "";
    private int averageLoad = -1;
    private int connectedUsers = -1;

    private boolean hasTorrent = false;
    private boolean hasVideoSurveillance = false;
    private boolean hasWakeOnLan = false;
    private boolean hasFileManager = false;

    private String localIPAddressesList;
    private String publicIPAddress;
    private String vpnIPAddress;

    public DeviceDataParser() {
    }

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

            String[] data = uptimeMessage.split("load average: ");


            if (data.length == 2) {

                try {
                    connectedUsers = Integer.parseInt(data[0].split(",")[2].trim().split(" ")[0]);
                    averageLoad = (int) (Double.parseDouble(data[1].split(", ")[0].replaceAll(",", ".")) * 100.0);
                } catch (IndexOutOfBoundsException | NumberFormatException e) {

                }
            }

            statusDataValidated = true;
            return true;

        } catch (JSONException e) {

            statusDataValidated = false;
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

            networkDataValidated = true;
            return true;

        } catch (JSONException e) {

            networkDataValidated = false;
            Log.e(TAG, e.getMessage());
            return false;

        }

    }

    private boolean validateStaticData() {

        try {

            JSONObject enabledInterfaces = staticDataJSON.getJSONObject("EnabledInterfaces");
            wakeonlanDataJSON = staticDataJSON.getJSONObject("WakeOnLan");
            videoSurveillanceDataJSON = staticDataJSON.getJSONObject("VideoSurveillance");

            hasTorrent = enabledInterfaces.getBoolean("TorrentManagement");
            hasVideoSurveillance = enabledInterfaces.getBoolean("VideoSurveillanceManagement");
            hasWakeOnLan = enabledInterfaces.getBoolean("WakeOnLanManagement");
            hasFileManager = enabledInterfaces.getBoolean("DirectoryNavigation");

            staticDataValidated = true;
            return true;

        } catch (JSONException e) {

            staticDataValidated = false;
            Log.e(TAG, e.getMessage());
            return false;

        }

    }

    public boolean isConnectedToVPN() {
        return !(vpnIPAddress.equals(VPN_NOTCONNECTED_ENTRY));
    }

    public String[] getIPAddressesForTCPConnection() {

        String result = "";

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

    public int getAverageLoad() {

        return this.averageLoad;

    }

    public int getConnectedUsers() {
        return this.connectedUsers;
    }

    public String getDiskStatus() {

        return String.format("%.1f MiB (%.1f %%)", this.availableDiskSpace, 100.0 * this.availableDiskSpace / this.totalDiskSpace);

    }

}
