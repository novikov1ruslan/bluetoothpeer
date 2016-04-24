package com.ivygames.morskoiboi;

import com.ivygames.morskoiboi.rt.InvitationEvent;
import com.ivygames.morskoiboi.screen.main.MainScreen;
import com.ivygames.morskoiboi.screen.view.InvitationButton;

import org.junit.Test;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class MainScreen_InvitationTest extends MainScreenTest {

    @Test
    public void WhenThereIsInvitation__EnvelopeIsShown() {
        setInvitation(true);
        showScreen();
        checkHasInvitation(true);
    }

    @Test
    public void WhenThereAreNoInvitations__EnvelopeIsHidden() {
        setInvitation(false);
        showScreen();
        checkHasInvitation(false);
    }

    public void WhenInvitationArrives__EnvelopeIsShown() {
//        setInvitation(false);
//        showScreen();
//        ((MainScreen)screen()).onEventMainThread(new InvitationEvent(null));
//        checkHasInvitation(true);

        // TODO:
    }

    private void setInvitation(boolean hasInvitation) {
        when(invitationManager().hasInvitation()).thenReturn(hasInvitation);
    }

    private void checkHasInvitation(boolean hasInvitation) {
        InvitationButton button = (InvitationButton) viewById(R.id.play);
        assertThat(button.hasInvitation(), is(hasInvitation));
    }
}