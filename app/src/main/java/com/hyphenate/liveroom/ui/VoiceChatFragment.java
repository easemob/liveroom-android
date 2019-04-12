package com.hyphenate.liveroom.ui;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hyphenate.EMConferenceListener;
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
import com.hyphenate.liveroom.manager.PreferenceManager;
import com.hyphenate.liveroom.utils.DimensUtil;
import com.hyphenate.liveroom.widgets.IStateView;
import com.hyphenate.liveroom.widgets.TalkerView;
import com.hyphenate.liveroom.widgets.StateTextButton;
import com.hyphenate.util.EMLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangsong on 19-4-10
 */
public class VoiceChatFragment extends BaseFragment {
    private static final String TAG = "VoiceChatFragment";

    public static final int EVENT_TOBE_AUDIENCE = 1;

    private static final int BUTTON_MIC = 0;
    private static final int BUTTON_DISCONN = 1;

    private static final int MAX_TALKERS = 6;

    private LinearLayout memberContainer;

    private boolean isCreator;
    private String confId;
    private String password;

    private EMStreamParam normalParam;
    private AudioManager audioManager;

    // <username, talker view>
    private Map<String, TalkerView> talkerMap = new HashMap<>();

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

        String ownerName = getArguments().getString(Constant.EXTRA_OWNER_NAME);
        isCreator = PreferenceManager.getInstance().getCurrentUsername().equalsIgnoreCase(ownerName);
        confId = getArguments().getString(Constant.EXTRA_CONFERENCE_ID);
        password = getArguments().getString(Constant.EXTRA_PASSWORD);

        memberContainer = getView().findViewById(R.id.container_member);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        for (int i = 0; i < MAX_TALKERS; i++) {
            TalkerView talkerView = TalkerView.create(getContext())
                    .setName("已下线")
                    .canTalk(false);
            addMemberView(talkerView);
            talkerMap.put(generateKey(i), talkerView);
        }

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);
        normalParam.setVideoOff(true);
        normalParam.setAudioOff(false);

        EMClient.getInstance().conferenceManager().joinConference(confId, password, new EMValueCallBack<EMConference>() {
            @Override
            public void onSuccess(final EMConference value) {
                EMLog.e(TAG, "join conference success");
                if (isCreator) {
                    publish();

                    String username = PreferenceManager.getInstance().getCurrentUsername();
                    // set channel attributes.
                    EMClient.getInstance().conferenceManager().setConferenceAttribute("admin", username, null);
                    // 管理员设置自己的TalkerView
                    runOnUiThread(() -> {
                        updatePosition(generateKey(0), username)
                                .setName(username)
                                .canTalk(true)
                                .setKing(true)
                                .setState(IStateView.State.ENABLEOFF)
                                .addButton(createButton(BUTTON_MIC, true));
                    });
                }
            }

            @Override
            public void onError(final int error, final String errorMsg) {
                EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        closeSpeaker();

        EMClient.getInstance().conferenceManager().removeConferenceListener(conferenceListener);

        if (isCreator) { // 管理员退出时销毁会议
            EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object value) {
                    EMLog.i(TAG, "destroyConference success");
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.e(TAG, "destroyConference failed " + error + ", " + errorMsg);
                }
            });
        } else {
            EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
                @Override
                public void onSuccess(Object value) {
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.e(TAG, "exit conference failed " + error + ", " + errorMsg);
                }
            });
        }
    }

    private void publish() {
        EMClient.getInstance().conferenceManager().publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "publish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    private void openSpeaker() {
        if (!audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        }
    }

    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    private void closeSpeaker() {
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
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
        EMClient.getInstance().conferenceManager().subscribe(stream, null, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }

    private String findPosition() {
        for (int i = 0; i < MAX_TALKERS; ++i) {
            String key = generateKey(i);
            if (talkerMap.containsKey(key)) {
                return key;
            }
        }
        return null;
    }

    private int findPosition(String key) {
        Set<String> set = talkerMap.keySet();
        int index = 0;
        for (String key1 : set) {
            if (key1.equals(key)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private TalkerView updatePosition(String originalKey, String targetKey) {
        TalkerView talkerView = talkerMap.get(originalKey);
        talkerMap.put(targetKey, talkerView);
        talkerMap.remove(originalKey);
        return talkerView;
    }

    private String generateKey(int index) {
        return "_" + index;
    }

    private StateTextButton createButton(int id, boolean enabled) {
        if (id == BUTTON_MIC) {
            String[] titles = new String[]{"打开麦克风", "关闭麦克风"};
            return TalkerView.createButton(getContext(), BUTTON_MIC,
                    enabled ? titles[1] : titles[0], enabled, (button) -> {
                        if (button.getState() == IStateView.State.ENABLEOFF) {
                            button.setState(IStateView.State.ENABLEON).setText(titles[1]);
                            EMClient.getInstance().conferenceManager().openVoiceTransfer();
                        } else {
                            button.setState(IStateView.State.ENABLEOFF).setText(titles[0]);
                            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                        }
                    });
        }
        if (id == BUTTON_DISCONN) {
            return TalkerView.createButton(getContext(), BUTTON_DISCONN,
                    "下线", enabled, (button) -> {
                        // 发送下线申请给管理员
                        if (onEventCallback != null) {
                            onEventCallback.onEvent(EVENT_TOBE_AUDIENCE);
                        }
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
            Log.i(TAG, "onStreamAdded: ");

            subscribe(stream);

            if (isCreator) {
                String key = findPosition();
                if (key == null) {
                    Log.i(TAG, "onStreamAdded: No position left.");
                    return;
                }

                runOnUiThread(() -> {
                    updatePosition(key, stream.getUsername())
                            .setName(stream.getUsername())
                            .canTalk(true)
                            .setState(IStateView.State.ENABLEOFF)
                            .addButton(createButton(BUTTON_MIC, true))
                            .addButton(createButton(BUTTON_DISCONN, true));
                });
            } else {
                runOnUiThread(() -> {
                    if (talkerMap.containsKey(stream.getUsername())) { // 创建Admin talker view。
                        TalkerView talkerView = talkerMap.get(stream.getUsername());
                        talkerView.setName(stream.getUsername())
                                .canTalk(!stream.isAudioOff())
                                .setKing(true);
                    } else {
                        String key = findPosition();
                        if (key == null) {
                            Log.i(TAG, "No position left.");
                            return;
                        }

                        updatePosition(key, stream.getUsername())
                                .setName(stream.getUsername())
                                .canTalk(!stream.isAudioOff());
                    }
                });
            }
            // TODO: update speakers view.
            // 1. 谁是speaker
            // 2. 该speaker mute状态
        }

        @Override
        public void onStreamRemoved(EMConferenceStream stream) {
            Log.i(TAG, "onStreamRemoved: ");

            String username = stream.getUsername();
            int index = findPosition(username);
            runOnUiThread(() -> {
                updatePosition(username, generateKey(index))
                        .setName("已下线")
                        .clearButtons()
                        .canTalk(false)
                        .setState(IStateView.State.DISABLE);
            });

            // TODO: update speakers view.
        }

        @Override
        public void onStreamUpdate(EMConferenceStream stream) {
            Log.i(TAG, "onStreamUpdate: ");

            String username = stream.getUsername();
            runOnUiThread(() -> {
                talkerMap.get(username).canTalk(!stream.isAudioOff());
            });

            // TODO: 修改静音state view
        }

        @Override
        public void onPassiveLeave(int i, String s) {
            Log.i(TAG, "onPassiveLeave: ");
            // TODO: invoke to leave chatroom
            getActivity().finish();
        }

        @Override
        public void onConferenceState(ConferenceState conferenceState) {
            Log.i(TAG, "onConferenceState: ");
        }

        @Override
        public void onStreamStatistics(EMStreamStatistics emStreamStatistics) {
            Log.i(TAG, "onStreamStatistics: ");
        }

        @Override
        public void onStreamSetup(String s) {
            Log.i(TAG, "onStreamSetup: ");
        }

        @Override
        public void onSpeakers(List<String> list) {
            Log.i(TAG, "onSpeakers: ");

            runOnUiThread(() -> {
                Set<String> set = talkerMap.keySet();
                for (String key : set) {
                    if (list.contains(key)) {
                        talkerMap.get(key).setTalking(true);
                    } else {
                        talkerMap.get(key).setTalking(false);
                    }
                }
            });

            // TODO: 谁在说话
        }

        @Override
        public void onReceiveInvite(String s, String s1, String s2) {
        }

        @Override
        public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {
            Log.i(TAG, "onRoleChanged: ");
            // TODO: request tobe talker, publish self stream

            String username = PreferenceManager.getInstance().getCurrentUsername();

            if (role == EMConferenceManager.EMConferenceRole.Talker) { // 观众变成了主播
                String key = findPosition();
                if (key == null) {
                    Log.i(TAG, "No position left.");
                    return;
                }

                runOnUiThread(() -> {
                    updatePosition(key, username)
                            .setName(username)
                            .canTalk(true)
                            .setState(IStateView.State.ENABLEOFF)
                            .addButton(createButton(BUTTON_MIC, true))
                            .addButton(createButton(BUTTON_DISCONN, true));
                });
            } else { // 主播变成了观众
                int index = findPosition(username);
                runOnUiThread(() -> {
                    updatePosition(username, generateKey(index))
                            .setName("已下线")
                            .clearButtons()
                            .canTalk(false)
                            .setState(IStateView.State.DISABLE);
                });
            }
        }

        @Override
        public void onAttributeUpdated(EMAttributeAction action, String s, String s1) {
            Log.i(TAG, "onAttributeUpdated: " + action + " - " + s + " - " + s1);

            if ("admin".equals(s)) {
                updatePosition(generateKey(0), s1);
            }

            // TODO: 1. 获取频道属性中的admin，根据onStreamAdded()添加TalkerView，标识出admin view
            /**
             * // 频道属性常驻存储内容：
             * {"admin":"memberName","type":"host"}
             * // Admin通知某人mute自己：
             * {"mute":["memberName"]}
             * //
             */
        }
    };
}
