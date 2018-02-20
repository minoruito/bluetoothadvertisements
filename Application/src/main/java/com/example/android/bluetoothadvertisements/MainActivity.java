/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothadvertisements;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;

    private ScanResultAdapter mAdapter;
    private boolean created = false;
    private boolean setuped_receive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new ScanResultAdapter(getApplicationContext(), (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE));


        ListView listView = (ListView) findViewById(R.id.listitem_scanerresult);
        listView.setAdapter(mAdapter);

        setTitle(R.string.activity_main_title);

        //位置情報権限の設定
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        boolean runningService = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ScannerService.class.getName().equals(serviceInfo.service.getClassName())) {
                runningService = true;
                break;
            }
        }
        if (!runningService) {
            Context c = getApplicationContext();
            Log.d(TAG, "start ScannerService");
            c.startService(new Intent(c, ScannerService.class));
        }
        setupRecevier();
        created = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecevier();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setdownReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setdownReceiver();
    }

    private void setupRecevier() {
        if (!setuped_receive) {
            upReceiver = new UpdateReceiver();
            intentFilter = new IntentFilter();
            intentFilter.addAction("UPDATE_ACTION");
            intentFilter.addAction("CLEAR_ACTION");
            registerReceiver(upReceiver, intentFilter);

            upReceiver.registerHandler(updateHandler);

            setuped_receive = true;
        }

    }

    private void setdownReceiver() {
        if (setuped_receive) {
            unregisterReceiver(upReceiver);
            setuped_receive = false;
        }
    }

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();

            if (bundle.getString("action").equals("UPDATE_ACTION")) {
                String deviceName = bundle.getString("device_name");
                String deviceAddress = bundle.getString("device_address");
                String lastSeen = bundle.getString("last_seen");

                Log.d(TAG, "device_name:" + deviceName + " device_address:" + deviceAddress + " last_seen:" + lastSeen);

                mAdapter.add(deviceName, deviceAddress, lastSeen);
            } else {
                mAdapter.clear();
            }
            if (created) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };
}