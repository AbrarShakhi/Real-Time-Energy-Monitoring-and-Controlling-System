package com.abrarshakhi.rtemcs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.abrarshakhi.rtemcs.api.TuyaOpenApi;
import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.abrarshakhi.rtemcs.model.TuyaCommandResponse;
import com.abrarshakhi.rtemcs.model.TuyaDeviceToken;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceService extends Service {
    public static final String STATE = "STATE";
    private static final String CHANNEL_ID = "device_monitoring_service";
    private Handler handler;
    private Runnable monitorTask;
    private Map<Integer, TuyaDeviceToken> deviceTokens;
    private DeviceInfoDb db;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        final Notification notification = getNotification();

        int id = intent.getIntExtra(DeviceInfo.ID, -1);
        SERVICE_STATE state = SERVICE_STATE.values()[intent.getIntExtra(STATE, SERVICE_STATE.IGNORE.ordinal())];
        DeviceInfo currDevice = db.findById(id);
        if (currDevice != null) {
            startForeground(1, notification);
            pickCommand(state, id, currDevice);
        }
        return START_STICKY;
    }

    private void pickCommand(SERVICE_STATE state, int id, DeviceInfo currDevice) {
        if (state == SERVICE_STATE.START_MONITORING) {
            TuyaDeviceToken.fetch(currDevice, new TuyaDeviceToken.Listener() {
                @Override
                public void onSuccess(TuyaDeviceToken token) {
                    deviceTokens.put(id, token);
                    db.updateDevice(currDevice);
                    startMonitoring(currDevice);
                    if (deviceTokens.isEmpty()) {
                        stopSelf();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
                    if (deviceTokens.isEmpty()) {
                        stopSelf();
                    }
                }
            });
        } else if (state == SERVICE_STATE.STOP_MONITORING) {
            currDevice.setRunning(false);
            db.updateDevice(currDevice);
            deviceTokens.remove(id);
            if (deviceTokens.isEmpty()) {
                stopSelf();
            }
        } else {
            TuyaDeviceToken deviceToken = deviceTokens.get(id);
            if (deviceToken == null || deviceToken.token == null) {
                return;
            }
            if (state == SERVICE_STATE.TURN_ON) {
                toggleDevice(id, deviceToken, true);
            } else if (state == SERVICE_STATE.TURN_OFF) {
                toggleDevice(id, deviceToken, false);
            }
        }
    }

    private void toggleDevice(int id, @NonNull TuyaDeviceToken currDevice, boolean power) {
        TuyaOpenApi api = TuyaOpenApi.getInstance();

        // TODO: REFRESH TOKEN IF NEEDED

        api.togglePower(currDevice.getDevice(), currDevice.token, power, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TuyaCommandResponse> call, @NonNull Response<TuyaCommandResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TuyaCommandResponse cmdResponse = response.body();
                    if (cmdResponse.isSuccess() && cmdResponse.isResult()) {
                        DeviceInfo di = currDevice.getDevice();
                        di.setTurnOn(power);
                        db.updateDevice(di);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuyaCommandResponse> call, @NonNull Throwable t) {
                Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMonitoring(@NonNull DeviceInfo currDevice) {
        if (currDevice.isRunning()) {
            return;
        }
        currDevice.setRunning(true);
        db.updateDevice(currDevice);

        monitorTask = new Runnable() {
            @Override
            public void run() {
                sendBroadcast(intentForBroadcast().putExtra(DeviceInfo.ID, currDevice.getId()));
                if (currDevice.isRunning()) {
                    handler.postDelayed(this, 2000);
                }
            }
        };

        handler.post(monitorTask);
    }

    @NonNull
    private Intent intentForBroadcast() {
        return new Intent(DeviceDetailActivity.ACTION_UPDATE_SWITCH).setPackage(getPackageName());
    }

    @NonNull
    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Device Service Running")
            .setContentText("Monitoring device activity...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build();
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
    public void onCreate() {
        super.onCreate();

        deviceTokens = new HashMap<>();
        db = new DeviceInfoDb(this);

        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceTokens.forEach((id, tt) -> {
            if (tt.getDevice().isRunning()) {
                tt.getDevice().setRunning(false);
                db.updateDevice(tt.getDevice());
            }
        });
        deviceTokens = null;
        db = null;
        if (handler != null && monitorTask != null) {
            handler.removeCallbacks(monitorTask);
        }
    }

    public enum SERVICE_STATE {
        IGNORE, TURN_ON, TURN_OFF, START_MONITORING, STOP_MONITORING
    }

}
