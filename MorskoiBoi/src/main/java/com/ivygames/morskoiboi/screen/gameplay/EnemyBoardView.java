package com.ivygames.morskoiboi.screen.gameplay;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.model.Board;
import com.ivygames.morskoiboi.model.PokeResult;
import com.ivygames.morskoiboi.model.Vector2;
import com.ivygames.morskoiboi.screen.view.Aiming;
import com.ivygames.morskoiboi.screen.view.BaseBoardView;

public class EnemyBoardView extends BaseBoardView {

    private PokeResult mLastShotResult;
    private Vector2 mAim;

    public EnemyBoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @NonNull
    @Override
    protected EnemyBoardPresenter presenter() {
        if (mPresenter == null) {
            mPresenter = new EnemyBoardPresenter(10, getResources().getDimension(R.dimen.ship_border));
        }
        return (EnemyBoardPresenter) mPresenter;
    }

    @NonNull
    @Override
    protected EnemyBoardRenderer renderer() {
        if (mRenderer == null) {
            mRenderer = new EnemyBoardRenderer(presenter(), getResources());
        }
        return (EnemyBoardRenderer) mRenderer;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        renderer().init(availableMemory());
    }

    private long availableMemory() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        renderer().release();
    }

    public void setShotListener(@NonNull ShotListener shotListener) {
        presenter().setShotListener(shotListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        renderer().drawNautical(canvas);
        super.onDraw(canvas);

        if (mAim != null) {
            renderer().drawAim(canvas, presenter().getAimRectDst(mAim));
        }

        if (presenter().startedDragging()) {
            int i = presenter().getTouchedI();
            int j = presenter().getTouchedJ();

            if (Board.containsCell(i, j)) {
                Aiming aiming = presenter().getAiming(i, j, 1, 1);
                renderer().drawAiming(canvas, aiming, isLocked(mBoard.getCell(i, j).beenShot()));
            }
        }

        if (renderer().isAnimationRunning()) {
            postInvalidateDelayed(renderer().animateExplosions(canvas, mLastShotResult.aim));
        }

//        if (GameConstants.IS_TEST_MODE) {
//            getRenderer().render(canvas, mTouchState.getX(), mTouchState.getY());
//        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        presenter().touch(event);
        invalidate();

        return true;
    }

    private boolean isLocked(boolean isShot) {
        return isShot || presenter().isLocked();
    }

    public void setAim(@NonNull Vector2 aim) {
        mAim = aim;
        invalidate();
    }

    public void removeAim() {
        mAim = null;
        invalidate();
    }

    public boolean isLocked() {
        return presenter().isLocked();
    }

    public void lock() {
        presenter().lock();
    }

    public void unLock() {
        presenter().unlock();
    }

    public void setShotResult(@NonNull PokeResult result) {
        mLastShotResult = result;
        renderer().startAnimation(result);
    }
}
