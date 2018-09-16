package loref.android.apps.androidtcpcomm;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

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
    private boolean isChangingAddress = false;

    /*
     * getters and setters
     */

    public String getRemoteAddress() {
        return socket.getRemoteSocketAddress().toString();
    }

    public void setRemoteAddress(String newAddress) {

        isChangingAddress=false;

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
     * methods
     */
    public void init() {

        /*
         inizializza il Socket
        */

        int b;
        ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

        try {

            socket = new Socket(hostAddress, port);
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());

            // manda una notifica all'interfaccia

            if (listener != null) {
                listener.onConnected(socket.getLocalPort());
            }

            while (!isChangingAddress && isRunning && (b = in.read()) != -1) {

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

            if (listener != null && !isChangingAddress) {
                listener.onClose(!isRunning);
            }

            socket.close();
            in.close();
            out.close();

        } catch (IOException e) {

            if (listener != null) {
                listener.onConnectionError(e);
            }

        }

    }

    public void startDataOutLoop() {

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

            while (isRunning && ((b=dataOutPipeReader.read())!=-1)){

                out.write(b);
                bytesOut++;

                if(b=='\n'){
                    out.flush();
                }

            }

            dataOutBuffered.close();
            dataOutPipeWriter.close();
            dataOutPipeReader.close();

        } catch (IOException e) {

            if (listener != null) {
                listener.onDataWriteError(e);
            }

        }

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

      isRunning=false;

    }

    public void disconnect() {

        sendData("@EXIT\n".getBytes());

    }

}
