package com.ivygames.bluetooth.peer;

import android.support.annotation.NonNull;

class ConnectedCommand implements Runnable {
    @NonNull
    private final BluetoothConnectionImpl mConnection;
    @NonNull
    private final ConnectionCreationListener mConnectionCreationListener;

    ConnectedCommand(@NonNull BluetoothConnectionImpl connection, @NonNull ConnectionCreationListener listener) {
        mConnection = connection;
        mConnectionCreationListener = listener;
    }

    @Override
    public void run() {
        mConnectionCreationListener.onConnected(mConnection);
    }
}
