package com.apps.lore_f.imtest;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";

    AbstractXMPPConnection connection = null;
    Chat chat = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... empty) {

            // Create the configuration for this new connection

            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
            configBuilder.setUsernameAndPassword("controller", "fornaci12Controller");
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            try {
                configBuilder.setXmppDomain("lorenzofailla.p1.im");
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }

            try {

                connection = new XMPPTCPConnection(configBuilder.build());
                connection.connect();
                connection.login();
                Log.d(TAG, "connected to " + connection.getHost());

            } catch (XmppStringprepException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }

            return connection.isConnected();
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {

            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            EntityBareJid jid = null;

            try {

                jid = JidCreate.entityBareFrom("Home@lorenzofailla.p1.im");

            } catch (XmppStringprepException e) {

                e.printStackTrace();
            }

            chat = chatManager.createChat(jid);
            Log.d(TAG, "chat session created.");

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendMessageButton = (Button) findViewById(R.id.sendMessageButton);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    chat.sendMessage("Howdy!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });

        new ConnectionTask().execute(new Void[0]);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
