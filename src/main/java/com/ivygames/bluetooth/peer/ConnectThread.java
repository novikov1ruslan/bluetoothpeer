package com.ivygames.bluetooth.peer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.commons.logger.Ln;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs while attempting to make an outgoing connection with a device. It runs straight through; the connection either succeeds or fails.
 */
final class ConnectThread extends ReceivingThread {

    @NonNull
    private final BluetoothDevice mDevice;
    @NonNull
    private final UUID mUuid;

    ConnectThread(@NonNull BluetoothDevice device,
                  @NonNull ConnectionListener connectionListener,
                  @NonNull UUID uuid) {
        super("bt_connect", connectionListener);
        mDevice = device;
        mUuid = uuid;
    }

    @Override
    public void run() {
        Ln.v("connecting to " + mDevice);
        mSocket = obtainSocketWithErrorHandling();
        if (mSocket == null) {
            return;
        }

        Ln.d("socket connected - starting transmission");
        try {
            startReceiving(mSocket);
        } finally {
            BluetoothUtils.close(mSocket);
        }
    }

    @Nullable
    private BluetoothSocket obtainSocketWithErrorHandling() {
        BluetoothSocket socket = null;
        try {
            socket = obtainConnectedSocket(mDevice);
        } catch (final IOException ioe) {
            if (mCancelled) {
                Ln.v("cancelled while connecting");
            } else {
                Ln.d(ioe, "failed to obtain socket");
                mHandler.post(new ConnectFailedCommand(mConnectionListener));
            }
            BluetoothUtils.close(mSocket);
        }
        return socket;
    }

    private BluetoothSocket obtainConnectedSocket(@NonNull BluetoothDevice device) throws IOException {
        // get a BluetoothSocket for a connection with the given BluetoothDevice
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(mUuid);

        Ln.v("socket created - connecting...");
        socket.connect();
        Ln.v("socket connected.");
        return socket;
    }

    @Override
    void cancel() {
        Ln.v("cancelling...");
        super.cancel();
        BluetoothUtils.close(mSocket);
    }

}