package com.app.mederrand.utils.notifications;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.app.mederrand.R;
import com.app.mederrand.activities.MessagesActivity;
import com.app.mederrand.utils.MederrandApp;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (applicationInForeground()) {
            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {

                String type = remoteMessage.getData().get("type");
                String title = remoteMessage.getData().get("title");
                String message = remoteMessage.getData().get("text");
                String username = remoteMessage.getData().get("username");
                String uid = remoteMessage.getData().get("uid");
                String fcmToken = remoteMessage.getData().get("fcm_token");

                // Don't show notification if chat activity is open.
                if (!MederrandApp.isChatActivityOpen()) {
                    sendNotification(type, title, message, username, uid, fcmToken);
                }
            }
        }

    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(String type, String title, String message, String receiver, String receiverUid, String firebaseToken) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }

        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra("otherUserName", receiver);
        intent.putExtra("otherUserId", receiverUid);
        intent.putExtra("otherFcmToken", firebaseToken);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent;
        pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name)).setContentTitle(title).setContentText(message).setSmallIcon(R.mipmap.ic_launcher).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent).setAutoCancel(true);
        notificationManager.notify(1, mBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "BMED App Notification";
        String adminChannelDescription = "Device to device notification";
        NotificationChannel adminChannel;

        adminChannel = new NotificationChannel(getString(R.string.app_name), adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.GREEN);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private boolean applicationInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> services = activityManager.getRunningAppProcesses();
        return services.get(0).processName.equalsIgnoreCase(getPackageName()) && services.get(0).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }
}