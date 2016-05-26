package com.ivygames.morskoiboi;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.when;

public class InternetGameScreenTest extends InternetGameScreen_ {

    @Test
    public void WhenBackPressed__SelectGameScreenOpens() {
        showScreen();
        pressBack();
        checkDisplayed(SELECT_GAME_LAYOUT);
    }

    @Test
    public void WhenScreenIsDisplayed__PlayerNameIsShown() {
        when(settings().getPlayerName()).thenReturn("Sagi");
        showScreen();
        onView(withId(R.id.player_name)).check(matches(withText("Sagi")));
    }

    @Test
    public void WhenInvitePlayerPressed__WaitDialogIsDisplayed() {
        showScreen();
        clickOn(inviteButton());
        checkDisplayed(waitDialog());
    }

    @Test
    public void WhenWaitDialogIsDisplayed__PressingBackHasNoEffect() {
        WhenInvitePlayerPressed__WaitDialogIsDisplayed();

        pressBack();

        checkDisplayed(waitDialog());
    }

}
