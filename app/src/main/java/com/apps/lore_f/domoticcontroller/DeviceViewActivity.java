package com.apps.lore_f.domoticcontroller;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.TimerTask;

public class DeviceViewActivity extends AppCompatActivity {

    // Firebase Database
    private DatabaseReference incomingMessages;

    private String remoteDeviceName;
    private boolean remoteDeviceTorrent;
    private boolean remoteDeviceDirNavi;

    private String thisDevice = "lorenzofailla-g3"; // TODO: 13-Sep-17 deve diventare un parametro di configurazione
    private long replyTimeoutConnection = 15000L; // ms // TODO: 20-Sep-17 deve diventare un parametro di configurazione
    private long replyTimeoutBase = 15000L; // ms // TODO: 20-Sep-17 deve diventare un parametro di configurazione

    private Handler handler;

    // Fragments
    private DeviceInfoFragment deviceInfoFragment;
    private TorrentViewerFragment torrentViewerFragment;
    private FileViewerFragment fileViewerFragment;

    // Estensione di un timertask per assicurarsi che il dispositivo sia connesso e che risponda alle richieste
    private Runnable watchDog = new Runnable() {

        @Override
        public void run() {

            manageRemoteDeviceNotResponding();

        }

    };

    // Listener per nuovi record nel nodo dei messaggi in ingresso.
    private ChildEventListener newCommandsToProcess = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // è arrivato un nuovo messaggio

            // recupera il nuovo messaggio nel formato della classe Message
            GenericTypeIndicator<Message> msg = new GenericTypeIndicator<Message>() {
            };
            Message incomingMessage = dataSnapshot.getValue(msg);

            // dorme per 10 ms - necessario per evitare timestamp identici negli ID delle risposte
            try {

                Thread.sleep(10);

            } catch (InterruptedException e) {

                // nessuna operazione

            }

            // processa il messaggio ricevuto
            processIncomingMessage(incomingMessage, dataSnapshot.getKey());

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            remoteDeviceName = extras.getString("__DEVICE_TO_CONNECT");
            remoteDeviceTorrent = extras.getBoolean("__HAS_TORRENT_MANAGEMENT");
            remoteDeviceDirNavi = extras.getBoolean("__HAS_DIRECTORY_NAVIGATION");

        } else {

            finish();
            return;

        }

        // inizia la connessione al dispositivo: invia la richiesta iniziale
        sendCommandToDevice(
                new Message(
                        "__requestWelcomeMessage",
                        "null",
                        thisDevice
                )
        );

        // attiva il timer
        handler=new Handler();
        handler.postDelayed(watchDog, replyTimeoutConnection);

    }

    private void showDeviceInfo() {

        // gestisce la visualizzazione dei pulsanti
        releaseControls();

        // mostra il Fragment 'deviceInfoFragment'
        deviceInfoFragment = new DeviceInfoFragment();

        deviceInfoFragment.setDeviceInfoFragmentListener(new DeviceInfoFragment.DeviceInfoFragmentListener() {
            @Override
            public void onViewCreated() {
                deviceInfoFragment.updateView();
            }

            @Override
            public void onRebootRemoteDeviceRequest(){

                // invia il comando per riavviare il dispositivo remoto
                rebootHost();
            };

            @Override
            public void onShutdownRemoteDeviceRequest(){

                // invia il comando per spengere il dispositivo remoto
                shutdownHost();

            };

        });

        deviceInfoFragment.logsNode = FirebaseDatabase.getInstance().getReference("Users/lorenzofailla/Devices/" + remoteDeviceName + "/Log");

        showFragment(deviceInfoFragment);

        // mostra il nome del dispositivo remoto nella TextView 'remoteHostName'
        TextView remoteHostName = (TextView) findViewById(R.id.TXV___DEVICEVIEW___HOSTNAME);
        remoteHostName.setText(remoteDeviceName);



        // invia al dispositivo remoto il comando per avere l'uptime
        sendCommandToDevice(
                new Message(
                        "__requestUpTime",
                        "null",
                        thisDevice
                )
        );

        // invia al dispositivo remoto il comando per avere lo spazio disponibile
        sendCommandToDevice(
                new Message(
                        "__requestFreeSpace",
                        "null",
                        thisDevice
                )
        );


    }

    @Override
    protected void onResume() {

        super.onResume();

        // assegna gli OnClickListener ai pulsanti
        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setOnClickListener(onClickListener);

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso
        incomingMessages = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices/lorenzofailla-g3/IncomingCommands");

        // associa un ChildEventListener al nodo per poter processare i messaggi in ingresso
        incomingMessages.addChildEventListener(newCommandsToProcess);

    }

    @Override
    protected void onPause() {

        super.onPause();

        // rimuove il ChildEventListener al nodo per poter processare i messaggi in ingresso
        incomingMessages.removeEventListener(newCommandsToProcess);

        // rimuove gli OnClickListener ai pulsanti
        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setOnClickListener(null);

    }

    public void processIncomingMessage(Message inMsg, String msgKey) {

        boolean deleteMsg = false;

        switch (inMsg.getHeader()) {

            case "WELCOME_MESSAGE":

                // servizi disponibili nel dispositivo remoto

                // TODO: 31-Aug-17 ferma il timer per mettere il dispositivo come offline 


                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "UPTIME_REPLY":
                // risposta al comando uptime

                // modifica il l'aspetto del fragment
                if (deviceInfoFragment != null) {
                    deviceInfoFragment.upTime = inMsg.getBody().replace("\n", "");

                    if (deviceInfoFragment.viewCreated) deviceInfoFragment.updateView();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "FREE_SPACE":
                // spazio a disposizione sul dispositivo remoto

                // modifica il l'aspetto del fragment
                if (deviceInfoFragment != null) {
                    deviceInfoFragment.freeSpace = inMsg.getBody().replace("\n", "");

                    if (deviceInfoFragment.viewCreated) deviceInfoFragment.updateView();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "TORRENT_STARTED":
            case "TORRENT_STOPPED":
            case "TORRENT_REMOVED":
            case "TORRENT_ADDED":

                // invia un instant message con la richiesta della lista dei torrents
                askForTorrentsList();

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "TORRENTS_LIST":

                if (torrentViewerFragment != null) {

                    torrentViewerFragment.nOfTorrents = inMsg.getBody().split("\n").length - 2;

                    if (torrentViewerFragment.nOfTorrents > 0) {

                        torrentViewerFragment.rawTorrentDataLines = inMsg.getBody().split("\n");

                    } else {

                        torrentViewerFragment.nOfTorrents = 0;
                        torrentViewerFragment.rawTorrentDataLines = null;

                    }

                    if (torrentViewerFragment.viewCreated)
                        torrentViewerFragment.updateContent();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "HOME_DIRECTORY":

                if (fileViewerFragment != null) {

                    fileViewerFragment.currentDirName = inMsg.getBody().replace("\n", "");
                    if (fileViewerFragment.viewCreated)
                        fileViewerFragment.updateContent();

                }

                // invia un instant message con la richiesta del contenuto della directory home ricevuta
                sendCommandToDevice(new Message("__get_directory_content", inMsg.getBody(), thisDevice));

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "DIRECTORY_CONTENT":

                if (fileViewerFragment != null) {

                    fileViewerFragment.rawDirData = inMsg.getBody();
                    if (fileViewerFragment.viewCreated)
                        fileViewerFragment.updateContent();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "GENERIC_NOTIFICATION":

                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("Message from " + inMsg.getReplyto())
                        .setContentText(inMsg.getBody())
                        .setSmallIcon(R.drawable.home)
                        .build();


        }

        // se il flag 'deleteMsg' è stato impostato su true, elimina il messaggio dalla coda
        if (deleteMsg)
            deleteMessage(msgKey);

    }

    private void sendCommandToDevice(Message command) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo remoto selezionato
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices");

        // aggiunge il messaggio al nodo
        deviceIncomingCommands
                .child(remoteDeviceName)
                .child("IncomingCommands")
                .child("" + System.currentTimeMillis())
                .setValue(command);

    }

    private void deleteMessage(String id) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo locale
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices");

        // rimuove il messaggio al nodo
        deviceIncomingCommands.child(thisDevice).child("IncomingCommands").child(id).removeValue();

    }

    private void showFragment(Fragment fragment) {

        /* mostra il fragment passato in argomento */
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.VIE___DEVICEVIEW___SUBVIEW, fragment);

        fragmentTransaction.commit();
    }

    private void startTorrentManager() {

        torrentViewerFragment = new TorrentViewerFragment();
        torrentViewerFragment.localTorrentsListAdapterListener = torrentsListAdapterListener;
        torrentViewerFragment.setTorrentsViewerListener(new TorrentViewerFragment.TorrentsViewerListener() {
            @Override
            public void onAddTorrentRequest() {

                addTorrent();

            }

        });

        showFragment(torrentViewerFragment);

        // invia un instant message con la richiesta della lista dei torrents
        askForTorrentsList();
    }

    private void askForTorrentsList() {

        // invia un instant message con la richiesta della lista dei torrents
        sendCommandToDevice(new Message("__listTorrents", "null", thisDevice));

    }

    private void releaseControls() {

        if (remoteDeviceTorrent) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.GONE);

        }

        if (remoteDeviceDirNavi) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.GONE);

        }

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.BTN___DEVICEVIEW___DEVICEINFO:

                    showDeviceInfo();

                    break;


                case R.id.BTN___DEVICEVIEW___FILEMANAGER:

                    startFileManager();

                    break;

                case R.id.BTN___DEVICEVIEW___TORRENTMANAGER:

                    startTorrentManager();

                    break;


            }

        }

    };



    private void rebootHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                // invia un instant message con il comando di reboot
                sendCommandToDevice(new Message("__reboot", "null", thisDevice));
                showDeviceInfo();

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

    private void shutdownHost() {

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

    private void startFileManager() {

        fileViewerFragment = new FileViewerFragment();
        fileViewerFragment.fileListAdapterListener = fileListFragmentAdapterListener;
        fileViewerFragment.setFileViewerFragmentListener(new FileViewerFragment.FileViewerFragmentListener() {
            @Override
            public void onViewCreated() {

                fileViewerFragment.updateContent();
            }
        });

        // mostra il Fragment
        showFragment(fileViewerFragment);

        // invia al dispositivo remoto la richiesta di conoscere la directory corrente
        sendCommandToDevice(new Message("__get_homedir", "null", thisDevice));

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

    private void addTorrent() {

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

    private TorrentsListAdapter.TorrentsListAdapterListener torrentsListAdapterListener = new TorrentsListAdapter.TorrentsListAdapterListener() {
        @Override
        public void onStartRequest(int torrentID) {

            // invia il commando all'host remoto
            sendCommandToDevice(new Message("__start_torrent", "" + torrentID, thisDevice));

        }

        @Override
        public void onStopRequest(int torrentID) {

            // invia il commando all'host remoto
            sendCommandToDevice(new Message("__stop_torrent", "" + torrentID, thisDevice));
        }

        @Override
        public void onRemoveRequest(final int torrentID) {

            removeTorrent(torrentID);

        }

    };

    private FileListAdapter.FileListAdapterListener fileListFragmentAdapterListener = new FileListAdapter.FileListAdapterListener() {
        @Override
        public void onItemSelected(FileInfo fileInfo) {

            if (fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_FILE) {

                // attiva la procedura di upload del file da parte del dispositivo remoto sulla piattaforma Firebase Storage
                sendCommandToDevice(new Message("__get_file", fileInfo.getFileRoorDir() + "/" + fileInfo.getFileName(), thisDevice));

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

    };

    private void manageRemoteDeviceNotResponding(){

        // costruisce un AlertDialog e lo mostra a schermo
        new AlertDialog.Builder(this)
                .setTitle(R.string.ALERTDIALOG_TITLE_REMOTE_DEVICE_NOT_RESPONDING)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_REMOTE_DEVICE_NOT_RESPONDING)
                .setPositiveButton(R.string.ALERTDIALOG_GOT_IT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // aggiorna il database cloud impostando la proprietà online del dispositivo remoto a false
                        finish();
                    }
                })
                .create()
                .show();


    }

}
