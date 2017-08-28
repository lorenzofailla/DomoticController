package com.apps.lore_f.domoticcontroller;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;


public class DownloadFileFromCloud extends Service {

    private String[] fileNames;

    NotificationManager notificationManager;

    // required empty constructor
    public DownloadFileFromCloud() {

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

        fileNames=intent.getStringArrayExtra("__file_to_download");

        // create the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cloud_download)
                        .setContentTitle("Domotic")
                        .setContentText("is downloading \"" + fileNames[0] + "\"");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DeviceViewActivity.class), 0);

        notificationBuilder.setContentIntent(contentIntent);

        Notification notification = notificationBuilder.build();

        // start the service in foreground
        startForeground(1, notification);

        new DownloadTask().execute(fileNames);

        // If we get killed, after returning from here, restart
        return START_STICKY;

    }


    @Override
    public void onDestroy() {

        stopForeground(true);

    }

    private class DownloadTask extends AsyncTask<String, Float, Void>{

        protected Void doInBackground(String... fileName){

            // ottiene un riferimento alla posizione di storage sul cloud
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://domotic-28a5e.appspot.com/Users/lorenzofailla/uploads/"+fileName[0]);

            // inizializza la directory locale per il download, se la directory non esiste la crea
             File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Domotic");
            if (!downloadDirectory.exists()){
                downloadDirectory.mkdir();
            }

            // inizializza il file locale per il download
            File localFile = new File(downloadDirectory.getPath() + File.separator + storageRef.getName());

            storageRef.getFile(localFile)

                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    // *** scaricamento completato

                    // chiude il servizio corrente
                    stopForeground(true);

                }

            })

            .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // *** scaricamento in corso

                    // aggiorna la notifica


                }
            });

            return null;
        }
    }

}
