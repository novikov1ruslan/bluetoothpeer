package com.ivygames.morskoiboi.bluetooth;

import android.support.annotation.NonNull;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.ScreenTest;
import com.ivygames.morskoiboi.screen.bluetooth.BluetoothScreen;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;


public class BluetoothScreenTest extends ScreenTest {

    @Before
    public void setup() {
        super.setup();
    }

    @Override
    public BluetoothScreen newScreen() {
        return new BluetoothScreen(activity);
    }

    @Test
    public void when_back_button_pressed__select_game_screen_opens() {
//        setProgress(100);
//        setScreen(newScreen());
//        doNothing().when(adapter).cancelDiscovery();
//        pressBack();
//        checkDisplayed(SELECT_GAME_LAYOUT);
    }

    @Test
    public void when_join_game_pressed__device_list_screen_displayed() {
//        setScreen(newScreen());
//        onView(joinGameButton()).perform(click());
//        checkDisplayed(DEVICE_LIST_LAYOUT);
    }

    @Test
    public void when_create_game_pressed__layout_disabled() {
//        setScreen(newScreen());
//        onView(withId(R.id.create_game_btn)).perform(click());
//        onView(joinGameButton()).perform(click());
//        onView(DEVICE_LIST_LAYOUT).check(matches(is(not(isDisplayed()))));
    }

    @NonNull
    private Matcher<View> joinGameButton() {
        return ViewMatchers.withId(R.id.join_game_btn);
    }

}
