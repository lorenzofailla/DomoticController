package loref.android.apps.androidtcpcomm;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPComm {

    public static final String TAG = "TCPComm";

    public static final int DEFAULT_PORT = 9099;

    private Socket socket;
    private String hostAddress;
    private int port = DEFAULT_PORT;

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private PipedInputStream dataOutPipeReader;
    private PipedOutputStream dataOutPipeWriter;
    private BufferedOutputStream dataOutBuffered;

    private long bytesIn = 0L;
    private long bytesOut = 0L;

    private boolean isRunning = true;
    private boolean addressChanged;
    private boolean isConnected = false;

    /*
     * getters and setters
     */

    public String getRemoteAddress() {
        return socket.getRemoteSocketAddress().toString();
    }

    public void setRemoteAddress(String newAddress) {

        hostAddress = newAddress;
        addressChanged = true;

    }

    public long getBytesIn() {
        return bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    /*
     * constructor
     */

    public TCPComm(String hostAddress) {

        this.hostAddress = hostAddress;

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
    Asynchronous threads
     */

    private class MainLoop extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            int b;
            ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

            while (isRunning) {

                addressChanged = false;

                try {

                    socket = new Socket();
                    socket.connect(new InetSocketAddress(hostAddress, port), Defaults.CONNECTION_TIMEOUT);
                    isConnected = true;

                    // manda una notifica all'interfaccia

                    if (listener != null) {
                        listener.onConnected(socket.getLocalPort());
                    }

                    in = new BufferedInputStream(socket.getInputStream());
                    out = new BufferedOutputStream(socket.getOutputStream());

                    while ((b = in.read()) != -1) {

                        bytesIn++;
                        receivedData.write(b);

                        if (b == '\n') {

                            receivedData.flush();
                            if (listener != null) {
                                listener.onDataLineReceived(receivedData.toByteArray());
                            }

                            // reinizializza l'array di byte
                            receivedData = new ByteArrayOutputStream();

                        }

                    }

                    if (listener != null && !addressChanged) {
                        listener.onClose(!isRunning);
                    }

                    isConnected = false;

                } catch (IOException e) {

                    if (listener != null) {

                        listener.onConnectionError(e);

                    }

                    while (isRunning && !addressChanged) {

                        // does nothing

                    }

                } finally {

                    try {
                        socket.close();
                    } catch (IOException e) {
                    } catch (NullPointerException e) {
                    }

                    try {
                        in.close();
                    } catch (IOException e) {
                    } catch (NullPointerException e) {
                    }

                    try {
                        out.close();
                    } catch (IOException e) {
                    } catch (NullPointerException e) {
                    }

                    try {
                        dataOutBuffered.close();
                    } catch (IOException e) {
                    } catch (NullPointerException e) {
                    }

                    try {
                        dataOutPipeReader.close();
                    } catch (IOException e) {
                    } catch (NullPointerException e) {
                    }

                    try {
                        dataOutPipeWriter.close();
                    } catch (IOException e) {
                    } catch (NullPointerException e) {
                    }

                }

            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d(TAG, "MainLoop closed.");
        }

    }

    private class DataOutLoop extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            int b;

            // inizializza i piped stream
            dataOutPipeReader = new PipedInputStream();
            dataOutPipeWriter = new PipedOutputStream();

            // inizializza il buffer dei dati in ingresso da mandare al server
            dataOutBuffered = new BufferedOutputStream(dataOutPipeWriter);

            // manda una notifica all'interfaccia

            if (listener != null) {
                listener.onInterfaceReady();
            }

            // collega i piped stream
            try {

                dataOutPipeWriter.connect(dataOutPipeReader);

                while (((b = dataOutPipeReader.read()) != -1)) {

                    out.write(b);
                    bytesOut++;

                    if (b == '\n') {
                        out.flush();
                    }

                }

            } catch (IOException e) {

                if (listener != null) {
                    listener.onDataWriteError(e);
                }

            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d(TAG, "DataOutLoop closed.");

        }

    }

    /*
     * methods
     */

    public void init() {

        /*
         inizializza il Socket
        */

        AsyncTaskCompat.executeParallel(new MainLoop());


    }

    public void startDataOutLoop() {

        AsyncTaskCompat.executeParallel(new DataOutLoop());

    }

    public void sendData(byte[] data) {

        try {

            dataOutBuffered.write(data);
            dataOutBuffered.flush();

        } catch (IOException e) {

            if (listener != null) {
                listener.onDataWriteError(e);
            }

        }

    }

    public void terminate() {

        isRunning = false;

    }

    public void disconnect() {

        sendData("@EXIT\n".getBytes());

    }

}
