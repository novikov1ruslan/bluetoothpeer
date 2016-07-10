package com.ivygames.morskoiboi.screen.help;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.ivygames.common.ui.BackPressListener;
import com.ivygames.morskoiboi.BattleshipActivity;
import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.screen.BattleshipScreen;
import com.ivygames.morskoiboi.screen.ScreenCreator;

import org.commons.logger.Ln;

public class HelpScreen extends BattleshipScreen implements BackPressListener {
    private static final String TAG = "HELP";
    private View mLayout;

    public HelpScreen(BattleshipActivity parent) {
        super(parent);
    }

    @Override
    public View onCreateView(@NonNull ViewGroup container) {
        mLayout = inflate(R.layout.help, container);
        Ln.d(this + " screen created");
        return mLayout;
    }

    @NonNull
    @Override
    public View getView() {
        return mLayout;
    }

    @Override
    public void onBackPressed() {
        setScreen(ScreenCreator.newMainScreen());
    }

    @Override
    public int getMusic() {
        return R.raw.intro_music;
    }

    @Override
    public String toString() {
        return TAG + debugSuffix();
    }

}
