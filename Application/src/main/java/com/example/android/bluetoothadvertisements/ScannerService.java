package com.example.android.bluetoothadvertisements;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by minoru on 2018/02/15.
 */

public class ScannerService extends Service {

    private static final String TAG = ScannerService.class.getSimpleName();

    public static boolean running = false;

    /**
     * Stops scanning after 5 seconds.
     */
    private static final long SCAN_PERIOD = 5000;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    private Handler mHandler;

    /**
     * Must be called after object creation by MainActivity.
     *
     * @param btAdapter the local BluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        running = true;
        mHandler = new Handler();

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {
                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        setBluetoothAdapter(mBluetoothAdapter);
                    }
                } else {
                }
            } else {
            }
        }

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                sendClearBroadCast();
                startScanning();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(r);

        goForeground();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        running = false;
        stopScanning();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    private void goForeground() {
        Log.d(TAG, "Service: go Foreground");
        NotificationHelper notificationHelper = new NotificationHelper(this.getApplicationContext(), Constants.MY_CHANNEL_ID);
        Notification.Builder builder = notificationHelper.getNotification("foreground1");
        notificationHelper.notify(Constants.FOREGROUND_NOTIFICATION_ID, builder);
        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, builder.build());
    }

    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
    public void startScanning() {
        if (mScanCallback == null) {
            Log.d(TAG, "Starting Scanning");

            // Will stop the scanning after a set time.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanning();
                }
            }, SCAN_PERIOD);

            // Kick off a new scan.
            mScanCallback = new ScannerService.SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);

            String toastText = getString(R.string.scan_start_toast) + " "
                    + TimeUnit.SECONDS.convert(SCAN_PERIOD, TimeUnit.MILLISECONDS) + " "
                    + getString(R.string.seconds);

            NotificationHelper notificationHelper = new NotificationHelper(getApplication(), Constants.MY_CHANNEL_ID);
            Notification.Builder builder = notificationHelper.getNotification(getApplicationContext(), toastText);
            Log.d(TAG, toastText);
            notificationHelper.notify(Constants.FOREGROUND_NOTIFICATION_ID, builder);
        } else {
            NotificationHelper notificationHelper = new NotificationHelper(getApplication(), Constants.MY_CHANNEL_ID);
            Notification.Builder builder = notificationHelper.getNotification(getApplicationContext(), getString(R.string.already_scanning));
            Log.d(TAG, getString(R.string.already_scanning));
            notificationHelper.notify(Constants.FOREGROUND_NOTIFICATION_ID, builder);
        }
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        Log.d(TAG, "Stopping Scanning");

        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;

    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        //すべてのBLE端末を取る。フィルターしない。
        //builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext(), Constants.MY_CHANNEL_ID);
            Notification.Builder builder = notificationHelper.getNotification(getApplicationContext(), result.getDevice().getAddress());
            Log.d(TAG, "device:" + result.getDevice().getAddress());
            notificationHelper.notify(Constants.FOREGROUND_NOTIFICATION_ID, builder);

            sendBroadCast(result.getDevice().getName(), result.getDevice().getAddress(), getTimeSinceString(getApplicationContext(), result.getTimestampNanos()));
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            NotificationHelper notificationHelper = new NotificationHelper(getApplication(), Constants.MY_CHANNEL_ID);
            Notification.Builder builder = notificationHelper.getNotification(getApplicationContext(), "Scan failed with error: " + errorCode);
            Log.d(TAG, "Scan failed with error: " + errorCode);
            notificationHelper.notify(Constants.FOREGROUND_NOTIFICATION_ID, builder);
        }
    }
    protected void sendBroadCast(String deviceName, String deviceAddress, String lastSeend) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("device_name", deviceName);
        broadcastIntent.putExtra("device_address", deviceAddress);
        broadcastIntent.putExtra("last_seen", lastSeend);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    protected void sendClearBroadCast() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("CLEAR_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    private String getTimeSinceString(Context context, long timeNanoseconds) {
        String lastSeenText = context.getResources().getString(R.string.last_seen) + " ";

        long timeSince = SystemClock.elapsedRealtimeNanos() - timeNanoseconds;
        long secondsSince = TimeUnit.SECONDS.convert(timeSince, TimeUnit.NANOSECONDS);

        if (secondsSince < 5) {
            lastSeenText += context.getResources().getString(R.string.just_now);
        } else if (secondsSince < 60) {
            lastSeenText += secondsSince + " " + context.getResources()
                    .getString(R.string.seconds_ago);
        } else {
            long minutesSince = TimeUnit.MINUTES.convert(secondsSince, TimeUnit.SECONDS);
            if (minutesSince < 60) {
                if (minutesSince == 1) {
                    lastSeenText += minutesSince + " " + context.getResources()
                            .getString(R.string.minute_ago);
                } else {
                    lastSeenText += minutesSince + " " + context.getResources()
                            .getString(R.string.minutes_ago);
                }
            } else {
                long hoursSince = TimeUnit.HOURS.convert(minutesSince, TimeUnit.MINUTES);
                if (hoursSince == 1) {
                    lastSeenText += hoursSince + " " + context.getResources()
                            .getString(R.string.hour_ago);
                } else {
                    lastSeenText += hoursSince + " " + context.getResources()
                            .getString(R.string.hours_ago);
                }
            }
        }

        return lastSeenText;
    }
}
