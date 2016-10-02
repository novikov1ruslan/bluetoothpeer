package com.ivygames.morskoiboi.main;

import android.support.annotation.NonNull;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.ScreenTest;

import org.hamcrest.Matcher;
import org.junit.Test;

import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.never;

public class MainScreen_AchievementsTest extends MainScreen_ {

    @NonNull
    protected static Matcher<View> achievementsButton() {
        return ViewMatchers.withId(R.id.achievements_button);
    }

    @NonNull
    protected static Matcher<View> achievementsDialog() {
        return withText(R.string.achievements_request);
    }

    @Test
    public void WhenAchievementsButtonPressedWhenNonConnected__SignInDialogDisplayed() {
        setSignedIn(false);
        setScreen(newScreen());
        ScreenTest.clickOn(achievementsButton());
        checkDisplayed(achievementsDialog());
    }

    @Test
    public void WhenSignInButtonPressedForAchievementsDialog__Connected() {
        WhenAchievementsButtonPressedWhenNonConnected__SignInDialogDisplayed();
        ScreenTest.clickOn(signInButton());
        verifyConnected();
    }

    @Test
    public void WhenCancelPressedForAchievementsDialog__NotConnectedAndDialogDismissed() {
        WhenAchievementsButtonPressedWhenNonConnected__SignInDialogDisplayed();
        ScreenTest.clickOn(cancelButton());
        verifyConnected(never());
        checkDoesNotExist(achievementsDialog());
    }

    @Test
    public void WhenBackPressedForAchievementsDialog__DialogDismissed() {
        WhenAchievementsButtonPressedWhenNonConnected__SignInDialogDisplayed();
        pressBack();
        checkDoesNotExist(achievementsDialog());
    }

    @Test
    public void WhenAchievementsButtonPressedWhenConnected__AchievementsIntentIsFired() {
        setSignedIn(true);
        showScreen();

        clickOn(achievementsButton());

        verifyAchievementsShown();
    }

}
