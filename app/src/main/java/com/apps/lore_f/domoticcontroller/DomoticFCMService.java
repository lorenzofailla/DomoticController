package com.apps.lore_f.domoticcontroller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by lore_f on 15/10/2017.
 */

public class DomoticFCMService extends FirebaseMessagingService {

    private static String TAG="DomoticFCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, remoteMessage.getData().toString());
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            int drawableResourceId;
            String activityToShow;
            String actionCommand;

            switch (remoteMessage.getNotification().getTitle()){

                case "Motion detected":
                    drawableResourceId = R.drawable.run;
                    activityToShow="";
                    actionCommand="";
                    break;

                default:
                    activityToShow="";
                    actionCommand="";
                    drawableResourceId = R.drawable.home;

            }

            sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(), drawableResourceId, activityToShow, actionCommand);

        }

    }

    private void sendNotification(String title, String message, int drawableRes, String activityToShow, String actionCommand) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getString(R.string.NOTIF_CHID___MOTION_EVENTS))
                        .setSmallIcon(drawableRes)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        NotificationChannel channel = new NotificationChannel(getString(R.string.NOTIF_CHID___MOTION_EVENTS),
                getString(R.string.NOTIF_CHID_READABLE___MOTION_EVENTS),
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }

}
