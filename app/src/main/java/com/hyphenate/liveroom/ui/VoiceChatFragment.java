package com.hyphenate.liveroom.ui;

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
import com.hyphenate.liveroom.utils.DimensUtil;
import com.hyphenate.liveroom.widgets.IStateView;
import com.hyphenate.liveroom.widgets.LayoutTalker;
import com.hyphenate.util.EMLog;

import java.util.List;

/**
 * Created by zhangsong on 19-4-10
 */
public class VoiceChatFragment extends BaseFragment {
    private static final String TAG = "VoiceChatFragment";

    private LinearLayout memberContainer;

    private boolean isCreator;
    private String confId;
    private String password;

    private EMStreamParam normalParam;

    private AudioManager audioManager;

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

        isCreator = getArguments().getBoolean(Constant.EXTRA_CREATOR, false);
        confId = getArguments().getString(Constant.EXTRA_VOICE_CONF_ID);
        password = getArguments().getString(Constant.EXTRA_PASSWORD);

        memberContainer = getView().findViewById(R.id.container_member);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        // TODO: for test.
        addMemberView(LayoutTalker.create(getContext())
                .setName("Shengxi")
                .canTalk(true)
                .addButton("关闭麦克风", true, (v) -> {
                    Log.i(TAG, "关闭麦克风");
                })
                .addButton("下麦", false, (v) -> {
                    Log.i(TAG, "下麦");
                })
                .setState(IStateView.State.ENABLEOFF));

        for (int i = 0; i < 5; i++) {
            addMemberView(LayoutTalker.create(getContext())
                    .setName("Disconnected")
                    .canTalk(false));
//            addMemberView(LayoutTalker.create(getContext())
//                    .setName("Shengxi")
//                    .canTalk(true)
//                    .addButton("关闭麦克风", true, (v) -> {
//                        Log.i(TAG, "关闭麦克风");
//                    })
//                    .addButton("下麦", false, (v) -> {
//                        Log.i(TAG, "下麦");
//                    })
//                    .setState(IStateView.State.ENABLEOFF));
        }

        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);
        normalParam.setVideoOff(true);
        normalParam.setAudioOff(false);

//        EMClient.getInstance().conferenceManager().joinConference(confId, password, new EMValueCallBack<EMConference>() {
//            @Override
//            public void onSuccess(final EMConference value) {
//                EMLog.e(TAG, "join conference success" + value.toString());
//            }
//
//            @Override
//            public void onError(final int error, final String errorMsg) {
//                EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
        EMLog.i(TAG, "publish start, params: " + normalParam.toString());
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

    public void openSpeaker() {
        // 检查是否已经开启扬声器
        if (!audioManager.isSpeakerphoneOn()) {
            // 打开扬声器
            audioManager.setSpeakerphoneOn(true);
        }
        // 开启了扬声器之后，因为是进行通话，声音的模式也要设置成通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    /**
     * 关闭扬声器，即开启听筒播放模式
     * 更多内容看{@link #openSpeaker()}
     */
    public void closeSpeaker() {
        // 检查是否已经开启扬声器
        if (audioManager.isSpeakerphoneOn()) {
            // 关闭扬声器
            audioManager.setSpeakerphoneOn(false);
        }
        // 设置声音模式为通讯模式
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void addMemberView(LayoutTalker memberView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = DimensUtil.dp2px(getContext(), 2);
        params.topMargin = margin;
        params.bottomMargin = margin;

        memberContainer.addView(memberView, params);
    }

    private EMConferenceListener conferenceListener = new EMConferenceListener() {
        @Override
        public void onMemberJoined(EMConferenceMember member) {
        }

        @Override
        public void onMemberExited(EMConferenceMember member) {
        }

        @Override
        public void onStreamAdded(EMConferenceStream stream) {
            // TODO: update speakers view.
            // 1. 谁是speaker
            // 2. 该speaker mute状态
        }

        @Override
        public void onStreamRemoved(EMConferenceStream stream) {
            // TODO: update speakers view.
        }

        @Override
        public void onStreamUpdate(EMConferenceStream stream) {
            // TODO: 修改静音state view
        }

        @Override
        public void onPassiveLeave(int i, String s) {
            // TODO: invoke to leave chatroom
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
            // TODO: 谁在说话
        }

        @Override
        public void onReceiveInvite(String s, String s1, String s2) {

        }

        @Override
        public void onRoleChanged(EMConferenceManager.EMConferenceRole emConferenceRole) {
            // TODO: request tobe talker, publish self stream
        }

        @Override
        public void onAttributeUpdated(EMAttributeAction emAttributeAction, String s, String s1) {
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
