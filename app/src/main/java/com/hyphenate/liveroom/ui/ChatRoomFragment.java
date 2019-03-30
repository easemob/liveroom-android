package com.hyphenate.liveroom.ui;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.widgets.EaseDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 19-3-29
 */
public class ChatRoomFragment extends BaseFragment {
    private static final String TAG = "ChatRoomFragment";

    private View contentView;

    private EditText searchEdittext;
    private TextView searchButton;
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
                    .setImage(R.id.image, R.drawable.em_icon_pwd)
                    .setOnClickListener(R.id.image, (dialog, v) -> dialog.dismiss())
                    .addButton("创建",
                            Color.parseColor("#000000"),
                            Color.parseColor("#FFFFFF"),
                            (dialog, v) -> {
                                String password = dialog.getText(R.id.edit);
                                Toast.makeText(getContext(), password, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                    .show();
        });

        dataList.add(new ChatRoom().setName("chatroom1").setIntroduce("introduce1"));
        dataList.add(new ChatRoom().setName("chatroom2").setIntroduce("introduce2"));
        dataList.add(new ChatRoom().setName("chatroom3").setIntroduce("introduce3"));
        dataList.add(new ChatRoom().setName("chatroom4").setIntroduce("introduce4"));
        dataList.add(new ChatRoom().setName("chatroom5").setIntroduce("introduce5"));
        dataList.add(new ChatRoom().setName("chatroom6").setIntroduce("introduce6"));
        dataList.add(new ChatRoom().setName("chatroom7").setIntroduce("introduce7"));
        dataList.add(new ChatRoom().setName("chatroom8").setIntroduce("introduce8"));

        roomAdapter = new RoomAdapter(getContext(), dataList);
        roomListView.setAdapter(roomAdapter);

        return contentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            roomAdapter.filter(searchEdittext.getText().toString());
        }
    };

    private static class RoomAdapter extends BaseAdapter {

        private Context context;
        private List<ChatRoom> fullDataList = new ArrayList<>();
        private List<ChatRoom> dataList;
        private ChatRoomFilter dataFilter;

        public RoomAdapter(Context context, @NonNull List<ChatRoom> dataList) {
            this.context = context;
            this.dataList = dataList;
            this.fullDataList.addAll(dataList);
        }

        public synchronized void filter(CharSequence constraint) {
            if (dataFilter == null) {
                dataFilter = new ChatRoomFilter(fullDataList);
            }

            dataFilter.filter(constraint, (ChatRoomFilter.OnFilterListener) result -> {
                dataList.clear();
                dataList.addAll(result);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.item_room, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            }

            ChatRoom item = (ChatRoom) getItem(position);
            vh.name.setText(item.getName());
            vh.introduce.setText(item.getIntroduce());

            return convertView;
        }

        private static class ViewHolder {
            TextView name;
            TextView introduce;

            ViewHolder(View v) {
                name = v.findViewById(R.id.txt_name);
                introduce = v.findViewById(R.id.txt_introduce);
            }
        }

        private static class ChatRoomFilter extends Filter {
            public interface OnFilterListener {
                void onFilter(List<ChatRoom> result);
            }

            private static final String TAG = "ChatRoomFilter";

            private List<ChatRoom> chatRooms;
            private OnFilterListener filterListener;

            public ChatRoomFilter(@NonNull List<ChatRoom> chatRooms) {
                this.chatRooms = chatRooms;
            }

            public void filter(CharSequence constraint, OnFilterListener listener) {
                filterListener = listener;
                super.filter(constraint);
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ChatRoom> list = new ArrayList<>();
                for (ChatRoom room : chatRooms) {
                    if (room.getName().contains(constraint) || room.getIntroduce().contains(constraint)) {
                        list.add(room);
                    }
                }

                FilterResults results = new FilterResults();
                results.count = list.size();
                results.values = list;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (filterListener != null) {
                    filterListener.onFilter((List<ChatRoom>) results.values);
                }
            }
        }
    }
}
