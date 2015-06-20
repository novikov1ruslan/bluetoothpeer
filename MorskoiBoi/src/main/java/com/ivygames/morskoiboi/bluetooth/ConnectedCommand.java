package com.ivygames.morskoiboi.bluetooth;

import org.apache.commons.lang3.Validate;

final class ConnectedCommand implements Runnable {

	private final ConnectionListener mConnnectionListener;
	private final MessageSender mSender;

	ConnectedCommand(ConnectionListener listener, MessageSender sender) {
		Validate.notNull(listener, "listener cannot be null");
		mConnnectionListener = listener;

		Validate.notNull(sender, "sender cannot be null");
		mSender = sender;
	}

	@Override
	public void run() {
		mConnnectionListener.onConnected(mSender);
	}
}
