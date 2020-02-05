package com.apps.lore_f.domoticcontroller.asynctask;

import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPComm {

    private Socket socket;

    private TCPCommListener listener;

    private String hostName;
    private int port;

    public TCPComm (String hostName, int port, TCPCommListener listener){
        this.hostName = hostName;
        this.port = port;
        this.listener = listener;

        start();
    }

    public void removeListener(){
        this.listener=null;
    }

    private class DataSender implements Runnable {

        private String data;
        private BufferedOutputStream out;

        public DataSender(String data){
            this.data=data;
        }

        @Override
        public void run() {
            try {
                out = new BufferedOutputStream(socket.getOutputStream());
                out.write(data.getBytes());
                out.flush();
            } catch (IOException e) {

            }
        }
    }

    private class TCPCommEngine extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

            BufferedInputStream in;

            try {

                socket = new Socket(hostName, port);
                in = new BufferedInputStream(socket.getInputStream());

                int b;

                ByteArrayOutputStream receivedString = new ByteArrayOutputStream();

                if(listener!=null){

                    listener.onSocketCreated();

                }

                while ((b = in.read()) != -1){

                    receivedString.write(b);

                    if (b == '\n') {

                        receivedString.flush();
                        if (listener != null) {
                            listener.onDataReceived(receivedString.toString());
                        }

                        // reinizializza l'array di byte
                        receivedString = new ByteArrayOutputStream();

                    }

                }

            } catch (UnknownHostException e0) {

            } catch (IOException e1) {

            }

            return null;
        }

    }

    private TCPCommEngine tcpCommEngine;

    private void start(){
        tcpCommEngine = new TCPCommEngine();
        tcpCommEngine.execute("");
    }

    public void sendData(String data){
        new Thread(new DataSender(data+"\n")).start();
    }

    public void terminate(){
        try {
            socket.close();
            if(listener!=null)
                listener.onSocketClosed();
        } catch (IOException e) {

        }
    }

}
