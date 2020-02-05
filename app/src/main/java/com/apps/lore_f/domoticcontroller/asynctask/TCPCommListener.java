package com.apps.lore_f.domoticcontroller.asynctask;

public interface TCPCommListener {

        void onSocketCreated();
        void onSocketClosed();
        void onDataReceived(String data);

}
