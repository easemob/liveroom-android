package com.hyphenate.liveroom.widgets.chatrow;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.liveroom.R;
import com.hyphenate.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EaseChatRowText extends EaseChatRow {

	private TextView contentView;
	private SimpleDateFormat dateFormat;

	public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	}

	@Override
	protected void onInflateView() {
		inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE
				? R.layout.ease_row_message : R.layout.ease_row_message, this);
	}

	@Override
	protected void onFindViewById() {
		contentView = findViewById(R.id.tv_chatcontent);
	}

	@Override
	protected void onSetUpView() {
		long timestamp = message.getMsgTime();

		String formatTimestamp = dateFormat.format(new Date(timestamp));

		String content = "";
		if (message.getType() == EMMessage.Type.TXT) {
			EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
			content = txtBody.getMessage();
		} else if (message.getType() == EMMessage.Type.IMAGE) {
			content = "[图片消息]";
		} else if (message.getType() == EMMessage.Type.VOICE) {
			content = "[语音消息]";
		} else {
			content = "[其他消息]";
		}
		contentView.setText(formatTimestamp + " [" + message.getFrom() + "] " + content);
	}

	@Override
	protected void onViewUpdate(EMMessage msg) {
		switch (msg.status()) {
			case CREATE:
				onMessageCreate();
				break;
			case SUCCESS:
				onMessageSuccess();
				break;
			case FAIL:
				onMessageError();
				break;
			case INPROGRESS:
				onMessageInProgress();
				break;
		}
	}

	private void onMessageCreate(){
		progressBar.setVisibility(View.GONE);
		statusView.setVisibility(View.VISIBLE);
	}

	private void onMessageSuccess(){
		progressBar.setVisibility(View.GONE);
		statusView.setVisibility(View.GONE);
	}

	private void onMessageError(){
		progressBar.setVisibility(View.GONE);
		statusView.setVisibility(View.VISIBLE);
	}

	private void onMessageInProgress(){
		progressBar.setVisibility(View.VISIBLE);
		statusView.setVisibility(View.GONE);
	}

}
