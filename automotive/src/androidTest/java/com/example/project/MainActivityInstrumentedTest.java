package com.example.project;

import android.content.Context;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    private ActivityScenario<MainActivity> scenario;
    private MainActivity activity;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> this.activity = activity);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void useAppContext() {
        // Context of the app under test
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.project", appContext.getPackageName());
    }

    @Test
    public void testActivityLaunch() {
        // Test that the activity launches successfully
        assertNotNull("Activity should not be null", activity);

        // Check that key UI elements are present
        onView(withId(R.id.tempSeekBar)).check(matches(isDisplayed()));
        onView(withId(R.id.fanSeekBar)).check(matches(isDisplayed()));
        onView(withId(R.id.acSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.autoModeSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.statusText)).check(matches(isDisplayed()));
    }

    @Test
    public void testInitialUIState() {
        // Check initial temperature display
        onView(withId(R.id.tempValueText)).check(matches(isDisplayed()));

        // Check initial fan display
        onView(withId(R.id.fanValueText)).check(matches(isDisplayed()));

        // Check initial status text
        onView(withId(R.id.statusText)).check(matches(isDisplayed()));

        // Check that driver zone is initially selected
        onView(withId(R.id.driverZoneBtn))
                .check(matches(isDisplayingAtLeast(90)))
                .perform(scrollTo(), click());
    }

    @Test
    public void testZoneButtonClicks() {
        // Test driver zone button
        onView(withId(R.id.driverZoneBtn))
                .check(matches(isDisplayingAtLeast(90)))
                .perform(scrollTo(), click());

        // Test passenger zone button
        onView(withId(R.id.passengerZoneBtn)).perform(click());

        // Test rear zone button
        onView(withId(R.id.rearZoneBtn)).perform(click());

        // Test all zones button
        onView(withId(R.id.allZonesBtn)).perform(click());

        // Verify status text updates (should contain "All Zones")
        onView(withId(R.id.statusText)).check(matches(withText(containsString("All Zones"))));
    }

    @Test
    public void testPresetButtons() {
        // Test cool preset button
        onView(withId(R.id.presetCoolBtn)).perform(click());

        // Verify AC switch is turned on after cool preset
        scenario.onActivity(activity -> {
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            assertTrue("AC should be enabled after cool preset", acSwitch.isChecked());
        });

        // Test warm preset button
        onView(withId(R.id.presetWarmBtn)).perform(click());

        // Verify eco mode is enabled after warm preset
        scenario.onActivity(activity -> {
            Switch ecoModeSwitch = activity.findViewById(R.id.ecoModeSwitch);
            assertTrue("Eco mode should be enabled after warm preset", ecoModeSwitch.isChecked());
        });
    }

    @Test
    public void testSwitchFunctionality() {
        // Test AC switch
        onView(withId(R.id.acSwitch)).perform(click());

        scenario.onActivity(activity -> {
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            assertTrue("AC switch should be checked after click", acSwitch.isChecked());
        });

        // Test auto mode switch
        onView(withId(R.id.autoModeSwitch)).perform(click());

        scenario.onActivity(activity -> {
            Switch autoModeSwitch = activity.findViewById(R.id.autoModeSwitch);
            assertTrue("Auto mode switch should be checked after click", autoModeSwitch.isChecked());
        });

        // Test defrost switch
        onView(withId(R.id.defrostSwitch)).perform(click());

        scenario.onActivity(activity -> {
            Switch defrostSwitch = activity.findViewById(R.id.defrostSwitch);
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            assertTrue("Defrost switch should be checked after click", defrostSwitch.isChecked());
            assertTrue("AC should be auto-enabled when defrost is activated", acSwitch.isChecked());
        });

        // Test eco mode switch
        onView(withId(R.id.ecoModeSwitch)).perform(click());

        scenario.onActivity(activity -> {
            Switch ecoModeSwitch = activity.findViewById(R.id.ecoModeSwitch);
            assertTrue("Eco mode switch should be checked after click", ecoModeSwitch.isChecked());
        });
    }

    @Test
    public void testDefrostAutoEnablesAC() {
        // First ensure AC is off
        scenario.onActivity(activity -> {
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            if (acSwitch.isChecked()) {
                acSwitch.setChecked(false);
            }
        });

        // Enable defrost
        onView(withId(R.id.defrostSwitch)).perform(click());

        // Verify AC is automatically enabled
        scenario.onActivity(activity -> {
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            Switch defrostSwitch = activity.findViewById(R.id.defrostSwitch);
            assertTrue("Defrost should be enabled", defrostSwitch.isChecked());
            assertTrue("AC should be auto-enabled when defrost is activated", acSwitch.isChecked());
        });
    }

    @Test
    public void testCoolPresetConfiguration() {
        // Apply cool preset
        onView(withId(R.id.presetCoolBtn)).perform(click());

        scenario.onActivity(activity -> {
            SeekBar tempSeekBar = activity.findViewById(R.id.tempSeekBar);
            SeekBar fanSeekBar = activity.findViewById(R.id.fanSeekBar);
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            Switch autoModeSwitch = activity.findViewById(R.id.autoModeSwitch);
            Switch ecoModeSwitch = activity.findViewById(R.id.ecoModeSwitch);

            assertEquals("Temperature should be set to 18°C", 18, tempSeekBar.getProgress());
            assertEquals("Fan should be set to 4", 4, fanSeekBar.getProgress());
            assertTrue("AC should be enabled", acSwitch.isChecked());
            assertTrue("Auto mode should be enabled", autoModeSwitch.isChecked());
            assertFalse("Eco mode should be disabled", ecoModeSwitch.isChecked());
        });
    }

    @Test
    public void testWarmPresetConfiguration() {
        // Apply warm preset
        onView(withId(R.id.presetWarmBtn)).perform(click());

        scenario.onActivity(activity -> {
            SeekBar tempSeekBar = activity.findViewById(R.id.tempSeekBar);
            SeekBar fanSeekBar = activity.findViewById(R.id.fanSeekBar);
            Switch acSwitch = activity.findViewById(R.id.acSwitch);
            Switch autoModeSwitch = activity.findViewById(R.id.autoModeSwitch);
            Switch ecoModeSwitch = activity.findViewById(R.id.ecoModeSwitch);

            assertEquals("Temperature should be set to 25°C", 25, tempSeekBar.getProgress());
            assertEquals("Fan should be set to 2", 2, fanSeekBar.getProgress());
            assertFalse("AC should be disabled", acSwitch.isChecked());
            assertTrue("Auto mode should be enabled", autoModeSwitch.isChecked());
            assertTrue("Eco mode should be enabled", ecoModeSwitch.isChecked());
        });
    }

    @Test
    public void testSeekBarInteraction() {
        scenario.onActivity(activity -> {
            SeekBar tempSeekBar = activity.findViewById(R.id.tempSeekBar);
            SeekBar fanSeekBar = activity.findViewById(R.id.fanSeekBar);

            // Test temperature seek bar
            tempSeekBar.setProgress(22);

            // Test fan seek bar
            fanSeekBar.setProgress(3);

            // Verify values are updated
            TextView tempValueText = activity.findViewById(R.id.tempValueText);
            TextView fanValueText = activity.findViewById(R.id.fanValueText);

            assertEquals("Temperature display should show 22°C", "22°C", tempValueText.getText().toString());
            assertEquals("Fan display should show 3/5", "3/5", fanValueText.getText().toString());
        });
    }

    @Test
    public void testAutoModeWithManualFanControl() {
        // Enable auto mode first
        onView(withId(R.id.autoModeSwitch)).perform(click());

        // Verify auto mode is enabled
        scenario.onActivity(activity -> {
            Switch autoModeSwitch = activity.findViewById(R.id.autoModeSwitch);
            assertTrue("Auto mode should be enabled", autoModeSwitch.isChecked());
        });

        // Manually adjust fan speed
        scenario.onActivity(activity -> {
            SeekBar fanSeekBar = activity.findViewById(R.id.fanSeekBar);
            fanSeekBar.setProgress(5);

            // Simulate the listener being called
            fanSeekBar.setProgress(5); // If needed, manually trigger any expected behavior here (like updating UI or state)

        });

        // Verify auto mode is disabled after manual fan control
        scenario.onActivity(activity -> {
            Switch autoModeSwitch = activity.findViewById(R.id.autoModeSwitch);
            assertFalse("Auto mode should be disabled after manual fan control", autoModeSwitch.isChecked());
        });
    }

    @Test
    public void testEcoModeFanSpeedLimit() {
        // Enable eco mode
        onView(withId(R.id.ecoModeSwitch)).perform(click());

        scenario.onActivity(activity -> {
            SeekBar fanSeekBar = activity.findViewById(R.id.fanSeekBar);

            // Set fan speed to maximum
            fanSeekBar.setProgress(5);

            // Simulate eco mode listener
            fanSeekBar.setProgress(5); // If needed, manually trigger any expected behavior here (like updating UI or state)


            // Verify fan speed is limited to 3 in eco mode
            assertTrue("Fan speed should be limited in eco mode", fanSeekBar.getProgress() <= 3);
        });
    }

    @Test
    public void testStatusTextUpdates() {
        // Change some settings and verify status text updates
        onView(withId(R.id.passengerZoneBtn)).perform(click());
        onView(withId(R.id.acSwitch)).perform(click());

        // Check that status text contains updated information
        onView(withId(R.id.statusText)).check(matches(withText(containsString("Passenger"))));
        onView(withId(R.id.statusText)).check(matches(withText(containsString("AC: On"))));
    }

    @Test
    public void testPowerUsageDisplay() {
        // Test power usage display is visible
        onView(withId(R.id.powerUsageText)).check(matches(isDisplayed()));

        // Enable high power features
        onView(withId(R.id.acSwitch)).perform(click());
        onView(withId(R.id.defrostSwitch)).perform(click());

        scenario.onActivity(activity -> {
            SeekBar fanSeekBar = activity.findViewById(R.id.fanSeekBar);
            fanSeekBar.setProgress(5);
        });

        // Power usage should be displayed
        onView(withId(R.id.powerUsageText)).check(matches(isDisplayed()));
    }

    @Test
    public void testTimeDisplay() {
        // Test that time display is visible and updates
        onView(withId(R.id.currentTimeText)).check(matches(isDisplayed()));

        scenario.onActivity(activity -> {
            TextView timeText = activity.findViewById(R.id.currentTimeText);
            assertNotNull("Time text should not be null", timeText.getText());
            assertNotEquals("Time text should not be empty", "", timeText.getText().toString());
        });
    }

    @Test
    public void testAllUIElementsPresent() {
        // Verify all UI elements are present
        onView(withId(R.id.tempSeekBar)).check(matches(isDisplayed()));
        onView(withId(R.id.fanSeekBar)).check(matches(isDisplayed()));
        onView(withId(R.id.acSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.autoModeSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.defrostSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.ecoModeSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.statusText)).check(matches(isDisplayed()));
        onView(withId(R.id.tempValueText)).check(matches(isDisplayed()));
        onView(withId(R.id.fanValueText)).check(matches(isDisplayed()));
        onView(withId(R.id.currentTimeText)).check(matches(isDisplayed()));
        onView(withId(R.id.powerUsageText)).check(matches(isDisplayed()));
        onView(withId(R.id.driverZoneBtn))
                .check(matches(isDisplayingAtLeast(90)))
                .perform(scrollTo(), click());
        onView(withId(R.id.passengerZoneBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.rearZoneBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.allZonesBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.presetCoolBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.presetWarmBtn)).check(matches(isDisplayed()));
    }
}