package com.hyphenate.liveroom.widgets.chatrow;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.widgets.EaseChatMessageList;

public abstract class EaseChatRow extends LinearLayout{

	public interface EaseChatRowActionCallback {
		void onResendClick(EMMessage message);
		void onBubbleClick(EMMessage message);
		void onDetachedFromWindow();
	}

	protected static final String TAG = "EaseChatRow";

	protected LayoutInflater inflater;
	protected Context context;
	protected BaseAdapter adapter;
	protected EMMessage message;
	protected int position;

	protected ImageView statusView;
	protected Activity activity;

	protected ProgressBar progressBar;

	protected EaseChatMessageList.MessageListItemClickListener itemClickListener;
	private EaseChatRowActionCallback itemActionCallback;

	public EaseChatRow(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context);
		this.context = context;
		this.message = message;
		this.position = position;
		this.adapter = adapter;
		this.activity = (Activity)context;
		inflater = LayoutInflater.from(context);

		initView();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (itemActionCallback != null) {
			itemActionCallback.onDetachedFromWindow();
		}
		super.onDetachedFromWindow();
	}

	public void updateView(final EMMessage msg) {
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onViewUpdate(msg);
			}
		});
	}

	private void initView(){
		onInflateView();
		progressBar = findViewById(R.id.progress_bar);
		statusView = findViewById(R.id.msg_status);

		onFindViewById();
	}

	public void setUpView(EMMessage message, int position,
			EaseChatMessageList.MessageListItemClickListener itemClickListener,
	                      EaseChatRowActionCallback itemActionCallback) {
		this.message = message;
		this.position = position;
		this.itemClickListener = itemClickListener;
		this.itemActionCallback = itemActionCallback;

		setUpBaseView();
		onSetUpView();
		setClickListener();
	}

	private void setUpBaseView() {
//		TextView timestamp = findViewById(R.id.timestamp);
//		if (timestamp != null) {
//			timestamp
//		}
	}

	private void setClickListener(){
		if (statusView != null) {
			statusView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (itemClickListener != null && itemClickListener.onResendClick(message)) {
						return;
					}
					if (itemActionCallback != null) {
						itemActionCallback.onResendClick(message);
					}
				}
			});
		}
	}

	protected abstract void onInflateView();

	protected abstract void onFindViewById();

	protected abstract void onViewUpdate(EMMessage msg);

	protected abstract void onSetUpView();

}
