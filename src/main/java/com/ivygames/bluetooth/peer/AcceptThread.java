package com.ivygames.bluetooth.peer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.commons.logger.Ln;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs while listening for incoming connections. It behaves like a server-side client. It runs until a connection is accepted (or until cancelled).
 */
final class AcceptThread extends Thread {
    // Name for the SDP record when creating server socket
    private static final String NAME = "BtGameManager";

    private volatile BluetoothServerSocket mServerSocket;
    private volatile BluetoothSocket mSocket;
    private volatile boolean mCancelled;

    @NonNull
    private final ConnectionListener mConnectionListener;
    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final BluetoothAdapter mAdapter;
    @NonNull
    private final UUID mUuid;

    AcceptThread(@NonNull ConnectionListener listener,
                 @NonNull BluetoothAdapter adapter,
                 @NonNull UUID uuid) {
        super("bt_accept");
        mConnectionListener = listener;
        mAdapter = adapter;
        mUuid = uuid;
    }

    @Override
    public void run() {
        Ln.v("obtaining transmission socket...");
        mSocket = obtainTransmissionSocketWithErrorHandling();
        if (mSocket == null) {
            return;
        }

        Ln.v("connection accepted - starting transmission");
        try {
            startReceiving(mSocket);
        }finally {
            BluetoothUtils.close(mSocket);
        }
    }

    void cancelAccept() {
        Ln.v("canceling accept...");
        mCancelled = true;
        interrupt();
        BluetoothUtils.close(mServerSocket);
        BluetoothUtils.close(mSocket);
    }

    @Nullable
    private BluetoothSocket obtainTransmissionSocketWithErrorHandling() {
        try {
            return acceptBluetoothSocket();
        } catch (IOException ioe) {
            if (mCancelled) {
                Ln.v("cancelled while accepting");
            } else {
                Ln.w(ioe, "failed to obtain socket");
                mHandler.post(new ConnectFailedCommand(mConnectionListener));
            }
            return null;
        }
    }

    private void startReceiving(@NonNull BluetoothSocket socket) {
        try {
            BluetoothConnectionImpl connection = connectToSocket(socket);
            mHandler.post(new ConnectedCommand(connection, mConnectionListener));
            connection.startReceiving();
        } catch (IOException ioe) {
            if (mCancelled) {
                Ln.v("cancelled while connected");
            } else {
                Ln.w(ioe);
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

    @NonNull
    private BluetoothSocket acceptBluetoothSocket() throws IOException {
        try {
            mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME, mUuid);
            Ln.v("server socket created, accepting connection...");
            // This is a blocking call and will only return on a successful connection or an exception
            return mServerSocket.accept();
        } finally {
            BluetoothUtils.close(mServerSocket);
        }
    }

}
