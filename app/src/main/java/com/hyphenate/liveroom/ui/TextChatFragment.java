package com.hyphenate.liveroom.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.adapter.EMAChatRoomManagerListener;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.utils.CommonUtils;
import com.hyphenate.liveroom.widgets.EaseChatInputMenu;
import com.hyphenate.liveroom.widgets.EaseChatMessageList;
import com.hyphenate.util.EMLog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextChatFragment extends BaseFragment implements EMMessageListener {
    protected static final String TAG = "TextChatFragment";

    public static final int MSG_FAVOURITE = 0;
    public static final int MSG_GIFT = 1;

    protected Bundle fragmentArgs;
    protected int chatType;
    protected String chatRoomId;
    protected EaseChatMessageList messageList;
    protected EaseChatInputMenu inputMenu;

    protected EMConversation conversation;
    protected InputMethodManager inputManager;

    protected Handler handler = new Handler();
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected ListView listView;

    private View kickedForOfflineLayout;
    protected boolean isloading;
    protected boolean haveMoreData = true;
    protected int pagesize = 20;
    protected ChatRoomListener chatRoomListener;

    private boolean isMessageListInited;
    protected boolean isRoaming = false;
    private ExecutorService fetchQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ease_fragment_chat, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fragmentArgs = getArguments();
        chatType = fragmentArgs.getInt(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_SINGLE);
        chatRoomId = ((ChatRoom) fragmentArgs.getSerializable(Constant.EXTRA_CHAT_ROOM)).getRoomId();

        initView();
        setUpView();
    }

    protected void initView() {
        messageList = getView().findViewById(R.id.message_list);
        listView = messageList.getListView();

        kickedForOfflineLayout = getView().findViewById(R.id.layout_alert_kicked_off);
        kickedForOfflineLayout.setOnClickListener(v -> onChatRoomViewCreation());

        inputMenu = getView().findViewById(R.id.input_menu);
        inputMenu.setChatInputMenuListener(new EaseChatInputMenu.ChatInputMenuListener() {
            @Override
            public void onSendMessage(String content) {
                sendTextMessage(content);
            }

            @Override
            public void onHeartClicked(View view) {
                sendTextMessage(Constant.MESSAGE_FAVOURITE);
            }

            @Override
            public void onGiftClicked(View view) {
                sendTextMessage(Constant.MESSAGE_GIFT);
            }

            @Override
            public void onEditTextClicked() {

            }
        });

        swipeRefreshLayout = messageList.getSwipeRefreshLayout();
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (isRoaming) {
            fetchQueue = Executors.newSingleThreadExecutor();
        }
    }

    protected void setUpView() {
        if (chatType == Constant.CHATTYPE_CHATROOM) {
            chatRoomListener = new ChatRoomListener();
            EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomListener);
            onChatRoomViewCreation();
        }

        if (chatType != Constant.CHATTYPE_CHATROOM) {
            onConversationInit();
            onMessageListInit();
        }

        setRefreshLayoutListener();
    }

    protected void onConversationInit() {
        conversation = EMClient.getInstance().chatManager().getConversation(chatRoomId, CommonUtils.getConversationType(chatType), true);
        conversation.markAllMessagesAsRead();

        if (!isRoaming) {
            final List<EMMessage> msgs = conversation.getAllMessages();
            int msgCount = msgs != null ? msgs.size() : 0;
            if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
                String msgId = null;
                if (msgs != null && msgs.size() > 0) {
                    msgId = msgs.get(0).getMsgId();
                }
                conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
            }
        } else {
            fetchQueue.execute(() -> {
                try {
                    EMClient.getInstance().chatManager().fetchHistoryMessages(chatRoomId, CommonUtils.getConversationType(chatType), pagesize, "");
                    final List<EMMessage> msgs = conversation.getAllMessages();
                    int msgCount = msgs != null ? msgs.size() : 0;
                    if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
                        String msgId = null;
                        if (msgs != null && msgs.size() > 0) {
                            msgId = msgs.get(0).getMsgId();
                        }
                        conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
                    }
                    messageList.refreshSelectLast();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    protected void onMessageListInit() {
        messageList.init(chatRoomId, chatType, null);
        setListItemClickListener();
        messageList.getListView().setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });
        isMessageListInited = true;
    }

    protected void setListItemClickListener() {
        messageList.setItemClickListener(new EaseChatMessageList.MessageListItemClickListener() {
            @Override
            public boolean onResendClick(EMMessage message) {
                return false;
            }

            @Override
            public void onMessageInProgress(EMMessage message) {
                message.setMessageStatusCallback(messageStatusCallback);
            }
        });
    }

    protected void setRefreshLayoutListener() {
        swipeRefreshLayout.setOnRefreshListener(() -> handler.postDelayed(() -> {
            if (!isRoaming) {
                loadMoreLocalMessage();
            } else {
                loadMoreRoamingMessages();
            }

        }, 600));
    }

    private void loadMoreLocalMessage() {
        if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
            List<EMMessage> messages;
            try {
                messages = conversation.loadMoreMsgFromDB(conversation.getAllMessages().size() == 0 ? "" : conversation.getAllMessages().get(0).getMsgId(), pagesize);
            } catch (Exception e1) {
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            if (messages.size() > 0) {
                messageList.refreshSeekTo(messages.size() - 1);
                if (messages.size() != pagesize) {
                    haveMoreData = false;
                }
            } else {
                haveMoreData = false;
            }

            isloading = false;
        } else {
            Toast.makeText(getActivity(), R.string.no_more_messages, Toast.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadMoreRoamingMessages() {
        if (!haveMoreData) {
            Toast.makeText(getActivity(), R.string.no_more_messages, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (fetchQueue != null) {
            fetchQueue.execute(() -> {
                try {
                    List<EMMessage> messages = conversation.getAllMessages();
                    EMClient.getInstance().chatManager().fetchHistoryMessages(chatRoomId, CommonUtils.getConversationType(chatType), pagesize,
                            (messages != null && messages.size() > 0) ? messages.get(0).getMsgId() : "");
                } catch (HyphenateException e) {
                    e.printStackTrace();
                } finally {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> loadMoreLocalMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMessageListInited) {
            messageList.refresh();
        }
        EMClient.getInstance().chatManager().addMessageListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EMClient.getInstance().chatManager().removeMessageListener(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatRoomListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomListener);
        }

        if (chatType == Constant.CHATTYPE_CHATROOM) {
            EMClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId);
        }
    }

    public void onBackPressed() {
        if (chatType == Constant.CHATTYPE_CHATROOM) {
            EMClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId);
        }
    }

    protected void onChatRoomViewCreation() {
        final ProgressDialog pd = ProgressDialog.show(getActivity(), "", getString(R.string.joining));
        EMClient.getInstance().chatroomManager().joinChatRoom(chatRoomId, new EMValueCallBack<EMChatRoom>() {
            @Override
            public void onSuccess(EMChatRoom emChatRoom) {
                if (getActivity() == null || !chatRoomId.equals(emChatRoom.getId())) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    pd.dismiss();
                    EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
                    if (room != null) {
                        EMLog.d(TAG, "join room success, roomname:" + room.getName());
                    }
                    onConversationInit();
                    onMessageListInit();

                    kickedForOfflineLayout.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG, "join room failure : " + error + " - " + errorMsg);
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    pd.dismiss();
                    getActivity().setResult(error);
                    getActivity().finish();
                });
            }
        });
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        for (EMMessage message : messages) {
            String username = null;
            if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                username = message.getTo();
            } else {
                username = message.getFrom();
            }

            // if the message is for current conversation
            if (username.equals(chatRoomId) || message.getTo().equals(chatRoomId) || message.conversationId().equals(chatRoomId)) {
                if (onEventCallback != null) {
                    EMMessageBody body = message.getBody();
                    if (body instanceof EMTextMessageBody) {
                        String content = ((EMTextMessageBody) body).getMessage();
                        if (Constant.MESSAGE_FAVOURITE.equals(content)) {
                            onEventCallback.onEvent(MSG_FAVOURITE);
                        } else if (Constant.MESSAGE_GIFT.equals(content)) {
                            onEventCallback.onEvent(MSG_GIFT);
                        }
                    }
                }

                messageList.refreshSelectLast();
                conversation.markMessageAsRead(message.getMsgId());
            }
            //TODO. notifer vibrate and playtone
        }
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
    }

    @Override
    public void onMessageRead(List<EMMessage> list) {
        if (isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageDelivered(List<EMMessage> list) {
        if (isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageRecalled(List<EMMessage> list) {
        if (isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object change) {
        if (isMessageListInited) {
            messageList.refresh();
        }
    }

    protected void sendTextMessage(String content) {
        EMMessage message = EMMessage.createTxtSendMessage(content, chatRoomId);
        sendMessage(message);
    }

    protected void sendMessage(EMMessage message) {
        if (message == null) {
            return;
        }
        if (chatType == Constant.CHATTYPE_GROUP) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        } else if (chatType == Constant.CHATTYPE_CHATROOM) {
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }

        message.setMessageStatusCallback(messageStatusCallback);

        EMClient.getInstance().chatManager().sendMessage(message);

        if (isMessageListInited) {
            messageList.refreshSelectLast();
        }
    }

    protected EMCallBack messageStatusCallback = new EMCallBack() {
        @Override
        public void onSuccess() {
            if (isMessageListInited) {
                messageList.refresh();
            }
        }

        @Override
        public void onError(int code, String error) {
            Log.i("EaseChatRowPresenter", "onError: " + code + ", error: " + error);
        }

        @Override
        public void onProgress(int progress, String status) {
            Log.i(TAG, "onProgress: " + progress);
            if (isMessageListInited) {
                messageList.refresh();
            }
        }
    };

    class ChatRoomListener extends EaseChatRoomListener {
        @Override
        public void onChatRoomDestroyed(String roomId, String roomName) {
            executeOnUi(() -> {
                if (roomId.equals(chatRoomId)) {
                    Toast.makeText(getActivity(), R.string.the_current_chat_room_destroyed, Toast.LENGTH_LONG).show();
                    finishActivity();
                }
            });
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            executeOnUi(() -> {
                if (roomId.equals(chatRoomId)) {
                    if (reason == EMAChatRoomManagerListener.BE_KICKED) {
                        Toast.makeText(getActivity(), R.string.quiting_the_chat_room, Toast.LENGTH_LONG).show();
                        finishActivity();
                    } else {
                        Toast.makeText(getActivity(), R.string.user_be_kicked_for_offline, Toast.LENGTH_SHORT).show();
                        kickedForOfflineLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @Override
        public void onMemberJoined(String roomId, String participant) {
            if (roomId.equals(chatRoomId)) {
                executeOnUi(() ->
                        Toast.makeText(getActivity(), "member join:" + participant, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onMemberExited(String roomId, String roomName, String participant) {
            if (roomId.equals(chatRoomId)) {
                executeOnUi(() ->
                        Toast.makeText(getActivity(), "member exit:" + participant, Toast.LENGTH_LONG).show());
            }
        }
    }

    public void executeOnUi(Runnable runnable) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(runnable);
    }

    public void finishActivity() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }
}
