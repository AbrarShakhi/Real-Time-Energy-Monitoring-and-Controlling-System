package com.abrarshakhi.rtemcs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abrarshakhi.rtemcs.data.DeviceInfoDb;
import com.abrarshakhi.rtemcs.data.PowerConsumptionHistDb;
import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.abrarshakhi.rtemcs.model.StatRecord;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceDetailActivity extends AppCompatActivity {
    public static final String ACTION_UPDATE_SWITCH = "com.abrarshakhi.rtemcs.UPDATE_SWITCH";
    public static final String POWER = "P";
    public static final String CURRENT = "C";
    public static final String VOLTAGE = "V";
    public static final String HAS_STAT = "S";

    DeviceInfoDb db;
    private ExecutorService executorService;
    private double power, current, voltage;
    private String showGraphFor;
    private long startMillis, endMillis;
    private BarChart graphView;
    private TextInputEditText etEndTime, etStartTime, etScheduleToggler;
    private Button btnBack, btnEdit;
    private ImageButton btnRefChart, btnExport, btnToggler;
    private Spinner spnGraphSelector;
    private DeviceInfo device;
    private MaterialSwitch swMonitorDevice, swToggleDevice;
    private TextView tvBill, tvPwr, tvEnergy, tvCur, tvVolt;
    private int lastSentId = 1;
    private PowerConsumptionHistDb powerConsumptionHistDb;

    /**
     * Broadcast receiver. If foreground service broadcast a power, current, information then it receives it.
     */
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
            if (swMonitorDevice.isChecked() != isRunning) {
                swMonitorDevice.setChecked(isRunning);
            }
            if (swToggleDevice.isChecked() != isTurnOn) {
                swToggleDevice.setChecked(isTurnOn);
            }
            // Load the payload.
            lastSentId = id;
            power = intent.getDoubleExtra(POWER, 0);
            current = intent.getDoubleExtra(CURRENT, 0);
            voltage = intent.getDoubleExtra(VOLTAGE, 0);
            boolean hasStat = intent.getBooleanExtra(HAS_STAT, false);
            if (hasStat) {
                Toast.makeText(context, "POWER RECEIVED: " + power, Toast.LENGTH_LONG).show();
            }
            // refresh the chart when receives a broadcast
            refreshChart();
        }
    };

    /**
     * Update graphs.
     * @param id
     */
    private void updateStats(int id) {
        List<StatRecord> records = powerConsumptionHistDb.getRecordsInRange(id, startMillis, endMillis);

        plotBarChart(records);

        tvPwr.setText(String.valueOf(power));
        tvCur.setText(String.valueOf(current));
        tvVolt.setText(String.valueOf(voltage));

        float energy = calculateEnergy(records, startMillis, endMillis);
        tvEnergy.setText(String.format(Locale.US, "%.2f", energy));

        double bill = calculateBill(energy);
        tvBill.setText(String.format(Locale.US, "৳ %.2f", bill));
    }

    /**
     * function that plots the graph
     * @param list
     */
    private void plotBarChart(List<StatRecord> list) {
        List<BarEntry> entries = new ArrayList<>();
        List<Long> xValues = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if (showGraphFor.equals("current")) {
                entries.add(new BarEntry(i, (float) list.get(i).getCurrentAmp()));
            } else if (showGraphFor.equals("voltage")) {
                entries.add(new BarEntry(i, (float) list.get(i).getVoltage()));
            } else {
                entries.add(new BarEntry(i, (float) list.get(i).getPowerKW()));
            }
            xValues.add(list.get(i).getTimestampMs());
        }

        BarDataSet dataSet = new BarDataSet(entries, showGraphFor);
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        graphView.setData(data);
        graphView.setFitBars(true);

        final XAxis xAxis = getXAxis(xValues, entries);
        xAxis.setLabelCount(entries.size(), true);

        // --- Y Axis ---
        YAxis left = graphView.getAxisLeft();
        left.setAxisMinimum(0f); // No negative values
        left.setGranularity(0.1f);
        left.setDrawLabels(true);  // Hide Y-axis labels
        left.setDrawGridLines(true); // Keep grid lines
        left.setDrawAxisLine(true); // Keep axis line

        graphView.getAxisRight().setEnabled(false); // Disable right Y axis

        // --- Enable scrolling + zoom ---
        graphView.setDragEnabled(true);
        graphView.setScaleEnabled(true);
        graphView.setPinchZoom(true);

        graphView.invalidate();
    }

    /**
     * Unitils function that returns graph X axis.
     * @param xValues
     * @param entries
     * @return
     */
    @NonNull
    private XAxis getXAxis(List<Long> xValues, List<BarEntry> entries) {
        XAxis xAxis = graphView.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f);

        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index < 0 || index >= xValues.size()) return "";
                return new SimpleDateFormat("dd/MM HH:mm", Locale.US)
                    .format(new Date(xValues.get(index)));
            }
        });

        xAxis.setAxisMaximum(entries.size() - 1);
        return xAxis;
    }

    /**
     * calculates energy.
     * @param csvData
     * @param startMillis
     * @param endMillis
     * @return
     */
    private float calculateEnergy(List<StatRecord> csvData, long startMillis, long endMillis) {
        if (csvData == null || csvData.isEmpty()) return 0f;

        csvData.sort(Comparator.comparingLong(StatRecord::getTimestampMs));

        float totalEnergy = 0f;

        long prevTime = startMillis;
        double prevPower = 0.0;

        for (StatRecord r : csvData) {

            long time = r.getTimestampMs();
            double power = r.getPowerKW();

            // Skip earlier data
            if (time < startMillis) {
                prevPower = power;
                prevTime = time;
                continue;
            }

            if (time > endMillis) break;

            // Calculate Δt (hours)
            long deltaMillis = time - prevTime;
            float deltaHours = deltaMillis / 3600000f;

            // Energy = previous power * time interval
            totalEnergy += (float) prevPower * deltaHours;

            prevTime = time;
            prevPower = power;
        }

        // Final segment until endMillis
        if (prevTime < endMillis) {
            long deltaMillis = endMillis - prevTime;
            float deltaHours = deltaMillis / 3600000f;
            totalEnergy += (float) prevPower * deltaHours;
        }

        return totalEnergy; // kWh
    }

    /**
     * Bill calculation function.It uses bangladesh 2025 electricity billing policy.
     * @param energyKWh
     * @return
     */
    private double calculateBill(float energyKWh) {
        double bill;

        if (energyKWh <= 50) {
            bill = energyKWh * 4.63;
        } else if (energyKWh <= 75) {
            bill = 50 * 4.63 + (energyKWh - 50) * 5.26;
        } else if (energyKWh <= 200) {
            bill = 50 * 4.63 + 25 * 5.26 + (energyKWh - 75) * 7.20;
        } else if (energyKWh <= 300) {
            bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + (energyKWh - 200) * 7.59;
        } else if (energyKWh <= 400) {
            bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + 100 * 7.59 + (energyKWh - 300) * 8.02;
        } else if (energyKWh <= 600) {
            bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + 100 * 7.59 + 100 * 8.02 + (energyKWh - 400) * 12.67;
        } else {
            bill = 50 * 4.63 + 25 * 5.26 + 125 * 7.20 + 100 * 7.59 + 100 * 8.02 + 200 * 12.67 + (energyKWh - 600) * 14.61;
        }

        return bill;
    }

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

        executorService = Executors.newFixedThreadPool(3);
        showGraphFor = "power";
        db = new DeviceInfoDb(this);
        powerConsumptionHistDb = new PowerConsumptionHistDb(this);
        initViews();
        initButtons();
        initSpinner();
        initSwitches();
        endMillis = System.currentTimeMillis();
        startMillis = 0;
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
            refreshChart();
        }
        btnEdit.setEnabled(!swMonitorDevice.isChecked());
        swToggleDevice.setEnabled(swMonitorDevice.isChecked());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackDetail);
        btnEdit = findViewById(R.id.btnEditDetail);
        swMonitorDevice = findViewById(R.id.swMonitorDeviceDetail);
        swToggleDevice = findViewById(R.id.swToggleDeviceDetail);
        tvBill = findViewById(R.id.tvBill);
        tvPwr = findViewById(R.id.tvPwr);
        tvEnergy = findViewById(R.id.tvEnergy);
        graphView = findViewById(R.id.graphView);
        btnRefChart = findViewById(R.id.btnRefChart);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnExport = findViewById(R.id.btnExport);
        spnGraphSelector = findViewById(R.id.spnGraphSelector);
        etScheduleToggler = findViewById(R.id.etScheduleToggler);
        tvCur = findViewById(R.id.tvCur);
        tvVolt = findViewById(R.id.tvVolt);
        btnToggler = findViewById(R.id.btnToggler);
    }

    private void initSpinner() {
        spnGraphSelector.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"power", "current", "voltage"}
        ));

        spnGraphSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showGraphFor = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showGraphFor = "power";
            }
        });
    }

    private void initButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v ->
            startActivity(new Intent(DeviceDetailActivity.this, DeviceInfoActivity.class).putExtra(DeviceInfo.ID, device.getId()))
        );
        etEndTime.setFocusable(false);
        etEndTime.setClickable(true);
        etScheduleToggler.setFocusable(false);
        etScheduleToggler.setClickable(true);
        etStartTime.setFocusable(false);
        etStartTime.setClickable(true);

        etEndTime.setOnClickListener(v -> showDateTimePicker(etEndTime));
        etStartTime.setOnClickListener(v -> showDateTimePicker(etStartTime));
        etScheduleToggler.setOnClickListener(v -> showDateTimePicker(etScheduleToggler));

        btnRefChart.setOnClickListener(v -> refreshChart());

        btnExport.setOnClickListener(v -> writeToExternalAppStorage(lastSentId));

        btnToggler.setOnClickListener(v -> scheduleOnOff());
    }

    private void scheduleOnOff() {
        String time = Objects.requireNonNull(etScheduleToggler.getText()).toString();
        long whenToToggle = convertToMillis(time);
        executorService.submit(() -> {
                long currentTime = System.currentTimeMillis();
                if (whenToToggle > currentTime) {
                    long delay = whenToToggle - currentTime;
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                toggleDevice(!device.isTurnOn());
            }
        );
    }

    private void writeToExternalAppStorage(int id) {
        executorService.submit(() -> {
            String fileName = "STAT" + id + ".csv";
            File file = new File(getExternalFilesDir(null), fileName);
            List<StatRecord> records = powerConsumptionHistDb.getAllRecords();
            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                if (file.length() == 0) {
                    out.println("Timestamp,PowerKiloWatt,CurrentAmp,Voltage");
                }
                for (final StatRecord r : records) {
                    out.printf("%d,%f,%f,%f\n", r.getTimestampMs(), r.getPowerKW(), r.getCurrentAmp(), r.getVoltage());
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(DeviceDetailActivity.this, "Data written successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(DeviceDetailActivity.this, "Error writing data.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshChart() {
        String end = Objects.requireNonNull(etEndTime.getText()).toString().trim();
        String start = Objects.requireNonNull(etStartTime.getText()).toString().trim();
        if (end.isBlank()) {
            endMillis = System.currentTimeMillis();
        } else {
            endMillis = convertToMillis(end);
        }
        if (endMillis == 0) {
            endMillis = System.currentTimeMillis();
        }
        if (start.isBlank()) {
            endMillis = 0;
        } else {
            endMillis = convertToMillis(end);
        }
        if (startMillis == 0) {
            startMillis = endMillis - (60 * 60 * 1000);
        }
        updateStats(lastSentId);
    }

    private long convertToMillis(String dateTime) {
        try {
            Date date = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).parse(dateTime);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }


    private void showDateTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePicker = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (timeView, hour, minute) -> {
                        target.setText(String.format(
                            Locale.US,
                            "%02d:%02d %02d/%02d/%04d",
                            hour, minute, dayOfMonth, month + 1, year
                        ));
                    },
                    currentHour,
                    currentMinute,
                    true
                );
                timePicker.show();
            },
            currentYear,
            currentMonth,
            currentDay
        );

        datePicker.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {

            int sY = data.getIntExtra("startYear", 0);
            int sM = data.getIntExtra("startMonth", 0);
            int sD = data.getIntExtra("startDay", 0);
            int sH = data.getIntExtra("startHour", 0);
            int sMin = data.getIntExtra("startMinute", 0);

            int eY = data.getIntExtra("endYear", 0);
            int eM = data.getIntExtra("endMonth", 0);
            int eD = data.getIntExtra("endDay", 0);
            int eH = data.getIntExtra("endHour", 0);
            int eMin = data.getIntExtra("endMinute", 0);

            String msg = "Start: " + sY + "-" + (sM + 1) + "-" + sD + " " + sH + ":" + sMin +
                "\nEnd: " + eY + "-" + (eM + 1) + "-" + eD + " " + eH + ":" + eMin;

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
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
            toggleDevice(isChecked);
        });
    }

    private void toggleDevice(boolean isChecked) {
        if (!device.isRunning()) {
            return;
        }
        Intent servIt = new Intent(DeviceDetailActivity.this, DeviceService.class);
        if (device == null) return;
        servIt.putExtra(DeviceInfo.ID, device.getId());
        servIt.putExtra(DeviceService.STATE,
            isChecked
                ? DeviceService.SERVICE_STATE.TURN_ON.ordinal()
                : DeviceService.SERVICE_STATE.TURN_OFF.ordinal());
        startForegroundService(servIt);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}