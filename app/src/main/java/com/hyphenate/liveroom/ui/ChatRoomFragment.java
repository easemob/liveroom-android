package com.hyphenate.liveroom.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.widgets.EaseDialog;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 19-3-29
 */
public class ChatRoomFragment extends BaseFragment {
    private static final String TAG = "ChatRoomFragment";

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
            pullToRefreshLayout.setRefreshing(false);
        });
        roomListView.setOnItemClickListener((parent, view, position, id) -> {
            final ChatRoom chatRoom = dataList.get(position);
            EaseDialog.create(getContext())
                    .setContentView(LayoutInflater.from(getContext()).inflate(
                            R.layout.dialog_content_join, null))
                    .setText(R.id.txt_room_name, chatRoom.getName())
                    .setText(R.id.txt_introduce, chatRoom.getIntroduce())
                    .setImage(R.id.image, R.drawable.em_ic_exit)
                    .setOnClickListener(R.id.image, (dialog, v) -> dialog.dismiss())
                    .addButton("观众加入",
                            Color.parseColor("#000000"),
                            Color.parseColor("#FFFFFF"),
                            (dialog, v) -> {
                                String password = dialog.getText(R.id.edit);
//                                Toast.makeText(getContext(), password, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra(Constant.EXTRA_USER_ID, chatRoom.getId());
                                intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_CHATROOM);
                                startActivity(intent);
                            })
                    .show();
        });

        roomAdapter = new RoomAdapter(getContext(), dataList);
        roomListView.setAdapter(roomAdapter);

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadLiveRoomData();
    }

    private void loadLiveRoomData() {
        EMClient.getInstance().chatroomManager().asyncFetchPublicChatRoomsFromServer(20, "", new EMValueCallBack<EMCursorResult<EMChatRoom>>() {
            @Override
            public void onSuccess(EMCursorResult<EMChatRoom> cursorResult) {
                if (cursorResult != null) {
                    List<EMChatRoom> chatRooms = cursorResult.getData();
                    List<ChatRoom> liveRooms = new ArrayList<>();
                    for (EMChatRoom chatRoom : chatRooms) {
                        liveRooms.add(new ChatRoom().setName(chatRoom.getName()).setIntroduce(chatRoom.getDescription()).setId(chatRoom.getId()));
                    }
                    dataList.clear();
                    dataList.addAll(liveRooms);
                    roomAdapter.changeList(dataList);
                    getActivity().runOnUiThread(() -> roomAdapter.notifyDataSetChanged());
                }
            }

            @Override
            public void onError(int i, String s) {
                EMLog.e(TAG, "onError:" + s);
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
                vh.name.setText(item.getName());
                vh.introduce.setText(item.getIntroduce());
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
                        if ((chatRoom.getName() != null && chatRoom.getName().contains(prefixString))
                                || (chatRoom.getIntroduce() != null && chatRoom.getIntroduce().contains(prefixString))) {
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
