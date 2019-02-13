package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.apps.lore_f.domoticcontroller.activities.DeviceSelectionActivity;
import com.apps.lore_f.domoticcontroller.activities.GroupSelection;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppIndex.API)
                .build();



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

        // autenticazione effettuata

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        String groupName=sharedPref.getString(getString(R.string.data_group_name),null);

        if(groupName==null){

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));


        } else {
            // nome del gruppo impostato,
            //
            // lancia l'Activity DeviceSelectionActivity per selezionare il dispositivo a cui connettersi
            Intent intent = new Intent(this, DeviceSelectionActivity.class);
            startActivity(intent);

        }

        // termina l'Activity corrente
        finish();
        return;

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
