package com.ivygames.bluetooth.peer;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.commons.logger.Ln;

import java.io.IOException;

abstract class ReceivingThread extends Thread {
    @NonNull
    final ConnectionListener mConnectionListener;
    @NonNull
    final Handler mHandler = new Handler(Looper.getMainLooper());
    protected volatile BluetoothSocket mSocket;
    volatile boolean mCancelled;

    ReceivingThread(String name, @NonNull ConnectionListener listener) {
        super(name);
        mConnectionListener = listener;
    }

    final void startReceiving(@NonNull BluetoothSocket socket) {
        try {
            BluetoothConnectionImpl connection = connectToSocket(socket);
            mHandler.post(new ConnectedCommand(connection, mConnectionListener));
            connection.startReceiving();
        } catch (IOException ioe) {
            if (mCancelled) {
                Ln.v("cancelled while connected");
            } else {
                Ln.d("connection lost: " + ioe.getMessage());
                mHandler.post(new ConnectionLostCommand(mConnectionListener));
            }
        }
    }

    @NonNull
    private BluetoothConnectionImpl connectToSocket(@NonNull BluetoothSocket socket) throws IOException {
        BluetoothConnectionImpl connection = new BluetoothConnectionImpl(socket);
        connection.connect();
        return connection;
    }

    void cancel() {
        mCancelled = true;
        interrupt();
    }
}
