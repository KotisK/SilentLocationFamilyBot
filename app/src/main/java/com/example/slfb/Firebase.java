package com.example.slfb;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Firebase extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        getSharedPreferences("firebaseToken", MODE_PRIVATE).edit().putString("fb", s).apply();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getNotification().getTitle();
        if(title.substring(0, title.indexOf(" ")).equals(getApplicationContext().getSharedPreferences("user", MODE_PRIVATE).getString("user","null")))
            return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "slfbChannel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //show notification to user
        //55 means the id for the pushing notification to the user
        NotificationChannel channel = new NotificationChannel("pushNotification","slfbChannel",NotificationManager.IMPORTANCE_HIGH );
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId("pushNotification");
        notificationManager.notify(0, builder.build());

    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("fb", "empty");
    }

    public static void senPushdNotification(final String topic , String location) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject notificationJson = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    notificationJson.put("text", "test");
                    notificationJson.put("title", topic+" went to "+location);
                    notificationJson.put("priority", "high");
                    dataJson.put("customId", "02");
                    dataJson.put("badge", 1);
                    dataJson.put("alert", "Alert");
                    json.put("notification", notificationJson);
                    json.put("data", dataJson);
                    json.put("to", "/topics/" + topic);
                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=AAAAaKf_Ojg:APA91bEzZ5CFCyxTh0hQl4NxuW6m9NUjs9oAJ1dge0pxeJJ2jKJQYRf2sCWCTCWKZirHMb5Sgak542D4XGzzn7cM33MfoP4OFQ4ZR8DNRzMxwuZ9A-F7LcFKS3Hj9IymCb03FnEpn9B8")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.i("TAG", finalResponse);
                } catch (Exception e) {

                    Log.i("TAG", e.getMessage());
                }
                return null;
            }
        }.execute();
    }

}