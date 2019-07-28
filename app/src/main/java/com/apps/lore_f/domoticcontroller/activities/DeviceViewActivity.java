package com.apps.lore_f.domoticcontroller.activities;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

import com.apps.lore_f.domoticcontroller.DeviceSSHFragment;
import com.apps.lore_f.domoticcontroller.DownloadFileFromDataSlots;
import com.apps.lore_f.domoticcontroller.FileViewerFragment;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.VideoCameraViewerFragment;
import com.apps.lore_f.domoticcontroller.fragments.DeviceInfoFragment;
import com.apps.lore_f.domoticcontroller.fragments.TorrentViewerFragment;
import com.apps.lore_f.domoticcontroller.fragments.WakeOnLanFragment;
import com.apps.lore_f.domoticcontroller.generic.classes.DeviceDataParser;
import com.apps.lore_f.domoticcontroller.generic.classes.Message;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.FileInfo;
import com.apps.lore_f.domoticcontroller.generic.classes.FragmentsCollection;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.zip.DataFormatException;

import static apps.android.loref.GeneralUtilitiesLibrary.decode;
import static apps.android.loref.GeneralUtilitiesLibrary.decompress;

import static com.apps.lore_f.domoticcontroller.DefaultValues.*;
import static com.apps.lore_f.domoticcontroller.generic.classes.FragmentsCollection.FragmentType.CAMERA_VIEWER;
import static com.apps.lore_f.domoticcontroller.generic.classes.FragmentsCollection.FragmentType.DEVICE_INFO;
import static com.apps.lore_f.domoticcontroller.generic.classes.FragmentsCollection.FragmentType.DIRECTORY_NAVIGATOR;
import static com.apps.lore_f.domoticcontroller.generic.classes.FragmentsCollection.FragmentType.TORRENT_MANAGER;
import static com.apps.lore_f.domoticcontroller.generic.classes.FragmentsCollection.FragmentType.WOL_MANAGER;

public class DeviceViewActivity extends AppCompatActivity {

    private static final String TAG = "DeviceViewActivity";

    private static final int UNSPECIFIED_INT_VALUE = -1;
    private static final String UNSPECIFIED_STRING_VALUE = "_unspecified_";

    public static final String SESSIONMODE_TAG = "__SESSION_MODE";
    public static final int SESSIONMODE_NEW = 1;
    public static final int SESSIONMODE_RETRIEVE = 2;

    public static final String CONNECTIONMETHOD_TAG = "__CONNECTION_METHOD";
    public static final int CONNECTIONMETHOD_FIREBASE = 1;
    public static final int CONNECTIONMETHOD_FIREBASE_CRITICAL = 2;

    public static final int CONNECTIONMETHOD_TCP = 3;

    public static final String DEVICE_TO_CONNECT_TAG = "__DEVICE_TO_CONNECT";
    public static final String STATICDATA_JSON_TAG = "__STATICDATA_JSON";

    public static final String ACTIONTYPE_TAG = "__ACTION_TYPE";
    public static final int ACTIONTYPE_NOTSPECIFIED = -1;
    public static final int ACTIONTYPE_VIEWALL = 1;
    public static final int ACTIONTYPE_CAMERAMONITOR = 2;

    public static final String IPADDRESSESLIST_TAG = "__IP_ADDRESSES_LIST";

    private int sessionMode = UNSPECIFIED_INT_VALUE;
    private int connectionType = UNSPECIFIED_INT_VALUE;
    private int viewType = UNSPECIFIED_INT_VALUE;

    //region GESTIONE FRAGMENTS

    private CollectionPagerAdapter collectionPagerAdapter;
    private FragmentsCollection fragments = new FragmentsCollection();

    private DeviceInfoFragment deviceInfoFragment;
    private TorrentViewerFragment torrentViewerFragment;
    private FileViewerFragment fileViewerFragment;
    private WakeOnLanFragment wakeOnLanFragment;
    private DeviceSSHFragment deviceSSHFragment;
    private String[] cameraNames;
    private String[] cameraIDs;
    private int nOfAvailableCameras;

    private int deviceInfoFragmentIndex = 0;
    private int firstCameraFragmentIndex = 0;

    private String staticDataJSON;

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

        CollectionPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int i) {

            Log.i(TAG, String.format("getItem(%d)", i));
            initializeFragmentAction(i);
            return fragments.getFragment(i);

        }

        @Override
        public int getCount() {

            return fragments.getFragmentsNumber();

        }

        public void initializeFragmentAction(int fragmentPosition) {

            switch (fragments.getFragmentType(fragmentPosition)) {

                case DEVICE_INFO:
                    if (deviceInfoFragment != null) {
                        deviceInfoFragment.refreshView();
                    }
                    break;

                case DIRECTORY_NAVIGATOR:

                    if (fileViewerFragment.currentDirName == null) {
                        // invia al dispositivo remoto la richiesta di conoscere la directory corrente
                        sendCommandToDevice(new Message("__get_homedir", "null", thisDevice));
                    }

                    break;

                case TORRENT_MANAGER:
                    requestTorrentsData();
                    break;

                case WOL_MANAGER:
                    // no action
                    break;

                case CAMERA_VIEWER:
                    /*
                     * Calls
                     * */

                    VideoCameraViewerFragment f = (VideoCameraViewerFragment) fragments.getFragment(fragmentPosition);
                    f.manageLiveBroadcastStatus();

            }

        }

    }

    private void initFragments() {

        DeviceDataParser deviceData = new DeviceDataParser();

        if (!deviceData.setStaticDataJSON(staticDataJSON)) {
            finish();
            return;
        }

        boolean createDeviceInfoFragment;

        switch (action) {

            case ACTIONTYPE_CAMERAMONITOR:
                createDeviceInfoFragment = false;
                break;

            default:
                createDeviceInfoFragment = true;

        }

        deviceInfoFragmentIndex = -1;
        firstCameraFragmentIndex = -1;

        int count = 0;

        fragments.clearFragments();

        //inizializza i fragment

        // WakeOnLanFragment
        if (deviceData.isHasWakeOnLan()) {

            wakeOnLanFragment = new WakeOnLanFragment();
            wakeOnLanFragment.parent = this;

            fragments.add(count, wakeOnLanFragment, WOL_MANAGER, "Wake-On-Lan");
            count++;

        }

        // TorrentViewerFragment
        if (deviceData.isHasTorrent()) {

            torrentViewerFragment = new TorrentViewerFragment();
            torrentViewerFragment.parent = this;

            fragments.add(count, torrentViewerFragment, TORRENT_MANAGER, "Transmission controller");
            count++;

        }

        // FileViewerFragment
        if (deviceData.isHasFileManager()) {

            fileViewerFragment = new FileViewerFragment();
            fileViewerFragment.parent = this;

            fragments.add(count, fileViewerFragment, DIRECTORY_NAVIGATOR, "Directory Navigator");
            count++;

        }

        // DeviceInfoFragment
        if (createDeviceInfoFragment) {

            deviceInfoFragment = new DeviceInfoFragment();
            deviceInfoFragment.setParent(this);

            fragments.add(count, deviceInfoFragment, DEVICE_INFO, "Device info");
            deviceInfoFragmentIndex = count;

            count++;

        }

        // VideoSurveillanceCameraListFragment
        if (deviceData.isHasVideoSurveillance()) {

            JSONObject data = deviceData.getVideoSurveillanceJSON();

            try {

                JSONArray cameras = data.getJSONArray("Cameras");

                for (int i = 0; i < cameras.length(); i++) {

                    VideoCameraViewerFragment temp;
                    String cameraID = cameras.getJSONObject(i).getString("ID");
                    temp = new VideoCameraViewerFragment();
                    temp.setCameraID(cameraID);
                    temp.setCameraName(cameras.getJSONObject(i).getString("Name"));
                    temp.setCameraFullID(cameras.getJSONObject(i).getString("FullID"));
                    temp.setParent(this);

                    fragments.add(count, temp, CAMERA_VIEWER, "Camera " + cameraID);

                    if (i == 0) {

                        firstCameraFragmentIndex = count;

                    }

                    count++;

                }

            } catch (JSONException e) {

                e.printStackTrace();

            }

        }

    }

    public void updateWOLFragment() {

        if (this.wakeOnLanFragment != null) {

            this.wakeOnLanFragment.updateContent(staticDataJSON);

        }

    }

    // endregion

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
            String deviceNode = String.format("Devices/%s/%s", groupName, remoteDeviceName);
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
    ValueEventListener for remote device general status data
     */

    ValueEventListener generalStatusValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot != null) {

                String generalStatusJSON = dataSnapshot.getValue().toString();

                if (deviceInfoFragment != null) {

                    if (deviceInfoFragment.getDeviceData().setStatusDataJSON(generalStatusJSON))
                        deviceInfoFragment.refreshView();

                }

            } else {

                String logData;
                logData = LOG_FIREBASEDB_NODATA;
                Log.d(TAG, "generalStatusValueEventListener - " + logData);

            }

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

            if (dataSnapshot != null) {

                String networkStatusJSON = dataSnapshot.getValue().toString();

                if (deviceInfoFragment != null) {

                    if (deviceInfoFragment.getDeviceData().setNetworkDataJSON(networkStatusJSON)) ;
                    deviceInfoFragment.refreshView();

                }

            } else {

                String logData;
                logData = LOG_FIREBASEDB_NODATA;
                Log.d(TAG, "networkStatusValueEventListener - " + logData);

            }

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

    private int action;

    public String thisDevice;
    public String groupName;

    private Handler handler;

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

    private void terminate() {
        finish();
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        groupName = sharedPref.getString(getString(R.string.data_group_name), null);

        if (groupName == null) {

            //  questa parte di codice non dovrebbe essere mai eseguita, viene tenuta per evitare eccezioni

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));

            // termina l'Activity corrente
            finish();
            return;

        }

        // recupera l'extra dall'intent,

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        action = intent.getIntExtra(ACTIONTYPE_TAG, UNSPECIFIED_INT_VALUE);
        remoteDeviceName = intent.getStringExtra(DEVICE_TO_CONNECT_TAG);

        // recupera la modalità di connessione
        connectionType = extras.getInt(CONNECTIONMETHOD_TAG);

        staticDataJSON = extras.getString(STATICDATA_JSON_TAG);

        initFragments();

        // crea il CollectionPagerAdapter
        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());

        setContentView(R.layout.activity_device_view);


            /*



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



        } else {

            // non ci sono extra nell'Intent

            finish();
            return;

        }

        // inizializza i fragments
        initFragments();

        // crea il CollectionPagerAdapter
        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
        */

    }

    @Override
    protected void onResume() {

        super.onResume();

        // definisce il nome del dispositivo locale
        thisDevice = Settings.Secure.getString(getContentResolver(), "bluetooth_name")+"_"+System.currentTimeMillis();

        // set up the toolbar of this activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.TBR___DEVICEVIEW___TOOLBAR);

        if (connectionType == CONNECTIONMETHOD_FIREBASE || connectionType == CONNECTIONMETHOD_FIREBASE_CRITICAL) {
            // imposta il metodo di comunicazione via Firebase DB

            attachFirebaseListeners();

            if (connectionType == CONNECTIONMETHOD_FIREBASE_CRITICAL) {
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

            } else {

                toolbar.setTitle(remoteDeviceName);
                toolbar.inflateMenu(R.menu.deviceview_menu);

            }


        }

        // imposta il ViewPager per la gestione dei fragment
        ViewPager viewPager = (ViewPager) findViewById(R.id.PGR___DEVICEVIEW___MAINPAGER);
        viewPager.setAdapter(collectionPagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);

        // se l'azione specificata non è "monitor", posiziona il ViewPager sul fragment deviceInfoFragment

        if (action != ACTIONTYPE_CAMERAMONITOR) {

            viewPager.setCurrentItem(deviceInfoFragmentIndex);

        }


/*
        // attiva il ciclo di richieste
        handler = new Handler();

        isPausing = false;

        setIsTCPCommIntefaceAvailable(false);

        */

    }

    private void attachFirebaseListeners() {

        // inizializza i riferimenti ai nodi del db Firebase
        String incomingMessagesNode = String.format("IncomingCommands/%s/%s", groupName, thisDevice);

        // definisce il nodo dei messaggi in ingresso
        incomingMessagesRef = FirebaseDatabase.getInstance().getReference(incomingMessagesNode);

        // associa un ChildEventListener al nodo per poter processare i messaggi in ingresso
        incomingMessagesRef.addChildEventListener(newCommandsToProcess);

        // rimuove i listener dai nodi
        String generalStatusNode = String.format("Devices/%s/%s/StatusData", groupName, remoteDeviceName);
        String networkStatusNode = String.format("Devices/%s/%s/NetworkData", groupName, remoteDeviceName);

        FirebaseDatabase.getInstance().getReference(generalStatusNode).addValueEventListener(generalStatusValueEventListener);
        FirebaseDatabase.getInstance().getReference(networkStatusNode).addValueEventListener(networkStatusValueEventListener);

    }

    private void detachFirebaseListeners() {
        // rimuove i ChildEventListener dai nodi del db di Firebase
        incomingMessagesRef.removeEventListener(newCommandsToProcess);

        // rimuove i listener dai nodi
        String generalStatusNode = String.format("Devices/%s/%s/StatusData", groupName, remoteDeviceName);
        String networkStatusNode = String.format("Devices/%s/%s/NetworkData", groupName, remoteDeviceName);

        FirebaseDatabase.getInstance().getReference(generalStatusNode).removeEventListener(generalStatusValueEventListener);
        FirebaseDatabase.getInstance().getReference(networkStatusNode).removeEventListener(networkStatusValueEventListener);

    }

    @Override
    protected void onPause() {

        super.onPause();

        isPausing = true;

        // rimuove i ChildEventListener dai nodi del db di Firebase
        detachFirebaseListeners();

        // se attivo, rimuove l'handler all'azione posticipata per gestire la mancata risposta dal server
        if (deviceNotRespondingAction != null) {
            handler.removeCallbacks(deviceNotRespondingAction);
        }

        // rimuove l'OnPageChangeListener al ViewPager
        ViewPager viewPager = (ViewPager) findViewById(R.id.PGR___DEVICEVIEW___MAINPAGER);
        viewPager.removeOnPageChangeListener(onPageChangeListener);

        handler = null;

    }

    private void processIncomingMessage(Message inMsg, String msgKey) {

        boolean deleteMsg = (msgKey != "null");
        String decodedBody = decode(inMsg.getBody());

        switch (inMsg.getHeader()) {

            case "WELCOME_MESSAGE":

                // mostra il nome del dispositivo remoto nella toolbar

                Toolbar toolbar = (Toolbar) findViewById(R.id.TBR___DEVICEVIEW___TOOLBAR);
                toolbar.setTitle(remoteDeviceName);
                toolbar.inflateMenu(R.menu.deviceview_menu);

                // ferma l'esecuzione del task
                if (deviceNotRespondingAction != null) {
                    handler.removeCallbacks(deviceNotRespondingAction);
                }

                break;



            case "TORRENT_STARTED":
            case "TORRENT_STOPPED":
            case "TORRENT_REMOVED":
            case "TORRENT_ADDED":

                // invia un instant message con la richiesta della lista dei torrents
                requestTorrentsData();

                break;

            case "TORRENT_DATA":

                if (torrentViewerFragment.viewCreated)
                    torrentViewerFragment.updateContent(decodedBody);

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
                VideoCameraViewerFragment fragment = (VideoCameraViewerFragment) fragments.getFragment(firstCameraFragmentIndex + cameraIndex - 1);

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

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo remoto selezionato
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("IncomingCommands");

        // aggiunge il messaggio al nodo
        deviceIncomingCommands
                .child(groupName)
                .child(remoteDeviceName)
                .child("" + System.currentTimeMillis())
                .setValue(command);

    }

    private void deleteMessage(String id) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo locale
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("IncomingCommands");

        // rimuove il messaggio al nodo
        deviceIncomingCommands.child(groupName).child(thisDevice).child(id).removeValue();

    }

    public void requestTorrentsData() {

        // invia un instant message con la richiesta della lista dei torrents
        sendCommandToDevice(new Message("__get_torrent_data_json", "null", thisDevice));

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

    private void removeTorrent(final String id) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_CONFIRM_TORRENT_REMOVAL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendCommandToDevice(new Message("__remove_torrent", id, thisDevice));

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

    public void torrentStartRequest(String torrentID) {

        // invia il commando all'host remoto
        sendCommandToDevice(new Message("__start_torrent", torrentID, thisDevice));

    }

    public void torrentStopRequest(String torrentID) {

        // invia il commando all'host remoto
        sendCommandToDevice(new Message("__stop_torrent", torrentID, thisDevice));
    }

    public void torrentRemoveRequest(String torrentID) {

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

}
