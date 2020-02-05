package com.apps.lore_f.domoticcontroller.generic.classes;


public class DeviceNetworkStatus extends DeviceStatus {

    public DeviceNetworkStatus(String jsonSource) {
        super(jsonSource);
    }

    public String getLocalIPAddresses() {

        return getStringValue("LocalIP");

    }

    public String getPublicIPAddress() {

        return getStringValue("PublicIP");

    }


}
