package com.example.android.bluetoothadvertisements;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by minoru on 2018/02/15.
 */

public class BootupReceiver extends BroadcastReceiver {

    private static final String TAG = BootupReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootupReceiver START");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, BootupService.class));
            } else {
                context.startService(new Intent(context, BootupService.class));
            }
        }
    }
}
