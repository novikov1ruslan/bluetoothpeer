package com.ivygames.morskoiboi.screen.boardsetup;

import android.support.annotation.NonNull;
import android.view.View;

class PickShipTask implements Runnable {

    private final int mTouchX;
    private final int mTouchY;
    @NonNull
    private final View.OnLongClickListener mListener;

    public PickShipTask(int x, int y, @NonNull View.OnLongClickListener listener) {
        mTouchX = x;
        mTouchY = y;
        mListener = listener;
    }

    @Override
    public void run() {
        mListener.onLongClick(null);
    }

    public boolean hasMovedBeyondSlope(int x, int y, int slop) {
        int dX = mTouchX - x;
        int dY = mTouchY - y;
        return Math.sqrt(dX * dX + dY * dY) > slop;
    }

    @Override
    public String toString() {
        return "PressTask [x=" + mTouchX + ",yj=" + mTouchY + "]#" + (hashCode() % 1000);
    }
}
