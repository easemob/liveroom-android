package com.hyphenate.liveroom.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.manager.HttpRequestManager;
import com.hyphenate.liveroom.manager.PreferenceManager;

/**
 * Created by zhangsong on 19-3-29
 */
public class CreateFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "CreateFragment";

    private EditText roomNameView;
    private EditText passwordView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_create, null);

        roomNameView = contentView.findViewById(R.id.et_room_name);
        passwordView = contentView.findViewById(R.id.et_password);
        contentView.findViewById(R.id.btn_create).setOnClickListener(this);

        return contentView;
    }

    @Override
    public void onClick(View v) {
        String roomName = roomNameView.getText().toString();
        String password = passwordView.getText().toString();

        HttpRequestManager.getInstance().createChatRoom(roomName, password, "",
                true, new HttpRequestManager.IRequestListener<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom chatRoom) {
                        getActivity().runOnUiThread(() -> {
                            roomNameView.setText("");
                            passwordView.setText("");
                        });

                        new ChatActivity.Builder(getActivity())
                                .setOwnerName(PreferenceManager.getInstance().getCurrentUsername())
                                .setRoomName(roomName)
                                .setChatroomId(chatRoom.getRoomId())
                                .setConferenceId(chatRoom.getRtcConfrId())
                                .setPassword(password)
                                .start();
                    }

                    @Override
                    public void onFailed(int errCode, String desc) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), errCode + " - " + desc, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
}
