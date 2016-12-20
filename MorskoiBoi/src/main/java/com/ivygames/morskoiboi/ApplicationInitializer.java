package com.ivygames.morskoiboi;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivygames.battleship.Configuration;
import com.ivygames.battleship.Rules;
import com.ivygames.battleship.RussianRules;
import com.ivygames.battleship.ai.AiPlayerFactory;
import com.ivygames.battleship.ai.AiPlayerFactoryImpl;
import com.ivygames.battleship.ai.RussianBot;
import com.ivygames.battleship.player.PlayerFactory;
import com.ivygames.common.AndroidDevice;
import com.ivygames.common.analytics.ExceptionEvent;
import com.ivygames.common.analytics.ExceptionHandler;
import com.ivygames.common.analytics.GoogleAnalyticsInitializer;
import com.ivygames.common.analytics.WarningEvent;
import com.ivygames.common.googleapi.GoogleApiClientWrapper;
import com.ivygames.common.multiplayer.MultiplayerImpl;
import com.ivygames.morskoiboi.achievement.AchievementsManager;
import com.ivygames.morskoiboi.bluetooth.BluetoothAdapterWrapper;
import com.ivygames.morskoiboi.bluetooth.peer.BluetoothPeer;
import com.ivygames.morskoiboi.progress.ProgressManager;
import com.ivygames.morskoiboi.russian.RussianFleetBitmapsChooser;
import com.ivygames.morskoiboi.russian.RussianScoresCalculator;

import org.acra.ACRA;
import org.commons.logger.Config;
import org.commons.logger.Ln;
import org.commons.logger.Logger;
import org.commons.logger.LoggerImpl;
import org.commons.logger.WarningListener;

import java.util.Random;
import java.util.UUID;

class ApplicationInitializer {
    // Unique UUID for this application
    static final UUID MY_UUID = UUID.fromString("9ecd276e-c044-43ea-969e-2ed67fc9f633");

    private static final String ANALYTICS_KEY = "UA-43473473-1";

    public static void initialize(@NonNull Application application) {
        Log.v("Battleship", "initializing application...");
        ACRA.init(application);
        initLogger(application);

        GoogleAnalyticsInitializer.initAnalytics(application, ANALYTICS_KEY);

        GameSettings settings = new GameSettings(application);
        Resources resources = application.getResources();
        Rules rules = new RussianRules();
        Random random = new Random();
        AndroidDevice device = new AndroidDevice(application);

        GoogleApiClientWrapper apiClient = new GoogleApiClientWrapper(application,
                BattleshipActivity.RC_SIGN_IN, application.getString(R.string.error),
                BattleshipActivity.SERVICE_RESOLVE);
        ProgressManager progressManager = new ProgressManager(apiClient, settings);
        AchievementsManager achievementsManager = new AchievementsManager(apiClient, settings);
        MultiplayerImpl multiplayerManager = new MultiplayerImpl(apiClient,
                BattleshipActivity.RC_WAITING_ROOM);
        PlayerFactory playerFactory = new PlayerFactory();
        AiPlayerFactory aiPlayerFactory = new AiPlayerFactoryImpl(new RussianBot(random), random);
        ScoresCalculator scoresCalculator = new RussianScoresCalculator();
        BluetoothAdapterWrapper bluetoothAdapter = new BluetoothAdapterWrapper(BluetoothAdapter.getDefaultAdapter());
        BluetoothPeer bluetooth = new BluetoothPeer(bluetoothAdapter, MY_UUID);

        Dependencies.inject(random);
        Dependencies.inject(apiClient);
        Dependencies.inject(multiplayerManager);
        Dependencies.inject(rules);
        Dependencies.inject(settings);
        Dependencies.inject(achievementsManager);
        Dependencies.inject(progressManager);
        Dependencies.inject(device);
        Dependencies.inject(playerFactory);
        Dependencies.inject(aiPlayerFactory);
        Dependencies.inject(scoresCalculator);
        Dependencies.inject(bluetooth);

        FleetBitmaps fleetBitmapsChooser = new RussianFleetBitmapsChooser();
        Bitmaps.loadBitmaps(fleetBitmapsChooser, resources);

        Configuration.DEBUG = BuildConfig.DEBUG;

        ExceptionHandler.setDryRun(BuildConfig.DEBUG);
        Ln.v("... application initialization complete");
    }

    private static void initLogger(Application application) {
        int minimumLogLevel = BuildConfig.DEBUG ? Log.VERBOSE : Log.INFO;
        String path = application.getFilesDir().getPath();
        // filesPath = Environment.getExternalStorageDirectory().getPath();
        Config logConfig = new Config(minimumLogLevel, path, "battleship");
        WarningListener warningListener = new WarningListener() {

            @Override
            public void onWaring(String message, int level) {
                if (level == Log.WARN) {
                    WarningEvent.send(message);
                } else {
                    ExceptionEvent.send(message);
                }
            }
        };
        Logger logger = new LoggerImpl(logConfig, warningListener);
        Ln.injectLogger(logger);
    }

}
