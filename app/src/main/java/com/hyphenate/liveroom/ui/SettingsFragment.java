package com.hyphenate.liveroom.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.liveroom.R;

/**
 * Created by zhangsong on 19-3-29
 */
public class SettingsFragment extends BaseFragment {
    private static final String TAG = "SettingsFragment";

    private View contentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_settings, null);
        return contentView;
    }
}
