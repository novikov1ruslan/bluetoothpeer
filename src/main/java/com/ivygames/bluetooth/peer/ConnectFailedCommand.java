package com.ivygames.bluetooth.peer;

import android.support.annotation.NonNull;

class ConnectFailedCommand implements Runnable {
    @NonNull
    private final ConnectionCreationListener mConnectionCreationListener;

    ConnectFailedCommand(@NonNull ConnectionCreationListener listener) {
        mConnectionCreationListener = listener;
    }

    @Override
    public void run() {
        mConnectionCreationListener.onConnectFailed();
    }
}
