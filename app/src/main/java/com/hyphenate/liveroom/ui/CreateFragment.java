package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.entities.RoomType;
import com.hyphenate.liveroom.manager.HttpRequestManager;
import com.hyphenate.liveroom.manager.PreferenceManager;
import com.hyphenate.liveroom.widgets.EaseTipDialog;

/**
 * Created by zhangsong on 19-3-29
 */
public class CreateFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "CreateFragment";

    private static final int REQUEST_JOIN = 100;

    private EditText roomNameView;
    private EditText passwordView;

    private RoomType roomType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_create, null);

        roomNameView = contentView.findViewById(R.id.et_room_name);
        passwordView = contentView.findViewById(R.id.et_password);
        Spinner spinner = contentView.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    roomType = RoomType.COMMUNICATION;
                } else if (position == 1) {
                    roomType = RoomType.HOST;
                } else if (position == 2) {
                    roomType = RoomType.MONOPOLY;
                }
                Log.i(TAG, "onItemSelected: " + roomType.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        contentView.findViewById(R.id.btn_create).setOnClickListener(this);

        roomType = RoomType.COMMUNICATION;

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_JOIN && resultCode != EMError.EM_NO_ERROR) {
            int error = resultCode;
            EaseTipDialog.Builder builder = new EaseTipDialog.Builder(getContext())
                    .setStyle(EaseTipDialog.TipDialogStyle.ERROR)
                    .setTitle("错误");
            if (error == EMError.INVALID_PASSWORD) {
                builder.setMessage("加入语聊房间失败, 密码错误.");
            } else {
                builder.setMessage("加入语聊房间失败: " + error);
            }
            builder.build().show();
        }
    }

    @Override
    public void onClick(View v) {
        String roomName = roomNameView.getText().toString();
        String password = passwordView.getText().toString();

        if (TextUtils.isEmpty(roomName) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "请先输入房间名称和密码 ~", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpRequestManager.getInstance().createChatRoom(roomName, password, "",
                PreferenceManager.getInstance().isAllowRequest(),
                new HttpRequestManager.IRequestListener<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom chatRoom) {
                        getActivity().runOnUiThread(() -> {
                            roomNameView.setText("");
                            passwordView.setText("");
                        });

                        Intent i = new ChatActivity.Builder(getActivity())
                                .setChatRoomEntity(chatRoom)
                                .setPassword(chatRoom.getRtcConfrPassword())
                                .setRoomType(roomType.getId())
                                .build();
                        startActivityForResult(i, REQUEST_JOIN);
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
