package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.entities.RoomType;
import com.hyphenate.liveroom.manager.PreferenceManager;
import com.hyphenate.liveroom.widgets.EaseTipDialog;

/**
 * Created by zhangsong on 19-3-29
 */
public class SettingsFragment extends BaseFragment {
    private static final String TAG = "SettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final TextView typeView = getView().findViewById(R.id.tv_type);

        RoomType roomType = RoomType.from(PreferenceManager.getInstance().getRoomType());
        typeView.setText(roomType.getName());

        getView().findViewById(R.id.btn_logout).setOnClickListener(v -> {
            EMClient.getInstance().logout(false);
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        getView().findViewById(R.id.btn_type).setOnClickListener(v -> {
            StringBuilder stringBuilder = new StringBuilder("切换房间互动模式会初始化麦序。");
            for (RoomType type : RoomType.values()) {
                stringBuilder.append(type.getDesc()).append(";");
            }
            stringBuilder.append("请确认切换的模式。");

            EaseTipDialog.Builder builder = new EaseTipDialog.Builder(getContext())
                    .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                    .setTitle("提示")
                    .setMessage(stringBuilder.toString());

            for (RoomType type : RoomType.values()) {
                builder.addButton(type.getName(),
                        Constant.COLOR_BLACK,
                        Constant.COLOR_WHITE,
                        (dialog, view) -> {
                            dialog.dismiss();
                            PreferenceManager.getInstance().setRoomType(type.getId());
                            typeView.setText(type.getName());
                        });
            }

            builder.build().show();
        });

        SwitchCompat allowRequestSwitch = getView().findViewById(R.id.switch_allow_request);
        allowRequestSwitch.setChecked(PreferenceManager.getInstance().isAllowRequest());
        allowRequestSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceManager.getInstance().setAllowRequest(isChecked);
        });

        SwitchCompat autoAgreeSwitch = getView().findViewById(R.id.switch_auto_agree);
        autoAgreeSwitch.setChecked(PreferenceManager.getInstance().isAutoAgree());
        autoAgreeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceManager.getInstance().setAutoAgree(isChecked);
        });

        SwitchCompat bgMusicSwitch = getView().findViewById(R.id.switch_bg_music);
        bgMusicSwitch.setChecked(PreferenceManager.getInstance().withBgMusic());
        bgMusicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceManager.getInstance().setBgMusic(isChecked);
        });
    }
}
