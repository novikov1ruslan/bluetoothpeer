package com.ivygames.common.ads;

import android.app.Activity;

import com.ivygames.common.ads.AdProvider;

public class NoAdsAdProvider implements AdProvider {
    @Override
    public void needToShowInterstitialAfterPlay() {

    }

    @Override
    public void showInterstitialAfterPlay() {

    }

    @Override
    public void resume(Activity activity) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}