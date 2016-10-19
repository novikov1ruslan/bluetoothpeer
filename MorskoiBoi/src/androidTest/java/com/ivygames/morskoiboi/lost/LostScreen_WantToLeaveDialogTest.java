package com.ivygames.morskoiboi.lost;

import com.ivygames.morskoiboi.model.Game;

import org.junit.Test;

import static android.support.test.espresso.Espresso.pressBack;
import static com.ivygames.morskoiboi.ScreenUtils.*;

public class LostScreen_WantToLeaveDialogTest extends LostScreenTest {

    @Test
    public void AfterNoPressedForNonAndroid__WantToLeaveDialogDisplayed() {
        setGameType(Game.Type.BLUETOOTH);
        showScreen();
        clickOn(noButton());
        checkDisplayed(wantToLeaveDialog());
    }

    @Test
    public void WhenBackButtonPressedForNonAndroid__WantToLeaveDialogShown() {
        setGameType(Game.Type.BLUETOOTH);
        showScreen();
        pressBack();
        checkDisplayed(wantToLeaveDialog());
    }

    @Test
    public void PressingOkOnWantToLeaveDialog__SelectGameScreenDisplayed() {
        WhenBackButtonPressedForNonAndroid__WantToLeaveDialogShown();
        clickOn(okButton());
        backToSelectGame();
    }

    @Test
    public void PressingCancelOnWantToLeaveDialog__RemovesDialog() {
        WhenBackButtonPressedForNonAndroid__WantToLeaveDialogShown();
        clickOn(cancelButton());
        checkDoesNotExist(wantToLeaveDialog());
        checkDisplayed(lostScreen());
    }

    @Test
    public void PressingBackOnWantToLeaveDialog__RemovesDialog() {
        WhenBackButtonPressedForNonAndroid__WantToLeaveDialogShown();
        pressBack();
        checkDoesNotExist(wantToLeaveDialog());
        checkDisplayed(lostScreen());
    }

}
