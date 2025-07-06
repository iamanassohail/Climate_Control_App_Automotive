package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    // UI Components
    private Spinner profileSpinner, temperatureUnitSpinner, themeSpinner;
    private Switch autoStartSwitch, soundFeedbackSwitch, scheduleEnabledSwitch;
    private Switch maintenanceReminderSwitch, energySavingSwitch;
    private TextView currentProfileText, systemVersionText, lastMaintenanceText;
    private TextView uptimeText, totalUsageText, energyEfficiencyText;
    private Button saveProfileBtn, deleteProfileBtn, diagnosticsBtn, backBtn;
    private Button scheduleBtn, resetSettingsBtn, exportDataBtn;
    private LinearLayout scheduleContainer;
    private TextView scheduleStatusText;
    private EditText profileNameInput;
    private CardView diagnosticsCard, profileCard, scheduleCard, systemCard;

    // Settings data
    private SharedPreferences sharedPreferences;
    private String[] profileNames = {"Default", "Eco", "Comfort", "Performance", "Custom"};
    private String[] temperatureUnits = {"Celsius (°C)", "Fahrenheit (°F)"};
    private String[] themes = {"Dark", "Light", "Auto"};

    // Schedule data
    private boolean scheduleEnabled = false;
    private String scheduleTime = "07:00";
    private int scheduleTemp = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupSpinners();
        setupListeners();
        loadSettings();
        updateSystemInfo();
    }

    private void initializeViews() {
        // Spinners
        profileSpinner = findViewById(R.id.profileSpinner);
        temperatureUnitSpinner = findViewById(R.id.temperatureUnitSpinner);
        themeSpinner = findViewById(R.id.themeSpinner);

        // Switches
        autoStartSwitch = findViewById(R.id.autoStartSwitch);
        soundFeedbackSwitch = findViewById(R.id.soundFeedbackSwitch);
        scheduleEnabledSwitch = findViewById(R.id.scheduleEnabledSwitch);
        maintenanceReminderSwitch = findViewById(R.id.maintenanceReminderSwitch);
        energySavingSwitch = findViewById(R.id.energySavingSwitch);

        // TextViews
        currentProfileText = findViewById(R.id.currentProfileText);
        systemVersionText = findViewById(R.id.systemVersionText);
        lastMaintenanceText = findViewById(R.id.lastMaintenanceText);
        uptimeText = findViewById(R.id.uptimeText);
        totalUsageText = findViewById(R.id.totalUsageText);
        energyEfficiencyText = findViewById(R.id.energyEfficiencyText);
        scheduleStatusText = findViewById(R.id.scheduleStatusText);

        // Buttons
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
        deleteProfileBtn = findViewById(R.id.deleteProfileBtn);
        diagnosticsBtn = findViewById(R.id.diagnosticsBtn);
        backBtn = findViewById(R.id.backBtn);
        scheduleBtn = findViewById(R.id.scheduleBtn);
        resetSettingsBtn = findViewById(R.id.resetSettingsBtn);
        exportDataBtn = findViewById(R.id.exportDataBtn);

        // Other components
        scheduleContainer = findViewById(R.id.scheduleContainer);
        profileNameInput = findViewById(R.id.profileNameInput);
        diagnosticsCard = findViewById(R.id.diagnosticsCard);
        profileCard = findViewById(R.id.profileCard);
        scheduleCard = findViewById(R.id.scheduleCard);
        systemCard = findViewById(R.id.systemCard);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ClimateControlSettings", MODE_PRIVATE);
    }

    private void setupSpinners() {
        // Profile Spinner
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, profileNames);
        profileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileSpinner.setAdapter(profileAdapter);

        // Temperature Unit Spinner
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, temperatureUnits);
        tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temperatureUnitSpinner.setAdapter(tempAdapter);

        // Theme Spinner
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, themes);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);
    }

    private void setupListeners() {
        // Profile Spinner Listener
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProfile = profileNames[position];
                currentProfileText.setText("Current: " + selectedProfile);
                applyProfile(selectedProfile);
                showToast("Profile changed to: " + selectedProfile);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Switch Listeners
        autoStartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettingBoolean("auto_start", isChecked);
            showToast(isChecked ? "Auto-start enabled" : "Auto-start disabled");
        });

        soundFeedbackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettingBoolean("sound_feedback", isChecked);
            showToast(isChecked ? "Sound feedback enabled" : "Sound feedback disabled");
        });

        scheduleEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scheduleEnabled = isChecked;
            scheduleContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateScheduleStatus();
            saveSettingBoolean("schedule_enabled", isChecked);
        });

        maintenanceReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettingBoolean("maintenance_reminder", isChecked);
            showToast(isChecked ? "Maintenance reminders enabled" : "Maintenance reminders disabled");
        });

        energySavingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettingBoolean("energy_saving", isChecked);
            showToast(isChecked ? "Energy saving mode enabled" : "Energy saving mode disabled");
        });

        // Button Listeners
        saveProfileBtn.setOnClickListener(v -> saveCustomProfile());
        deleteProfileBtn.setOnClickListener(v -> deleteCustomProfile());
        diagnosticsBtn.setOnClickListener(v -> runDiagnostics());
        backBtn.setOnClickListener(v -> finish());
        scheduleBtn.setOnClickListener(v -> showScheduleDialog());
        resetSettingsBtn.setOnClickListener(v -> resetAllSettings());
        exportDataBtn.setOnClickListener(v -> exportUserData());

        // Theme Spinner Listener
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = themes[position];
                saveSettingString("theme", selectedTheme);
                showToast("Theme changed to: " + selectedTheme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Temperature Unit Spinner Listener
        temperatureUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = temperatureUnits[position];
                saveSettingString("temperature_unit", selectedUnit);
                showToast("Temperature unit changed to: " + selectedUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyProfile(String profileName) {
        switch (profileName) {
            case "Eco":
                energySavingSwitch.setChecked(true);
                showToast("Eco profile: Energy saving enabled, limited power usage");
                break;
            case "Comfort":
                autoStartSwitch.setChecked(true);
                showToast("Comfort profile: Auto-start enabled, balanced settings");
                break;
            case "Performance":
                energySavingSwitch.setChecked(false);
                showToast("Performance profile: Maximum performance, higher power usage");
                break;
            case "Custom":
                showToast("Custom profile: User-defined settings");
                break;
            default:
                showToast("Default profile: Standard settings");
                break;
        }
    }

    private void saveCustomProfile() {
        String profileName = profileNameInput.getText().toString().trim();
        if (profileName.isEmpty()) {
            showToast("Please enter a profile name");
            return;
        }

        // Save custom profile settings
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("custom_profile_name", profileName);
        editor.putBoolean("custom_auto_start", autoStartSwitch.isChecked());
        editor.putBoolean("custom_sound_feedback", soundFeedbackSwitch.isChecked());
        editor.putBoolean("custom_energy_saving", energySavingSwitch.isChecked());
        editor.apply();

        showToast("Custom profile '" + profileName + "' saved successfully");
        profileNameInput.setText("");
    }

    private void deleteCustomProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("custom_profile_name");
        editor.remove("custom_auto_start");
        editor.remove("custom_sound_feedback");
        editor.remove("custom_energy_saving");
        editor.apply();

        showToast("Custom profile deleted");
        profileSpinner.setSelection(0); // Reset to default
    }

    private void runDiagnostics() {
        // Simulate diagnostic tests
        diagnosticsBtn.setEnabled(false);
        diagnosticsBtn.setText("Running...");

        // Simulate diagnostic process
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate diagnostic time

                runOnUiThread(() -> {
                    // Update diagnostic results
                    updateSystemInfo();
                    diagnosticsBtn.setEnabled(true);
                    diagnosticsBtn.setText("Run Diagnostics");

                    // Show diagnostic results
                    showDiagnosticResults();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showDiagnosticResults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Diagnostic Results");

        StringBuilder results = new StringBuilder();
        results.append("System Status: ✓ Healthy\n");
        results.append("Temperature Sensors: ✓ OK\n");
        results.append("Fan Motor: ✓ OK\n");
        results.append("AC Compressor: ✓ OK\n");
        results.append("Power Supply: ✓ OK\n");
        results.append("Communication: ✓ OK\n\n");
        results.append("Recommendations:\n");
        results.append("• Next maintenance due in 45 days\n");
        results.append("• Filter replacement recommended\n");
        results.append("• System performance: 94%");

        builder.setMessage(results.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Schedule Settings");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_schedule, null);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        SeekBar tempSeekBar = dialogView.findViewById(R.id.scheduleTemperatureSeekBar);
        TextView tempText = dialogView.findViewById(R.id.scheduleTemperatureText);

        // Set current values
        String[] timeParts = scheduleTime.split(":");
        timePicker.setHour(Integer.parseInt(timeParts[0]));
        timePicker.setMinute(Integer.parseInt(timeParts[1]));
        tempSeekBar.setProgress(scheduleTemp);
        tempText.setText(scheduleTemp + "°C");

        tempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempText.setText(progress + "°C");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            scheduleTime = String.format(Locale.getDefault(), "%02d:%02d",
                    timePicker.getHour(), timePicker.getMinute());
            scheduleTemp = tempSeekBar.getProgress();
            updateScheduleStatus();
            saveScheduleSettings();
            showToast("Schedule updated");
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateScheduleStatus() {
        if (scheduleEnabled) {
            scheduleStatusText.setText("Next activation: " + scheduleTime + " at " + scheduleTemp + "°C");
            scheduleStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            scheduleStatusText.setText("Schedule disabled");
            scheduleStatusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void resetAllSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Settings");
        builder.setMessage("Are you sure you want to reset all settings to default values?");
        builder.setPositiveButton("Reset", (dialog, which) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            loadSettings(); // Reload default settings
            showToast("All settings reset to default");
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void exportUserData() {
        // Simulate data export
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Data");
        builder.setMessage("User data and settings have been exported to:\n/storage/emulated/0/ClimateControl/export_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json");
        builder.setPositiveButton("OK", null);
        builder.show();

        showToast("Data exported successfully");
    }

    private void updateSystemInfo() {
        systemVersionText.setText("Version: 2.1.0");
        lastMaintenanceText.setText("Last maintenance: 15 days ago");
        uptimeText.setText("System uptime: 127 hours");
        totalUsageText.setText("Total usage: 1,247 hours");
        energyEfficiencyText.setText("Energy efficiency: 94%");
    }

    private void loadSettings() {
        // Load saved settings
        autoStartSwitch.setChecked(sharedPreferences.getBoolean("auto_start", false));
        soundFeedbackSwitch.setChecked(sharedPreferences.getBoolean("sound_feedback", true));
        scheduleEnabled = sharedPreferences.getBoolean("schedule_enabled", false);
        scheduleEnabledSwitch.setChecked(scheduleEnabled);
        maintenanceReminderSwitch.setChecked(sharedPreferences.getBoolean("maintenance_reminder", true));
        energySavingSwitch.setChecked(sharedPreferences.getBoolean("energy_saving", false));

        // Load spinner selections
        String savedTheme = sharedPreferences.getString("theme", "Dark");
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(savedTheme)) {
                themeSpinner.setSelection(i);
                break;
            }
        }

        String savedUnit = sharedPreferences.getString("temperature_unit", "Celsius (°C)");
        for (int i = 0; i < temperatureUnits.length; i++) {
            if (temperatureUnits[i].equals(savedUnit)) {
                temperatureUnitSpinner.setSelection(i);
                break;
            }
        }

        scheduleContainer.setVisibility(scheduleEnabled ? View.VISIBLE : View.GONE);
        updateScheduleStatus();
    }

    private void saveScheduleSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("schedule_time", scheduleTime);
        editor.putInt("schedule_temp", scheduleTemp);
        editor.apply();
    }

    private void saveSettingBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void saveSettingString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}