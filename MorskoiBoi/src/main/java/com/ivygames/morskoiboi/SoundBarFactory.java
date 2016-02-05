package com.ivygames.morskoiboi;

import android.content.res.AssetManager;

import org.commons.logger.Ln;

import java.io.IOException;

public class SoundBarFactory {
    public static SoundBar create(AssetManager assets, String soundName) {
        if (GameSettings.get().isSoundOn()) {
            try {
                return new SoundBarImpl(assets.openFd(soundName));
            } catch (IOException ioe) {
                Ln.w(ioe);
            }
        }

        return new NullSoundBar();
    }

    private static class NullSoundBar implements SoundBar {
        @Override
        public void play() {
            Ln.i("dummy");
        }

        @Override
        public void release() {
            Ln.i("dummy");
        }

        @Override
        public void resume() {
            Ln.i("dummy");
        }

        @Override
        public void pause() {
            Ln.i("dummy");
        }
    }
}