package com.ivygames.morskoiboi.screen.gameplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivygames.battleship.board.Board;
import com.ivygames.battleship.board.Vector2;
import com.ivygames.battleship.ship.Ship;
import com.ivygames.battleship.shot.ShotResult;
import com.ivygames.morskoiboi.R;

import java.util.Collection;

public class GameplayLayoutVertical extends OldHandsetGameplayLayout {

    private FleetBoardView mMyBoardView;
    private EnemyBoardView mEnemyBoardView;
    private FleetView mFleetView;
    private TextView mPlayerNameView;
    private TextView mEnemyNameView;
    private View mChatButton;

    private final Animation mShake;
    private Bitmap mBwBitmap;
    private DigitalTimerView mTimerView;
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
        mTimerView = (DigitalTimerView) findViewById(R.id.timer);

        mSettingBoardText = (TextView) findViewById(R.id.setting_board_notification);
        lock();
    }

    @Override
    public void setSound(boolean on) {
    }

    @Override
    public void setAim(@NonNull Vector2 aim) {
        mEnemyBoardView.setLockAim(aim);
    }

    @Override
    public void removeAim() {
        mEnemyBoardView.removeLockAim();
    }

    @Override
    public void setPlayerName(@NonNull CharSequence name) {
        if (mPlayerNameView != null) {
            mPlayerNameView.setText(name);
        }
    }

    @Override
    public void setEnemyName(@NonNull CharSequence name) {
        if (mEnemyNameView != null) {
            mEnemyNameView.setText(name);
        }
    }

    @Override
    public void allowAdjacentShips() {
        mMyBoardView.allowAdjacentShips();
        mEnemyBoardView.allowAdjacentShips();
    }

    @Override
    public void setPlayerBoard(@NonNull Board board) {
        mMyBoardView.setBoard(board);
    }

    @Override
    public void setEnemyBoard(@NonNull Board board) {
        mEnemyBoardView.setBoard(board);
        invalidate();
    }

    @Override
    public void updateMyWorkingShips(@NonNull Collection<Ship> workingShips) {
        if (mFleetView == null) {
            // TODO; check if these verifications needed
            return;
        }
        mFleetView.setMyShips(workingShips);
    }

    @Override
    public void setShipsSizes(@NonNull int[] shipsSizes) {
        mFleetView.init(shipsSizes);
    }

    @Override
    public void updateEnemyWorkingShips(@NonNull Collection<Ship> workingShips) {
        if (mFleetView == null) {
            return;
        }
        mFleetView.setEnemyShips(workingShips);
    }

    @Override
    public void setShotListener(@NonNull ShotListener listener) {
        mEnemyBoardView.setShotListener(listener);
    }

    @Override
    public void unLock() {
        mEnemyBoardView.unLock();
    }

    @Override
    public boolean isLocked() {
        return mEnemyBoardView.isLocked();
    }

    @Override
    public void lock() {
        mEnemyBoardView.lock();
    }

    /**
     * locks and sets border
     */
    @Override
    public void enemyTurn() {
        lock();
        mEnemyBoardView.hideTurnBorder();
        mMyBoardView.showTurnBorder();
    }

    /**
     * unlocks and sets border
     */
    @Override
    public void playerTurn() {
        unLock();
        mEnemyBoardView.showTurnBorder();
        mMyBoardView.hideTurnBorder();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mMyBoardView.invalidate();
    }

    @Override
    public void shakePlayerBoard() {
        mMyBoardView.startAnimation(mShake);
    }

    @Override
    public void shakeEnemyBoard() {
        mEnemyBoardView.startAnimation(mShake);
    }

    @Override
    public void win() {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(2);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
        gameOver(cf);
    }

    @Override
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

    @Override
    public void setShotResult(@NonNull ShotResult result) {
        mEnemyBoardView.setShotResult(result);
        mEnemyBoardView.invalidate();
    }

    @Override
    public void setCurrentTime(int seconds) {
        mTimerView.setCurrentTime(seconds);
    }

    @Override
    public void setAlarmTime(int alarmTimeSeconds) {
        mTimerView.setAlarmThreshold(alarmTimeSeconds);
    }

    @Override
    public void setLayoutListener(@NonNull GameplayLayoutListener listener) {
        mListener = listener;
    }

    @Override
    public void hideChatButton() {
        mChatButton.setVisibility(GONE);
    }

    @Override
    public void showOpponentSettingBoardNote(@NonNull String message) {
        mSettingBoardText.setText(message);
        mSettingBoardText.setVisibility(VISIBLE);
    }

    @Override
    public void hideOpponentSettingBoardNotification() {
        mSettingBoardText.setVisibility(GONE);
    }
}
