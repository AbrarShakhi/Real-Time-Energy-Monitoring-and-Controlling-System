package com.abrarshakhi.rtemcs;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.api.ITuyaActivatorListener;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.common.TuyaUtil;


public class MainActivity extends AppCompatActivity {

    ListView lvListOfDeviceMain;
    Button btnAddDeviceMain;

    List<DeviceListAdapter.Device> deviceList = new ArrayList<>();
    DeviceListAdapter deviceListAdapter;

    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TuyaHomeSdk.setDebugMode(true);

        btnAddDeviceMain = findViewById(R.id.btnAddDeviceMain);

        String AccessId = "4ardymv3p77tcka877dj";
        String AccessKey = "dc3edde2a86e44f98955f5764c43431a";
        String apiEndpoint = "https://openapi.tuyaeu.com";
        String deviceId = "bf92f59ca62e225506gyey";

        TuyaHomeSdk.init(this, AccessId, AccessKey, new IResultCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "connection successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(
                    MainActivity.this,
                    String.format("connection unsuccessfully with code: %s. Error: %s", code, error),
                    Toast.LENGTH_SHORT
                ).show();
            }
        });

        flag = true;
        btnAddDeviceMain.setOnClickListener(v -> {
            ITuyaDevice device = TuyaHomeSdk.newDeviceInstance(deviceId);
            Map<String, Object> commands = new HashMap<>();
            commands.put("switch", !flag); // or false
            device.publishDps(TuyaUtil.dps2Json(commands), new IResultCallback() {
                @Override
                public void onSuccess() {
                    flag = !flag;
                    Toast.makeText(MainActivity.this, String.format("device is turned %s", (flag) ? "on" : "off"), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String code, String error) {
                    Log.e("Tuya", "Control failed: " + error);
                }
            });

        });
    }


}
