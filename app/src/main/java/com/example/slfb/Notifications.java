package com.example.slfb;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifications {
    public void createNotificationChannel(Context blockedPhoneContext, String phone) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(blockedPhoneContext, "slfbChannel")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Family Bot")
                    .setContentText( phone + " has been blocked")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) blockedPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
            //show notification to user
            //55 means the id for the pushing notification to the user
            NotificationChannel channel = new NotificationChannel("pushNotification","slfbChannel",NotificationManager.IMPORTANCE_HIGH );
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("pushNotification");
            notificationManager.notify(0, builder.build());
        }
    }
}

