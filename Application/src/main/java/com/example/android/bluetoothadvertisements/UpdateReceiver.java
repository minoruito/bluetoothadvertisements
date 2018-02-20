package com.example.android.bluetoothadvertisements;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by minoru on 2018/02/19.
 */

public class UpdateReceiver extends BroadcastReceiver {

    public static Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        if(handler !=null){
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("action", intent.getAction());
            if (intent.getAction().equals("UPDATE_ACTION")) {
                String deviceName = bundle.getString("device_name");
                String deviceAddress = bundle.getString("device_address");
                String lastSeen = bundle.getString("last_seen");
                data.putString("device_name", deviceName);
                data.putString("device_address", deviceAddress);
                data.putString("last_seen", lastSeen);
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    /**
     * メイン画面の表示を更新
     */
    public void registerHandler(Handler locationUpdateHandler) {
        handler = locationUpdateHandler;
    }

}
