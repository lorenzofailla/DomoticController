package com.apps.lore_f.domoticcontroller.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDBComm extends Service {

    public FirebaseDBComm() {
    }

    private String groupName;
    private String remoteDeviceName;
    private String thisDeviceName;


    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {

        public FirebaseDBComm getService() {
            // Return this instance of LocalService so clients can call public methods
            return FirebaseDBComm.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void sendCommandToDevice(MessageStructure command) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo remoto selezionato
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("IncomingCommands");

        // aggiunge il messaggio al nodo
        deviceIncomingCommands
                .child(groupName)
                .child(remoteDeviceName)
                .child("" + System.currentTimeMillis())
                .setValue(command);

    }

    public String getGroupName(){
        return this.groupName;
    }

    public String getRemoteDeviceName(){
        return this.remoteDeviceName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setRemoteDeviceName(String remoteDeviceName) {
        this.remoteDeviceName = remoteDeviceName;
    }

    public String getThisDeviceName() {
        return thisDeviceName;
    }

    public void setThisDeviceName(String thisDeviceName) {
        this.thisDeviceName = thisDeviceName;
    }
}
