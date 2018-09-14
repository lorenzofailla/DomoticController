package loref.android.apps.androidtcpcomm;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TCPComm {

    public static final String TAG = "TCPComm";

    public static final int DEFAULT_PORT = 9099;

    private Socket socket;
    private String hostAddress;
    private int port;

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private boolean keepLooping = true;

    private long bytesIn = 0L;
    private long bytesOut = 0L;

    /*
     * getters and setters
     */

    public String getRemoteAddress(){
        return socket.getRemoteSocketAddress().toString();
    }

    public long getBytesIn(){
        return bytesIn;
    }

    public long getBytesOut(){
        return bytesOut;
    }

    /*
     * constructor
     */

    public TCPComm(String hostAddress) {
        this.hostAddress = hostAddress;
        this.port = DEFAULT_PORT;

    }

    public TCPComm(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    /*
     * listener
     */

    private TCPCommListener listener;

    public void setListener(TCPCommListener l) {
        listener = l;
    }

    public TCPCommListener getListener() {
        return listener;
    }

    /*
     * methods
     */
    public void init() {

        /*
         inizializza il Socket
        */

        // avvia il ciclo principale che gestisce la lettura dei dati in entrata
        AsyncTaskCompat.executeParallel(new MainLoop());

    }

    public void sendData(byte[] data) {

        AsyncTaskCompat.executeParallel(new SendDataAsync(data));

    }

    public void terminate() {

        keepLooping = false;

    }

    private class SendDataAsync extends AsyncTask<Void, Void, Void> {

        private byte[] dataToBeSent;

        public SendDataAsync(byte[] dataToBeSent) {
            this.dataToBeSent = dataToBeSent;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                out.write(dataToBeSent);
                out.flush();

                bytesOut+=dataToBeSent.length;

                String logData;
                try {
                    logData = new String(dataToBeSent, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logData = "";
                }
                Log.d(TAG, "TCP data sent: " + logData);

            } catch (IOException e) {

                if (listener != null) {
                    listener.onDataWriteError(e);
                }

            }

            return null;
        }

    }

    private class MainLoop extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                socket = new Socket(hostAddress, port);
                in = new BufferedInputStream(socket.getInputStream());
                out = new BufferedOutputStream(socket.getOutputStream());

                // manda una notifica all'interfaccia

                if (listener != null) {
                    listener.onConnected(socket.getLocalPort());
                }

            } catch (IOException e) {

                if (listener != null) {
                    listener.onConnectionError(e);
                }
                return null;
            }

            int b;
            ByteArrayOutputStream outputData = new ByteArrayOutputStream();

            try {

                while (keepLooping && (b = in.read()) != -1) {

                    bytesIn++;
                    outputData.write(b);

                    if (b == '\n') {

                        outputData.flush();
                        if (listener != null) {
                            listener.onDataLineReceived(outputData.toByteArray());
                        }

                        // reinizializza l'array di byte
                        outputData = new ByteArrayOutputStream();

                    }

                }

                if (listener != null) {
                    listener.onClose(!keepLooping);
                }

                socket.close();

                Log.d(TAG, "Socket closed");

            } catch (IOException e) {

                if (listener != null) {
                    listener.onDataReadError(e);

                }

            }

            return null;

        }

    }

}
