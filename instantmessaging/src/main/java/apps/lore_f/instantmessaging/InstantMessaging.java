package apps.lore_f.instantmessaging;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import org.jivesoftware.smackx.si.packet.StreamInitiation;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;

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

    private StreamInitiation streamInitiation;

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {

            if (instantMessagingListener != null) instantMessagingListener.onConnected();

            Log.d(TAG, "connected");

        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {

            if (instantMessagingListener != null) instantMessagingListener.onLogIn();

            Log.d(TAG, "authenticated");

        }

        @Override
        public void connectionClosed() {

            if (instantMessagingListener != null) instantMessagingListener.onDisconnected();

            Log.d(TAG, "connectionClosed");

        }

        @Override
        public void connectionClosedOnError(Exception e) {

            if (instantMessagingListener != null) instantMessagingListener.onConnectionError(e);

            Log.d(TAG, "connectionClosedOnError - " + e.getMessage());
        }

        @Override
        public void reconnectionSuccessful() {

            if (instantMessagingListener != null) instantMessagingListener.onConnected();

            Log.d(TAG, "reconnectionSuccessful");
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.d(TAG, "reconnectingIn - " + seconds);
        }

        @Override
        public void reconnectionFailed(Exception e) {

            if (instantMessagingListener != null) instantMessagingListener.onConnectionError(e);

            Log.d(TAG, "reconnectionFailed - " + e.getMessage());
        }

    };

    public interface InstantMessagingListener {
        void onConnected();

        void onDisconnected();

        void onConnectionError(Exception e);

        void onLogIn();

        void onChatCreated();

        void onFileTransferManagerCreated();

        void onMessageReceived(String sender, String messageBody);

        void onFileTransferRequest(FileTransferRequest request);

        void onFileTranferUpdate(double progress, long bytesWritten);

        void onFileTransferCompleted();

        void onFileTransferCompletedWithError();

    }

    private InstantMessagingListener instantMessagingListener;

    public void setInstantMessagingListener(InstantMessagingListener listener) {

        instantMessagingListener = listener;

    }

    public void removeInstantMessagingListener() {

        instantMessagingListener = null;

    }

    public void login() {

        if (!(connection.isConnected() && connection.isAuthenticated())) {

            try {

                connection.login();

            } catch (XMPPException | SmackException | IOException | InterruptedException e) {

                if (instantMessagingListener != null) instantMessagingListener.onConnectionError(e);

            }

        } else {

            if (instantMessagingListener != null) instantMessagingListener.onLogIn();
        }

    }

    public InstantMessaging(String domainName, String username, String password, String resource) {

        /***
         * costruttore
         */

        /* Creo un nuovo oggetto XMPPTCPConnectionConfiguration.Builder */
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .setKeystoreType(null)
                .setConnectTimeout(15000);

        try {

            configBuilder.setResource(resource);
            configBuilder.setXmppDomain(domainName);

            /* inizializzo la classe per la connessione al server XMPP */
            connection = new XMPPTCPConnection(configBuilder.build());
            connection.setPacketReplyTimeout(30000);
            connection.addConnectionListener(connectionListener);

        } catch (XmppStringprepException e) {

            e.printStackTrace();

        }

    }

    public void connect() {

        if (!connection.isConnected()) {

            new AsyncTask<Void, Void, Boolean>() {

                Exception exception = null;

                @Override
                protected Boolean doInBackground(Void... params) {


                    try {

                        connection.connect();
                        return true;

                    } catch (IOException | InterruptedException | SmackException | XMPPException e) {

                        exception = e;
                        return false;

                    }
                }

                protected void onPostExecute(Boolean result) {

                    if (instantMessagingListener != null) {

                        if (result == Boolean.TRUE) {

                            instantMessagingListener.onConnected();

                        } else {

                            instantMessagingListener.onConnectionError(exception);
                        }

                    }

                }

            }.execute();

        }

    }

    public void disconnect() {

        connection.disconnect();

    }

    public boolean isConnected() {

        return connection.isConnected();

    }

    public void sendMessage(String recipient, String messageText) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {

        if (chatManager != null) {

            EntityBareJid jid = JidCreate.entityBareFrom(recipient);
            Chat chat = chatManager.chatWith(jid);
            chat.send(messageText);

            Log.i(TAG, "Message <" + messageText + "> sent to <" + recipient + ">");

        }

    }

    public void createChat() {

        chatManager = ChatManager.getInstanceFor(connection);

        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {

                if (instantMessagingListener != null) {

                    instantMessagingListener.onMessageReceived(from.asEntityBareJidString(), message.getBody());
                }

            }
        });

        if (instantMessagingListener != null) instantMessagingListener.onChatCreated();

    }

    public void createFileTransferManager() {

        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);

        FileTransferNegotiator.IBB_ONLY = true; // TODO: 04/07/2017 deve essere un parametro impostabile

        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {

                if (instantMessagingListener != null && pendingFileTransferRequest == null) {

                    /* non ci sono trasferimenti in corso */
                    pendingFileTransferRequest = request;

                    /* invia la notifica al listener */
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



        if (instantMessagingListener != null)

            instantMessagingListener.onFileTransferManagerCreated();

    }

    public void rejectFileTransfer() {

        if (pendingFileTransferRequest != null) {

            try {

                pendingFileTransferRequest.reject();

            } catch (SmackException.NotConnectedException e) {

                e.printStackTrace();

            } catch (InterruptedException e) {

                e.printStackTrace();

            }

            pendingFileTransferRequest = null;
        }

    }

    public void acceptFileTransfer(File file) {

        fileToDownload = file;
        incomingFileTransfer = pendingFileTransferRequest.accept();

        new Thread() {

            public void run() {

                try {

                    incomingFileTransfer.recieveFile(fileToDownload);
                    String logText;

                    while (!incomingFileTransfer.isDone()) {
                        if (instantMessagingListener != null)
                            instantMessagingListener.onFileTranferUpdate(incomingFileTransfer.getProgress(), incomingFileTransfer.getAmountWritten());
                        logText = incomingFileTransfer.getProgress() + "; " +
                                incomingFileTransfer.getAmountWritten() + "; " +
                                incomingFileTransfer.getStatus().toString() + "; " +
                                incomingFileTransfer.getFileName() + "; " +
                                incomingFileTransfer.getFileSize();

                        if (incomingFileTransfer.getError() != null)
                            logText += "Error: " + incomingFileTransfer.getError().toString() + "; ";
                        if (incomingFileTransfer.getException() != null)
                            logText += "Error: " + incomingFileTransfer.getException().toString() + "; ";

                        Log.d(TAG, logText);
                        sleep(100);

                    }

                    logText = "";
                    if (incomingFileTransfer.getError() != null)
                        logText += "Error: " + incomingFileTransfer.getError().toString() + "; ";
                    if (incomingFileTransfer.getException() != null)
                        logText += "Error: " + incomingFileTransfer.getException().toString() + "; ";

                    Log.d(TAG, logText);

                    if (instantMessagingListener != null)
                        instantMessagingListener.onFileTransferCompleted();

                    pendingFileTransferRequest = null;
                    incomingFileTransfer = null;

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

    public void cancelFileTransfer() {

        incomingFileTransfer.cancel();

    }

}
