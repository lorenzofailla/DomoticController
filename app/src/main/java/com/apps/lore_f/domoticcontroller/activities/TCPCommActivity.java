package com.apps.lore_f.domoticcontroller.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apps.lore_f.domoticcontroller.asynctask.TCPComm;
import com.apps.lore_f.domoticcontroller.asynctask.TCPCommListener;

public abstract class TCPCommActivity extends AppCompatActivity implements TCPCommListener {

    protected TCPComm tcpComm;
    protected boolean tcpCommAvailable = false;

    @Override
    public void onSocketCreated() {
        tcpCommAvailable = true;
    }

    @Override
    public void onSocketClosed() {
        tcpCommAvailable = false;
    }

    @Override
    public void onDataReceived(String data) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // controlla che l'intent abbia gli extra
        if (!(getIntent().hasExtra("__host") && getIntent().hasExtra("__port"))) {
            finish();
            return;
        }

        tcpComm = new TCPComm(getIntent().getStringExtra("__host"), getIntent().getIntExtra("__port", -1), this);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        tcpCommAvailable = false;
        tcpComm.removeListener();
        tcpComm.terminate();

    }

}
