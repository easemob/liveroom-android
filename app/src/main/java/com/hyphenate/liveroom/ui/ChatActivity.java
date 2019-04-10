package com.hyphenate.liveroom.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.manager.HttpRequestManager;
import com.hyphenate.liveroom.manager.PreferenceManager;


public class ChatActivity extends BaseActivity {
    private static final String TAG = "ChatActivity";

    private TextView roomNameView;
    private TextView accountView;

    private VoiceChatFragment voiceChatFragment;
    private TextChatFragment textChatFragment;

    private boolean isCreator;
    private String roomName;
    private String toChatUsername;
    private String password; // 用于加入音视频会议

    public static void start(Activity original, boolean creator, String roomName, String textChatRoomId, String voiceConfId, String password) {
        Intent intent = new Intent(original, ChatActivity.class);
        intent.putExtra(Constant.EXTRA_CREATOR, creator);
        intent.putExtra(Constant.EXTRA_ROOM_NAME, roomName);
        intent.putExtra(Constant.EXTRA_TEXT_CHATROOM_ID, textChatRoomId);
        intent.putExtra(Constant.EXTRA_VOICE_CONF_ID, voiceConfId);
        intent.putExtra(Constant.EXTRA_PASSWORD, password);
        intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_CHATROOM);
        original.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        isCreator = getIntent().getBooleanExtra(Constant.EXTRA_CREATOR, false);
        roomName = getIntent().getStringExtra(Constant.EXTRA_ROOM_NAME);
        toChatUsername = getIntent().getStringExtra(Constant.EXTRA_TEXT_CHATROOM_ID);
        password = getIntent().getStringExtra(Constant.EXTRA_PASSWORD);

        roomNameView = findViewById(R.id.txt_room_name);
        accountView = findViewById(R.id.txt_account);

        roomNameView.setText(roomName);
        accountView.setText(PreferenceManager.getInstance().getCurrentUsername());

        textChatFragment = new TextChatFragment();
        textChatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container_chat, textChatFragment).commit();

        voiceChatFragment = new VoiceChatFragment();
        voiceChatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container_member, voiceChatFragment).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String username = intent.getStringExtra(Constant.EXTRA_TEXT_CHATROOM_ID);
        if (toChatUsername.equals(username)) {
            super.onNewIntent(intent);
        } else {
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void exit(View view) {
        if (!isCreator) { // 非语聊室创建者退出
            finish();
            return;
        }

        HttpRequestManager.getInstance().deleteChatRoom(toChatUsername, new HttpRequestManager.IRequestListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }

            @Override
            public void onFailed(int errCode, String desc) {
                Toast.makeText(ChatActivity.this, errCode + " - " + desc, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
