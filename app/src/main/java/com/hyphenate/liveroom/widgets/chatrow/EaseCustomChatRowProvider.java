package com.hyphenate.liveroom.widgets.chatrow;

import android.widget.BaseAdapter;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.liveroom.widgets.presenter.EaseChatRowPresenter;

public interface EaseCustomChatRowProvider {

	int getCustomChatRowTypeCount();

	int getCustomChatRowType(EMMessage message);

	EaseChatRowPresenter getCustomChatRow(EMMessage message, int position, BaseAdapter adapter);

}
