package com.apps.lore_f.domoticcontroller.firebase.dataobjects;

/**
 * Created by lore_f on 09/09/2018.
 */

public class RemoteDevNetworkStatus {

    // required empty constructor
    public RemoteDevNetworkStatus(){};

    private String LocalIP;
    private String PublicIP;

    public String getLocalIP() {
        return LocalIP;
    }

    public void setLocalIP(String localIP) {
        LocalIP = localIP;
    }

    public String getPublicIP() {
        return PublicIP;
    }

    public void setPublicIP(String publicIP) {
        PublicIP = publicIP;
    }
}
