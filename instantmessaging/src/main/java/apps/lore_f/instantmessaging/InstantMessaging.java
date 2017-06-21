package apps.lore_f.instantmessaging;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.StreamNegotiator;
import org.jivesoftware.smackx.si.packet.StreamInitiation;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimerTask;

/**
 * Created by 105053228 on 23/mar/2017.
 */

public class InstantMessaging {

    private static final String TAG = "InstantMessaging";
    private AbstractXMPPConnection connection = null;
    private ChatManager chatManager = null;

    private FileTransferRequest pendingFileTransferRequest;
    private IncomingFileTransfer incomingFileTransfer;
    private File fileToDownload;

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

    private Handler handler = new Handler();

    private Runnable monitorIncomingFileTransfer = new Runnable() {
        @Override
        public void run() {


        }

    };

    public interface InstantMessagingListener{
        void onConnected();
        void onChatCreated();
        void onFileTransferManagerCreated();
        void onMessageReceived(String sender, String messageBody);
        void onFileTransferRequest(FileTransferRequest request);
        void onFileTranferUpdate(double progress, long bytesWritten);
        void onFileTransferCompleted();
        void onFileTransferCompletedWithError();

    }

    InstantMessagingListener instantMessagingListener;

    public void setInstantMessagingListener(InstantMessagingListener listener){

        instantMessagingListener=listener;

    }

    public void removeInstantMessagingListener(){

        instantMessagingListener=null;

    }

    public InstantMessaging(String domainName, String username, String password, String resource){

        /***
         * inizializza la classe
        */

        /* Creo un nuovo oggetto XMPPTCPConnectionConfiguration.Builder */
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        try {
            configBuilder.setResource(resource);
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

    public void createFileTransferManager(){

        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);

        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {

                if (instantMessagingListener!=null && pendingFileTransferRequest==null) {

                    /* non ci sono trasferimenti in corso */
                    pendingFileTransferRequest=request;
                    instantMessagingListener.onFileTransferRequest(pendingFileTransferRequest);


                } else {
                    /* un trasferimento è già in corso */
                    try {
                        request.reject();
                    } catch (SmackException.NotConnectedException | InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }

                }

            }

        });

        if (instantMessagingListener!=null) instantMessagingListener.onFileTransferManagerCreated();

    }

    public void rejectFileTransfer(){

        if (pendingFileTransferRequest!=null){
            try {
                pendingFileTransferRequest.reject();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pendingFileTransferRequest=null;
        }

    }

    public void acceptFileTransfer(File file){

        fileToDownload = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "dio-cane.png");
        incomingFileTransfer = pendingFileTransferRequest.accept();

        new Thread() {

            public void run() {

                try {

                    incomingFileTransfer.recieveFile(fileToDownload);

                    while(!incomingFileTransfer.isDone()){
                        if(instantMessagingListener!=null) instantMessagingListener.onFileTranferUpdate(incomingFileTransfer.getProgress(), incomingFileTransfer.getAmountWritten());
                        String logText= incomingFileTransfer.getProgress()+"; "+
                                incomingFileTransfer.getAmountWritten()+"; "+
                                incomingFileTransfer.getStatus().toString()+"; "+
                                incomingFileTransfer.getFileName()+"; "+
                                incomingFileTransfer.getFileSize();

                        if (incomingFileTransfer.getError()!=null) logText +="Error: "+incomingFileTransfer.getError().toString() +"; ";
                        if (incomingFileTransfer.getException()!=null) logText +="Error: "+incomingFileTransfer.getException().toString() +"; ";

                        Log.d(TAG,logText);
                        sleep(100);
                    }

                    if(instantMessagingListener!=null) instantMessagingListener.onFileTransferCompleted();


                } catch (SmackException e) {

                    e.printStackTrace();

                } catch (InterruptedException e) {

                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }.start();

    }

    public void cancelFileTransfer(){

        incomingFileTransfer.cancel();

    }

}
