package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.R;
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
        String type = PreferenceManager.getInstance().getRoomType();
        if (Constant.ROOM_TYPE_HOST.equals(type)) {
            typeView.setText("主持模式");
        } else if (Constant.ROOM_TYPE_MONOPOLY.equals(type)) {
            typeView.setText("抢麦模式");
        } else {
            typeView.setText("互动模式");
        }

        getView().findViewById(R.id.btn_logout).setOnClickListener(v -> {
            EMClient.getInstance().logout(false);
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        getView().findViewById(R.id.btn_type).setOnClickListener(v -> {
            new EaseTipDialog.Builder(getContext())
                    .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                    .setTitle("提示")
                    .setMessage("切换房间互动模式会初始化麦序。主播模式为当前只有管理员能发言；抢麦模式为当前只有管理员可以发言；互动模式为全部主播均可发言。请确认切换的模式。")
                    .addButton("主持模式",
                            Color.parseColor("#000000"),
                            Color.parseColor("#FFFFFF"),
                            (dialog, view) -> {
                                dialog.dismiss();
                                PreferenceManager.getInstance().setRoomType(Constant.ROOM_TYPE_HOST);
                                typeView.setText("主持模式");
                            })
                    .addButton("抢麦模式",
                            Color.parseColor("#000000"),
                            Color.parseColor("#FFFFFF"),
                            (dialog, view) -> {
                                dialog.dismiss();
                                PreferenceManager.getInstance().setRoomType(Constant.ROOM_TYPE_MONOPOLY);
                                typeView.setText("抢麦模式");
                            })
                    .addButton("互动模式",
                            Color.parseColor("#000000"),
                            Color.parseColor("#FFFFFF"),
                            (dialog, view) -> {
                                dialog.dismiss();
                                PreferenceManager.getInstance().setRoomType(Constant.ROOM_TYPE_COMMUNICATION);
                                typeView.setText("互动模式");
                            })
                    .build()
                    .show();
        });
    }
}
