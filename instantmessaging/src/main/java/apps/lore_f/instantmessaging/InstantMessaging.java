package apps.lore_f.instantmessaging;

import android.os.AsyncTask;
import android.util.Log;


import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

/**
 * Created by 105053228 on 23/mar/2017.
 */

public class InstantMessaging {

    private static final String TAG = "InstantMessaging";
    private AbstractXMPPConnection connection = null;
    private ChatManager chatManager = null;

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... emptyParams) {

            if(!connection.isConnected()) {
                try {

                    connection.connect().login();
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

            }

            return connection.isConnected();

        }

        @Override
        protected void onPostExecute(Boolean isConnected) {

            if(isConnected){
                if (instantMessagingListener!=null) instantMessagingListener.onConnected();
            }

        }

    }

    public interface InstantMessagingListener{
        void onConnected();
        void onChatCreated();
        void onMessageReceived(String sender, String messageBody);
    }

    InstantMessagingListener instantMessagingListener;

    public void setInstantMessagingListener(InstantMessagingListener listener){

        instantMessagingListener=listener;

    }

    public void removeInstantMessagingListener(){

        instantMessagingListener=null;

    }

    public InstantMessaging(String domainName, String username, String password){

        /***
         * inizializza la classe
        */

        /* Creo un nuovo oggetto XMPPTCPConnectionConfiguration.Builder */
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        try {

            configBuilder.setXmppDomain(domainName);

            /* faccio costruire al builder l'oggetto AbstractXMPPConnection */
            connection = new XMPPTCPConnection(configBuilder.build());
            connect();

        } catch (XmppStringprepException e) {

            e.printStackTrace();

        }

    }

    public void connect(){

        new ConnectionTask().execute();

    }

    public void disconnect(){

    }

    public void sendMessage(String recipient, String messageText) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {

        if (chatManager!=null) {

            EntityBareJid jid = JidCreate.entityBareFrom(recipient);
            Chat chat = chatManager.chatWith(jid);
            chat.send(messageText);

            Log.i(TAG, "Message <"+messageText+"> sent to <"+recipient +">");
        }

    }

    public void createChat(){

        chatManager = ChatManager.getInstanceFor(connection);

        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {

                if(instantMessagingListener!=null){

                    instantMessagingListener.onMessageReceived(from.asEntityBareJidString(), message.getBody());
                }

            }
        });

        if(instantMessagingListener!=null) instantMessagingListener.onChatCreated();

    }

}
