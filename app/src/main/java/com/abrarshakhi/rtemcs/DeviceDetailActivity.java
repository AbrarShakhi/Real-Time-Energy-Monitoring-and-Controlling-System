package com.abrarshakhi.rtemcs;

import android.Manifest;
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
    DeviceInfoDb db;
    private Button btnBack, btnEdit;
    private DeviceInfo device;
    private MaterialSwitch swMonitorDevice, swToggleDevice;

    public static final String ACTION_UPDATE_SWITCH = "com.abrarshakhi.rtemcs.UPDATE_SWITCH";
    public static final String EXTRA_COUNT = "COUNT";
    private final BroadcastReceiver switchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && DeviceDetailActivity.ACTION_UPDATE_SWITCH.equals(intent.getAction())) {
                int count = intent.getIntExtra(DeviceDetailActivity.EXTRA_COUNT, 0);
                System.out.println(count);
                swToggleDevice.setChecked(count % 2 == 1);
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

        Intent it = getIntent();
        if (it == null || it.getIntExtra("ID", -1) == -1) {
            Toast.makeText(this, "Activity started without specifying ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        device = db.findById(it.getIntExtra("ID", -1));
        if (device == null) {
            Toast.makeText(this, "Unable to find device info in the database.", Toast.LENGTH_LONG).show();
            finish();
        }

        IntentFilter filter = new IntentFilter(ACTION_UPDATE_SWITCH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(switchReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(switchReceiver, filter);
        }

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
            swMonitorDevice.setChecked(device.isRunning());
        }
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
            startActivity(new Intent(DeviceDetailActivity.this, DeviceInfoActivity.class).putExtra("ID", device.getId()))
        );
    }

    private void initSwitches() {
        swMonitorDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent servIt = new Intent(DeviceDetailActivity.this, DeviceService.class);
            if (isChecked && device != null) {
                btnEdit.setEnabled(false);
                servIt.putExtra("ID", device.getId());
                startForegroundService(servIt);
            } else {
                stopService(servIt);
                btnEdit.setEnabled(true);
            }
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