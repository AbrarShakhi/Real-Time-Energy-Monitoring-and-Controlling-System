package com.abrarshakhi.rtemcs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;

public class DeviceInfoActivity extends AppCompatActivity {
    DeviceInfoDb db;
    private EditText etDeviceName, etDeviceId, etAccessId, etAccessSecret;
    private Button btnCancel, btnSave;
    boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DeviceInfoDb(this);
        Intent it = getIntent();
        isNew = it == null || it.getIntExtra(DeviceInfo.ID, -1) == -1;
        initViews();
        initButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isNew) {
            return;
        }

        DeviceInfo device = db.findById(getIntent().getIntExtra(DeviceInfo.ID, -1));
        if (device == null) {
            Toast.makeText(this, "Unable to find device info in the database.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        etDeviceName.setText(device.getDeviceName());
        etDeviceId.setText(device.getDeviceId());
        etAccessId.setText(device.getAccessId());
        etAccessSecret.setText(device.getAccessSecret());
    }

    private void initViews() {
        etDeviceName = findViewById(R.id.etDeviceNameAddDevice);
        etDeviceId = findViewById(R.id.etDeviceIdAddDevice);
        etAccessId = findViewById(R.id.etAccessIdAddDevice);
        etAccessSecret = findViewById(R.id.etAccessSecretAddDevice);
        btnCancel = findViewById(R.id.btnCancelAddDevice);
        btnSave = findViewById(R.id.btnSaveAddDevice);


    }

    private void initButtons() {
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            String deviceName = etDeviceName.getText().toString().strip();
            String deviceId = etDeviceId.getText().toString().strip();
            String accessId = etAccessId.getText().toString().strip();
            String accessSecret = etAccessSecret.getText().toString().strip();

            if (deviceName.isEmpty() || deviceId.isEmpty() || accessId.isEmpty() || accessSecret.isEmpty()) {
                Toast.makeText(DeviceInfoActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isNew) {
                insertDeviceToDb(deviceName, deviceId, accessId, accessSecret);
            } else {
                editDeviceInDb(getIntent().getIntExtra(DeviceInfo.ID, -1), deviceName, deviceId, accessId, accessSecret);
            }
        });
    }

    private void editDeviceInDb(int id, String deviceName, String deviceId, String accessId, String accessSecret) {
        DeviceInfo device = db.findById(id);
        if (device == null) {
            Toast.makeText(DeviceInfoActivity.this, "Could not found the device in the database.", Toast.LENGTH_SHORT).show();
            return;
        }

        device.setDeviceName(deviceName);
        device.setDeviceId(deviceId);
        device.setAccessId(accessId);
        device.setAccessSecret(accessSecret);

        if (db.updateDevice(device) != 0) {
            Toast.makeText(this, "Device saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save device", Toast.LENGTH_SHORT).show();
        }
    }


    private void insertDeviceToDb(String deviceName, String deviceId, String accessId, String accessSecret) {
        if (db.insertDevice(
            new DeviceInfo.Builder()
                .id(1)
                .deviceName(deviceName)
                .deviceId(deviceId)
                .accessId(accessId)
                .accessSecret(accessSecret)
                .build()
        )) {
            Toast.makeText(this, "Device saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save device", Toast.LENGTH_SHORT).show();
        }
    }
}