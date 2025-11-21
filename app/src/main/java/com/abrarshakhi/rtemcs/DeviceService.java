package com.abrarshakhi.rtemcs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.abrarshakhi.rtemcs.api.TuyaOpenApi;
import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.data.PowerConsumptionHistDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.abrarshakhi.rtemcs.model.StatRecord;
import com.abrarshakhi.rtemcs.model.TuyaCommandResponse;
import com.abrarshakhi.rtemcs.model.TuyaDeviceToken;
import com.abrarshakhi.rtemcs.model.TuyaShadowPropertiesResponse;
import com.abrarshakhi.rtemcs.model.TuyaTokenInfo;
import com.abrarshakhi.rtemcs.model.TuyaTokenResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceService extends Service {
    public static final String STATE = "STATE";
    private static final String CHANNEL_ID = "device_monitoring_service";
    private Handler handler;
    private Map<Integer, Runnable> monitorTasks;
    private Map<Integer, TuyaDeviceToken> deviceTokens;
    private DeviceInfoDb db;
    private PowerConsumptionHistDb statsDb;

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
                    startMonitoring(token);
                    ifEmptyThenSelfKill();
                }

                @Override
                public void onError(Throwable t) {
                    Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
                    ifEmptyThenSelfKill();
                }
            });
        } else if (state == SERVICE_STATE.STOP_MONITORING) {
            stopMonitoring(id, currDevice);
        } else {
            if (deviceTokens == null || deviceTokens.isEmpty() || monitorTasks.isEmpty()) {
                return;
            }
            TuyaDeviceToken deviceToken = deviceTokens.get(id);
            if (deviceToken == null || deviceToken.getToken() == null) {
                return;
            }
            if (state == SERVICE_STATE.TURN_ON) {
                toggleDevice(deviceToken, true);
            } else if (state == SERVICE_STATE.TURN_OFF) {
                toggleDevice(deviceToken, false);
            }
        }
    }

    private void stopMonitoring(int id, DeviceInfo currDevice) {
        Runnable task = monitorTasks.remove(id);
        if (task != null) {
            handler.removeCallbacks(task);
        }

        currDevice.setRunning(false);
        db.updateDevice(currDevice);
        deviceTokens.remove(id);

        ifEmptyThenSelfKill();
    }

    private void ifEmptyThenSelfKill() {
        if (deviceTokens.isEmpty() || monitorTasks.isEmpty()) {
            stopSelf();
        }
    }

    private void toggleDevice(@NonNull TuyaDeviceToken currDevice, boolean power) {
        TuyaOpenApi api = TuyaOpenApi.getInstance();

        boolean isExpectedToChange = api.refreshTokenIfNeeded(currDevice.getDevice(), currDevice.getToken(), new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TuyaTokenResponse> call, @NonNull Response<TuyaTokenResponse> response) {
                TuyaTokenResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getResult() != null) {
                    body.updateExpiredTime();
                    TuyaTokenInfo tokenInfo = body.getResult();
                    currDevice.copyToken(tokenInfo);
                    toggleDevice(currDevice, power);
                } else {
                    broadcastId(currDevice.getDevice().getId());
                    Toast.makeText(DeviceService.this, "request is not successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuyaTokenResponse> call, @NonNull Throwable t) {
                broadcastId(currDevice.getDevice().getId());
                Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
                stopMonitoring(currDevice.getDevice().getId(), currDevice.getDevice());
            }
        });

        if (isExpectedToChange) {
            return;
        }
        api.togglePower(currDevice.getDevice(), currDevice.getToken(), power, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TuyaCommandResponse> call, @NonNull Response<TuyaCommandResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TuyaCommandResponse cmdResponse = response.body();
                    if (cmdResponse.isSuccess() && cmdResponse.isResult()) {
                        DeviceInfo di = currDevice.getDevice();
                        di.setTurnOn(power);
                        db.updateDevice(di);
                    } else {
                        broadcastId(currDevice.getDevice().getId());
                        Toast.makeText(DeviceService.this, "request is not successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuyaCommandResponse> call, @NonNull Throwable t) {
                broadcastId(currDevice.getDevice().getId());
                stopMonitoring(currDevice.getDevice().getId(), currDevice.getDevice());
                Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMonitoring(@NonNull TuyaDeviceToken currDeviceToken) {
        TuyaOpenApi api = TuyaOpenApi.getInstance();
        int id = currDeviceToken.getDevice().getId();
        if (monitorTasks.containsKey(id)) {
            return;
        }
        currDeviceToken.getDevice().setRunning(true);
        db.updateDevice(currDeviceToken.getDevice());

        Runnable task = new Runnable() {
            @Override
            public void run() {
                Runnable r = this;
                boolean isExpectedToChange = api.refreshTokenIfNeeded(currDeviceToken.getDevice(), currDeviceToken.getToken(), new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<TuyaTokenResponse> call, @NonNull Response<TuyaTokenResponse> response) {
                        TuyaTokenResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess() && body.getResult() != null) {
                            body.updateExpiredTime();
                            TuyaTokenInfo tokenInfo = body.getResult();
                            currDeviceToken.copyToken(tokenInfo);
                            fetchInformation(r, currDeviceToken, id);
                        } else {
                            broadcastId(currDeviceToken.getDevice().getId());
                            Toast.makeText(DeviceService.this, "request is not successfully", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TuyaTokenResponse> call, @NonNull Throwable t) {
                        broadcastId(currDeviceToken.getDevice().getId());
                        Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
                        stopMonitoring(currDeviceToken.getDevice().getId(), currDeviceToken.getDevice());
                    }
                });

                if (!isExpectedToChange) {
                    fetchInformation(r, currDeviceToken, id);
                }
            }
        };
        monitorTasks.put(id, task);
        handler.post(task);
    }

    private void fetchInformation(Runnable r, @NonNull TuyaDeviceToken currDeviceToken, int id) {
        pullShadowPropertiesAndWrite(currDeviceToken);
        DeviceInfo updated = db.findById(id);
        if (updated != null && updated.isRunning()) {
            handler.postDelayed(r, 10 * 1000);
        }
        broadcastId(id);
    }

    private void pullShadowPropertiesAndWrite(@NonNull TuyaDeviceToken currDeviceToken) {
        TuyaOpenApi api = TuyaOpenApi.getInstance();
        api.shadowProperties(currDeviceToken.getDevice(), currDeviceToken.getToken(), new Callback<>() {
            private static final String SWITCH = "switch";
            private static final String VOLTAGE = "output_voltage";
            private static final String CURRENT = "output_current";
            private static final String POWER = "output_power";

            private boolean toBoolean(Object value) {
                if (value instanceof Boolean) return (Boolean) value;
                if (value instanceof Number) return ((Number) value).intValue() != 0;
                return Boolean.parseBoolean(value.toString());
            }

            private double toDouble(Object value) {
                if (value instanceof Number) return ((Number) value).doubleValue();
                try {
                    return Double.parseDouble(value.toString());
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public void onResponse(@NonNull Call<TuyaShadowPropertiesResponse> call, @NonNull Response<TuyaShadowPropertiesResponse> response) {
                TuyaShadowPropertiesResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getResult() != null && body.getResult().getProperties() != null) {
                    DeviceInfo device = currDeviceToken.getDevice();
                    boolean switchState = device.isTurnOn();
                    double power = 0, voltage = 0, current = 0;
                    try {
                        for (var prop : body.getResult().getProperties()) {
                            switch (prop.getCode()) {
                                case SWITCH:
                                    switchState = toBoolean(prop.getValue());
                                    break;
                                case VOLTAGE:
                                    voltage = toDouble(prop.getValue());
                                    break;
                                case CURRENT:
                                    current = toDouble(prop.getValue());
                                    break;
                                case POWER:
                                    power = toDouble(prop.getValue());
                                    break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    device.setTurnOn(switchState);
                    db.updateDevice(device);
                    saveToDb(device.getId(), body.getTimestamp(), power / 1000);
                } else {
                    stopMonitoring(currDeviceToken.getDevice().getId(), currDeviceToken.getDevice());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuyaShadowPropertiesResponse> call, @NonNull Throwable t) {
                broadcastId(currDeviceToken.getDevice().getId());
                stopMonitoring(currDeviceToken.getDevice().getId(), currDeviceToken.getDevice());
                Toast.makeText(DeviceService.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToDb(int id, long timestamp, double power) {
        statsDb.insertRecord(new StatRecord(id, timestamp, power));
    }

    private void writeToExternalAppStorage(int id, long timestamp, double powerKiloWatt) {
        String fileName = "STAT" + id + ".csv";
        File file = new File(getExternalFilesDir(null), fileName);

        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            if (file.length() == 0) {
                out.println("Timestamp,PowerKiloWatt");
            }

            out.printf("%d,%f%n", timestamp, powerKiloWatt);

        } catch (Exception e) {
            Log.e("CSV_WRITE", "Error writing CSV: " + e.getMessage());
        }

        sendBroadcast(
            intentForBroadcast()
                .putExtra(DeviceInfo.ID, id)
                .putExtra(DeviceDetailActivity.POWER, powerKiloWatt)
                .putExtra(DeviceDetailActivity.HAS_STAT, true)
        );
    }


    private void broadcastId(int id) {
        sendBroadcast(
            intentForBroadcast()
                .putExtra(DeviceInfo.ID, id)
        );
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
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Device Service Channel", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        monitorTasks = new HashMap<>();
        deviceTokens = new HashMap<>();
        db = new DeviceInfoDb(this);
        statsDb = new PowerConsumptionHistDb(this);

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
        if (handler != null) {
            for (Runnable task : monitorTasks.values()) {
                handler.removeCallbacks(task);
            }
        }
        monitorTasks.clear();
        deviceTokens.clear();
    }

    public enum SERVICE_STATE {
        IGNORE, TURN_ON, TURN_OFF, START_MONITORING, STOP_MONITORING
    }

}
