package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.adapter.EaseMessageAdapter;
import com.hyphenate.liveroom.utils.CommonUtils;
import com.hyphenate.liveroom.widgets.chatrow.EaseCustomChatRowProvider;

public class EaseChatMessageList extends RelativeLayout {

	protected static final String TAG = "EaseChatMessageList";
	protected ListView listView;
	protected SwipeRefreshLayout swipeRefreshLayout;
	protected Context context;
	protected EMConversation conversation;
	protected int chatType;
	protected String toChatUsername;
	protected EaseMessageAdapter messageAdapter;

	public EaseChatMessageList(Context context) {
		this(context, null);
	}

	public EaseChatMessageList(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EaseChatMessageList(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		parseStyle(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
		swipeRefreshLayout = findViewById(R.id.chat_swipe_layout);
		listView = findViewById(R.id.list);
	}

	public void init(String toChatUsername, int chatType, EaseCustomChatRowProvider customChatRowProvider) {
		this.chatType = chatType;
		this.toChatUsername = toChatUsername;

		conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, CommonUtils.getConversationType(chatType), true);
		messageAdapter = new EaseMessageAdapter(context, toChatUsername, chatType, listView);
		messageAdapter.setCustomChatRowProvider(customChatRowProvider);
		listView.setAdapter(messageAdapter);
		refreshSelectLast();
	}


	protected void parseStyle(Context context, AttributeSet attrs) {
	}

	public void refresh(){
		if (messageAdapter != null){
			messageAdapter.refresh();
		}
	}

	public void refreshSelectLast(){
		if (messageAdapter != null) {
			messageAdapter.refreshSelectLast();
		}
	}

	public void refreshSeekTo(int position) {
		if (messageAdapter != null) {
			messageAdapter.refreshSeekTo(position);
		}
	}

	public ListView getListView() {
		return listView;
	}

	public SwipeRefreshLayout getSwipeRefreshLayout() {
		return swipeRefreshLayout;
	}

	public EMMessage getItem(int position) {
		if (messageAdapter != null) {
			return messageAdapter.getItem(position);
		}
		return null;
	}

	public interface MessageListItemClickListener {
		boolean onResendClick(EMMessage message);
		void onMessageInProgress(EMMessage message);
	}

	public void setItemClickListener(MessageListItemClickListener listener) {
		if (messageAdapter != null) {
			messageAdapter.setItemClickListener(listener);
		}
	}

	public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider) {
		if (messageAdapter != null) {
			messageAdapter.setCustomChatRowProvider(rowProvider);
		}
	}


}
