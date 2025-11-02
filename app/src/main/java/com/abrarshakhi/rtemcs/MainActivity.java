package com.abrarshakhi.rtemcs;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lvListOfDeviceMain;
    Button btnAddDeviceMain;

    List<DeviceListAdapter.Device> deviceList = new ArrayList<>();
    DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvListOfDeviceMain = findViewById(R.id.lvListOfDeviceMain);
        btnAddDeviceMain = findViewById(R.id.btnAddDeviceMain);

        deviceListAdapter = new DeviceListAdapter(this, deviceList);
        lvListOfDeviceMain.setAdapter(deviceListAdapter);

        btnAddDeviceMain.setOnClickListener(v -> addDeviceCard());
    }

    private void addDeviceCard() {
        DeviceListAdapter.Device newDevice = new DeviceListAdapter.Device(
                "Device " + (deviceList.size() + 1),
                "Device information",
                "Stopped"
        );

        deviceList.add(newDevice);
        deviceListAdapter.notifyDataSetChanged();
    }
}
