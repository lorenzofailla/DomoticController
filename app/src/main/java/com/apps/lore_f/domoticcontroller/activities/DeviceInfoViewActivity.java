package com.apps.lore_f.domoticcontroller.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.generic.classes.DeviceGeneralStatus;
import com.apps.lore_f.domoticcontroller.generic.classes.DeviceNetworkStatus;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;

import org.json.JSONException;
import org.json.JSONObject;

import apps.android.loref.GeneralUtilitiesLibrary;

public class DeviceInfoViewActivity extends TCPCommActivity {

    private final static String TAG = "DeviceInfoViewActivity";

    private final static int REFRESH_GENERAL = 0x0001;
    private final static int REFRESH_NETWORK = 0x0002;
    private final static int REFRESH_NAME = 0x0003;

    @Override
    public void onSocketCreated() {

        super.onSocketCreated();

        // attiva il timer delle richieste
        handler.post(sendRequest);

        tcpComm.sendData(new MessageStructure("__get_device_data", "network", "-").getMessageAsJSONString());
        tcpComm.sendData(new MessageStructure("__get_device_data", "devicename", "-").getMessageAsJSONString());

    }

    @Override
    public void onSocketClosed() {
        super.onSocketClosed();
    }

    @Override
    public void onDataReceived(String data) {

        super.onDataReceived(data);

        Log.i(TAG, "data received from tcp comm interface:" + data);

        try {
            JSONObject reply = new JSONObject(data);
            switch (reply.getString("header")) {

                case "__device_generalstatus_reply":
                    deviceGeneralStatus = new DeviceGeneralStatus(reply.getString("body"));
                    responseTime = System.currentTimeMillis() - lastMessageTimeStamp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshUI(REFRESH_GENERAL);
                        }
                    });

                    break;

                case "__device_networkstatus_reply":

                    deviceNetworkStatus = new DeviceNetworkStatus(reply.getString("body"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshUI(REFRESH_NETWORK);
                        }
                    });

                    break;

                case "__device_name_reply":
                    remoteDeviceName = reply.getString("body");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshUI(REFRESH_NAME);
                        }
                    });

                    break;

            }

        } catch (JSONException e) {
            Log.d(TAG, "NOT A JSON REPLY!" + data);
        }

    }


    private Toolbar toolbar;
    private DeviceGeneralStatus deviceGeneralStatus;
    private DeviceNetworkStatus deviceNetworkStatus;
    private String remoteDeviceName;

    private TextView textViewSystemLoad;
    private TextView textViewResponseTime;
    private TextView textViewFreeDiskSpace;
    private TextView textViewPublicIP;
    private TextView textViewLocalIP;
    private TextView textViewRunningSince;
    private TextView textViewLastUpdate;

    private long lastMessageTimeStamp;
    private long responseTime;

    private Handler handler;



    private final static long REQUEST_INTERVAL = 5000L;

    private Runnable sendRequest = new Runnable() {

        @Override
        public void run() {

            lastMessageTimeStamp = System.currentTimeMillis();
            tcpComm.sendData(new MessageStructure("__get_device_data", "general", "-").getMessageAsJSONString());
            handler.postDelayed(this, REQUEST_INTERVAL);

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info_view);

        // inizializza l'handler
        handler = new Handler();

        // set up the toolbar of this activity
        toolbar = findViewById(R.id.TBR___DEVICEINFOVIEW___TOOLBAR);
        toolbar.inflateMenu(R.menu.deviceview_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.MENU_DEVICEVIEW_SHUTDOWN:
                        shutdownHost();
                        return true;

                    case R.id.MENU_DEVICEVIEW_REBOOT:
                        rebootHost();
                        return true;

                    default:
                        return false;

                }
            }

        });

        //
        textViewFreeDiskSpace = findViewById(R.id.TXV___DEVICEINFOVIEW___DISKSTATUS_VALUE);
        textViewResponseTime = findViewById(R.id.TXV___DEVICEINFOVIEW___RESPONSETIME_VALUE);
        textViewSystemLoad = findViewById(R.id.TXV___DEVICEINFOVIEW___SYSLOAD_VALUE);
        textViewPublicIP = findViewById(R.id.TXV___DEVICEINFOVIEW___PRIVATEIP_VALUE);
        textViewLocalIP = findViewById(R.id.TXV___DEVICEINFOVIEW___PUBLICIP_VALUE);
        textViewRunningSince = findViewById(R.id.TXV___DEVICEINFOVIEW___RUNNINGSINCE_VALUE);
        textViewLastUpdate = findViewById(R.id.TXV___DEVICEINFOVIEW___LASTUPDATE_VALUE);

    }

    @Override
    protected void onDestroy(){

        handler.removeCallbacks(sendRequest);

        super.onDestroy();

    }


    private void refreshUI(int flag) {

        switch (flag) {

            case REFRESH_GENERAL:

                textViewResponseTime.setText(String.format("%d ms", responseTime));

                textViewSystemLoad.setText(String.format("%d%%", deviceGeneralStatus.getAverageLoad()));
                textViewFreeDiskSpace.setText(String.format("%.0f MB", deviceGeneralStatus.getFreeSpace()));
                textViewRunningSince.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceGeneralStatus.getRunningSince(), this));
                //textViewLastUpdate.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getLastUpdate(), this));

                break;

            case REFRESH_NETWORK:

                textViewPublicIP.setText(deviceNetworkStatus.getPublicIPAddress());
                textViewLocalIP.setText(deviceNetworkStatus.getLocalIPAddresses());

                break;


            case REFRESH_NAME:
                toolbar.setTitle(remoteDeviceName);
                break;

        }




    }

    private void sendShutdownCommand() {
        if (tcpCommAvailable && tcpComm != null)
            tcpComm.sendData(new MessageStructure("__execcmd", "shutdown -h now", "-").getMessageAsJSONString());
    }

    private void sendRebootCommand() {
        if (tcpCommAvailable && tcpComm != null)
            tcpComm.sendData(new MessageStructure("__execcmd", "reboot", "-").getMessageAsJSONString());
    }

    public void rebootHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                // send the reboot command to the remote host
                sendRebootCommand();

                finish();
                return;

            }

        });

        builder.setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });

        builder.setMessage(R.string.ALERTDIALOG_MESSAGE_REBOOT);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void shutdownHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                // send the shutdown command to the remote host
                sendShutdownCommand();

                finish();
                return;

            }

        });
        builder.setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setMessage(R.string.ALERTDIALOG_MESSAGE_SHUTDOWN);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

}
