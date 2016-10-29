package com.ivygames.morskoiboi.scenario;

import android.support.annotation.NonNull;
import android.view.View;

import com.ivygames.morskoiboi.Placement;
import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.Rules;
import com.ivygames.morskoiboi.ScreenUtils;
import com.ivygames.morskoiboi.model.Vector2;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

class Shooter {

    @NonNull
    private final List<Vector2> mShots;
    private final View mView;
    private final int mPadding;
    private int mCurShot;

    Shooter(Rules rules, Placement placement, View view, int padding) {
        mPadding = padding;
        mShots = Utils.getShots(rules, placement);
        mView = view;
    }

    public void shoot() {
        MyProcessor processor = new MyProcessor(10, mPadding);
        processor.measure(mView.getWidth(), mView.getHeight(), mView.getPaddingLeft(), mView.getPaddingTop());

        Vector2 vector2 = mShots.get(mCurShot);
        mCurShot++;
        int x = processor.getX(vector2.getX());
        int y = processor.getY(vector2.getY());
        onView(withId(R.id.enemy_board)).perform(ScreenUtils.clickXY(x, y));
    }

    public boolean hasShots() {
        return mCurShot < mShots.size();
    }

}