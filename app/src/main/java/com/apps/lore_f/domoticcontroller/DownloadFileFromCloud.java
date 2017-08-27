package com.apps.lore_f.domoticcontroller;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;


public class DownloadFileFromCloud extends Service {

    private String fileToBeDownloaded;

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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        fileToBeDownloaded=intent.getStringExtra("__file_to_download");

        // create the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cloud_download)
                        .setContentTitle("Domotic")
                        .setContentText("is downloading \"" + fileToBeDownloaded + "\"");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DeviceViewActivity.class), 0);

        notificationBuilder.setContentIntent(contentIntent);

        Notification notification = notificationBuilder.build();

        // start the service in foreground
        startForeground(1, notification);

        // If we get killed, after returning from here, restart
        return START_STICKY;

    }


    @Override
    public void onDestroy() {

        stopForeground(true);

    }

}
