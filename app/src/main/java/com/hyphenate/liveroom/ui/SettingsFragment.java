package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.manager.PreferenceManager;
import com.hyphenate.liveroom.utils.CommonUtils;

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
        final String username = PreferenceManager.getInstance().getCurrentUsername();
        ((Button) getView().findViewById(R.id.btn_logout))
                .setText(getString(R.string.btn_logout) + " (" + username + ")");

        ((TextView)getView().findViewById(R.id.tv_version)).setText(CommonUtils.getVersionName(getContext()));

        getView().findViewById(R.id.btn_logout).setOnClickListener(v -> {
            EMClient.getInstance().logout(false);
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
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
