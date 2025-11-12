package com.abrarshakhi.rtemcs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    ListView lvListOfDevice;
    FloatingActionButton btnAddDevice;

    private List<DeviceInfo> deviceInfoList;
    private DeviceListAdapter deviceListAdapter;
    private DeviceInfoDb db;

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

    @Override
    protected void onStart() {
        super.onStart();
        deviceInfoList.clear();
        List<DeviceInfo> devices = db.getAllDevices();
        for (int i = 0; i < devices.size(); i++) {
            deviceInfoList.add(devices.get(i));
            deviceListAdapter.notifyDataSetChanged();
        }
    }

}