package loref.android.apps.androidtcpcomm;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPComm {

    public static final int DEFAULT_PORT = 9099;

    private Socket socket;
    private String hostAddress;
    private int port;

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private boolean keepLooping = true;

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
            new MainLoop().execute();




    }

    public void sendData(byte[] data) {

        try {

            out.write(data);
            out.flush();

        } catch (IOException e) {

            if (listener != null) {
                listener.onDataWriteError(e);
            }

        }

    }

    public void terminate() {

        keepLooping = false;

    }


    private class MainLoop extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                socket = new Socket(hostAddress, port);
                in = new BufferedInputStream(socket.getInputStream());
                out = new BufferedOutputStream(socket.getOutputStream());

                socket.connect(socket.getRemoteSocketAddress());

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

                    if (b == '\n') {

                        outputData.flush();
                        if(listener!=null) {
                            listener.onDataLineReceived(outputData.toByteArray());
                        }

                        // reinizializza l'array di byte
                        outputData = new ByteArrayOutputStream();

                    }

                }

                if (listener != null) {
                    listener.onClose(!keepLooping);
                }

            } catch (IOException e) {

                if (listener != null) {
                    listener.onDataReadError(e);
                }

            }

            return null;

        }

    }

    private class InitThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            return null;

        }

    }

}
