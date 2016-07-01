package com.ivygames.morskoiboi.main;

import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.screen.view.InvitationButton;

import org.junit.Test;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;

public class MainScreen_InvitationTest extends MainScreen_ {

    @Test
    public void WhenThereIsInvitation__EnvelopeIsShown() {
        setInvitations(INVITATIONS);
        showScreen();
        checkHasInvitation(true);
    }

    @Test
    public void WhenThereAreNoInvitations__EnvelopeIsHidden() {
        setInvitations(NO_INVITATIONS);
        showScreen();
        checkHasInvitation(false);
    }

    @Test
    public void WhenInvitationArrives__EnvelopeIsShown() {
        setInvitations(NO_INVITATIONS);
        showScreen();
        sendInvitation("Sagi", "test_id");
        checkHasInvitation(true);
    }

    private void checkHasInvitation(boolean hasInvitation) {
        InvitationButton button = (InvitationButton) viewById(R.id.play);
        assertThat(button.hasInvitation(), is(hasInvitation));
    }
}
