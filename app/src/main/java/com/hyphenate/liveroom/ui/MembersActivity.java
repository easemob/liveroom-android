package com.hyphenate.liveroom.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.manager.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 19-4-13
 */
public class MembersActivity extends BaseActivity {
    private static final String TAG = "MembersActivity";

    private EditText searchEdittext;
    private ImageButton searchButton;
    private ListView roomListView;

    private String roomId;
    private String ownerName;
    private boolean isAdmin;
    private List<String> dataList = new ArrayList<>();
    private MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        roomId = getIntent().getStringExtra(Constant.EXTRA_CHATROOM_ID);

        searchEdittext = findViewById(R.id.et_search);
        searchButton = findViewById(R.id.btn_search);
        roomListView = findViewById(R.id.list_room);

        searchEdittext.addTextChangedListener(textWatcher);
        searchButton.setOnClickListener((v) -> {
            hideSoftKeyboard();
        });

        memberAdapter = new MemberAdapter(this, dataList);
        roomListView.setAdapter(memberAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMembers();
    }

    private void refreshMembers() {
        EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(roomId);
        if (room == null) {
            return;
        }

        ownerName = room.getOwner();
        isAdmin = PreferenceManager.getInstance().getCurrentUsername().equals(ownerName);

        dataList.add(ownerName);
        dataList.addAll(room.getAdminList());
        dataList.addAll(room.getMemberList());

        memberAdapter.changeList(dataList);
        memberAdapter.notifyDataSetChanged();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (memberAdapter != null) {
                memberAdapter.getDataFilter().filter(s);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private class MemberAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private MemberFilter dataFilter;
        private List<String> chatRooms;
        private List<String> copyList;

        public MemberAdapter(@NonNull Context context, List<String> rooms) {
            chatRooms = rooms;
            copyList = new ArrayList<>();
            copyList.addAll(chatRooms);
            inflater = LayoutInflater.from(context);
        }

        public MemberFilter getDataFilter() {
            if (dataFilter == null) {
                dataFilter = new MemberFilter();
            }
            return dataFilter;
        }

        public void changeList(List<String> rooms) {
            this.chatRooms = rooms;
            copyList.clear();
            copyList.addAll(chatRooms);
        }

        @Override
        public int getCount() {
            return chatRooms == null ? 0 : chatRooms.size();
        }

        @Override
        public String getItem(int position) {
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
                convertView = inflater.inflate(R.layout.item_member, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            }

            String username = getItem(position);
            vh.nameView.setText(username);

            if (ownerName.equals(username)) {
                vh.kingView.setVisibility(View.VISIBLE);
            } else {
                vh.kingView.setVisibility(View.GONE);

                if (isAdmin) {
                    vh.kickOffBtn.setOnClickListener((v) -> {
                        // 把某人踢出聊天室
                        try {
                            EMClient.getInstance().chatroomManager().removeChatRoomMembers(roomId,
                                    new ArrayList<String>() {{
                                        add(username);
                                    }});

                            refreshMembers();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
            return convertView;
        }

        class ViewHolder {
            TextView nameView;
            ImageView kingView;
            View kickOffBtn;

            ViewHolder(View v) {
                nameView = v.findViewById(R.id.txt_name);
                kingView = v.findViewById(R.id.iv_king);
                kickOffBtn = v.findViewById(R.id.btn_kickoff);
            }
        }

        class MemberFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if (prefix == null || prefix.length() == 0) {
                    results.values = copyList;
                    results.count = copyList.size();
                } else {
                    String prefixString = prefix.toString();
                    List<String> newValues = new ArrayList<>();
                    for (String chatRoom : chatRooms) {
                        if ((chatRoom != null && chatRoom.contains(prefixString))) {
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
                chatRooms.addAll((List<String>) results.values);
                if (chatRooms.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}
