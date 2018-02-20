package com.example.android.bluetoothadvertisements;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by minoru on 2018/02/08.
 */
public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_GENERAL_ID = "general";
    private NotificationManager manager;

    private String channelId = "general";

    public NotificationHelper(Context base, String channelId) {
        super(base);

        this.channelId = channelId;

        if (isOreoOrLater()) {
            NotificationChannel channel = new NotificationChannel(channelId, "General Notifications", NotificationManager.IMPORTANCE_LOW);
            getManager().createNotificationChannel(channel);
        }
    }

    public Notification.Builder getNotification(String contentText) {
        return getNotification(this, contentText);
    }
    public Notification.Builder getNotification(Context context, String contentText) {
        Notification.Builder builder = isOreoOrLater()
                ? new Notification.Builder(context, channelId)
                : new Notification.Builder(context);

        return builder.setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher);
    }

    public void notify(int id, Notification.Builder builder) {
        getManager().notify(id, builder.build());
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    private boolean isOreoOrLater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O;
    }
}
