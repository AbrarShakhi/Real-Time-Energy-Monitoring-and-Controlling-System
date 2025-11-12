package com.abrarshakhi.rtemcs;

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

public class NewDeviceActivity extends AppCompatActivity {
    DeviceInfoDb deviceInfoDb;
    private EditText etDeviceName, etDeviceId, etAccessId, etAccessSecret;
    private Button btnCancel, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceInfoDb = new DeviceInfoDb(this);
        initViews();
        initButtons();
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
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (deviceInfoDb.insertDevice(
                new DeviceInfo.Builder()
                    .id(1)
                    .deviceName(deviceName)
                    .deviceId(deviceId)
                    .accessId(accessId)
                    .accessSecret(accessSecret)
                    .isRunning(false)
                    .build()
            )) {
                Toast.makeText(this, "Device saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save device", Toast.LENGTH_SHORT).show();
            }
        });
    }
}