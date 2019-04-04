package com.hyphenate.liveroom.widgets.presenter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.liveroom.widgets.chatrow.EaseChatRow;
import com.hyphenate.liveroom.widgets.chatrow.EaseChatRowText;

public class EaseChatTextPresenter extends EaseChatRowPresenter {
	private static final String TAG = "EaseChatTextPresenter";

	@Override
	protected EaseChatRow onCreateChatRow(Context ctx, EMMessage message, int position, BaseAdapter adapter) {
		return new EaseChatRowText(ctx, message, position, adapter);
	}


}
