package com.hyphenate.liveroom.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.liveroom.R;

public class EaseTipDialog extends Dialog {

	private ImageView ivTipIcon;
	private TextView tvTitle;
	private TextView tvMessage;
	private ImageView ivCancel;

	private TipDialogStyle dialogStyle = TipDialogStyle.DEFAULT;
	private String title = "";
	private String content = "";


	public static enum TipDialogStyle {
		DEFAULT,
		INFO,
		ERROR
	}

	public EaseTipDialog(Context context) {
		super(context);
		setCanceledOnTouchOutside(true);
	}

	public EaseTipDialog(Context context, TipDialogStyle dialogStyle) {
		super(context);
		setCanceledOnTouchOutside(true);
		this.dialogStyle = dialogStyle;
	}

	public void setStyle(TipDialogStyle dialogStyle){
		this.dialogStyle = dialogStyle;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public void setMessage(String content){
		this.content = content;
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ease_tip_dialog);
		initViews();
		if (dialogStyle != TipDialogStyle.DEFAULT) {
			switch (dialogStyle) {
				case INFO:
					ivTipIcon.setBackgroundColor(Color.parseColor("#f2a735"));
					ivTipIcon.setImageResource(R.drawable.em_ic_info);
					break;
				case ERROR:
					ivTipIcon.setBackgroundColor(Color.parseColor("#cc3a23"));
					ivTipIcon.setImageResource(R.drawable.em_ic_exit);
					break;
			}
		}
		tvTitle.setText(title);
		tvMessage.setText(content);

		ivCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	private void initViews(){
		ivTipIcon = findViewById(R.id.iv_tip_icon);
		tvTitle = findViewById(R.id.tv_title);
		tvMessage = findViewById(R.id.tv_message);
		ivCancel = findViewById(R.id.iv_cancel);
	}

	public static class Builder {
		private Context context;
		private String title = "";
		private String content = "";
		private TipDialogStyle style = TipDialogStyle.DEFAULT;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setStyle(TipDialogStyle dialogStyle){
			this.style = dialogStyle;
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setTitle(int resTitle){
			this.title = context.getResources().getString(resTitle);
			return this;
		}

		public Builder setMessage(String message){
			this.content = message;
			return this;
		}

		public Builder setMessage(int resMessage) {
			this.content = context.getResources().getString(resMessage);
			return this;
		}

		public EaseTipDialog build(){
			EaseTipDialog dialog = new EaseTipDialog(context, style);
			dialog.setMessage(content);
			dialog.setTitle(title);
			return dialog;
		}
	}

}
