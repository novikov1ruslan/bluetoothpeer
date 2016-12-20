package com.ivygames.morskoiboi.bluetooth.peer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivygames.common.multiplayer.ConnectionLostListener;
import com.ivygames.common.multiplayer.MultiplayerEvent;

import org.commons.logger.Ln;

import java.util.Set;
import java.util.UUID;

public class BluetoothPeer {

    @NonNull
    private final BluetoothAdapter mBtAdapter;
    @NonNull
    private final UUID mUuid;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectionCreationListener mConnectionCreationListener;
    private ConnectionLostListener mConnectionLostListener;
    @NonNull
    private final ConnectionListener mConnectionListener = new ConnectionListener() {
        @Override
        public void onConnectionLost(@NonNull MultiplayerEvent event) {
            mConnectionLostListener.onConnectionLost(event);
        }

        @Override
        public void onConnected(@NonNull BluetoothConnection connection) {
            mConnectionCreationListener.onConnected(connection);
        }

        @Override
        public void onConnectFailed() {
            mConnectionCreationListener.onConnectFailed();
        }
    };

    public BluetoothPeer(@NonNull BluetoothAdapter btAdapter, @NonNull UUID uuid) {
        mBtAdapter = btAdapter;
        mUuid = uuid;
    }

    public void setConnectionListener(@NonNull ConnectionCreationListener listener) {
        mConnectionCreationListener = listener;
        Ln.v("connection listener = " + listener);
    }

    /**
     * Start AcceptThread to begin a session in listening (server) mode. Called by the Activity onResume()
     */
    public void startAccepting() {
        if (mAcceptThread != null) {
            Ln.e("already accepting");
            return;
        }

        Ln.d("starting listening to new connections");
        mAcceptThread = new AcceptThread(mConnectionListener, mBtAdapter, mUuid);
        mAcceptThread.start();
    }

    public void cancelAcceptAndCloseConnection() {
        if (mAcceptThread == null) {
            Ln.e("not accepting - cannot close");
            return;
        }

        mAcceptThread.cancelAccept();
        BluetoothUtils.join(mAcceptThread);
        mAcceptThread = null;
    }

    @Nullable
    public Set<BluetoothDevice> getBondedDevices() {
        return mBtAdapter.getBondedDevices();
    }

    public BluetoothDevice getRemoteDevice(@NonNull String address) {
        return mBtAdapter.getRemoteDevice(address);
    }

    public void cancelDiscovery() {
        mBtAdapter.cancelDiscovery();
    }

    public void startDiscovery() {
        mBtAdapter.startDiscovery();
    }

    public boolean isDiscoverable() {
        return mBtAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
    }

    public boolean isConnecting() {
        return mConnectThread != null;
    }

    /**
     * Cancel any thread attempting to make a connection
     */
    public void stopConnecting() {
        if (!isConnecting()) {
            Ln.e("cannot stop connecting");
            return;
        }

        Ln.v("canceling current connection attempt...");
        mConnectThread.cancel();
        BluetoothUtils.join(mConnectThread);
        mConnectThread = null;
        Ln.v("connection cancelled");
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public void connectToDevice(@NonNull BluetoothDevice device) {
        stopConnecting();
        Ln.v("connecting to: " + device);
        mConnectThread = new ConnectThread(device, mConnectionListener, mUuid);
        mConnectThread.start();
    }

    public void registerConnectionLostListener(@NonNull ConnectionLostListener listener) {
        mConnectionLostListener = listener;
        Ln.v("connection lost listener = " + listener);
    }
}
