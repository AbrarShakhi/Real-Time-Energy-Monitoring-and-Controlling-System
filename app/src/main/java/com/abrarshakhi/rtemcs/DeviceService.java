package com.abrarshakhi.rtemcs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

public class DeviceService extends Service {
    private static final String CHANNEL_ID = "device_monitoring_service";
    private Handler handler;
    private Runnable monitorTask;
    private boolean running = false;
    private int count;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Device Service Running")
            .setContentText("Monitoring device activity...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build();

        startForeground(1, notification);

        startMonitoring();
        return START_STICKY;
    }

    private void startMonitoring() {
        if (running) return;
        running = true;

        monitorTask = new Runnable() {
            @Override
            public void run() {
                count++;
                System.out.println("ii = " + count);
                Intent intent = new Intent(DeviceDetailActivity.ACTION_UPDATE_SWITCH);
                intent.putExtra(DeviceDetailActivity.EXTRA_COUNT, count);
                intent.setPackage(getPackageName());
                sendBroadcast(intent);
                sendBroadcast(intent);

                if (running) {
                    handler.postDelayed(this, 2000);
                }
            }
        };

        handler.post(monitorTask);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
        count = 0;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
            CHANNEL_ID,
            "Device Service Channel",
            NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        if (handler != null && monitorTask != null) {
            handler.removeCallbacks(monitorTask);
        }
        count = 0;
    }
}
