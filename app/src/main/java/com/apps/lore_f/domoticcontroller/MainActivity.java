package com.apps.lore_f.domoticcontroller;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.io.File;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient googleApiClient;

    // Firebase Auth
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    private String pendingDownloadFileName;

    private Runnable hostReplyTimeout = new Runnable() {
        @Override
        public void run() {

            /* messaggio utente */
            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                    .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create();

        }
    };

    /*
    */

   /*

        @Override
        public void onMessageReceived(String sender, String messageBody) {

            Log.i(TAG, messageBody);
            String command = messageBody.substring(0, 23);

            switch (command) {

                case "%%%_home_directory__%%%":

                    fileViewerFragment.currentDirName = messageBody.substring(23).replace("\n","");
                    sendIM(HOME_ADDRESS, "__get_directory_content:::"+fileViewerFragment.currentDirName);
                    break;

                case "%%%_dir_content_____%%%":

                    fileViewerFragment.rawDirData= messageBody.substring(23);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            fileViewerFragment.updateContent();

                        }

                    });


                    break;

                case "%%%_command_reply___%%%":
                    Log.i(TAG,messageBody.substring(23));
                    break;



                    break;



            }

        }

        @Override
        public void onFileTransferRequest(FileTransferRequest request) {

            // inizializza il file su cui verrà effettuato il download della risorsa remota
            File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Raspberry-pi remote");
            if (!downloadDirectory.exists()){
                downloadDirectory.mkdir();
            }

            File downloadFileResource = new File(downloadDirectory.getPath() + File.separator + pendingDownloadFileName);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    fileViewerFragment.hideContent();

                }
            });

            instantMessaging.acceptFileTransfer(downloadFileResource);

        }

        @Override
        public void onFileTranferUpdate(double progress, long bytesWritten) {

            final double progressToShow = progress;
            final long bytesWrittenToShow = bytesWritten;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    fileViewerFragment.updateFileTransferProgress(progressToShow, bytesWrittenToShow);

                }
            });

        }

        @Override
        public void onFileTransferCompleted() {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    fileViewerFragment.updateContent();

                }
            });

        }

        @Override
        public void onFileTransferCompletedWithError() {

        }

    };

    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppIndex.API)
                .build();

        // inizializza il FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        // ottiene l'user corrente
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // autenticazione non effettuata

            // lancia la SignInActivity e termina l'attività corrente
            startActivity(new Intent(this, GoogleSignInActivity.class));
            finish();
            return;

        }

        // lancia DeviceSelectionActivity per selezionare il dispositivo a cui connettersi
        startActivity(new Intent(this, DeviceSelectionActivity.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainactivity_menu, menu);

        return true;

    }






    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApiClient.connect();
        AppIndex.AppIndexApi.start(googleApiClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(googleApiClient, getIndexApiAction());
        googleApiClient.disconnect();
    }

    @Override
    public void onResume() {

        super.onResume();

    }

    public void onPause() {
        super.onPause();

    }

    public void onDestroy() {

        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.sign_out_menuEntry:

                // è stato selezionata l'opzione di sign out dal menu
                firebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient);

                startActivity(new Intent(this, GoogleSignInActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }





}
