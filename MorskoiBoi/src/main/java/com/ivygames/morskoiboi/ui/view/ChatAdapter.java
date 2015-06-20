package com.ivygames.morskoiboi.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivygames.morskoiboi.R;
import com.ivygames.morskoiboi.model.ChatMessage;

public class ChatAdapter extends BaseAdapter {
	private final List<ChatMessage> mMesages = new ArrayList<ChatMessage>();
	private final LayoutInflater mInflater;

	public ChatAdapter(LayoutInflater inflater) {
		mInflater = inflater;
	}

	public void add(ChatMessage message) {
		mMesages.add(message);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mMesages.size();
	}

	@Override
	public ChatMessage getItem(int position) {
		return mMesages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessage message = getItem(position);
		if (convertView == null) {
			// convertView =
			// LayoutInflater.from(mContext).inflate(R.layout.my_message_entry,
			// parent);
			int resource = message.isMe() ? R.layout.my_message_entry : R.layout.enemy_message_entry;
			convertView = mInflater.inflate(resource, parent, false);
		}

		TextView messageView = (TextView) convertView;
		messageView.setText(message.getText());

		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).isMe() ? 0 : 1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

}
