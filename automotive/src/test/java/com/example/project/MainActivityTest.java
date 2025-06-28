package com.example.project;

import static org.junit.Assert.*;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import android.widget.Switch;

public class MainActivityTest {
    @Test
    public void testInitialValues() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> {
            assertEquals(20, activity.findViewById(R.id.tempSeekBar).getProgress());
            assertEquals(2, activity.findViewById(R.id.fanSeekBar).getProgress());
            assertFalse(((Switch) activity.findViewById(R.id.acSwitch)).isChecked());
            assertFalse(((Switch) activity.findViewById(R.id.autoModeSwitch)).isChecked());
            assertFalse(((Switch) activity.findViewById(R.id.defrostSwitch)).isChecked());
        });
    }
}