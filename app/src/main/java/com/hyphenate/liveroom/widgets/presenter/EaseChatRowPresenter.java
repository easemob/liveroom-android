package com.hyphenate.liveroom.widgets.presenter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.liveroom.widgets.EaseChatMessageList;
import com.hyphenate.liveroom.widgets.chatrow.EaseChatRow;

public abstract class EaseChatRowPresenter implements EaseChatRow.EaseChatRowActionCallback {

	private EaseChatRow chatRow;
	private Context context;
	private BaseAdapter adapter;
	private EMMessage message;
	private int position;

	private EaseChatMessageList.MessageListItemClickListener itemClickListener;

	@Override
	public void onResendClick(EMMessage message) {

	}

	@Override
	public void onBubbleClick(EMMessage message) {

	}

	@Override
	public void onDetachedFromWindow() {

	}

	public EaseChatRow createChatRow(Context ctx, EMMessage message, int position, BaseAdapter adapter) {
		this.context = ctx;
		this.adapter = adapter;
		chatRow = onCreateChatRow(ctx, message, position, adapter);
		return chatRow;
	}

	public void setup(EMMessage msg, int position, EaseChatMessageList.MessageListItemClickListener itemClickListener) {
		this.message = msg;
		this.position = position;
		this.itemClickListener = itemClickListener;

		chatRow.setUpView(message, position, itemClickListener, this);

		handleMessage();
	}

	protected void handleSendMessage(final EMMessage message) {
		getChatRow().updateView(message);
		if (message.status() == EMMessage.Status.INPROGRESS) {
			if (itemClickListener != null) {
				itemClickListener.onMessageInProgress(message);
			}
		}
	}

	protected void handleReceiveMessage(EMMessage message) {

	}

	protected abstract EaseChatRow onCreateChatRow(Context ctx, EMMessage message, int position, BaseAdapter adapter);

	protected EaseChatRow getChatRow() {
		return chatRow;
	}

	protected Context getContext() {
		return context;
	}

	protected BaseAdapter getAdapter() {
		return adapter;
	}

	protected EMMessage getMessage() {
		return message;
	}

	protected int getPosition() {
		return position;
	}

	private void handleMessage() {
		if (message.direct() == EMMessage.Direct.SEND) {
			handleSendMessage(message);
		} else if (message.direct() == EMMessage.Direct.RECEIVE) {
			handleReceiveMessage(message);
		}
	}


}
