package com.apps.lore_f.domoticcontroller;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.RemoteDevGeneralStatus;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.RemoteDevNetworkStatus;
import com.apps.lore_f.domoticcontroller.fragments.DeviceInfoFragment;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.FileInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import loref.android.apps.androidtcpcomm.TCPComm;
import loref.android.apps.androidtcpcomm.TCPCommListener;

import static apps.android.loref.GeneralUtilitiesLibrary.decode;
import static apps.android.loref.GeneralUtilitiesLibrary.decompress;

import static com.apps.lore_f.domoticcontroller.DefaultValues.*;

public class DeviceViewActivity extends AppCompatActivity {

    private static final String TAG = "DeviceViewActivity";

    private enum FragmentType {
        DEVICE_INFO,
        DIRECTORY_NAVIGATOR,
        TORRENT_MANAGER,
        WOL_MANAGER,
        SSH_MANAGER,
        CAMERA_VIEWER

    }

    /*
    ************************************************************************************************
    General purpose flags
     */

    private boolean isPausing;

    // Device reply timeout management
    private final static long DEFAULT_FIRST_RESPONSE_TIMEOUT = 10000;

    private class DeviceNotRespondingAction implements Runnable {

        @Override
        public void run() {

            // imposta lo stato del dispositivo come offline
            String deviceNode = DefaultValues.GROUPNODE + "/" + groupName + "/" + DefaultValues.DEVICENODE + "/" + thisDevice;
            FirebaseDatabase.getInstance().getReference(deviceNode).child("online").setValue(false, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    Log.d(TAG, "No response from server. Closing activity.");

                    if (databaseError != null)
                        Log.e(TAG, databaseError.getMessage());

                    deviceNotRespondingAction = null;

                    // termina l'activity corrente
                    finish();

                }
            });

        }

    }

    private DeviceNotRespondingAction deviceNotRespondingAction = null;

    /*
    ************************************************************************************************
    TCP Connection Interface
     */

    private TCPComm tcpComm;
    private boolean isTCPCommInterfaceAvailable = false;

    private AlertDialog connectingToDeviceAlertDialog;

    private void setTCPConnectionAlertDialogMessage(String message) {
        connectingToDeviceAlertDialog.setMessage(message);
    }

    public TCPComm getTcpComm() {
        return tcpComm;
    }

    public boolean getTCPCommInterfaceStatus() {
        return isTCPCommInterfaceAvailable;
    }

    public void setIsTCPCommIntefaceAvailable(boolean value) {

        this.isTCPCommInterfaceAvailable = value;

        if (deviceInfoFragment != null) {

            if (!isTCPCommInterfaceAvailable) {
                deviceInfoFragment.resetCurrentHostAddrIndex();
            }

            deviceInfoFragment.updateTCPStatus();

        }

        manageTCPInterfaceStatus();
    }

    private TCPCommListener tcpCommListener = new TCPCommListener() {

        @Override
        public void onInterfaceReady() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // dismette la finestra di dialogo, se presente
                    if (connectingToDeviceAlertDialog.isShowing()) {
                        connectingToDeviceAlertDialog.dismiss();
                    }
                    if (!isTCPCommInterfaceAvailable) {
                        setIsTCPCommIntefaceAvailable(true);
                    }

                }
            });

        }

        @Override
        public void onConnected(int port) {

            /*
            L'interfaccia TCP è disponibile
             */

            // attiva il loop di invio dati
            tcpComm.startDataOutLoop();

        }

        @Override
        public void onConnectionError(Exception e) {

            /*
            L'interfaccia TCP non è disponibile
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!isTCPCommInterfaceAvailable) {

                        /*
                        E' trascorso il tempo massimo per testare l'interfaccia TCP, senza risultato.
                        */

                        // incrementa l'indice di indirizzo IP da testare
                        deviceInfoFragment.increaseCurrentHostAddrIndex();

                        if (!deviceInfoFragment.getCurrentAddress().equals("")) {

                            // distrugge l'interfaccia TCP
                            tcpComm.setRemoteAddress(deviceInfoFragment.getCurrentAddress());

                            setTCPConnectionAlertDialogMessage(deviceInfoFragment.getCurrentAddress());

                        } else {

                            // chiama il metodo cancel() dell'AlertDialog

                            if (connectingToDeviceAlertDialog.isShowing()) {
                                connectingToDeviceAlertDialog.cancel();
                            }

                            setIsTCPCommIntefaceAvailable(false);

                        }

                    } else {

                        setIsTCPCommIntefaceAvailable(false);

                    }
                }


            });


        }

        @Override
        public void onDataWriteError(Exception e) {

            /*
            L'interfaccia TCP non è disponibile
             */

            if (isTCPCommInterfaceAvailable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // pone a false il flag isTCPCommInterfaceAvailable
                        setIsTCPCommIntefaceAvailable(false);
                    }

                });

            }

        }

        @Override
        public void onDataReadError(Exception e) {

            /*
            L'interfaccia TCP non è disponibile
             */
            if (isTCPCommInterfaceAvailable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // pone a false il flag isTCPCommInterfaceAvailable
                        setIsTCPCommIntefaceAvailable(false);
                    }
                });
            }
        }

        @Override
        public void onDataLineReceived(byte[] data) {

            String logData;
            try {
                logData = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logData = "";
            }
            Log.d(TAG, "TCP data received: " + logData);

            final Message inCmd = getCommandFromBytesArray(data);
            if (!inCmd.getHeader().equals("")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processIncomingMessage(inCmd, "null");
                    }
                });


            }

        }

        @Override
        public void onClose(boolean byLocal) {

            if (!isPausing) {

                tcpComm.terminate();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setIsTCPCommIntefaceAvailable(false);
                    }

                });

            } else {

                tcpComm.setListener(null);

            }

        }

    };

    private void manageTCPInterfaceStatus() {

        // inizializza i riferimenti ai nodi del db Firebase
        String incomingMessagesNode = new StringBuilder()
                .append("/Groups/")
                .append(groupName)
                .append("/Devices/")
                .append(thisDevice)
                .append("/IncomingCommands")
                .toString(); // todo: riformattare

        incomingMessagesRef = FirebaseDatabase.getInstance().getReference(incomingMessagesNode);

        if (!isTCPCommInterfaceAvailable) {

            // distrugge l'oggetto TCPComm, se esiste
            if (tcpComm != null) {
                tcpComm.setListener(null);
                tcpComm = null;
            }

            // associa un ChildEventListener al nodo per poter processare i messaggi in ingresso
            incomingMessagesRef.addChildEventListener(newCommandsToProcess);

        } else {

            // rimuove il ChildEventListener al nodo per poter processare i messaggi in ingresso
            incomingMessagesRef.removeEventListener(newCommandsToProcess);

        }

        // invia un messaggio al dispositivo remoto con la richiesta del nome del dispositivo
        sendCommandToDevice(
                new Message("__requestWelcomeMessage",
                        "-",
                        thisDevice)
        );

        // inizializza e pianifica l'azione da intraprendere nel caso in cui la risposta non arrivi entro il timeout prefissato

        if (deviceNotRespondingAction != null) {
            handler.removeCallbacks(deviceNotRespondingAction);
        }

        deviceNotRespondingAction = new DeviceNotRespondingAction();
        handler.postDelayed(deviceNotRespondingAction, DEFAULT_FIRST_RESPONSE_TIMEOUT);

        // mostra il nome del dispositivo remoto nella TextView 'remoteHostName'
        TextView remoteHostName = (TextView) findViewById(R.id.TXV___DEVICEVIEW___HOSTNAME);
        remoteHostName.setText(R.string.GENERIC_PLACEHOLDER_WAITING);


        sendCommandToDevice(
                new Message("__update_status",
                        "general",
                        thisDevice)
        );

        sendCommandToDevice(
                new Message("__update_status",
                        "network",
                        thisDevice)
        );

    }

    public void startTCPInterfaceTest() {

        if (deviceInfoFragment != null) {

            deviceInfoFragment.increaseCurrentHostAddrIndex();

            if (!deviceInfoFragment.getCurrentAddress().equals("")) {

                // crea l'AlertDialog
                connectingToDeviceAlertDialog = new AlertDialog.Builder(this)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {

                    /*
                    L'interfaccia TCP non è disponibile
                     */

                                // imposta il flag su falso
                                setIsTCPCommIntefaceAvailable(false);

                            }
                        })
                        .setTitle(R.string.ALERTDIALOG_TITLE_PLEASE_WAIT)
                        .setMessage(R.string.ALERTDIALOG_MESSAGE_DEVICE_CONNECTION)
                        .create();

                // mostra l'AlertDialog
                connectingToDeviceAlertDialog.show();

                // inizializza l'interfaccia TCP
                tcpComm = new TCPComm(deviceInfoFragment.getCurrentAddress());
                tcpComm.setListener(tcpCommListener);
                tcpComm.init();

                setTCPConnectionAlertDialogMessage(deviceInfoFragment.getCurrentAddress());

            }

        }

    }

    public String getCurrentTCPAddress() {
        return deviceInfoFragment.getCurrentAddress();
    }

    /*
    ************************************************************************************************
    ValueEventListener for remote device general status data
     */

    ValueEventListener generalStatusValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            String logData;
            if (dataSnapshot != null) {

                logData = dataSnapshot.toString();
                RemoteDevGeneralStatus status = dataSnapshot.getValue(RemoteDevGeneralStatus.class);

                if (deviceInfoFragment != null) {
                    deviceInfoFragment.setGeneralStatus(status);
                }

            } else {

                logData = LOG_FIREBASEDB_NODATA;

            }

            Log.d(TAG, logData);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    /*
    ValueEventListener for remote device network status data
     */

    ValueEventListener networkStatusValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            String logData;
            if (dataSnapshot != null) {

                logData = dataSnapshot.toString();
                RemoteDevNetworkStatus status = dataSnapshot.getValue(RemoteDevNetworkStatus.class);

                if (deviceInfoFragment != null) {
                    deviceInfoFragment.setNetworkStatus(status);
                }

            } else {

                logData = LOG_FIREBASEDB_NODATA;

            }

            Log.d(TAG, logData);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


    // Firebase Database
    private DatabaseReference incomingMessagesRef;

    public String remoteDeviceName;
    private boolean remoteDeviceTorrent;
    private boolean remoteDeviceDirNavi;
    private boolean remoteDeviceWakeOnLan;
    private boolean remoteDeviceVideoSurveillance;

    private String action;

    public String thisDevice = "lorenzofailla-g3";
    public String groupName;

    private Handler handler;

    private CollectionPagerAdapter collectionPagerAdapter;
    private ViewPager viewPager;

    /* Fragments */
    private DeviceInfoFragment deviceInfoFragment;
    private TorrentViewerFragment torrentViewerFragment;
    private FileViewerFragment fileViewerFragment;
    private WakeOnLanFragment wakeOnLanFragment;
    private DeviceSSHFragment deviceSSHFragment;
    private VSCameraViewerFragment[] cameraViewFragment;
    private String[] cameraNames;
    private String[] cameraIDs;
    private int nOfAvailableCameras;
    private int homeFragment;

    int deviceInfoFragmentIndex = 0;
    int firstCameraFragmentIndex = 0;

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            Log.i(TAG, String.format("ViewPager.OnPageChangeListener.onPageSelected(%d)", position));
            collectionPagerAdapter.initializeFragmentAction(position);

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public class CollectionPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;
        private String[] pageTitle;
        private FragmentType[] fragmentTypes;

        CollectionPagerAdapter(
                FragmentManager fm,
                Fragment[] fragments,
                String[] titles,
                FragmentType[] types
        ) {
            super(fm);

            this.fragments = fragments;
            this.pageTitle = titles;
            this.fragmentTypes = types;

        }

        @Override
        public Fragment getItem(int i) {

            Log.i(TAG, String.format("getItem(%d)", i));
            initializeFragmentAction(i);
            return fragments[i];

        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return pageTitle[position];

        }

        public void initializeFragmentAction(int fragmentPosition) {

            switch (fragmentTypes[fragmentPosition]) {

                case DEVICE_INFO:
                    // nessuna azione
                    break;

                case DIRECTORY_NAVIGATOR:

                    if (fileViewerFragment.currentDirName == null) {
                        // invia al dispositivo remoto la richiesta di conoscere la directory corrente
                        sendCommandToDevice(new Message("__get_homedir", "null", thisDevice));
                    }

                    break;

                case TORRENT_MANAGER:
                    requestTorrentsList();
                    break;

                case WOL_MANAGER:
                    /* no action */
                    break;

                case CAMERA_VIEWER:
                    /*
                     * Calls
                     * */

                    VSCameraViewerFragment f = (VSCameraViewerFragment) fragments[fragmentPosition];
                    f.manageLiveBroadcastStatus();

            }

        }

    }

    /*
    Listener per nuovi record nel nodo dei messaggi in ingresso.
     */
    private ChildEventListener newCommandsToProcess = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // è arrivato un nuovo messaggio

            if (dataSnapshot != null) {

                // recupera il nuovo messaggio nel formato della classe Message
                GenericTypeIndicator<Message> msg = new GenericTypeIndicator<Message>() {
                };
                Message incomingMessage = dataSnapshot.getValue(msg);

                // dorme per 10 ms - necessario per evitare timestamp identici negli ID delle risposte
                try {

                    Thread.sleep(10);

                } catch (InterruptedException e) {
                }

                // processa il messaggio ricevuto
                if (incomingMessage != null)
                    processIncomingMessage(incomingMessage, dataSnapshot.getKey());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_view);

        // recupera l'extra dall'intent,
        // ottiene il nome del dispositivo remoto e altri parametri

    }

    @Override
    protected void onResume() {

        super.onResume();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
            Context context = getApplicationContext();
            SharedPreferences sharedPref = context.getSharedPreferences(
                    getString(R.string.data_file_key), Context.MODE_PRIVATE);

            groupName = sharedPref.getString(getString(R.string.data_group_name), null);

            if (groupName == null) {

            /*
            questa parte di codice non dovrebbe essere mai eseguita, viene tenuta per evitare eccezioni
             */

                // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
                startActivity(new Intent(this, GroupSelection.class));

                // termina l'Activity corrente
                finish();
                return;

            }

            remoteDeviceName = extras.getString("__DEVICE_TO_CONNECT");

            remoteDeviceTorrent = intent.hasExtra("__HAS_TORRENT_MANAGEMENT") && extras.getBoolean("__HAS_TORRENT_MANAGEMENT");
            remoteDeviceDirNavi = intent.hasExtra("__HAS_DIRECTORY_NAVIGATION") && extras.getBoolean("__HAS_DIRECTORY_NAVIGATION");
            remoteDeviceWakeOnLan = intent.hasExtra("__HAS_WAKEONLAN") && extras.getBoolean("__HAS_WAKEONLAN");
            remoteDeviceVideoSurveillance = intent.hasExtra("__HAS_VIDEOSURVEILLANCE") && extras.getBoolean("__HAS_VIDEOSURVEILLANCE");

            if (remoteDeviceVideoSurveillance && intent.hasExtra("__CAMERA_NAMES") && intent.hasExtra("__CAMERA_IDS")) {

                try {

                    cameraNames = extras.getString("__CAMERA_NAMES").split(";");
                    cameraIDs = extras.getString("__CAMERA_IDS").split(";");

                    nOfAvailableCameras = cameraIDs.length;

                } catch (NullPointerException e) {

                    remoteDeviceVideoSurveillance = false;
                    nOfAvailableCameras = 0;

                }

            } else {

                remoteDeviceVideoSurveillance = false;
                nOfAvailableCameras = 0;

            }

            if (intent.hasExtra("__ACTION")) {

                action = intent.getStringExtra("__ACTION");

            } else {

                action = "default";
            }

        } else {

            // non ci sono extra nell'Intent

            finish();
            return;

        }

        // attiva il ciclo di richieste
        handler = new Handler();

        isPausing = false;

        // aggancia i listener ai nodi
        String generalStatusNode = GROUPNODE + "/" + groupName + "/" + DEVICENODE + "/" + remoteDeviceName + "/" + GENERALSTATUSNODE;
        String networkStatusNode = GROUPNODE + "/" + groupName + "/" + DEVICENODE + "/" + remoteDeviceName + "/" + NETWORKSTATUSNODE;

        FirebaseDatabase.getInstance().getReference(generalStatusNode).addValueEventListener(generalStatusValueEventListener);
        FirebaseDatabase.getInstance().getReference(networkStatusNode).addValueEventListener(networkStatusValueEventListener);

        initView();

        setIsTCPCommIntefaceAvailable(false);

    }

    @Override
    protected void onPause() {

        super.onPause();

        isPausing = true;

        /*
        Se è attiva l'interfaccia TCP, la termina. Altrimenti, rimuove i ChildEventListener dai nodi del db di Firebase
         */
        if (isTCPCommInterfaceAvailable) {

            tcpComm.disconnect();

        } else {

            // rimuove i ChildEventListener dai nodi del db di Firebase
            incomingMessagesRef.removeEventListener(newCommandsToProcess);

        }

        // rimuove i listener dai nodi
        String generalStatusNode = GROUPNODE + "/" + groupName + "/" + DEVICENODE + "/" + remoteDeviceName + "/" + GENERALSTATUSNODE;
        String networkStatusNode = GROUPNODE + "/" + groupName + "/" + DEVICENODE + "/" + remoteDeviceName + "/" + NETWORKSTATUSNODE;

        FirebaseDatabase.getInstance().getReference(generalStatusNode).removeEventListener(generalStatusValueEventListener);
        FirebaseDatabase.getInstance().getReference(networkStatusNode).removeEventListener(networkStatusValueEventListener);

        // se attivo, rimuove l'handler all'azione posticipata per gestire la mancata risposta dal server
        if (deviceNotRespondingAction != null) {
            handler.removeCallbacks(deviceNotRespondingAction);
        }

        // rimuove l'OnPageChangeListener al ViewPager
        if (viewPager != null)
            viewPager.removeOnPageChangeListener(onPageChangeListener);

        handler = null;

    }

    private void initView() {

        initFragments();

        viewPager = (ViewPager) findViewById(R.id.PGR___DEVICEVIEW___MAINPAGER);
        viewPager.setAdapter(collectionPagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setCurrentItem(homeFragment);

    }

    private void processIncomingMessage(Message inMsg, String msgKey) {

        boolean deleteMsg = (msgKey != "null");
        String decodedBody = decode(inMsg.getBody());

        switch (inMsg.getHeader()) {

            case "WELCOME_MESSAGE":

                // mostra il nome del dispositivo remoto nella TextView 'remoteHostName'
                TextView remoteHostName = (TextView) findViewById(R.id.TXV___DEVICEVIEW___HOSTNAME);
                remoteHostName.setText(remoteDeviceName);

                // ferma l'esecuzione del task
                if (deviceNotRespondingAction != null) {
                    handler.removeCallbacks(deviceNotRespondingAction);
                }

                break;

            //case "UPTIME_REPLY":

            // nessuna azione

            //break;

            //case "FREE_SPACE":

            // nessuna azione

            //break;

            case "TORRENT_STARTED":
            case "TORRENT_STOPPED":
            case "TORRENT_REMOVED":
            case "TORRENT_ADDED":

                // invia un instant message con la richiesta della lista dei torrents
                requestTorrentsList();

                break;

            case "TORRENTS_LIST":

                if (torrentViewerFragment == null) {
                    break;
                }

                // imposta i parametri di visualizzazione del fragment
                torrentViewerFragment.nOfTorrents = decodedBody.split("\n").length - 2;

                if (torrentViewerFragment.nOfTorrents > 0) {

                    torrentViewerFragment.rawTorrentDataLines = decodedBody.split("\n");

                } else {

                    torrentViewerFragment.nOfTorrents = 0;
                    torrentViewerFragment.rawTorrentDataLines = null;

                }

                if (torrentViewerFragment.viewCreated)
                    torrentViewerFragment.updateContent();

                break;

            case "HOME_DIRECTORY":

                if (fileViewerFragment != null) {

                    fileViewerFragment.currentDirName = decodedBody.replace("\n", "");
                    if (fileViewerFragment.viewCreated)
                        fileViewerFragment.updateContent();

                    // invia un instant message con la richiesta del contenuto della directory home ricevuta
                    sendCommandToDevice(new Message("__get_directory_content", decodedBody, thisDevice));
                }

                break;

            case "DIRECTORY_CONTENT":

                if (fileViewerFragment != null) {

                    fileViewerFragment.rawDirData = decodedBody;
                    if (fileViewerFragment.viewCreated)
                        fileViewerFragment.updateContent();
                }

                break;

            case "GENERIC_NOTIFICATION":

                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("Message from " + inMsg.getReplyto())
                        .setContentText(decodedBody)
                        .setSmallIcon(R.drawable.home)
                        .build();

                break;

            case "FILE_READY_FOR_DOWNLOAD":

                String[] param = {"Groups/" + groupName + "/Devices/" + thisDevice + "/IncomingFiles/"};
                Intent intent = new Intent(this, DownloadFileFromDataSlots.class);
                intent.putExtra("__file_to_download", param);

                startService(intent);

                break;

            case "SSH_SHELL_READY":

                break;

            // case "REMOTE_CURRENT_TIME":

            // nessuna azione

            // break;

            case "FRAME_IMAGE_DATA":

                // recupera l'ID della telecamera e i dati del fotogramma

                String frameCameraID = decodedBody.substring(0, 1);
                String frameData = decodedBody.substring(7);

                int cameraIndex = Integer.parseInt(frameCameraID);
                VSCameraViewerFragment fragment = (VSCameraViewerFragment) getAvailableFragments()[firstCameraFragmentIndex + cameraIndex - 1];

                try {
                    fragment.refreshFrame((decompress(Base64.decode(frameData, Base64.DEFAULT))));
                } catch (IOException | DataFormatException e) {

                }


                break;

        }

        // se il flag 'deleteMsg' è stato impostato su true, elimina il messaggio dalla coda
        if (deleteMsg)
            deleteMessage(msgKey);

    }

    public void sendCommandToDevice(Message command) {

        if (isTCPCommInterfaceAvailable) {

            tcpComm.sendData(getCommandAsByteArray(command));

        } else {


            // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo remoto selezionato
            DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Groups/" + groupName + "/Devices");

            // aggiunge il messaggio al nodo
            deviceIncomingCommands
                    .child(remoteDeviceName)
                    .child("IncomingCommands")
                    .child("" + System.currentTimeMillis())
                    .setValue(command);
        }

    }

    private void deleteMessage(String id) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo locale
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Groups/" + groupName + "/Devices");

        // rimuove il messaggio al nodo
        deviceIncomingCommands.child(thisDevice).child("IncomingCommands").child(id).removeValue();

    }

    private void requestTorrentsList() {

        // invia un instant message con la richiesta della lista dei torrents
        sendCommandToDevice(new Message("__listTorrents", "null", thisDevice));

    }

    public void rebootHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                // invia un instant message con il comando di reboot
                sendCommandToDevice(new Message("__reboot", "null", thisDevice));

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

                /* invia un instant message con il comando di reboot */
                sendCommandToDevice(new Message("__shutdown", "null", thisDevice));
                finish();

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

    private void removeTorrent(final int id) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_CONFIRM_TORRENT_REMOVAL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendCommandToDevice(new Message("__remove_torrent", "" + id, thisDevice));

                    }
                })
                .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle(R.string.ALERTDIALOG_TITLE_CONFIRM_TORRENT_REMOVAL)
                .create()
                .show();

    }

    public void torrentAddRequest() {

        final EditText torrentURL = new EditText(this);

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_ADD_TORRENT)
                .setView(torrentURL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendCommandToDevice(new Message("__add_torrent", torrentURL.getText().toString(), thisDevice));
                    }
                })
                .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .setTitle(R.string.ALERTDIALOG_TITLE_ADD_TORRENT)
                .create()
                .show();

    }

    public void torrentStartRequest(int torrentID) {

        // invia il commando all'host remoto
        sendCommandToDevice(new Message("__start_torrent", "" + torrentID, thisDevice));

    }

    public void torrentStopRequest(int torrentID) {

        // invia il commando all'host remoto
        sendCommandToDevice(new Message("__stop_torrent", "" + torrentID, thisDevice));
    }

    public void torrentRemoveRequest(final int torrentID) {

        removeTorrent(torrentID);

    }

    public void manageFileViewerFragmentRequest(FileInfo fileInfo) {

        if (fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_FILE) {

            // attiva la procedura di upload del file da parte del dispositivo remoto sulla piattaforma Firebase Storage
            sendCommandToDevice(new Message("__get_file", fileInfo.getFileRootDir() + "/" + fileInfo.getFileName(), thisDevice));

        } else {

            // è stata selezionata un'entità diversa da un file (directory, '.' o '..')

            if (fileInfo.getFileName().equals(".")) {
                // è stato selezionato '.'
                // nessuna modifica a currentDirName

            } else if (fileInfo.getFileName().equals("..") && !fileViewerFragment.currentDirName.equals("/")) {
                // è stato selezionato '..'

                // modifica currentDirName per salire al livello di directory superiore

                String[] directoryArray = fileViewerFragment.currentDirName.split("/");

                fileViewerFragment.currentDirName = "/";
                for (int i = 0; i < directoryArray.length - 1; i++) {

                    if (fileViewerFragment.currentDirName.equals("/")) {

                        fileViewerFragment.currentDirName += directoryArray[i];

                    } else {

                        fileViewerFragment.currentDirName += "/" + directoryArray[i];
                    }

                }

            } else {

                // è stato selezionata una directory
                // modifica currentDirName per scendere al livello di directory selezionato
                if (fileViewerFragment.currentDirName.equals("/")) {
                    fileViewerFragment.currentDirName += fileInfo.getFileName();
                } else {
                    fileViewerFragment.currentDirName += "/" + fileInfo.getFileName();
                }

            }

            fileViewerFragment.hideContent();

            // invia il messaggio di richiesta dati della directory al dispositivo remoto
            sendCommandToDevice(new Message("__get_directory_content", fileViewerFragment.currentDirName, thisDevice));

        }

    }

    public void uploadAsDataSlot(FileInfo fileInfo) {

        sendCommandToDevice(new Message("__upload_file", fileInfo.getFileRootDir() + "/" + fileInfo.getFileName(), thisDevice));

    }

    /*
    Metodi e funzioni relativi al fragment SSH

     */
    private void sendSSHDisconnectionRequest() {

        sendCommandToDevice(new Message("__disconnect_ssh", null, thisDevice));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i(TAG, "Key: " + keyCode + " Event: " + event.getUnicodeChar(event.getMetaState()));

        switch (keyCode) {

            case 67: // backspace

                if (deviceSSHFragment != null)
                    deviceSSHFragment.sendBackSpace();

                return super.onKeyDown(keyCode, event);

            default:
                if (deviceSSHFragment != null)
                    deviceSSHFragment.addCharacterToBuffer(event.getUnicodeChar(event.getMetaState()));

                return super.onKeyDown(keyCode, event);

        }

    }

    private Fragment[] getAvailableFragments() {
        /*
        Restituisce un array di Fragment, contenente le varie pagine video
         */
        List<Fragment> result = new ArrayList<>();

        deviceInfoFragmentIndex = 0;
        firstCameraFragmentIndex = 0;

        int count = 0;

        if (wakeOnLanFragment != null) {
            result.add(wakeOnLanFragment);
            count++;
        }

        if (torrentViewerFragment != null) {
            result.add(torrentViewerFragment);
            count++;
        }

        if (fileViewerFragment != null) {
            result.add(fileViewerFragment);
            count++;
        }

        if (deviceInfoFragment != null) {
            result.add(deviceInfoFragment);
            deviceInfoFragmentIndex = count;
            count++;
        }

        for (int i = 0; i < nOfAvailableCameras; i++) {
            result.add(cameraViewFragment[i]);
            if (i == 0) {
                firstCameraFragmentIndex = count;
            }
            count++;
        }

        switch (action) {
            case "monitor":
                homeFragment = firstCameraFragmentIndex;
                break;

            default:
                homeFragment = deviceInfoFragmentIndex;

        }

        return result.toArray(new Fragment[0]);

    }

    private String[] getAvailableFragmentTitles() {

        List<String> result = new ArrayList<String>();

        if (wakeOnLanFragment != null) {
            result.add("Wake-on-lan");
        }

        if (torrentViewerFragment != null) {
            result.add("Torrent manager");
        }

        if (fileViewerFragment != null) {
            result.add("File manager");
        }

        if (deviceInfoFragment != null) {
            result.add("Remote device info");

        }

        for (int i = 0; i < nOfAvailableCameras; i++) {
            result.add(String.format("Videosurveillance camera: %s", cameraIDs[i]));
        }

        return result.toArray(new String[0]);

    }

    private FragmentType[] getAvailableFragmentTypes() {

        List<FragmentType> result = new ArrayList<FragmentType>();

        if (wakeOnLanFragment != null) {
            result.add(FragmentType.WOL_MANAGER);
        }

        if (torrentViewerFragment != null) {
            result.add(FragmentType.TORRENT_MANAGER);
        }

        if (fileViewerFragment != null) {
            result.add(FragmentType.DIRECTORY_NAVIGATOR);
        }

        if (deviceInfoFragment != null) {
            result.add(FragmentType.DEVICE_INFO);

        }

        for (int i = 0; i < nOfAvailableCameras; i++) {
            result.add(FragmentType.CAMERA_VIEWER);
        }

        return result.toArray(new FragmentType[0]);
    }

    private void initFragments() {
        /* inizializza i fragment */

        boolean createDeviceInfoFragment=(action=="monitor");

        switch (action) {

            case "monitor":
                createDeviceInfoFragment = false;
                break;

            default:
                createDeviceInfoFragment = true;

        }

        //
        // DeviceInfoFragment

        if (createDeviceInfoFragment) {
            deviceInfoFragment = new DeviceInfoFragment();
            deviceInfoFragment.setParent(this);
        }

        //
        // VideoSurveillanceCameraListFragment
        if (remoteDeviceVideoSurveillance) {

            // crea una query per calcolare il numero di videocamere disponibili
            // inizializza l'array
            cameraViewFragment = new VSCameraViewerFragment[nOfAvailableCameras];

            for (int i = 0; i < nOfAvailableCameras; i++) {

                VSCameraViewerFragment temp;
                temp = new VSCameraViewerFragment();
                temp.setCameraID(cameraIDs[i]);
                temp.setCameraName(cameraNames[i]);
                temp.setParent(this);

                cameraViewFragment[i] = temp;

            }

        }

        //
        // FileViewerFragment
        if (remoteDeviceDirNavi) {
            fileViewerFragment = new FileViewerFragment();
            fileViewerFragment.parent = this;
        }

        //
        // TorrentViewerFragment
        if (remoteDeviceTorrent) {
            torrentViewerFragment = new TorrentViewerFragment();
            torrentViewerFragment.parent = this;
        }

        //
        // WakeOnLanFragment
        if (remoteDeviceWakeOnLan) {
            wakeOnLanFragment = new WakeOnLanFragment();
            wakeOnLanFragment.parent = this;
        }

        // crea il CollectionPagerAdapter con le pagine video
        collectionPagerAdapter =
                new CollectionPagerAdapter(
                        getSupportFragmentManager(),
                        getAvailableFragments(),
                        getAvailableFragmentTitles(),
                        getAvailableFragmentTypes()
                );
    }


    private byte[] getCommandAsByteArray(Message command) {
        return String.format("@COMMAND?header=%s&body=%s\n", command.getHeader(), command.getBody()).getBytes();
    }

    private Message getCommandFromBytesArray(byte[] rawData) {

        Message message = new Message("", "", "");
        String rawString = "";
        try {

            rawString = new String(rawData, "UTF-8");

        } catch (UnsupportedEncodingException e) {

            return message;

        }

        String[] mainLine = rawString.split("[?]");
        if (mainLine.length != 2) {
            return message;
        }

        String[] lines = mainLine[1].split("[&]");

        if (lines.length != 3) {

            return message;

        } else {

            String header = "";
            String body = "";
            String replyto = "";

            for (String l : lines) {

                String[] struct = l.split("[=]");

                if (struct.length != 2) {

                    return message;

                } else {

                    switch (struct[0]) {
                        case "header":
                            header = struct[1];
                            break;
                        case "body":
                            body = struct[1];
                            break;
                        case "replyto":
                            replyto = struct[1];
                            break;

                    }

                }

            }

            return new Message(header, body, replyto);

        }

    }

}
