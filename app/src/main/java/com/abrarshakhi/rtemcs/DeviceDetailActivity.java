package com.abrarshakhi.rtemcs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.google.android.material.materialswitch.MaterialSwitch;

public class DeviceDetailActivity extends AppCompatActivity {
    public static final String ACTION_UPDATE_SWITCH = "com.abrarshakhi.rtemcs.UPDATE_SWITCH";

    DeviceInfoDb db;
    private Button btnBack, btnEdit;
    private DeviceInfo device;
    private MaterialSwitch swMonitorDevice, swToggleDevice;

    private final BroadcastReceiver switchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !DeviceDetailActivity.ACTION_UPDATE_SWITCH.equals(intent.getAction())) {
                return;
            }
            int id = intent.getIntExtra(DeviceInfo.ID, -1);
            DeviceInfo d = db.findById(id);
            if (d == null)
                return;
            boolean isRunning = d.isRunning();
            boolean isTurnOn = d.isTurnOn();
            if (swMonitorDevice.isEnabled() != isRunning) {
                swMonitorDevice.setChecked(isRunning);
            }
            if (swToggleDevice.isEnabled() != isTurnOn) {
                swToggleDevice.setChecked(isTurnOn);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DeviceInfoDb(this);
        initViews();
        initButtons();
        initSwitches();
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkPermission();
        if (findDevice()) return;
        registerBroadcast();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_SWITCH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(switchReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(switchReceiver, filter);
        }
    }

    private boolean findDevice() {
        Intent it = getIntent();
        if (it == null || it.getIntExtra(DeviceInfo.ID, -1) == -1) {
            Toast.makeText(this, "Activity started without specifying ID", Toast.LENGTH_LONG).show();
            finish();
            return true;
        }
        device = db.findById(it.getIntExtra(DeviceInfo.ID, -1));
        if (device == null) {
            Toast.makeText(this, "Unable to find device info in the database.", Toast.LENGTH_LONG).show();
            finish();
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(switchReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            device = db.findById(device.getId());
            swMonitorDevice.setChecked(device.isRunning());
            swToggleDevice.setChecked(device.isTurnOn());
        }
        btnEdit.setEnabled(!swMonitorDevice.isChecked());
        swToggleDevice.setEnabled(swMonitorDevice.isChecked());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackDetail);
        btnEdit = findViewById(R.id.btnEditDetail);
        swMonitorDevice = findViewById(R.id.swMonitorDeviceDetail);
        swToggleDevice = findViewById(R.id.swToggleDeviceDetail);
    }

    private void initButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v ->
            startActivity(new Intent(DeviceDetailActivity.this, DeviceInfoActivity.class).putExtra(DeviceInfo.ID, device.getId()))
        );
    }

    private void initSwitches() {
        swMonitorDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent servIt = new Intent(DeviceDetailActivity.this, DeviceService.class);
            if (device == null) return;
            servIt.putExtra(DeviceInfo.ID, device.getId());
            btnEdit.setEnabled(!isChecked);
            swToggleDevice.setEnabled(isChecked);
            servIt.putExtra(DeviceService.STATE,
                (isChecked)
                    ? DeviceService.SERVICE_STATE.START_MONITORING.ordinal()
                    : DeviceService.SERVICE_STATE.STOP_MONITORING.ordinal());
            startForegroundService(servIt);
        });
        swToggleDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent servIt = new Intent(DeviceDetailActivity.this, DeviceService.class);
            if (device == null) return;
            servIt.putExtra(DeviceInfo.ID, device.getId());
            servIt.putExtra(DeviceService.STATE,
                (isChecked)
                    ? DeviceService.SERVICE_STATE.TURN_ON.ordinal()
                    : DeviceService.SERVICE_STATE.TURN_OFF.ordinal());
            startForegroundService(servIt);
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow notification permission first.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}