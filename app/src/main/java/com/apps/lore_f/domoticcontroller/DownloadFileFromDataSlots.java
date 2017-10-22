package com.apps.lore_f.domoticcontroller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DownloadFileFromDataSlots extends Service {

    private String[] resourceKey;
    boolean loopFlag;

    NotificationManager notificationManager;

    // required empty constructor
    public DownloadFileFromDataSlots() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        super.onCreate();

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        resourceKey=intent.getStringArrayExtra("__file_to_download");

        // create the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cloud_download)
                        .setContentTitle("Domotic")
                        .setContentText("is downloading \"" + resourceKey + "\"");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DeviceViewActivity.class), 0);

        notificationBuilder.setContentIntent(contentIntent);

        Notification notification = notificationBuilder.build();

        // start the service in foreground
        startForeground(1, notification);

        new DownloadTask().execute(resourceKey);

        // If we get killed, after returning from here, restart
        return START_STICKY;

    }


    @Override
    public void onDestroy() {

        stopForeground(true);

    }

    private class DownloadTask extends AsyncTask<String, Float, Void>{

        protected Void doInBackground(String... resourceReference){

            // ottiene un riferimento alla posizione di storage sul cloud
            DatabaseReference storageRef = FirebaseDatabase.getInstance().getReference(resourceReference[0]);

            storageRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {

                    String filename = snapshot.child("name").getValue().toString();
                    int slots = Integer.parseInt(snapshot.child("slots").getValue().toString());
                    int bytesInLastSlot = Integer.parseInt(snapshot.child("bytesinlastslot").getValue().toString());

                    // inizializza la directory locale per il download, se la directory non esiste la crea
                    File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Domotic");
                    if (!downloadDirectory.exists()){
                        downloadDirectory.mkdir();
                    }

                    // inizializza il file locale per il download
                    File fileToWrite = new File(downloadDirectory.getPath() + File.separator + filename);

                    // inizializza l'OutputStream
                    try {

                    OutputStream outputStream = new FileOutputStream(fileToWrite);
                    fileToWrite.createNewFile();

                    byte[] bytes = new byte[65536];

                    for (int i = 1; i < slots + 1; i++) {

                        String data = snapshot.child("" + i).getValue().toString();
                        bytes = Base64.decode(data, Base64.DEFAULT);

                        if (i == slots) {

                            outputStream.write(bytes,0,bytesInLastSlot);
                            loopFlag=false;

                        } else {

                            outputStream.write(bytes);

                        }

                        System.out.println("slot " + i + " decoded.");

                    }

                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }




            }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            while (loopFlag) {

                try {

                    Thread.sleep(1000);

                } catch (InterruptedException e) {

                    e.printStackTrace();

                }

            }

            return null;
        }
    }

}
