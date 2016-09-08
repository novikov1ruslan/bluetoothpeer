package com.ivygames.morskoiboi;

import android.app.Activity;

import com.ivygames.common.ads.AdProvider;
import com.ivygames.common.ads.NoAdsAdProvider;

public class AdProviderFactory {
    private static AdProvider sAdProvider = new NoAdsAdProvider();

    static void init(Activity activity) {
        sAdProvider = new AdmobAdProvider(activity);
    }

    static void noAds() {
        sAdProvider = new NoAdsAdProvider();
    }

    public static AdProvider getAdProvider() {
        return sAdProvider;
    }
}
