package com.ivygames.bluetooth.peer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.commons.logger.Ln;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs while listening for incoming connections. It behaves like a server-side client. It runs until a connection is accepted (or until cancelled).
 */
final class AcceptThread extends ReceivingThread {
    // Name for the SDP record when creating server socket
    private static final String NAME = "BtGameManager";

    private volatile BluetoothServerSocket mServerSocket;

    @NonNull
    private final BluetoothAdapter mAdapter;
    @NonNull
    private final UUID mUuid;

    AcceptThread(@NonNull ConnectionListener listener,
                 @NonNull BluetoothAdapter adapter,
                 @NonNull UUID uuid) {
        super("bt_accept", listener);
        mAdapter = adapter;
        mUuid = uuid;
    }

    @Override
    public void run() {
        Ln.v("obtaining transmission socket...");
        mSocket = acceptTransmissionSocketWithErrorHandling();
        if (mSocket == null) {
            return;
        }

        Ln.v("connection accepted - starting transmission");
        try {
            startReceiving(mSocket);
        } finally {
            BluetoothUtils.close(mSocket);
        }
    }

    @Override
    void cancel() {
        super.cancel();
        BluetoothUtils.close(mServerSocket);
    }

    @Nullable
    private BluetoothSocket acceptTransmissionSocketWithErrorHandling() {
        try {
            return acceptBluetoothSocket();
        } catch (IOException ioe) {
            processConnectionFailure(ioe);
            return null;
        }
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
