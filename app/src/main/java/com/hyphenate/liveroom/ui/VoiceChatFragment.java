package com.hyphenate.liveroom.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.ChatRoom;
import com.hyphenate.liveroom.entities.RoomType;
import com.hyphenate.liveroom.manager.ConferenceAttributesManager;
import com.hyphenate.liveroom.manager.CountDownManager;
import com.hyphenate.liveroom.manager.HttpRequestManager;
import com.hyphenate.liveroom.manager.PreferenceManager;
import com.hyphenate.liveroom.utils.DimensUtil;
import com.hyphenate.liveroom.widgets.IBorderView;
import com.hyphenate.liveroom.widgets.StateTextButton;
import com.hyphenate.liveroom.widgets.TalkerView;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangsong on 19-4-10
 */
public class VoiceChatFragment extends BaseFragment {
    private static final String TAG = "VoiceChatFragment";

    public static final int EVENT_TOBE_AUDIENCE = 1;
    public static final int EVENT_ROOM_TYPE_CHANGED = 2;
    public static final int EVENT_BE_TALKER_SUCCESS = 3;
    public static final int EVENT_BE_TALKER_FAILED = 4;
    public static final int EVENT_BE_AUDIENCE_SUCCESS = 5;
    // 创建房间默认开启了背景音乐
    public static final int EVENT_PLAY_MUSIC_DEFAULT = 6;

    public static final int RESULT_NO_HANDLED = 0;
    public static final int RESULT_NO_POSITION = 1;
    public static final int RESULT_ALREADY_TALKER = 2;

    // 开启/关闭麦克风按钮
    private static final int BUTTON_VOICE = 0;
    // 下线按钮
    private static final int BUTTON_DISCONN = 1;
    // 发言按钮
    private static final int BUTTON_TALK = 2;
    // 抢麦按钮
    private static final int BUTTON_MIC_OCCUPY = 3;
    // 释放麦按钮
    private static final int BUTTON_MIC_RELEASE = 4;

    private static final int MAX_TALKERS = 6;

    private LinearLayout memberContainer;

    private EMConferenceManager.EMConferenceRole conferenceRole;
    private String streamId;
    private EMStreamParam normalParam;
    private AudioManager audioManager;

    // Map<streamId, username>
    private Map<String, String> streamMap = new HashMap<>();
    // Pair<username, talker view>
    private List<Pair<String, TalkerView>> talkerViews = new ArrayList<>(MAX_TALKERS);
    private String publishId = null;
    private ChatRoom chatRoom;
    // 模式
    private RoomType roomType;
    private String currentUsername;
    // 主持模式和抢麦模式下的当前说话者
    private String currentTalker;
    private EMConferenceManager conferenceManager;
    private ConferenceAttributesManager attributesManager;

    //蓝牙耳机是否连接
    private boolean bluetoothIsConnected;
    private BluetoothConnectionReceiver bluetoothReceiver = new BluetoothConnectionReceiver();
    private HeadsetReceiver headsetReceiver = new HeadsetReceiver();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_member_container, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chatRoom = (ChatRoom) getArguments().getSerializable(Constant.EXTRA_CHAT_ROOM);
        final String confId = chatRoom.getRtcConfrId();
        final String password = getArguments().getString(Constant.EXTRA_PASSWORD);
        currentUsername = PreferenceManager.getInstance().getCurrentUsername();
        conferenceManager = EMClient.getInstance().conferenceManager();
        attributesManager = new ConferenceAttributesManager(attributesUpdateListener);

        memberContainer = getView().findViewById(R.id.container_member);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        registerHeadsetReceiver();
        checkCurrentBluetoothState();
        if (bluetoothIsConnected) {
            changeToBluetooth();
        } else {
            changeToSpeaker();
        }

        for (int i = 0; i < MAX_TALKERS; i++) {
            TalkerView talkerView = TalkerView.create(getContext())
                    .setName("已下线")
                    .canTalk(false);
            addMemberView(talkerView);
            Pair<String, TalkerView> value;
            if (i == 0) { // 把Admin的TalkerView放置在第一个位置
                value = new Pair<>(chatRoom.getOwnerName(), talkerView);
            } else {
                value = new Pair<>(null, talkerView);
            }
            talkerViews.add(value);
        }

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);
        normalParam.setVideoOff(true);
        normalParam.setAudioOff(false);

        conferenceManager.joinConference(confId, password, new EMValueCallBack<EMConference>() {
            @Override
            public void onSuccess(final EMConference value) {
                conferenceRole = value.getConferenceRole();
                streamId = value.getConferenceId();
                EMLog.e(TAG, "join conference success, role: " + conferenceRole);

                startAudioTalkingMonitor();

                if (conferenceRole == EMConferenceManager.EMConferenceRole.Admin) { // 管理员加入会议,默认publish 语音流.
                    roomType = RoomType.from(getArguments().getString(Constant.EXTRA_ROOM_TYPE));
                    // set channel attributes.
                    attributesManager.addOrUpdateConferenceAttribute(Constant.PROPERTY_TYPE, roomType.getId());
                    if (roomType == RoomType.HOST) { // 主持模式管理员默认可以说话
                        attributesManager.addOrUpdateConferenceAttribute(Constant.PROPERTY_TALKER, currentUsername);
                    } else if (roomType == RoomType.MONOPOLY) { // 抢麦模式默认所有人都不说话
                        attributesManager.addOrUpdateConferenceAttribute(Constant.PROPERTY_TALKER, "");
                    }
                    attributesManager.send(null);

                    // 抢麦模式下,管理员加入后默认不能说话,其他模式管理员加入后默认都可以说话
                    publish(roomType == RoomType.MONOPOLY);

                    // 管理员设置自己的TalkerView
                    runOnUiThread(() -> {
                        TalkerView talkerView = updatePositionValue(0, currentUsername);
                        talkerView.setName(currentUsername)
                                .setKing(true)
                                .setBorder(IBorderView.Border.GRAY);

                        if (roomType == RoomType.COMMUNICATION) {
                            talkerView.canTalk(true)
                                    .addButton(createButton(talkerView, BUTTON_VOICE, IBorderView.Border.GREEN));
                        } else if (roomType == RoomType.HOST) {
                            talkerView.canTalk(true)
                                    .addButton(createButton(talkerView, BUTTON_TALK, IBorderView.Border.GREEN));
                        } else if (roomType == RoomType.MONOPOLY) {
                            talkerView.canTalk(false)
                                    .addButton(createButton(talkerView, BUTTON_MIC_OCCUPY, IBorderView.Border.GREEN))
                                    .addButton(createButton(talkerView, BUTTON_MIC_RELEASE, IBorderView.Border.GRAY));
                        }

                        // 加入会议成功后开启伴音功能,伴音功能接口需要加入会议成功后调用.
                        if (PreferenceManager.getInstance().withBgMusic()) {
                            if (onEventCallback != null) {
                                onEventCallback.onEvent(EVENT_PLAY_MUSIC_DEFAULT);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(final int error, final String errorMsg) {
                EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);
                if (getActivity() != null) {
                    getActivity().setResult(error);
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
        closeSpeaker();
        unregisterHeadsetReceiver();
        stopAudioTalkingMonitor();
        releaseMicIfNeeded(currentUsername);

        conferenceManager.removeConferenceListener(conferenceListener);
        conferenceManager.exitConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object value) {
                Log.i(TAG, "exitConference success");
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "exit conference failed " + error + ", " + errorMsg);
            }
        });
    }

    private void checkCurrentBluetoothState() {
        bluetoothIsConnected = audioManager.isBluetoothScoAvailableOffCall() && audioManager.isBluetoothScoOn();
    }

    private class BluetoothConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) { // 蓝牙连接状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                // 连接上，切换到蓝牙耳机播放
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    changeToBluetooth();
                } else if (state == BluetoothAdapter.STATE_DISCONNECTED) { // 失联，切换到扬声器播放
                    changeToSpeaker();
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) { // 本地蓝牙打开或关闭
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    // 断开，切换音频输出
                    changeToSpeaker();
                    bluetoothIsConnected = false;
                }
            }
        }
    }

    private class HeadsetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 插入和拔出耳机会触发广播
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                // 耳机插入状态 0 拔出, 1 插入
                int state = intent.getIntExtra("state", 0);
                // 耳机类型
                String name = intent.getStringExtra("name");
                // 耳机是否带有麦克风 0 没有，1 有
                boolean hasMic = intent.getIntExtra("microphone", 0) == 1;
                if (state == 1) {
                    // 耳机已插入
                    closeSpeaker();
                } else {
                    // 耳机已拔出
                    changeToSpeaker();
                }
            }
        }
    }


    private void registerHeadsetReceiver() {
        // 蓝牙状态广播监听
        IntentFilter audioFilter = new IntentFilter();
        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getContext().registerReceiver(bluetoothReceiver, audioFilter);
        // 耳机监听
        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        getContext().registerReceiver(headsetReceiver, headsetFilter);
    }

    private void unregisterHeadsetReceiver() {
        getContext().unregisterReceiver(bluetoothReceiver);
        getContext().unregisterReceiver(headsetReceiver);
    }

    /**
     * 切换到外放
     */
    private void changeToSpeaker() {
        if (!audioManager.isSpeakerphoneOn()) {
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(true);
        }
    }

    private void closeSpeaker() {
        changeToHeadset();
    }

    /**
     * 切换到蓝牙音箱
     */
    private void changeToBluetooth() {
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    private void changeToHeadset() {
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
    }

    public String getStreamId() {
        return streamId;
    }

    public int handleTalkerRequest() {
        int p = findEmptyPosition();
        if (p == -1) {
            Log.i(TAG, "No position left.");
            return RESULT_NO_POSITION;
        }

        if (conferenceRole == EMConferenceManager.EMConferenceRole.Talker) {
            Log.i(TAG, "Current role is talker, publish directly.");

            TalkerView talkerView = updatePositionValue(p, currentUsername);
            if (roomType == RoomType.HOST) { // 主持模式
                if (currentUsername.equals(currentTalker)) { // 自己发言中kill掉app再进入且当前自己仍为发言状态
                    publish(false);
                    talkerView.canTalk(true);
                } else {
                    publish(true);
                    talkerView.canTalk(false);
                }
            } else if (roomType == RoomType.MONOPOLY) { // 抢麦模式
                if (currentUsername.equals(currentTalker)) { // 自己发言中kill掉app再进入且当前自己仍为发言状态
                    publish(false);
                    talkerView.canTalk(true)
                            .addButton(createButton(talkerView, BUTTON_MIC_OCCUPY, IBorderView.Border.GRAY))
                            .addButton(createButton(talkerView, BUTTON_MIC_RELEASE, IBorderView.Border.RED));
                } else {
                    publish(true);
                    talkerView.canTalk(false)
                            .addButton(createButton(talkerView, BUTTON_MIC_OCCUPY, IBorderView.Border.GRAY))
                            .addButton(createButton(talkerView, BUTTON_MIC_RELEASE, IBorderView.Border.GRAY));
                }
            } else {
                publish(false);
                talkerView.canTalk(true)
                        .addButton(createButton(talkerView, BUTTON_VOICE, IBorderView.Border.GREEN));
            }
            talkerView.setName(currentUsername)
                    .setBorder(IBorderView.Border.GRAY)
                    .addButton(createButton(talkerView, BUTTON_DISCONN, IBorderView.Border.RED));

            return RESULT_ALREADY_TALKER;
        }

        return RESULT_NO_HANDLED;
    }

    public void handleConferenceAttribute(EMConferenceListener.EMAttributeAction action, String key,
                                          String value, EMValueCallBack<Void> callBack) {
        if (action == EMConferenceListener.EMAttributeAction.ADD || action == EMConferenceListener.EMAttributeAction.UPDATE) {
            attributesManager.addOrUpdateConferenceAttribute(key, value).send(callBack);
        } else {
            attributesManager.removeConferenceAttribute(key).send(callBack);
        }
    }

    private void publish(boolean pauseVoice) {
        if (pauseVoice) {
            normalParam.setAudioOff(true);
        } else {
            normalParam.setAudioOff(false);
        }
        conferenceManager.publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                publishId = value;
                streamMap.put(publishId, currentUsername);
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "publish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    /**
     * 停止推自己的数据
     */
    private void unpublish(final String publishId) {
        if (TextUtils.isEmpty(publishId)) {
            return;
        }

        conferenceManager.unpublish(publishId, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.i(TAG, "unpublish success.");
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "unpublish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    private void startAudioTalkingMonitor() {
        conferenceManager.startMonitorSpeaker(300);
    }

    private void stopAudioTalkingMonitor() {
        conferenceManager.stopMonitorSpeaker();
    }

    private void addMemberView(TalkerView memberView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = DimensUtil.dp2px(getContext(), 1);
        params.topMargin = margin;
        params.bottomMargin = margin;
        memberContainer.addView(memberView, params);
    }

    /**
     * 订阅指定成员 stream
     */
    private void subscribe(EMConferenceStream stream) {
        conferenceManager.subscribe(stream, null, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.i(TAG, "Subscribe stream success");
            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e(TAG, "Subscribe stream failed: " + error + " - " + errorMsg);
            }
        });
    }

    // 找一个未被使用的位置
    private int findEmptyPosition() {
        for (int i = 0; i < MAX_TALKERS; ++i) {
            if (talkerViews.get(i).first == null) {
                return i;
            }
        }
        return -1;
    }

    private int findExistPosition(String key) {
        for (int i = 0; i < MAX_TALKERS; ++i) {
            if (key != null && key.equals(talkerViews.get(i).first)) {
                return i;
            }
        }
        return -1;
    }

    private TalkerView updatePositionValue(int position, String targetKey) {
        TalkerView talkerView = talkerViews.remove(position).second;
        talkerViews.add(position, new Pair<>(targetKey, talkerView));
        return talkerView;
    }

    private void resetTalkerViewByPosition(int position) {
        TalkerView talkerView = talkerViews.get(position).second;
        talkerView.setName("已下线")
                .clearButtons()
                .setKing(false)
                .setTalking(false)
                .canTalk(false)
                .stopCountDown()
                .setBorder(IBorderView.Border.NONE);

        talkerViews.remove(position);
        talkerViews.add(MAX_TALKERS - 1, new Pair<>(null, talkerView));

        memberContainer.removeView(talkerView);
        memberContainer.addView(talkerView);
    }

    private void releaseMicIfNeeded(String occupiedUsername) {
        // 标记是否为抢麦模式下抢到了麦克风
        boolean isOccupied = roomType == RoomType.MONOPOLY
                && occupiedUsername != null
                && occupiedUsername.equals(currentTalker);
        if (isOccupied) {
            Log.i(TAG, "Self occupied microphone, release microphone.");
            attributesManager.addOrUpdateConferenceAttribute(Constant.PROPERTY_TALKER, "").send(null);
            // 调用app server释放麦克风接口
            HttpRequestManager.getInstance().releaseMic(chatRoom.getRoomId(), currentUsername, null);
        }
    }

    private StateTextButton createButton(TalkerView v, int id, IBorderView.Border border) {
        if (id == BUTTON_VOICE) {
            String[] titles = new String[]{"打开麦克风", "关闭麦克风"};
            return v.createButton(getContext(), BUTTON_VOICE,
                    border != IBorderView.Border.GRAY ? titles[1] : titles[0], border,
                    (view, button) -> {
                        if (button.getBorder() == IBorderView.Border.GRAY) {
                            view.canTalk(true);
                            button.setBorder(IBorderView.Border.GREEN).setText(titles[1]);
                            conferenceManager.openVoiceTransfer();
                        } else {
                            view.canTalk(false);
                            button.setBorder(IBorderView.Border.GRAY).setText(titles[0]);
                            conferenceManager.closeVoiceTransfer();
                        }
                    });
        }
        if (id == BUTTON_DISCONN) {
            return v.createButton(getContext(), BUTTON_DISCONN,
                    "下麦", border, (view, button) -> {
                        releaseMicIfNeeded(view.getName());
                        // 发送下线申请给管理员
                        if (onEventCallback != null) {
                            onEventCallback.onEvent(EVENT_TOBE_AUDIENCE, view.getName());
                        }
                    });
        }
        if (id == BUTTON_TALK) { // 只存在于主持模式下
            return v.createButton(getContext(), BUTTON_TALK,
                    "发言", border, (view, button) -> {
                        if (button.getBorder() == IBorderView.Border.GREEN) { // 当前正在发言过程中
                            Log.i(TAG, "BUTTON_TALK button clicked, already in talking state.");
                            return;
                        }
                        // 把当前被点击人的发言按钮border颜色设置为green
                        button.setBorder(IBorderView.Border.GREEN);
                        // 设置频道属性
                        attributesManager.addOrUpdateConferenceAttribute(
                                Constant.PROPERTY_TALKER, view.getName()).send(null);
                    });
        }
        if (id == BUTTON_MIC_OCCUPY) { // 抢麦按钮
            return v.createButton(getContext(), BUTTON_MIC_OCCUPY,
                    "抢麦", border, (view, button) -> {
                        if (button.getBorder() == IBorderView.Border.GRAY) { // 当前为不可抢麦状态
                            return;
                        }

                        button.setBorder(IBorderView.Border.GRAY);

                        // 调用app server抢麦接口
                        HttpRequestManager.getInstance().occupyMic(chatRoom.getRoomId(), currentUsername, new HttpRequestManager.IRequestListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) { // 抢麦成功
                                Log.i(TAG, "occupyMic onSuccess: ");
                                // 设置频道属性
                                attributesManager.addOrUpdateConferenceAttribute(
                                        Constant.PROPERTY_TALKER, view.getName()).send(null);
                            }

                            @Override
                            public void onFailed(int errCode, String desc) {
                                Log.i(TAG, "occupyMic onFailed: " + errCode + " - " + desc);
                                // 抢麦失败
                                if (TextUtils.isEmpty(currentTalker)) {
                                    button.setBorder(IBorderView.Border.GREEN);
                                }
                            }
                        });
                    });
        }
        if (id == BUTTON_MIC_RELEASE) {
            return v.createButton(getContext(), BUTTON_MIC_RELEASE,
                    "释放麦", border, (view, button) -> {
                        if (button.getBorder() == IBorderView.Border.GRAY) { // 未占用麦克风
                            return;
                        }
                        // 释放麦
                        releaseMicIfNeeded(currentUsername);
                    });
        }

        Log.e(TAG, "createButton, unknown button type: " + id);
        return null;
    }

    private void runOnUiThread(Runnable r) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(r);
        }
    }

    private EMConferenceListener conferenceListener = new EMConferenceListener() {
        @Override
        public void onMemberJoined(EMConferenceMember member) {
            Log.i(TAG, "onMemberJoined: " + member.toString());
        }

        @Override
        public void onMemberExited(EMConferenceMember member) {
            Log.i(TAG, "onMemberExited: ");
        }

        @Override
        public void onStreamAdded(final EMConferenceStream stream) {
            streamMap.put(stream.getStreamId(), stream.getUsername());
            Log.i(TAG, "onStreamAdded: " + streamMap.toString());
            subscribe(stream);

            // 更新名称已存在的TalkerView, 主要包含admin的TalkerView
            TalkerView talkerView;
            final int existPosition = findExistPosition(stream.getUsername());
            if (existPosition != -1) { // 创建Admin talker view。
                talkerView = talkerViews.get(existPosition).second;
            } else {
                // 寻找空位置放置其他主播
                int emptyPosition = findEmptyPosition();
                if (emptyPosition == -1) {
                    Log.i(TAG, "No position left.");
                    return;
                }
                talkerView = updatePositionValue(emptyPosition, stream.getUsername());
            }

            runOnUiThread(() -> {
                talkerView.setName(stream.getUsername())
                        .canTalk(!stream.isAudioOff());
                if (existPosition != -1) {
                    talkerView.setKing(true);
                }
                if (conferenceRole == EMConferenceManager.EMConferenceRole.Admin) {
                    if (roomType == RoomType.HOST) { // 主持模式下,管理员视角其他主播view中都有一个发言的按钮
                        if (stream.getUsername().equals(currentTalker)) {
                            talkerView.addButton(createButton(talkerView, BUTTON_TALK, IBorderView.Border.GREEN));
                        } else {
                            talkerView.addButton(createButton(talkerView, BUTTON_TALK, IBorderView.Border.GRAY));
                        }
                    }
                    talkerView.addButton(createButton(talkerView, BUTTON_DISCONN, IBorderView.Border.RED));
                }
            });
        }

        @Override
        public void onStreamRemoved(EMConferenceStream stream) {
            Log.i(TAG, "onStreamRemoved: " + stream.getUsername());
            streamMap.remove(stream.getStreamId());
            final int existPosition = findExistPosition(stream.getUsername());
            if (existPosition != -1) {
                runOnUiThread(() -> resetTalkerViewByPosition(existPosition));
            }
        }

        @Override
        public void onStreamUpdate(EMConferenceStream stream) {
            Log.i(TAG, "onStreamUpdate: ");
            final int existPosition = findExistPosition(stream.getUsername());
            if (existPosition != -1) {
                runOnUiThread(() -> talkerViews.get(existPosition).second.canTalk(!stream.isAudioOff()));
            }
        }

        /**
         * 会议销毁或者被踢出音视频会议
         *
         * @param i
         * @param s
         */
        @Override
        public void onPassiveLeave(int i, String s) {
            Log.i(TAG, "onPassiveLeave: " + i + " - " + s);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }

        @Override
        public void onConferenceState(ConferenceState conferenceState) {
        }

        @Override
        public void onStreamStatistics(EMStreamStatistics emStreamStatistics) {
        }

        @Override
        public void onStreamSetup(String s) {
        }

        @Override
        public void onSpeakers(List<String> list) {
            runOnUiThread(() -> {
                for (String streamId : streamMap.keySet()) {
                    final int p = findExistPosition(streamMap.get(streamId));
                    if (p == -1) {
                        continue;
                    }

                    if (list.contains(streamId)) {
                        talkerViews.get(p).second.setTalking(true);
                    } else {
                        talkerViews.get(p).second.setTalking(false);
                    }
                }
            });
        }

        @Override
        public void onReceiveInvite(String s, String s1, String s2) {
        }

        @Override
        public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {
            conferenceRole = role;
            Log.i(TAG, "onRoleChanged: " + conferenceRole);

            if (role == EMConferenceManager.EMConferenceRole.Talker) { // 观众变成了主播
                // 寻找一个空位置放置自己的TalkerView
                final int emptyPosition = findEmptyPosition();
                if (emptyPosition == -1) {
                    Log.i(TAG, "No position left.");
                    if (onEventCallback != null) {
                        onEventCallback.onEvent(EVENT_BE_TALKER_FAILED);
                    }
                    return;
                }

                if (onEventCallback != null) {
                    onEventCallback.onEvent(EVENT_BE_TALKER_SUCCESS);
                }

                // 互动模式下,新晋主播可以说话;主持模式/抢麦模式下新晋主播默认不能说话
                publish(roomType != RoomType.COMMUNICATION);

                runOnUiThread(() -> {
                    TalkerView talkerView = updatePositionValue(emptyPosition, currentUsername);
                    if (roomType == RoomType.HOST) { // 主持模式
                        talkerView.canTalk(false);
                    } else if (roomType == RoomType.MONOPOLY) { // 抢麦模式
                        boolean isMicOccupied = !TextUtils.isEmpty(currentTalker);
                        boolean isSelfOccupiedMic = currentUsername.equals(currentTalker);

                        if (isMicOccupied) {
                            talkerView.addButton(createButton(talkerView, BUTTON_MIC_OCCUPY, IBorderView.Border.GRAY));
                            if (isSelfOccupiedMic) {
                                talkerView.canTalk(true)
                                        .addButton(createButton(talkerView, BUTTON_MIC_RELEASE, IBorderView.Border.RED));
                            } else {
                                talkerView.canTalk(false)
                                        .addButton(createButton(talkerView, BUTTON_MIC_RELEASE, IBorderView.Border.GRAY));
                            }
                        } else {
                            talkerView.canTalk(false)
                                    .addButton(createButton(talkerView, BUTTON_MIC_OCCUPY, IBorderView.Border.GREEN))
                                    .addButton(createButton(talkerView, BUTTON_MIC_RELEASE, IBorderView.Border.GRAY));
                        }
                    } else { // 互动模式
                        talkerView.canTalk(true)
                                .addButton(createButton(talkerView, BUTTON_VOICE, IBorderView.Border.GREEN));
                    }
                    talkerView.setName(currentUsername)
                            .setBorder(IBorderView.Border.GRAY)
                            .addButton(createButton(talkerView, BUTTON_DISCONN, IBorderView.Border.RED));
                });
            } else { // 主播变成了观众
                unpublish(publishId);
                releaseMicIfNeeded(currentUsername);
                final int existPosition = findExistPosition(currentUsername);
                runOnUiThread(() -> resetTalkerViewByPosition(existPosition));

                if (onEventCallback != null) {
                    onEventCallback.onEvent(EVENT_BE_AUDIENCE_SUCCESS);
                }
            }
        }


        /**
         * 自己设置的频道属性自己也可以收到该回调
         *
         * @param action
         * @param key
         * @param value
         */
        @Override
        public void onAttributeUpdated(EMAttributeAction action, String key, String value) {
            Log.i(TAG, "onAttributeUpdated: " + action + " - " + key + " - " + value);
            attributesManager.parse(value);
        }
    };

    private ConferenceAttributesManager.OnAttributesUpdateListener attributesUpdateListener = entries -> {
        Log.i(TAG, "onAttrsUpdated: " + Arrays.toString(entries));

        // 第一次加入房间时会获取到当前语聊房间的互动模式
        ConferenceAttributesManager.Entry typeEntry = getEntryByKey(entries, Constant.PROPERTY_TYPE);
        if (typeEntry != null) {
            roomType = RoomType.from(typeEntry.value);
            if (onEventCallback != null) {
                onEventCallback.onEvent(EVENT_ROOM_TYPE_CHANGED, roomType);
            }
        }

        // 获取当前房间的伴音情况
        ConferenceAttributesManager.Entry musicEntry = getEntryByKey(entries, Constant.PROPERTY_MUSIC);
        if (musicEntry != null) {
            if (musicEntry.action == EMConferenceListener.EMAttributeAction.DELETE) {
                // 需要在加入音视频会议成功后调用
                EMClient.getInstance().conferenceManager().stopAudioMixing();
            } else {
                // 需要在加入音视频会议成功后调用
                final int result = EMClient.getInstance().conferenceManager().startAudioMixing("/assets/audio.mp3", -1);
                if (result != EMError.EM_NO_ERROR) {
                    runOnUiThread(() ->
                            Toast.makeText(getActivity(), "伴音开启失败: " + result, Toast.LENGTH_SHORT).show());
                }
                EMClient.getInstance().conferenceManager().adjustAudioMixingVolume(10);
            }
        }

        // 主持模式且发言主播发生变化.(不需要处理action为ADD的情况,该情况相当于会议中已有人发言,这时候以观众身份加入会议,
        // 观众视角不需要显示该view的状态,只需要根据stream的状态设置该view是否canTalk)
        ConferenceAttributesManager.Entry talkerEntry = getEntryByKey(entries, Constant.PROPERTY_TALKER);

        if (roomType == RoomType.HOST && talkerEntry != null
                && talkerEntry.action == EMConferenceListener.EMAttributeAction.UPDATE) {
            if (conferenceRole == EMConferenceManager.EMConferenceRole.Admin) {
                // 把上一个发言人的发言按钮border颜色设置为gray
                if (!TextUtils.isEmpty(currentTalker)) {
                    final int previousTalkerPosition = findExistPosition(currentTalker);
                    if (previousTalkerPosition != -1) {
                        runOnUiThread(() -> talkerViews.get(previousTalkerPosition).second
                                .findButton(BUTTON_TALK)
                                .setBorder(IBorderView.Border.GRAY));
                    }
                }
            }
            // 改变自己的view状态
            if (conferenceRole != EMConferenceManager.EMConferenceRole.Audience) {
                // 更新自己的UI状态
                final int selfPosition = findExistPosition(currentUsername);
                if (currentUsername.equals(talkerEntry.value)) { // 点击了自己的发言按钮
                    conferenceManager.openVoiceTransfer();
                    if (selfPosition != -1) {
                        runOnUiThread(() -> talkerViews.get(selfPosition).second.canTalk(true));
                    }
                } else { // 点击了别人的发言按钮
                    conferenceManager.closeVoiceTransfer();
                    if (selfPosition != -1) {
                        runOnUiThread(() -> talkerViews.get(selfPosition).second.canTalk(false));
                    }
                }
            }
        }

        // 抢麦模式收到某人抢到麦就重新开始倒计时.麦被释放就停止倒计时,恢复自己状态为可抢麦
        // 倒计时结束后恢复自己状态为可抢麦
        // 如果有人抢到麦后加入房间,开始倒计时,倒计时结束后恢复自己的抢麦状态
        if (roomType == RoomType.MONOPOLY && talkerEntry != null) { // 抢麦模式且talker发生变化
            if (TextUtils.isEmpty(talkerEntry.value)) { // 麦被释放
                // 停止倒计时
                CountDownManager.getInstance().stopCountDown();
                // 如果是自己释放麦,则关闭自己的麦克风
                final boolean isSelfOccupiedMic = currentUsername.equals(currentTalker);
                if (isSelfOccupiedMic) {
                    conferenceManager.closeVoiceTransfer();
                }
                resetMyTalkerViewInMonopolyMode();
            } else {
                // 如果是自己抢到麦则打开麦克风,否则关闭自己的麦克风
                final boolean isSelfOccupiedMic = currentUsername.equals(talkerEntry.value);
                if (isSelfOccupiedMic) {
                    conferenceManager.openVoiceTransfer();
                } else {
                    conferenceManager.closeVoiceTransfer();
                }
                // 有人抢到麦,更新自己的按钮状态
                final int selfPosition = findExistPosition(currentUsername);
                if (selfPosition == -1) {
                    Log.e(TAG, "MONOPOLY room, can not get self TalkerView.");
                } else {
                    runOnUiThread(() -> {
                        TalkerView talkerView = talkerViews.get(selfPosition).second;
                        talkerView.findButton(BUTTON_MIC_OCCUPY).setBorder(IBorderView.Border.GRAY);
                        if (isSelfOccupiedMic) {
                            talkerView.canTalk(true);
                            talkerView.findButton(BUTTON_MIC_RELEASE).setBorder(IBorderView.Border.RED);
                        } else {
                            talkerView.canTalk(false);
                            talkerView.findButton(BUTTON_MIC_RELEASE).setBorder(IBorderView.Border.GRAY);
                        }
                    });
                }

                /**
                 * 开始倒计时,如果已经有倒计时已在运行中,则先停止上一个倒计时(会回调上一个{@link CountDownManager.CountDownCallback#onCancel()}
                 * 方法),重新启动倒计时.
                 */
                CountDownManager.getInstance().startCountDown(Constant.SECONDS_MIC_OCCUPIED, new CountDownManager.CountDownCallback() {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        final int currentTalkerPosition = findExistPosition(talkerEntry.value);
                        if (currentTalkerPosition != -1) {
                            runOnUiThread(() -> {
                                TalkerView talkerView = talkerViews.get(currentTalkerPosition).second;
                                talkerView.setCountDown(millisUntilFinished);
                            });
                        }
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onFinish() {
                        // 释放麦
                        releaseMicIfNeeded(currentUsername);
                        // 倒计时结束,更新UI
                        resetMyTalkerViewInMonopolyMode();
                    }
                });
            }
        }

        if (talkerEntry != null) { // 标记当前正在说话的人
            currentTalker = talkerEntry.value;
        }
    };

    private void resetMyTalkerViewInMonopolyMode() {
        // 停止上一个抢麦者的倒计时
        final int previousTalkerPosition = findExistPosition(currentTalker);
        if (previousTalkerPosition == -1) {
            Log.e(TAG, "MONOPOLY room, can not get target TalkerView.");
        } else {
            runOnUiThread(() -> {
                TalkerView talkerView = talkerViews.get(previousTalkerPosition).second;
                talkerView.stopCountDown();
            });
        }

        // 恢复自己的按钮为可抢麦模式
        final int selfPosition = findExistPosition(currentUsername);
        if (selfPosition == -1) {
            Log.e(TAG, "MONOPOLY room, can not get self TalkerView.");
        } else {
            runOnUiThread(() -> {
                TalkerView talkerView = talkerViews.get(selfPosition).second;
                talkerView.canTalk(false);
                talkerView.findButton(BUTTON_MIC_OCCUPY).setBorder(IBorderView.Border.GREEN);
                talkerView.findButton(BUTTON_MIC_RELEASE).setBorder(IBorderView.Border.GRAY);
            });
        }
    }

    private ConferenceAttributesManager.Entry getEntryByKey(ConferenceAttributesManager.Entry[] entries, String key) {
        for (ConferenceAttributesManager.Entry entry : entries) {
            if (key.equals(entry.key)) {
                return entry;
            }
        }
        return null;
    }
}
