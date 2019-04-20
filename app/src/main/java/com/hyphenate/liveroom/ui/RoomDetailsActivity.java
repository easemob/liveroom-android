package com.hyphenate.liveroom.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.entities.RoomType;

/**
 * Created by zhangsong on 19-4-19
 */
public class RoomDetailsActivity extends BaseActivity {
    private static final String TAG = "RoomDetailsActivity";

    public static class Builder {
        private Intent intent;

        public Builder(Activity original) {
            intent = new Intent(original, RoomDetailsActivity.class);
        }

        public Builder setChatRoomEntity(ChatRoom chatRoom) {
            intent.putExtra(Constant.EXTRA_CHAT_ROOM, chatRoom);
            return this;
        }

        public Builder setPassword(String password) {
            intent.putExtra(Constant.EXTRA_PASSWORD, password);
            return this;
        }

        public Builder setRoomType(String roomType) {
            intent.putExtra(Constant.EXTRA_ROOM_TYPE, roomType);
            return this;
        }

        public Intent build() {
            return intent;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        final ChatRoom chatRoom = (ChatRoom) getIntent().getSerializableExtra(Constant.EXTRA_CHAT_ROOM);
        final String password = getIntent().getStringExtra(Constant.EXTRA_PASSWORD);
        final RoomType roomType = RoomType.from(getIntent().getStringExtra(Constant.EXTRA_ROOM_TYPE));

        setTextViewText(R.id.tv_admin, chatRoom.getOwnerName());
        setTextViewText(R.id.tv_password, password);
        setTextViewText(R.id.tv_id_chatroom, chatRoom.getRoomId());
        setTextViewText(R.id.tv_id_conference, chatRoom.getRtcConfrId());
        setTextViewText(R.id.tv_mem_limit, chatRoom.getRtcConfrAudienceLimit() + "");
        setTextViewText(R.id.tv_create_time, chatRoom.getRtcConfrCreateTime());
        setTextViewText(R.id.tv_allow_request, chatRoom.isAllowAudienceTalk() + "");
        setTextViewText(R.id.tv_type, roomType.getName());
    }

    private void setTextViewText(int id, String text) {
        ((TextView) findViewById(id)).setText(text);
    }
}
