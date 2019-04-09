package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.DimensUtil;
import com.hyphenate.liveroom.widgets.IStateView;
import com.hyphenate.liveroom.widgets.LayoutTalker;

public class ChatActivity extends BaseActivity {
    private static final String TAG = "ChatActivity";

    private EaseChatFragment chatFragment;
    private String toChatUsername;

    private LinearLayout memberContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        memberContainer = findViewById(R.id.container_member);

        toChatUsername = getIntent().getStringExtra("userId");

        chatFragment = new EaseChatFragment();
        chatFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction().add(R.id.chat_container, chatFragment).commit();

        // TODO: for test.
        addMemberView(LayoutTalker.create(this)
                .setName("Shengxi")
                .canTalk(true)
                .addButton("关闭麦克风", true, (v) -> {
                    Log.i(TAG, "关闭麦克风");
                })
                .addButton("下麦", false, (v) -> {
                    Log.i(TAG, "下麦");
                })
                .setState(IStateView.State.ENABLEOFF));

        for (int i = 0; i < 5; i++) {
            addMemberView(LayoutTalker.create(this)
                    .setName("Disconnected")
                    .canTalk(false));
        }
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

    private void addMemberView(LayoutTalker memberView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = DimensUtil.dp2px(this, 2);
        params.topMargin = margin;
        params.bottomMargin = margin;

        memberContainer.addView(memberView, params);
    }
}
