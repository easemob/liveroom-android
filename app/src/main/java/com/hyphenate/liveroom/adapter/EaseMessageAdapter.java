package com.hyphenate.liveroom.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.liveroom.utils.CommonUtils;
import com.hyphenate.liveroom.widgets.EaseChatMessageList;
import com.hyphenate.liveroom.widgets.chatrow.EaseCustomChatRowProvider;
import com.hyphenate.liveroom.widgets.presenter.EaseChatRowPresenter;
import com.hyphenate.liveroom.widgets.presenter.EaseChatTextPresenter;

import java.util.List;

public class EaseMessageAdapter extends BaseAdapter {

	private final static String TAG = "EaseMessageAdapter";
	private Context context;

	private static final int HANDLE_MESSAGE_REFRESH_LIST = 0;
	private static final int HANDLE_MESSAGE_SELECT_LAST = 1;
	private static final int HANDLE_MESSAGE_SEEK_TO = 2;

	private static final int MESSAGE_TYPE_RECV_TXT = 0;
	private static final int MESSAGE_TYPE_SENT_TXT = 1;

	public int itemTypeCount;
	private EMConversation conversation;
	EMMessage[] messages = null;

	private String toChatUsername;
	private EaseChatMessageList.MessageListItemClickListener itemClickListener;
	private EaseCustomChatRowProvider customRowProvider;

	private ListView listView;

	public EaseMessageAdapter(Context context, String username, int chatType, ListView listView) {
		this.context = context;
		this.listView = listView;
		this.toChatUsername = username;
		this.conversation = EMClient.getInstance().chatManager().getConversation(username, CommonUtils.getConversationType(chatType), true);
	}

	Handler handler = new Handler() {
		private void refreshList() {
			List<EMMessage> var = conversation.getAllMessages();
			messages = var.toArray(new EMMessage[var.size()]);
			Log.i(TAG, "refreshList: " + messages.length);
			conversation.markAllMessagesAsRead();
			notifyDataSetChanged();
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case HANDLE_MESSAGE_REFRESH_LIST:
					refreshList();
					break;
				case HANDLE_MESSAGE_SELECT_LAST:
					if (messages != null && messages.length > 0) {
						listView.setSelection(messages.length - 1);
					}
					break;
				case HANDLE_MESSAGE_SEEK_TO:
					int position = msg.arg1;
					listView.setSelection(position);
					break;
					default:break;
			}
		}
	};

	public void refresh(){
		if (handler.hasMessages(HANDLE_MESSAGE_REFRESH_LIST)) {
			return;
		}
		Message msg = handler.obtainMessage(HANDLE_MESSAGE_REFRESH_LIST);
		handler.sendMessage(msg);
	}

	public void refreshSelectLast(){
		final int TIME_DELAY_REFRESH_SELECT_LAST  = 100;
		handler.removeMessages(HANDLE_MESSAGE_REFRESH_LIST);
		handler.removeMessages(HANDLE_MESSAGE_SELECT_LAST);
		handler.sendEmptyMessageDelayed(HANDLE_MESSAGE_REFRESH_LIST, TIME_DELAY_REFRESH_SELECT_LAST);
		handler.sendEmptyMessageDelayed(HANDLE_MESSAGE_SELECT_LAST, TIME_DELAY_REFRESH_SELECT_LAST);
	}

	public void refreshSeekTo(int position) {
		handler.sendMessage(handler.obtainMessage(HANDLE_MESSAGE_REFRESH_LIST));
	}

	@Override
	public EMMessage getItem(int position) {
		if (messages != null && position < messages.length) {
			return messages[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return messages == null ? 0 : messages.length;
	}

	@Override
	public int getViewTypeCount() {
		if (customRowProvider != null && customRowProvider.getCustomChatRowTypeCount() > 0) {
			return customRowProvider.getCustomChatRowTypeCount() + 2;
		}
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		EMMessage message = getItem(position);
		if (message == null) {
			return -1;
		}
		if (customRowProvider != null && customRowProvider.getCustomChatRowType(message) > 0) {
			return customRowProvider.getCustomChatRowType(message) + 2;
		}

		if (message.getType() == EMMessage.Type.TXT) {
			return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
		}
		return -1;
	}

	protected EaseChatRowPresenter createChatRowPresenter(EMMessage message, int position) {
		if (customRowProvider != null && customRowProvider.getCustomChatRow(message, position, this) != null) {
			return customRowProvider.getCustomChatRow(message, position, this);
		}

		return new EaseChatTextPresenter();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EMMessage message = getItem(position);
		EaseChatRowPresenter presenter = null;
		if (convertView == null) {
			presenter = createChatRowPresenter(message, position);
			convertView = presenter.createChatRow(context, message, position, this);
			convertView.setTag(presenter);
		} else {
			presenter = (EaseChatRowPresenter) convertView.getTag();
		}
		presenter.setup(message, position, itemClickListener);
		return convertView;
	}

	public void setItemClickListener(EaseChatMessageList.MessageListItemClickListener listener) {
		itemClickListener = listener;
	}

	public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider) {
		customRowProvider = rowProvider;
	}

}
