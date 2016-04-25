package com.ivygames.morskoiboi.screen.gameplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.model.Board;
import com.ivygames.morskoiboi.model.PokeResult;
import com.ivygames.morskoiboi.model.Ship;
import com.ivygames.morskoiboi.model.Vector2;

import org.commons.logger.Ln;

import java.util.Collection;

public class GameplayLayoutVertical extends OldHandsetGameplayLayout {

    private FleetBoardView mMyBoardView;
    private EnemyBoardView mEnemyBoardView;
    private FleetView mFleetView;
    private TextView mPlayerNameView;
    private TextView mEnemyNameView;
    private View mChatButton;

    private final Animation mShake;
    private long mStartTime;
    private long mUnlockedTime;
    private boolean mGameIsOn;
    private Bitmap mBwBitmap;
    private TimerViewInterface mTimerView;
    private GameplayLayoutListener mListener;
    private TextView mSettingBoardText;

    public GameplayLayoutVertical(Context context, AttributeSet attrs) {
        super(context, attrs);

        mShake = AnimationUtils.loadAnimation(context, R.anim.shake);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMyBoardView = (FleetBoardView) findViewById(R.id.board_view_fleet);
        mEnemyBoardView = (EnemyBoardView) findViewById(R.id.board_view_enemy);
        mFleetView = (FleetView) findViewById(R.id.status);
        mChatButton = findViewById(R.id.chat_button);
        mChatButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onChatClicked();
                }
            }
        });
        mPlayerNameView = (TextView) findViewById(R.id.player);
        mEnemyNameView = (TextView) findViewById(R.id.enemy);
        mTimerView = (TimerViewInterface) findViewById(R.id.timer);

        mSettingBoardText = (TextView) findViewById(R.id.setting_board_notification);
    }

    public void setSound(boolean on) {
    }

    public void setAim(@NonNull Vector2 aim) {
        mEnemyBoardView.setAim(aim);
    }

    public void removeAim() {
        mEnemyBoardView.removeAim();
    }

    public long getUnlockedTime() {
        return mUnlockedTime;
    }

    public void setPlayerName(@NonNull CharSequence name) {
        if (mPlayerNameView != null) {
            mPlayerNameView.setText(name);
        }
    }

    public void setEnemyName(@NonNull CharSequence name) {
        if (mEnemyNameView != null) {
            mEnemyNameView.setText(name);
        }
    }

    public void setPlayerBoard(@NonNull Board board) {
        mMyBoardView.setBoard(board);
    }

    public void setEnemyBoard(@NonNull Board board) {
        mEnemyBoardView.setBoard(board);
        invalidate();
    }

    public void updateMyWorkingShips(@NonNull Collection<Ship> workingShips) {
        if (mFleetView == null) {
            // TODO; check if these verifications needed
            return;
        }
        mFleetView.setMyShips(workingShips);
    }

    @Override
    public void setShipsSizes(@NonNull int[] shipsSizes) {
        mFleetView.setShipsSizes(shipsSizes);
    }

    public void updateEnemyWorkingShips(@NonNull Collection<Ship> workingShips) {
        if (mFleetView == null) {
            return;
        }
        mFleetView.setEnemyShips(workingShips);
    }

    public void setShotListener(@NonNull ShotListener listener) {
        mEnemyBoardView.setShotListener(listener);
    }

    public void unLock() {
        mGameIsOn = true;
        mEnemyBoardView.unLock();
        mStartTime = SystemClock.elapsedRealtime();
    }

    public boolean isLocked() {
        return mEnemyBoardView.isLocked();
    }

    public void lock() {
        mEnemyBoardView.lock();

        // game is on after the first unlock
        if (!mGameIsOn) {
            return;
        }

        long d = SystemClock.elapsedRealtime() - mStartTime;
        mUnlockedTime += d;
        Ln.v("d = " + d + ", mUnlockedTime=" + mUnlockedTime);
    }

    /**
     * locks and sets border
     */
    public void enemyTurn() {
        lock();
        mEnemyBoardView.hideTurnBorder();
        mMyBoardView.showTurnBorder();
    }

    /**
     * unlocks and sets border
     */
    public void playerTurn() {
        unLock();
        mEnemyBoardView.showTurnBorder();
        mMyBoardView.hideTurnBorder();
    }

    public void invalidateEnemyBoard() {
        mEnemyBoardView.invalidate();
    }

    public void invalidatePlayerBoard() {
        mMyBoardView.invalidate();
    }

    public void shakePlayerBoard() {
        mMyBoardView.startAnimation(mShake);
    }

    public void shakeEnemyBoard() {
        mEnemyBoardView.startAnimation(mShake);
    }

    public void win() {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(2);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
        gameOver(cf);
    }

    public void lost() {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
        gameOver(cf);
    }

    private void gameOver(ColorMatrixColorFilter cf) {
        Bitmap bitmap = createScreenBitmap();
        if (bitmap != null) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.setVisibility(GONE);
            }

            // ColorMatrix cm1 = new ColorMatrix();
            // cm1.set(new float[] {
            // 1.5f, 1.5f, 1.5f, 0, 0,
            // 1.5f, 1.5f, 1.5f, 0, 0,
            // 1.5f, 1.5f, 1.5f, 0, 0,
            // -1f, -1f, -1f, 0f, 1f});

            ImageView bw = (ImageView) findViewById(R.id.bw);
            bw.setColorFilter(cf);
            bw.setImageBitmap(bitmap);

            bw.setVisibility(VISIBLE);
            bw.startAnimation(mShake);
        }
    }

    private Bitmap createScreenBitmap() {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap drawingCache = getDrawingCache();
        Bitmap bitmap = null;
        if (drawingCache != null) {
            /*
             * [main] java.lang.NullPointerException at android.graphics.Bitmap.createBitmap(Bitmap.java:455) at com.ivygames
			 * .morskoiboi.ui.view.GameplayLayout.createBwBitmap(GameplayLayout .java:242)
			 */
            bitmap = Bitmap.createBitmap(drawingCache);
        }
        setDrawingCacheEnabled(false);

        return bitmap;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBwBitmap != null) {
            mBwBitmap.recycle();
            mBwBitmap = null;
        }
    }

    public void setShotResult(@NonNull PokeResult result) {
        mEnemyBoardView.setShotResult(result);
    }

    @Override
    public void setCurrentTime(int seconds) {
        mTimerView.setCurrentTime(seconds);
    }

    public void setAlarmTime(int alarmTimeSeconds) {
        mTimerView.setAlarmThreshold(alarmTimeSeconds);
    }

    public void setListener(@NonNull GameplayLayoutListener listener) {
        mListener = listener;
    }

    public void hideChatButton() {
        mChatButton.setVisibility(GONE);
    }

    public void showOpponentSettingBoardNotification(@NonNull String message) {
        mSettingBoardText.setText(message);
        mSettingBoardText.setVisibility(VISIBLE);
    }

    public void hideOpponentSettingBoardNotification() {
        mSettingBoardText.setVisibility(GONE);
    }
}
