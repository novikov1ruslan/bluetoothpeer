package com.ivygames.bluetooth.peer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.commons.logger.Ln;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs while attempting to make an outgoing connection with a device. It runs straight through; the connection either succeeds or fails.
 */
final class ConnectThread extends Thread {
    private volatile BluetoothSocket mSocket;
    private volatile boolean mCancelled;

    @NonNull
    private final BluetoothDevice mDevice;
    @NonNull
    private final ConnectionListener mConnectionListener;
    @NonNull
    private final UUID mUuid;
    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    ConnectThread(@NonNull BluetoothDevice device,
                  @NonNull ConnectionListener connectionListener,
                  @NonNull UUID uuid) {
        super("bt_connect");
        mDevice = device;
        mConnectionListener = connectionListener;
        mUuid = uuid;
    }

    @Override
    public void run() {
        Ln.v("connecting to " + mDevice);
        try {
            mSocket = obtainSocket(mDevice);
        } catch (final IOException ioe) {
            if (mCancelled) {
                Ln.v("cancelled while connecting");
            } else {
                Ln.d(ioe, "failed to obtain socket");
                mHandler.post(new ConnectFailedCommand(mConnectionListener));
            }
            BluetoothUtils.close(mSocket);
            return;
        }

        Ln.d("socket connected - starting transmission");
        try {
            final BluetoothConnectionImpl connection = new BluetoothConnectionImpl(mSocket);
            connection.connect();

            // we post connected event after connection object is created
            mHandler.post(new ConnectedCommand(connection, mConnectionListener));
            connection.startReceiving();
        } catch (IOException ioe) {
            if (mCancelled) {
                Ln.d("cancelled while connected");
            } else {
                Ln.d("connection lost: " + ioe.getMessage());
                mHandler.post(new ConnectionLostCommand(mConnectionListener));
            }
        } finally {
            BluetoothUtils.close(mSocket);
        }
    }

    private BluetoothSocket obtainSocket(BluetoothDevice device) throws IOException {
        // get a BluetoothSocket for a connection with the given BluetoothDevice
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(mUuid);

        Ln.v("socket created - connecting...");
        // This is a blocking call and will only return on a
        // successful connection or an exception
        socket.connect();
        return socket;
    }

    void cancel() {
        Ln.v("cancelling...");
        mCancelled = true;
        interrupt();
        BluetoothUtils.close(mSocket);
    }

}