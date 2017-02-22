package com.ivygames.bluetooth.peer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.commons.logger.Ln;

import java.io.Closeable;
import java.io.IOException;

public class BluetoothUtils {

    private BluetoothUtils() {

    }

    /**
     * Get the device MAC address, which is the last 17 chars in the info
     */
    public static String extractMacAddress(@NonNull String info) {
        return info.substring(info.length() - 17);
    }

    static void close(@Nullable Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                Ln.w(e, "error closing");
            }
        }
    }

    static void join(@NonNull Thread t) {
        try {
            t.join();
        } catch (InterruptedException ie) {
            Ln.d("interrupted while joining");
            Thread.currentThread().interrupt();
        }
    }
}
