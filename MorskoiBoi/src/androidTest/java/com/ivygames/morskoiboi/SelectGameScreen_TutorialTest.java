package com.ivygames.morskoiboi;

import android.support.annotation.NonNull;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Test;

import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelectGameScreen_TutorialTest extends SelectGameScreenTest {

    @Test
    public void FirstTime__TutorialShown() {
        setShowTutorial(true);
        showScreen();
        checkDisplayed(tutorial());
    }

    @Test
    public void SecondTime__TutorialNotShown() {
        setShowTutorial(false);
        showScreen();
        checkDoesNotExist(tutorial());
    }

    @Test
    public void PressingHelp__ShowsTutorial() {
        showScreen();
        clickOn(help());
        checkDisplayed(tutorial());
    }

    @Test
    public void WhenTutorialShown_PressingBack__DismissesTutorial() {
        showScreen();
        clickOn(help());
        pressBack();
        checkDoesNotExist(tutorial());
        checkDisplayed(SELECT_GAME_LAYOUT);
    }

    @Test
    public void WhenTutorialShown_PressingGotIt__DismissesTutorial() {
        showScreen();
        clickOn(help());
        clickOn(gotIt());
        checkDoesNotExist(tutorial());
        checkDisplayed(SELECT_GAME_LAYOUT);
    }

    @Test
    public void WhenScreenIsPaused__TutorialDismissed() {
        showScreen();
        clickOn(help());
        pause();
        checkDoesNotExist(tutorial());
    }

    @Test
    public void IfTutorialDismissed__ItIsNotShownAgain() {
        setScreen(newScreen());
        clickOn(help());
        clickOn(gotIt());
        verify(settings(), times(1)).hideProgressHelp();
    }

    @NonNull
    protected Matcher<View> help() {
        return withId(R.id.help_button);
    }

    @NonNull
    protected Matcher<View> tutorial() {
        return withText(R.string.see_ranks);
    }

    protected void setShowTutorial(boolean show) {
        when(settings().showProgressHelp()).thenReturn(show);
    }

    @NonNull
    protected Matcher<View> gotIt() {
        return withId(R.id.got_it_button);
    }
}
