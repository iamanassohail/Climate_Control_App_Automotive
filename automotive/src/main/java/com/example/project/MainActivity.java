package com.example.project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;

public class MainActivity extends AppCompatActivity {
    private SeekBar tempSeekBar, fanSeekBar;
    private Switch acSwitch, autoModeSwitch, defrostSwitch;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempSeekBar = findViewById(R.id.tempSeekBar);
        fanSeekBar = findViewById(R.id.fanSeekBar);
        acSwitch = findViewById(R.id.acSwitch);
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        defrostSwitch = findViewById(R.id.defrostSwitch);
        statusText = findViewById(R.id.statusText);

        updateStatus();

        tempSeekBar.setOnSeekBarChangeListener(new SimpleListener());
        fanSeekBar.setOnSeekBarChangeListener(new SimpleListener());

        acSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateStatus());
        autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateStatus());
        defrostSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateStatus());
    }

    private void updateStatus() {
        int temp = tempSeekBar.getProgress();
        int fan = fanSeekBar.getProgress();
        boolean ac = acSwitch.isChecked();
        boolean auto = autoModeSwitch.isChecked();
        boolean defrost = defrostSwitch.isChecked();

        String status = "Temp: " + temp + "°C\n" +
                "Fan: " + fan + "/5\n" +
                "AC: " + (ac ? "On" : "Off") + "\n" +
                "Auto Mode: " + (auto ? "Enabled" : "Disabled") + "\n" +
                "Defrost: " + (defrost ? "Active" : "Off");

        statusText.setText(status);
    }

    private class SimpleListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { updateStatus(); }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}