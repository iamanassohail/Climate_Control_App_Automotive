package com.example.project;

import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {

    private MainActivity activity;

    @Mock
    private SeekBar mockTempSeekBar;

    @Mock
    private SeekBar mockFanSeekBar;

    @Mock
    private Switch mockAcSwitch;

    @Mock
    private Switch mockAutoModeSwitch;

    @Mock
    private Switch mockDefrostSwitch;

    @Mock
    private Switch mockEcoModeSwitch;

    @Mock
    private TextView mockStatusText;

    @Mock
    private TextView mockTempValueText;

    @Mock
    private TextView mockFanValueText;

    @Mock
    private Button mockDriverZoneBtn;

    @Mock
    private Button mockPassengerZoneBtn;

    @Mock
    private Button mockRearZoneBtn;

    @Mock
    private Button mockAllZonesBtn;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @Test
    public void testActivityCreation() {
        assertNotNull("MainActivity should be created", activity);
    }

    @Test
    public void testInitialValues() {
        // Test initial zone selection
        assertEquals("Driver", getPrivateField(activity, "currentZone"));
        assertEquals(20, getPrivateField(activity, "targetTemperature"));
        assertFalse((Boolean) getPrivateField(activity, "isAutoModeActive"));
    }

    @Test
    public void testTemperaturePresets() {
        // Test cool preset constants
        assertEquals(18, getPrivateField(activity, "COOL_PRESET_TEMP"));
        assertEquals(25, getPrivateField(activity, "WARM_PRESET_TEMP"));
        assertEquals(4, getPrivateField(activity, "COOL_PRESET_FAN"));
        assertEquals(2, getPrivateField(activity, "WARM_PRESET_FAN"));
    }

    @Test
    public void testZoneSelection() {
        // Test zone selection logic
        invokePrivateMethod(activity, "selectZone", "Passenger");
        assertEquals("Passenger", getPrivateField(activity, "currentZone"));

        invokePrivateMethod(activity, "selectZone", "Rear");
        assertEquals("Rear", getPrivateField(activity, "currentZone"));

        invokePrivateMethod(activity, "selectZone", "All Zones");
        assertEquals("All Zones", getPrivateField(activity, "currentZone"));
    }

    @Test
    public void testAutoModeLogic() {
        // Set up mocks
        when(mockTempSeekBar.getProgress()).thenReturn(20);
        when(mockEcoModeSwitch.isChecked()).thenReturn(false);
        when(mockFanSeekBar.getProgress()).thenReturn(0);

        // Test auto mode with different temperatures
        setPrivateField(activity, "isAutoModeActive", true);
        setPrivateField(activity, "ecoModeSwitch", mockEcoModeSwitch);
        setPrivateField(activity, "tempSeekBar", mockTempSeekBar);
        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);

        // Test cold temperature (≤18)
        when(mockTempSeekBar.getProgress()).thenReturn(16);
        invokePrivateMethod(activity, "handleAutoMode");
        verify(mockFanSeekBar).setProgress(1);

        // Test comfortable temperature (19-22)
        when(mockTempSeekBar.getProgress()).thenReturn(20);
        invokePrivateMethod(activity, "handleAutoMode");
        verify(mockFanSeekBar).setProgress(2);

        // Test warm temperature (23-26)
        when(mockTempSeekBar.getProgress()).thenReturn(24);
        invokePrivateMethod(activity, "handleAutoMode");
        verify(mockFanSeekBar).setProgress(3);

        // Test hot temperature (>26)
        when(mockTempSeekBar.getProgress()).thenReturn(28);
        invokePrivateMethod(activity, "handleAutoMode");
        verify(mockFanSeekBar).setProgress(4);
    }

    @Test
    public void testEcoModeWithAutoMode() {
        // Test eco mode limiting fan speed
        when(mockTempSeekBar.getProgress()).thenReturn(30); // Hot temperature
        when(mockEcoModeSwitch.isChecked()).thenReturn(true);
        when(mockFanSeekBar.getProgress()).thenReturn(0);

        setPrivateField(activity, "isAutoModeActive", true);
        setPrivateField(activity, "ecoModeSwitch", mockEcoModeSwitch);
        setPrivateField(activity, "tempSeekBar", mockTempSeekBar);
        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);

        invokePrivateMethod(activity, "handleAutoMode");

        // Should be limited to 3 due to eco mode
        verify(mockFanSeekBar).setProgress(3);
    }

    @Test
    public void testPowerUsageCalculation() {
        // Mock the UI components
        when(mockFanSeekBar.getProgress()).thenReturn(2);
        when(mockAcSwitch.isChecked()).thenReturn(true);
        when(mockDefrostSwitch.isChecked()).thenReturn(false);
        when(mockEcoModeSwitch.isChecked()).thenReturn(false);

        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);
        setPrivateField(activity, "acSwitch", mockAcSwitch);
        setPrivateField(activity, "defrostSwitch", mockDefrostSwitch);
        setPrivateField(activity, "ecoModeSwitch", mockEcoModeSwitch);
        setPrivateField(activity, "powerUsageText", mockStatusText);

        invokePrivateMethod(activity, "updatePowerUsage");

        // Power score should be: fan(2) + AC(3) = 5 (Medium)
        verify(mockStatusText).setText("Medium");
    }

    @Test
    public void testPowerUsageWithEcoMode() {
        // Test power usage with eco mode enabled
        when(mockFanSeekBar.getProgress()).thenReturn(3);
        when(mockAcSwitch.isChecked()).thenReturn(true);
        when(mockDefrostSwitch.isChecked()).thenReturn(true);
        when(mockEcoModeSwitch.isChecked()).thenReturn(true);

        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);
        setPrivateField(activity, "acSwitch", mockAcSwitch);
        setPrivateField(activity, "defrostSwitch", mockDefrostSwitch);
        setPrivateField(activity, "ecoModeSwitch", mockEcoModeSwitch);
        setPrivateField(activity, "powerUsageText", mockStatusText);

        invokePrivateMethod(activity, "updatePowerUsage");

        // Power score: fan(3) + AC(3) + defrost(2) - eco(2) = 6 (High)
        verify(mockStatusText).setText("High");
    }

    @Test
    public void testTemperatureDisplay() {
        when(mockTempSeekBar.getProgress()).thenReturn(22);

        setPrivateField(activity, "tempSeekBar", mockTempSeekBar);
        setPrivateField(activity, "tempValueText", mockTempValueText);

        invokePrivateMethod(activity, "updateTemperatureDisplay");

        verify(mockTempValueText).setText("22°C");
    }

    @Test
    public void testFanDisplayOff() {
        when(mockFanSeekBar.getProgress()).thenReturn(0);

        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);
        setPrivateField(activity, "fanValueText", mockFanValueText);

        invokePrivateMethod(activity, "updateFanDisplay");

        verify(mockFanValueText).setText("Off");
    }

    @Test
    public void testFanDisplayOn() {
        when(mockFanSeekBar.getProgress()).thenReturn(3);

        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);
        setPrivateField(activity, "fanValueText", mockFanValueText);

        invokePrivateMethod(activity, "updateFanDisplay");

        verify(mockFanValueText).setText("3/5");
    }

    @Test
    public void testAutoModeInactive() {
        setPrivateField(activity, "isAutoModeActive", false);
        setPrivateField(activity, "fanSeekBar", mockFanSeekBar);

        invokePrivateMethod(activity, "handleAutoMode");

        // Should not interact with fanSeekBar when auto mode is inactive
        verify(mockFanSeekBar, never()).setProgress(anyInt());
    }

    // Helper methods for accessing private fields and methods
    private Object getPrivateField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object invokePrivateMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
                if (paramTypes[i] == String.class) {
                    // Handle String parameter
                } else if (paramTypes[i] == Integer.class) {
                    paramTypes[i] = int.class;
                } else if (paramTypes[i] == Boolean.class) {
                    paramTypes[i] = boolean.class;
                }
            }

            java.lang.reflect.Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            // Try with no parameters if the above fails
            try {
                java.lang.reflect.Method method = obj.getClass().getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(obj);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}