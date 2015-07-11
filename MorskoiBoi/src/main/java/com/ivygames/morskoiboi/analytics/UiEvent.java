package com.ivygames.morskoiboi.analytics;

import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.Tracker;

import org.commons.logger.Ln;

import java.util.Map;

public final class UiEvent {
    private final EventBuilder builder;
    private static final String GA_CAT_UI = "ui";

    public UiEvent(String action) {
        Ln.v("action=" + action);
        builder = new EventBuilder(UiEvent.GA_CAT_UI, action);
    }

    public UiEvent(String action, String label) {
        Ln.v("action=" + action + "; label=" + label);
        builder = new EventBuilder(UiEvent.GA_CAT_UI, action).setLabel(label);
    }

    public UiEvent(String action, int value) {
        Ln.v("action=" + action + "; value=" + value);
        builder = new EventBuilder(UiEvent.GA_CAT_UI, action).setValue(value);
    }

    public UiEvent(String action, String label, int value) {
        Ln.v("action=" + action + "; label=" + label + "; value=" + value);
        builder = new EventBuilder(UiEvent.GA_CAT_UI, action).setLabel(label).setValue(value);
    }

    public Map<String, String> build() {
        return builder.build();
    }

    public static void send(Tracker tracker, String action) {
        tracker.send(new UiEvent(action).build());
    }

    public static void send(Tracker tracker, String action, String label) {
        tracker.send(new UiEvent(action, label).build());
    }
}