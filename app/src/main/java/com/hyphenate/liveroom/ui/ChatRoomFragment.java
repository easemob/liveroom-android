package com.hyphenate.liveroom.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.manager.HttpRequestManager;
import com.hyphenate.liveroom.widgets.EaseDialog;
import com.hyphenate.liveroom.widgets.EaseTipDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 19-3-29
 */
public class ChatRoomFragment extends BaseFragment {
    private static final String TAG = "ChatRoomFragment";

    private static final int REQUEST_JOIN = 100;

    private View contentView;

    private EditText searchEdittext;
    private ImageButton searchButton;
    private SwipeRefreshLayout pullToRefreshLayout;
    private ListView roomListView;

    private List<ChatRoom> dataList = new ArrayList<>();
    private RoomAdapter roomAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_chatroom, null);

        searchEdittext = contentView.findViewById(R.id.et_search);
        searchButton = contentView.findViewById(R.id.btn_search);
        pullToRefreshLayout = contentView.findViewById(R.id.layout_pull_refresh);
        roomListView = contentView.findViewById(R.id.list_room);

        searchEdittext.addTextChangedListener(textWatcher);
        searchButton.setOnClickListener((v) -> {
            hideSoftKeyboard();
        });
        pullToRefreshLayout.setOnRefreshListener(() -> {
            dataList.clear();
            roomAdapter.changeList(dataList);
            roomAdapter.notifyDataSetChanged();

            loadLiveRoomData(true);
        });
        roomListView.setOnItemClickListener((parent, view, position, id) -> {
            final ChatRoom chatRoom = dataList.get(position);
            EaseDialog.create(getContext())
                    .setContentView(LayoutInflater.from(getContext()).inflate(
                            R.layout.dialog_content_join, null))
                    .setText(R.id.txt_room_name, chatRoom.getRoomName())
                    .setText(R.id.tv_admin, chatRoom.getOwnerName())
                    .setText(R.id.tv_id_chatroom, chatRoom.getRoomId())
                    .setText(R.id.tv_id_conference, chatRoom.getRtcConfrId())
                    .setText(R.id.tv_mem_limit, chatRoom.getRtcConfrAudienceLimit() + "")
                    .setText(R.id.tv_create_time, chatRoom.getRtcConfrCreateTime())
                    .setText(R.id.tv_allow_request, chatRoom.isAllowAudienceTalk() + "")
                    .setImage(R.id.image, R.drawable.em_ic_exit)
                    .setOnClickListener(R.id.image, (dialog, v) -> dialog.dismiss())
                    .addButton("观众加入",
                            Constant.COLOR_BLACK,
                            Constant.COLOR_WHITE,
                            (dialog, v) -> {
                                dialog.dismiss();
                                String password = dialog.getText(R.id.edit);
                                Intent i = new ChatActivity.Builder(getActivity())
                                        .setOwnerName(chatRoom.getOwnerName())
                                        .setRoomName(chatRoom.getRoomName())
                                        .setChatroomId(chatRoom.getRoomId())
                                        .setConferenceId(chatRoom.getRtcConfrId())
                                        .setPassword(password)
                                        .build();
                                startActivityForResult(i, REQUEST_JOIN);
                            })
                    .show();
        });

        roomAdapter = new RoomAdapter(getContext(), dataList);
        roomListView.setAdapter(roomAdapter);

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLiveRoomData(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_JOIN) {
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

    private void loadLiveRoomData(final boolean refresh) {
        HttpRequestManager.getInstance().getChatRooms(0, 200, new HttpRequestManager.IRequestListener<List<ChatRoom>>() {
            @Override
            public void onSuccess(List<ChatRoom> chatRooms) {
                dataList.clear();
                dataList.addAll(chatRooms);
                getActivity().runOnUiThread(() -> {
                    roomAdapter.changeList(dataList);
                    roomAdapter.notifyDataSetChanged();

                    if (refresh) {
                        pullToRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailed(int errCode, String desc) {
                Toast.makeText(getActivity(), errCode + " - " + desc, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (roomAdapter != null) {
                roomAdapter.getDataFilter().filter(s);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private class RoomAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ChatRoomFilter dataFilter;
        private List<ChatRoom> chatRooms;
        private List<ChatRoom> copyList;

        public RoomAdapter(@NonNull Context context, List<ChatRoom> rooms) {
            chatRooms = rooms;
            copyList = new ArrayList<>();
            copyList.addAll(chatRooms);
            inflater = LayoutInflater.from(context);
        }

        public ChatRoomFilter getDataFilter() {
            if (dataFilter == null) {
                dataFilter = new ChatRoomFilter();
            }
            return dataFilter;
        }

        public void changeList(List<ChatRoom> rooms) {
            this.chatRooms = rooms;
            copyList.clear();
            copyList.addAll(chatRooms);
        }

        @Override
        public int getCount() {
            return chatRooms == null ? 0 : chatRooms.size();
        }

        @Override
        public ChatRoom getItem(int position) {
            return chatRooms == null ? null : chatRooms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView != null) {
                vh = (ViewHolder) convertView.getTag();
            } else {
                convertView = inflater.inflate(R.layout.item_room, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            }

            ChatRoom item = getItem(position);

            if (item != null) {
                vh.name.setText(item.getRoomName());
                vh.introduce.setText(item.getRoomId());
            }

            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView introduce;

            ViewHolder(View v) {
                name = v.findViewById(R.id.txt_name);
                introduce = v.findViewById(R.id.txt_introduce);
            }
        }

        class ChatRoomFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if (prefix == null || prefix.length() == 0) {
                    results.values = copyList;
                    results.count = copyList.size();
                } else {
                    String prefixString = prefix.toString();
                    List<ChatRoom> newValues = new ArrayList<>();
                    for (ChatRoom chatRoom : chatRooms) {
                        if ((chatRoom.getRoomName() != null && chatRoom.getRoomName().contains(prefixString))
                                || (chatRoom.getRoomId() != null && chatRoom.getRoomId().contains(prefixString))) {
                            newValues.add(chatRoom);
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                chatRooms.clear();
                chatRooms.addAll((List<ChatRoom>) results.values);
                if (chatRooms.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}
