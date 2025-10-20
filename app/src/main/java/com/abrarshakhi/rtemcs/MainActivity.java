package com.abrarshakhi.rtemcs;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.abrarshakhi.rtemcs.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deviceList = new ArrayList<>();
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);

        binding.lvListOfDeviceMain.setAdapter(deviceAdapter);

        binding.btnAddDeviceMain.setOnClickListener(v -> {
            String newDevice = "Device " + (deviceList.size() + 1);
            deviceList.add(newDevice);

            deviceAdapter.notifyDataSetChanged();

            Toast.makeText(MainActivity.this, newDevice + " added!", Toast.LENGTH_SHORT).show();
        });
    }
}
