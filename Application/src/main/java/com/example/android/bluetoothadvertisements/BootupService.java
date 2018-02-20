package com.example.android.bluetoothadvertisements;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by minoru on 2018/02/15.
 */

public class BootupService extends Service {

    private static final String TAG = BootupService.class.getSimpleName();
    private NotificationManager mNM;

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");
        NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext(), TAG);
        mNM = notificationHelper.getManager();

        showNotification();
        Context c = getApplicationContext();
        Log.d(TAG, "start ScannerService");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            c.startForegroundService(new Intent(c, ScannerService.class));
        } else {
            c.startService(new Intent(c, ScannerService.class));
        }
        goForeground();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("Debug TEST", "onDestroy");
        super.onDestroy();
        mNM.cancel(1);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    private void showNotification() {
        Log.d(TAG, "showNotification");
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class), 0);

        Notification notif= new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Bootup Service Start")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(contentIntent)
                .build();
        //常駐させる
        notif.flags = Notification.FLAG_ONGOING_EVENT;
        mNM.notify(Constants.FOREGROUND_NOTIFICATION_ID, notif);
    }

    private void goForeground() {
        Log.d(TAG, "Service: go Foreground");
        NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext(), Constants.MY_CHANNEL_ID);
        Notification.Builder builder = notificationHelper.getNotification("foreground1");
        notificationHelper.notify(Constants.FOREGROUND_NOTIFICATION_ID, builder);
        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, builder.build());
    }
}
