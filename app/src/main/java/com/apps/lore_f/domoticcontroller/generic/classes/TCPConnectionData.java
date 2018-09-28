package com.apps.lore_f.domoticcontroller.generic.classes;

public class TCPConnectionData {

    public String[] getHostAddresses() {
        return hostAddresses;
    }

    public String getAddress(int index) {

        try {
            return hostAddresses[index];
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

    }

    public String getCurrentAddress() {

        return getAddress(currentHostAddrIndex);

    }

    public int getNOfHostAddresses() {
        return hostAddresses.length;
    }

    private int currentHostAddrIndex = -1;

    public int getCurrentHostAddrIndex() {
        return currentHostAddrIndex;
    }

    public void increaseCurrentHostAddrIndex() {
        currentHostAddrIndex++;
    }

    public void resetCurrentHostAddrIndex() {
        currentHostAddrIndex = -1;

    }


}
