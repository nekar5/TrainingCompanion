package com.example.trainingcompanion.extra;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.trainingcompanion.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "1";

    public static void createNotificationChannel(Context context) {
        CharSequence name = "Notification Channel";
        String description = "Channel for workout notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void sendNotification(Activity activity, String title, String text) {
        Log.w("NotificationHelper", "Preparing to send notification: " + title);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon_grey)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            createNotificationChannel(activity);
            notificationManager.notify(1, builder.build());
            Log.d("NotificationHelper", "Notification sent: " + title);
        } else {
            Log.e("NotificationHelper", "Notifications permission not given");
        }
    }
}

