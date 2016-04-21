package com.ivygames.morskoiboi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.ivygames.common.billing.PurchaseManager;
import com.ivygames.common.billing.PurchaseStatusListener;
import com.ivygames.morskoiboi.achievement.AchievementsManager;
import com.ivygames.morskoiboi.invitations.InvitationManager;
import com.ivygames.morskoiboi.invitations.InvitationReceivedListener;
import com.ivygames.morskoiboi.model.ChatMessage;
import com.ivygames.morskoiboi.model.Game;
import com.ivygames.morskoiboi.model.Model;
import com.ivygames.morskoiboi.progress.ProgressManager;
import com.ivygames.morskoiboi.screen.BattleshipScreen;
import com.ivygames.morskoiboi.utils.UiUtils;
import com.ruslan.fragmentdialog.FragmentAlertDialog;

import org.commons.logger.Ln;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class BattleshipActivity extends Activity implements ConnectionCallbacks {

    public static final int RC_SELECT_PLAYERS = 10000;
    public static final int RC_INVITATION_INBOX = 10001;
    public final static int RC_WAITING_ROOM = 10002;
    public static final int RC_ENSURE_DISCOVERABLE = 3;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    public static final int RC_UNUSED = 0;
    public static final int PLUS_ONE_REQUEST_CODE = 20001;
    public static final int RC_ENABLE_BT = 2;
    public static final int RC_PURCHASE = 10003;

    private static final int SERVICE_RESOLVE = 9002;

    private static final Configuration CONFIGURATION_LONG = new Configuration.Builder().setDuration(Configuration.DURATION_LONG).build();

    private final PurchaseManager mPurchaseManager = new PurchaseManager(this);

    private boolean mRecreating;

    private final GameSettings mSettings = Dependencies.getSettings();

    /**
     * volume stream is saved on onResume and restored on onPause
     */
    private int mVolumeControlStream;

    @NonNull
    private final GoogleApiClientWrapper mGoogleApiClient = Dependencies.getApiClient();

    @NonNull
    private final AchievementsManager mAchievementsManager = Dependencies.getAchievementsManager();

    @NonNull
    private final InvitationManager mInvitationManager = Dependencies.getInvitationManager();

    @NonNull
    private final ProgressManager mProgressManager = Dependencies.getProgressManager();

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure;

    private MusicPlayer mMusicPlayer;
    private View mBanner;

    private ScreenManager mScreenManager;

    private BattleshipScreen mCurrentScreen;

    @NonNull
    private final OnConnectionFailedListener mConnectionFailedListener = new OnConnectionFailedListenerImpl();
    private ViewGroup mLayout;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecreating = savedInstanceState != null;
        if (mRecreating) {
            Ln.i("app is recreating, restart it");
            finish();
            return;
        }

        AndroidDevice device = Dependencies.getDevice();

        if (device.isTablet()) {
            Ln.d("device is tablet");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            Ln.d("device is handset");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mMusicPlayer = MusicPlayer.create(this, R.raw.intro_music);

        Ln.d("google play services available = " + device.isGoogleServicesAvailable());

        mLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.battleship, null);
        setContentView(mLayout);
        mScreenManager = new ScreenManager((ViewGroup) mLayout.findViewById(R.id.container));
        mBanner = mLayout.findViewById(R.id.banner);

        mGoogleApiClient.setConnectionCallbacks(this);
        mGoogleApiClient.setOnConnectionFailedListener(mConnectionFailedListener);
        if (mSettings.shouldAutoSignIn()) {
            Ln.d("should auto-signin - connecting...");
            mGoogleApiClient.connect();
        }

        mInvitationManager.setInvitationReceivedListener(new InvitationReceivedListener() {
            @Override
            public void onInvitationReceived(String displayName) {
                showInvitationCrouton(getString(R.string.received_invitation, displayName));
            }
        });

        if (mSettings.noAds()) {
            hideAds();
        } else {
            AdProviderFactory.init(this);
            if (device.isBillingAvailable()) {
                mPurchaseManager.query(new PurchaseStatusListenerImpl());
            } else {
                Ln.e("gpgs_not_available");
                hideNoAdsButton();
            }
        }

//        FacebookSdk.sdkInitialize(getApplicationContext());
        Ln.i("game fully created");

        GameHandler.setActivity(this);
        GameHandler.setApiClient(mGoogleApiClient);
        GameHandler.setSettings(mSettings);

        setScreen(GameHandler.newMainScreen());
    }

    public void showChatCrouton(ChatMessage message) {
        if (mScreenManager.isStarted()) {
            View layout = UiUtils.inflateChatCroutonLayout(getLayoutInflater(), message.getText(), mLayout);
            Crouton.make(this, layout).setConfiguration(CONFIGURATION_LONG).show();
        }
    }

    public void showInvitationCrouton(String message) {
        View view = UiUtils.inflateInfoCroutonLayout(getLayoutInflater(), message, mLayout);
        Crouton.make(this, view).setConfiguration(CONFIGURATION_LONG).show();
    }

    public void playMusic(int music) {
        mMusicPlayer.play(music);
    }

    public void stopMusic() {
        mMusicPlayer.stop();
    }

    private void hideNoAdsButton() {
        mScreenManager.hideNoAdsButton();
    }

    private void hideAds() {
        AdProviderFactory.getAdProvider().destroy();
        AdProviderFactory.noAds();
        mBanner.setVisibility(View.GONE);
        hideNoAdsButton();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AndroidDevice.printIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mRecreating) {
            Ln.v("recreating");
            return;
        }
        Ln.d("UI partially visible - keep screen On");
        keepScreenOn();

        mScreenManager.onStart();

        if (mGoogleApiClient.isConnected()) {
            Ln.d("API is connected - register invitation listener");
            mInvitationManager.loadInvitations();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecreating) {
            Ln.v("recreating");
            return;
        }

        Ln.v("resuming");
        mScreenManager.onResume();

        mVolumeControlStream = getVolumeControlStream();

        // Set the hardware buttons to control the music
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AdProviderFactory.getAdProvider().resume(this);

        mMusicPlayer.play(mCurrentScreen.getMusic());
//        AppEventsLogger.activateApp(this); // #FB
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRecreating) {
            Ln.v("recreating");
            return;
        }
        Ln.v("pausing");
        setVolumeControlStream(mVolumeControlStream);
        AdProviderFactory.getAdProvider().pause();

        mScreenManager.onPause();

        mMusicPlayer.pause();
//        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecreating) {
            Ln.v("recreating");
            return;
        }
        stopKeepingScreenOn();

        mScreenManager.onStop();
        if (mGoogleApiClient.isConnected()) {
            Ln.d("API is connected - unregister invitation listener");
            mGoogleApiClient.unregisterInvitationListener();
        }
        EventBus.getDefault().unregister(this);
        Ln.d("game fully obscured - stop keeping screen On");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecreating) {
            Ln.d("destroyed during recreation - restart");
            mRecreating = false;
            startActivity(getIntent());
            return;
        }

        mScreenManager.onDestroy();
        if (Model.instance != null && Model.instance.game != null) {
            Game game = Model.instance.game;
            if (!game.hasFinished()) {
                Ln.e("application destroyed while game is on");
                game.finish();
            }
        }

        // screens will cancel all their croutons, but activity has its own
        Crouton.cancelAllCroutons();
        AdProviderFactory.getAdProvider().destroy();

        mPurchaseManager.destroy();

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(mConnectionFailedListener);
        mGoogleApiClient.disconnect();

        mMusicPlayer.release();
        Ln.d("game destroyed");
    }

    public void dismissTutorial() {
        mScreenManager.dismissTutorial();
    }

    public void showTutorial(@Nullable View view) {
        mScreenManager.showTutorial(view);
    }

    @Override
    public void onBackPressed() {
        if (mScreenManager.handleBackPress()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mRecreating) {
            Ln.w("activity result received while recreating - ignore");
            return;
        }

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Ln.d("connection issue is resolved - reconnecting");
                mGoogleApiClient.connect();
            } else {
                Ln.w("connection issue could not be resolved");
                mResolvingConnectionFailure = false;
            }
        } else if (requestCode == RC_PURCHASE) {
            mPurchaseManager.handleActivityResult(requestCode, resultCode, data);
        } else {
            mCurrentScreen.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Ln.d("connection suspended - trying to reconnect: " + GpgsUtils.connectionCauseToString(cause));
        // GoogleApiClient will automatically attempt to restore the connection.
        // Applications should disable UI components that require the service,
        // and wait for a call to onConnected(Bundle) to re-enable them.
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Ln.d("signed in");
        mResolvingConnectionFailure = false;
        mSettings.enableAutoSignIn();

        if (TextUtils.isEmpty(mSettings.getPlayerName())) {
            String name = mGoogleApiClient.getDisplayName();
            Ln.i("player's name is not set - setting to G+ name [" + name + "]");
            mSettings.setPlayerName(name);
        }

        mAchievementsManager.loadAchievements();
        mProgressManager.loadProgress();

        mScreenManager.onSignInSucceeded();

        if (mScreenManager.isStarted()) {
            Ln.d("started - load invitations");
            mInvitationManager.loadInvitations();
        }
    }

    /**
     * Sets the flag to keep this screen on. It's recommended to do that during the handshake when setting up a game, because if the screen turns off, the game
     * will be cancelled.
     */
    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Clears the flag that keeps the screen on.
     */
    private void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onEventMainThread(ChatMessage message) {
        showChatCrouton(message);
    }

    public final void setScreen(@NonNull BattleshipScreen screen) {
        if (mCurrentScreen != null) {
            if (mCurrentScreen.getMusic() != screen.getMusic()) {
                mMusicPlayer.stop();
            }
        }
        mCurrentScreen = screen;

        mScreenManager.setScreen(screen);
        mMusicPlayer.play(screen.getMusic());
    }

    public void purchase() {
        mPurchaseManager.purchase(BattleshipActivity.RC_PURCHASE, new PurchaseStatusListenerImpl());
    }

    private class PurchaseStatusListenerImpl implements PurchaseStatusListener {
        @Override
        public void onPurchaseFailed() {
            FragmentAlertDialog.showNote(getFragmentManager(), FragmentAlertDialog.TAG, R.string.purchase_error);
        }

        @Override
        public void onHasNoAds() {
            Ln.d("Purchase is premium upgrade. Congratulating user.");
            mSettings.setNoAds();
            hideAds();
        }
    }

    private class OnConnectionFailedListenerImpl implements OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult result) {
            Ln.d("connection failed - result: " + result);

            switch (result.getErrorCode()) {
                case ConnectionResult.SERVICE_MISSING:
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                case ConnectionResult.SERVICE_DISABLED:
                    Ln.w("connection failed: " + result.getErrorCode());
                    Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(BattleshipActivity.this, result.getErrorCode(), SERVICE_RESOLVE);
                    errorDialog.show();
                    return;
            }

            if (mResolvingConnectionFailure) {
                Ln.d("ignoring connection failure; already resolving.");
                return;
            }

            Ln.d("resolving connection failure");
            mResolvingConnectionFailure = mGoogleApiClient.resolveConnectionFailure(BattleshipActivity.this, result, RC_SIGN_IN, getString(R.string.error));
            Ln.d("has resolution = " + mResolvingConnectionFailure);
        }
    }
}
