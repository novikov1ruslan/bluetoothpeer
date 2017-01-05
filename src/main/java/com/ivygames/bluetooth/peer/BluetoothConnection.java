package com.ivygames.bluetooth.peer;

import android.support.annotation.NonNull;

public interface BluetoothConnection {

    void send(@NonNull String message);

    void setMessageReceiver(@NonNull MessageReceiver listener);

    void disconnect();
}
