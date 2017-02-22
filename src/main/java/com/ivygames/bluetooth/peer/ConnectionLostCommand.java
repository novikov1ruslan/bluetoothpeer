package com.ivygames.bluetooth.peer;

import android.support.annotation.NonNull;

class ConnectionLostCommand implements Runnable {
    @NonNull
    private final ConnectionLostListener mConnectionLostListener;

    ConnectionLostCommand(@NonNull ConnectionLostListener listener) {
        mConnectionLostListener = listener;
    }

    @Override
    public void run() {
        mConnectionLostListener.onConnectionLost();
    }
}
