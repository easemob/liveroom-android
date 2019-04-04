package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hyphenate.liveroom.R;

public class ChatActivity extends BaseActivity {

	private EaseChatFragment chatFragment;
	private String toChatUsername;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		toChatUsername = getIntent().getStringExtra("userId");

		chatFragment = new EaseChatFragment();
		chatFragment.setArguments(getIntent().getExtras());

		getSupportFragmentManager().beginTransaction().add(R.id.chat_container, chatFragment).commit();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String username = intent.getStringExtra("userId");
		if (toChatUsername.equals(username)) {
			super.onNewIntent(intent);
		} else {
			finish();
			startActivity(intent);
		}
	}

	@Override
	public void onBackPressed() {
		chatFragment.onBackPressed();
	}

	public void exit(View view) {
		finish();
	}
}
