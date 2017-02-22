package com.ivygames.bluetooth.peer;

import android.support.annotation.NonNull;

public interface ConnectionCreationListener {

    void onConnected(@NonNull BluetoothConnection connection);

    /**
     * connection attempt failed
     */
    void onConnectFailed();

}