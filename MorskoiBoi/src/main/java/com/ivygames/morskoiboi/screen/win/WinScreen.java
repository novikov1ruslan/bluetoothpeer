package com.ivygames.morskoiboi.screen.win;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.gms.games.Player;
import com.ivygames.common.analytics.AnalyticsEvent;
import com.ivygames.common.analytics.UiEvent;
import com.ivygames.morskoiboi.BackPressListener;
import com.ivygames.morskoiboi.BattleshipActivity;
import com.ivygames.morskoiboi.Dependencies;
import com.ivygames.morskoiboi.GameConstants;
import com.ivygames.morskoiboi.GameHandler;
import com.ivygames.morskoiboi.GameSettings;
import com.ivygames.morskoiboi.GoogleApiClientWrapper;
import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.Rules;
import com.ivygames.morskoiboi.RulesFactory;
import com.ivygames.morskoiboi.SignInListener;
import com.ivygames.morskoiboi.SoundBar;
import com.ivygames.morskoiboi.SoundBarFactory;
import com.ivygames.morskoiboi.achievement.AchievementsManager;
import com.ivygames.morskoiboi.bluetooth.BluetoothGame;
import com.ivygames.morskoiboi.model.Game;
import com.ivygames.morskoiboi.model.Game.Type;
import com.ivygames.morskoiboi.model.Model;
import com.ivygames.morskoiboi.model.Ship;
import com.ivygames.morskoiboi.progress.ProgressManager;
import com.ivygames.morskoiboi.rt.InternetGame;
import com.ivygames.morskoiboi.screen.BackToSelectGameCommand;
import com.ivygames.morskoiboi.screen.DialogUtils;
import com.ivygames.morskoiboi.screen.OnlineGameScreen;
import com.ivygames.morskoiboi.screen.boardsetup.BoardSetupScreen;
import com.ruslan.fragmentdialog.FragmentAlertDialog;

import org.commons.logger.Ln;

import java.util.Collection;

public class WinScreen extends OnlineGameScreen implements BackPressListener, SignInListener {
    private static final String TAG = "WIN";
    private static final String DIALOG = FragmentAlertDialog.TAG;

    private final Game mGame;
    private WinLayoutSmall mLayout;
    private long mTime;

    @NonNull
    private final Collection<Ship> mShips;

    private final int mScores;

    @NonNull
    private final SoundBar mSoundBar;
    @NonNull
    private final GoogleApiClientWrapper mApiClient = Dependencies.getApiClient();
    @NonNull
    private final GameSettings mSettings = Dependencies.getSettings();
    @NonNull
    private final AchievementsManager mAchievementsManager = Dependencies.getAchievementsManager();
    @NonNull
    private final ProgressManager mProgressManager = Dependencies.getProgressManager();
    @NonNull
    private final Rules mRules = RulesFactory.getRules();
    private final boolean mOpponentSurrendered;

    public WinScreen(@NonNull BattleshipActivity parent,
                     @NonNull Collection<Ship> fleet, boolean opponentSurrendered) {
        super(parent);
        mOpponentSurrendered = opponentSurrendered;

        mGame = Model.instance.game;

        AudioManager audioManager = (AudioManager) mParent.getSystemService(Context.AUDIO_SERVICE);
        mSoundBar = SoundBarFactory.create(mParent.getAssets(), "win.ogg", audioManager);
        mSoundBar.play();

        mTime = mGame.getTimeSpent();
        mShips = fleet;
        mScores = mRules.calcTotalScores(mShips, mGame);
        Ln.d("time spent in the game = " + mTime + "; scores = " + mScores + " incrementing played games counter");

        mSettings.incrementGamesPlayedCounter();
        Ln.v("fleet: " + mShips);

        if (mGame.getType() == Type.VS_ANDROID) {
            if (GameConstants.IS_TEST_MODE) {
                Ln.i("game is in test mode - achievements not updated");
            } else {
                mAchievementsManager.processAchievements(mGame, mShips);
            }
        }
        processScores();
    }

    @Override
    public View getView() {
        return mLayout;
    }

    @Override
    public View onCreateView(ViewGroup container) {
        mLayout = (WinLayoutSmall) getLayoutInflater().inflate(R.layout.win, container, false);
        if (mGame.getType() != Type.VS_ANDROID) {
            Ln.d("online game - hiding scores", mGame.getType());
            mLayout.hideScorables();
        }

        mLayout.setYesClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UiEvent.send("continue", "win");
                backToBoardSetup();
            }
        });

        mLayout.setNoClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UiEvent.send("dont_continue", "win");
                doNotContinue();
            }
        });

        // scorables ---
        mLayout.setTime(mTime);
        mLayout.setTotalScore(mScores);
        // scorables ---
        mLayout.setShips(mShips);

        mLayout.setSignInClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UiEvent.send(GameConstants.GA_ACTION_SIGN_IN, "win");
                mApiClient.connect();
            }
        });

        if (mOpponentSurrendered) {
            Ln.d("opponent has surrendered - hiding continue option");
            mLayout.opponentSurrendered();
        }

        Ln.d(this + " screen created");
        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGame.getType() == Type.VS_ANDROID && !mApiClient.isConnected()) {
            Ln.d("game vs Android, but client is not connected - show sign button");
            mLayout.showSignInForAchievements();
        } else {
            mLayout.hideSignInForAchievements();
        }
        mSoundBar.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSoundBar.pause();
    }

    @Override
    public void onSignInSucceeded() {
        Ln.d("sign in succeeded - hiding sign in button");
        mLayout.hideSignInForAchievements();
    }

    private void processScores() {
        int scores = calculateScores(mGame.getType(), mOpponentSurrendered);

        int penalty = mSettings.getProgressPenalty();
        Ln.d("updating player's progress [" + scores + "] for game type: " + mGame.getType() + "; penalty=" + penalty);
        int progressIncrement = scores - penalty;
        if (progressIncrement > 0) {
            mProgressManager.incrementProgress(progressIncrement);
            mSettings.setProgressPenalty(0);
        } else {
            mSettings.setProgressPenalty(-progressIncrement);
        }
    }

    private int calculateScores(Type type, boolean surrendered) {
        int progress = calculateScoresForGameType(type);
        if (surrendered) {
            progress = progress / 2;
        }
        return progress;
    }

    private int calculateScoresForGameType(Type type) {
        int progress;
        if (type == Type.VS_ANDROID) {
            progress = mScores * AchievementsManager.NORMAL_DIFFICULTY_PROGRESS_FACTOR;
        } else if (type == Type.INTERNET) {
            progress = InternetGame.WIN_PROGRESS_POINTS;
        } else {
            progress = BluetoothGame.WIN_PROGRESS_POINTS;
        }
        return progress;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGame.getType() == Type.VS_ANDROID) {
            if (GameConstants.IS_TEST_MODE) {
                Ln.i("game is in the test mode - scores are not submitted");
            } else {
                if (mApiClient.isConnected()) {
                    submitScore(mScores);
                    sendAnalyticsForPlayersScores(mScores);
                } else {
                    Ln.d("### client is not connected - could not submit scores!");
                }
            }
        }

        mSoundBar.release();
        Ln.d(this + " destroyed - sound pool released");
    }

    @Override
    public void onBackPressed() {
        UiEvent.send(GameConstants.GA_ACTION_BACK, "win");
        doNotContinue();
    }

    private void backToBoardSetup() {
        Ln.d("getting back to " + BoardSetupScreen.TAG);
        Model.instance.game.clearState();
        setScreen(GameHandler.newBoardSetupScreen());
    }

    private void showWantToLeaveRoomDialog() {
        String displayName = Model.instance.opponent.getName();
        String message = getString(R.string.want_to_leave_room, displayName);
        DialogUtils.newOkCancelDialog(message, new BackToSelectGameCommand(parent())).show(mFm, DIALOG);
    }

    private void submitScore(int totalScores) {
        Ln.d("submitting scores: " + totalScores);
        mApiClient.submitScore(getString(R.string.leaderboard_normal), totalScores);
    }

    private void sendAnalyticsForPlayersScores(int totalScores) {
        Player currentPlayer = mApiClient.getCurrentPlayer();
        if (currentPlayer != null) {
            String playerName = currentPlayer.getDisplayName();
            String player = String.valueOf(playerName.hashCode());
            AnalyticsEvent.send("scores", player, totalScores);
        }
    }

    private void doNotContinue() {
        if (shouldNotifyOpponent() && !mOpponentSurrendered) {
            showWantToLeaveRoomDialog();
        } else {
            new BackToSelectGameCommand(parent()).run();
        }
    }

    @Override
    public String toString() {
        return TAG + debugSuffix();
    }

}
