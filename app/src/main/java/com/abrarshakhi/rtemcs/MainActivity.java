package com.abrarshakhi.rtemcs;

import static com.google.gson.internal.GsonTypes.arrayOf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.abrarshakhi.rtemcs.utils.DeviceListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    ListView lvListOfDevice;
    FloatingActionButton btnAddDevice;
    private List<DeviceInfo> deviceInfoList;
    private DeviceListAdapter deviceListAdapter;
    private DeviceInfoDb db;

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnAddDevice = findViewById(R.id.btnAddDeviceMain);
        lvListOfDevice = findViewById(R.id.lvListOfDeviceMain);

        db = new DeviceInfoDb(this);
        deviceInfoList = new ArrayList<>();
        deviceListAdapter = new DeviceListAdapter(this, deviceInfoList);
        lvListOfDevice.setAdapter(deviceListAdapter);

        btnAddDevice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DeviceInfoActivity.class)));
    }

    /**
     * If the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        deviceInfoList.clear();
        List<DeviceInfo> devices = db.getAllDevices();
        for (int i = 0; i < devices.size(); i++) {
            deviceInfoList.add(devices.get(i));
            deviceListAdapter.notifyDataSetChanged();
        }

        requestNotificationPermission();
    }

    /**
     * For foreground service first ask for sending network permission.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Foreground service may not show notifications.", Toast.LENGTH_LONG).show();
            }
        }
    }
}