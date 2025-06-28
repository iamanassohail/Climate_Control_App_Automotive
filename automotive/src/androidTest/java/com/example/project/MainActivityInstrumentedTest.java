package com.example.project;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void switchAcOnUpdatesStatus() {
        Espresso.onView(ViewMatchers.withId(R.id.acSwitch)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.statusText)).check(ViewAssertions.matches(
                ViewMatchers.withText(org.hamcrest.core.StringContains.containsString("AC: On"))));
    }

    @Test
    public void autoModeSwitchUpdatesStatus() {
        Espresso.onView(ViewMatchers.withId(R.id.autoModeSwitch)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.statusText)).check(ViewAssertions.matches(
                ViewMatchers.withText(org.hamcrest.core.StringContains.containsString("Auto Mode: Enabled"))));
    }

    @Test
    public void defrostSwitchUpdatesStatus() {
        Espresso.onView(ViewMatchers.withId(R.id.defrostSwitch)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.statusText)).check(ViewAssertions.matches(
                ViewMatchers.withText(org.hamcrest.core.StringContains.containsString("Defrost: Active"))));
    }
}