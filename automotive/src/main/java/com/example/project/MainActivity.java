package com.example.project;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // UI Components
    private SeekBar tempSeekBar, fanSeekBar;
    private Switch acSwitch, autoModeSwitch, defrostSwitch, ecoModeSwitch;
    private TextView statusText, tempValueText, fanValueText, currentTimeText, powerUsageText;
    private Button driverZoneBtn, passengerZoneBtn, rearZoneBtn, allZonesBtn;
    private Button presetCoolBtn, presetWarmBtn;

    // State variables
    private String currentZone = "Driver";
    private Handler timeHandler;
    private Runnable timeRunnable;
    private boolean isAutoModeActive = false;
    private int targetTemperature = 20;

    // Climate presets
    private static final int COOL_PRESET_TEMP = 18;
    private static final int WARM_PRESET_TEMP = 25;
    private static final int COOL_PRESET_FAN = 4;
    private static final int WARM_PRESET_FAN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
        setupTimeUpdater();
        updateStatus();
        updateZoneButtons();
        updateSwitchColors(); // Initialize switch colors
    }

    private void initializeViews() {
        // SeekBars
        tempSeekBar = findViewById(R.id.tempSeekBar);
        fanSeekBar = findViewById(R.id.fanSeekBar);

        // Switches
        acSwitch = findViewById(R.id.acSwitch);
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        defrostSwitch = findViewById(R.id.defrostSwitch);
        ecoModeSwitch = findViewById(R.id.ecoModeSwitch);

        // TextViews
        statusText = findViewById(R.id.statusText);
        tempValueText = findViewById(R.id.tempValueText);
        fanValueText = findViewById(R.id.fanValueText);
        currentTimeText = findViewById(R.id.currentTimeText);
        powerUsageText = findViewById(R.id.powerUsageText);

        // Zone control buttons
        driverZoneBtn = findViewById(R.id.driverZoneBtn);
        passengerZoneBtn = findViewById(R.id.passengerZoneBtn);
        rearZoneBtn = findViewById(R.id.rearZoneBtn);
        allZonesBtn = findViewById(R.id.allZonesBtn);

        // Preset buttons
        presetCoolBtn = findViewById(R.id.presetCoolBtn);
        presetWarmBtn = findViewById(R.id.presetWarmBtn);

        // Set initial values
        tempSeekBar.setMin(16); // Set minimum temperature to 16°C
        updateTemperatureDisplay();
        updateFanDisplay();
    }

    private void setupListeners() {
        // SeekBar listeners
        tempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    targetTemperature = progress;
                    updateTemperatureDisplay();
                    updateStatus();
                    handleAutoMode();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        fanSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateFanDisplay();
                    updateStatus();
                    // Disable auto mode when manually changing fan speed
                    if (autoModeSwitch.isChecked()) {
                        autoModeSwitch.setChecked(false);
                        showToast("Auto mode disabled - Manual fan control");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Switch listeners
        acSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors();
            updateStatus();
            if (isChecked) {
                showToast("Air conditioning enabled");
            } else {
                showToast("Air conditioning disabled");
            }
        });

        autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAutoModeActive = isChecked;
            updateSwitchColors();
            updateStatus();
            if (isChecked) {
                handleAutoMode();
                showToast("Auto mode enabled - System will adjust fan speed automatically");
            } else {
                showToast("Auto mode disabled");
            }
        });

        defrostSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors();
            updateStatus();
            if (isChecked) {
                // Auto-enable AC for defrost
                acSwitch.setChecked(true);
                showToast("Defrost activated - AC automatically enabled");
            } else {
                showToast("Defrost deactivated");
            }
        });

        ecoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors();
            updateStatus();
            if (isChecked) {
                // Eco mode limits fan speed and temperature range
                if (fanSeekBar.getProgress() > 3) {
                    fanSeekBar.setProgress(3);
                }
                showToast("Eco mode enabled - Power consumption reduced");
            } else {
                showToast("Eco mode disabled");
            }
        });

        // Zone control button listeners
        driverZoneBtn.setOnClickListener(v -> selectZone("Driver"));
        passengerZoneBtn.setOnClickListener(v -> selectZone("Passenger"));
        rearZoneBtn.setOnClickListener(v -> selectZone("Rear"));
        allZonesBtn.setOnClickListener(v -> selectZone("All Zones"));

        // Preset button listeners
        presetCoolBtn.setOnClickListener(v -> applyCoolPreset());
        presetWarmBtn.setOnClickListener(v -> applyWarmPreset());
    }

    private void setupTimeUpdater() {
        timeHandler = new Handler(Looper.getMainLooper());
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateCurrentTime();
                timeHandler.postDelayed(this, 1000); // Update every second
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        currentTimeText.setText(sdf.format(new Date()));
    }

    private void updateTemperatureDisplay() {
        int temp = tempSeekBar.getProgress();
        tempValueText.setText(temp + "°C");
    }

    private void updateFanDisplay() {
        int fan = fanSeekBar.getProgress();
        if (fan == 0) {
            fanValueText.setText("Off");
        } else {
            fanValueText.setText(fan + "/5");
        }
    }

    private void selectZone(String zone) {
        currentZone = zone;
        updateZoneButtons();
        updateStatus();
        showToast("Zone selected: " + zone);
    }

    private void updateZoneButtons() {
        // Reset all buttons to default color
        int defaultColor = getResources().getColor(android.R.color.darker_gray);
        int selectedColor = getResources().getColor(android.R.color.holo_green_dark);

        driverZoneBtn.setBackgroundColor(defaultColor);
        passengerZoneBtn.setBackgroundColor(defaultColor);
        rearZoneBtn.setBackgroundColor(defaultColor);
        allZonesBtn.setBackgroundColor(defaultColor);

        // Highlight selected zone
        switch (currentZone) {
            case "Driver":
                driverZoneBtn.setBackgroundColor(selectedColor);
                break;
            case "Passenger":
                passengerZoneBtn.setBackgroundColor(selectedColor);
                break;
            case "Rear":
                rearZoneBtn.setBackgroundColor(selectedColor);
                break;
            case "All Zones":
                allZonesBtn.setBackgroundColor(selectedColor);
                break;
        }
    }

    private void applyCoolPreset() {
        tempSeekBar.setProgress(COOL_PRESET_TEMP);
        fanSeekBar.setProgress(COOL_PRESET_FAN);
        acSwitch.setChecked(true);
        autoModeSwitch.setChecked(true);
        ecoModeSwitch.setChecked(false);

        targetTemperature = COOL_PRESET_TEMP;
        updateTemperatureDisplay();
        updateFanDisplay();
        updateStatus();
        updateSwitchColors(); // Update switch colors for presets
        updateSwitchColors(); // Update switch colors for presets
        showToast("Cool preset applied");
    }

    private void applyWarmPreset() {
        tempSeekBar.setProgress(WARM_PRESET_TEMP);
        fanSeekBar.setProgress(WARM_PRESET_FAN);
        acSwitch.setChecked(false);
        autoModeSwitch.setChecked(true);
        ecoModeSwitch.setChecked(true);

        targetTemperature = WARM_PRESET_TEMP;
        updateTemperatureDisplay();
        updateFanDisplay();
        updateStatus();
        showToast("Warm preset applied");
    }

    private void handleAutoMode() {
        if (!isAutoModeActive) return;

        // Simulate automatic fan speed adjustment based on temperature
        int currentTemp = tempSeekBar.getProgress();
        int optimalFanSpeed;

        if (currentTemp <= 18) {
            optimalFanSpeed = 1; // Low fan for cold temperatures
        } else if (currentTemp <= 22) {
            optimalFanSpeed = 2; // Medium-low fan for comfortable temperatures
        } else if (currentTemp <= 26) {
            optimalFanSpeed = 3; // Medium fan for warm temperatures
        } else {
            optimalFanSpeed = 4; // High fan for hot temperatures
        }

        // Apply eco mode limitations
        if (ecoModeSwitch.isChecked() && optimalFanSpeed > 3) {
            optimalFanSpeed = 3;
        }

        fanSeekBar.setProgress(optimalFanSpeed);
        updateFanDisplay();
    }

    private void updateStatus() {
        int temp = tempSeekBar.getProgress();
        int fan = fanSeekBar.getProgress();
        boolean ac = acSwitch.isChecked();
        boolean auto = autoModeSwitch.isChecked();
        boolean defrost = defrostSwitch.isChecked();
        boolean eco = ecoModeSwitch.isChecked();

        StringBuilder status = new StringBuilder();
        status.append("Zone: ").append(currentZone).append("\n");
        status.append("Temperature: ").append(temp).append("°C\n");
        status.append("Fan Speed: ");
        if (fan == 0) {
            status.append("Off");
        } else {
            status.append(fan).append("/5");
        }
        status.append("\n");
        status.append("AC: ").append(ac ? "On" : "Off").append("\n");
        status.append("Auto Mode: ").append(auto ? "Enabled" : "Disabled").append("\n");
        status.append("Defrost: ").append(defrost ? "Active" : "Off").append("\n");
        status.append("Eco Mode: ").append(eco ? "Enabled" : "Disabled");

        statusText.setText(status.toString());
        updatePowerUsage();
    }

    private void updatePowerUsage() {
        int fan = fanSeekBar.getProgress();
        boolean ac = acSwitch.isChecked();
        boolean defrost = defrostSwitch.isChecked();
        boolean eco = ecoModeSwitch.isChecked();

        String powerLevel;
        int powerScore = 0;

        // Calculate power usage score
        powerScore += fan; // Fan speed contributes to power usage
        if (ac) powerScore += 3; // AC uses significant power
        if (defrost) powerScore += 2; // Defrost uses moderate power
        if (eco) powerScore -= 2; // Eco mode reduces power usage

        // Determine power level
        if (powerScore <= 2) {
            powerLevel = "Low";
            powerUsageText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (powerScore <= 5) {
            powerLevel = "Medium";
            powerUsageText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            powerLevel = "High";
            powerUsageText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        powerUsageText.setText(powerLevel);
    }

    private void updateSwitchColors() {
        // Update switch colors based on their state
        updateSwitchColor(acSwitch, android.R.color.holo_green_light, android.R.color.darker_gray);
        updateSwitchColor(autoModeSwitch, android.R.color.holo_orange_light, android.R.color.darker_gray);
        updateSwitchColor(defrostSwitch, android.R.color.holo_red_light, android.R.color.darker_gray);
        updateSwitchColor(ecoModeSwitch, android.R.color.holo_green_dark, android.R.color.darker_gray);
    }

    private void updateSwitchColor(Switch switchView, int onColorRes, int offColorRes) {
        int onColor = getResources().getColor(onColorRes);
        int offColor = getResources().getColor(offColorRes);

        if (switchView.isChecked()) {
            switchView.getThumbDrawable().setTint(onColor);
            switchView.getTrackDrawable().setTint(onColor);
        } else {
            switchView.getThumbDrawable().setTint(offColor);
            switchView.getTrackDrawable().setTint(offColor);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
}