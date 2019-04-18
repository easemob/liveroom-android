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
            StringBuilder stringBuilder = new StringBuilder("切换房间互动模式会初始化麦序。");
            for (RoomType roomType : RoomType.values()) {
                stringBuilder.append(roomType.getDesc()).append(";");
            }
            stringBuilder.append("请确认切换的模式。");

            EaseTipDialog.Builder builder = new EaseTipDialog.Builder(getContext())
                    .setStyle(EaseTipDialog.TipDialogStyle.INFO)
                    .setTitle("提示")
                    .setMessage(stringBuilder.toString());

            for (RoomType roomType : RoomType.values()) {
                builder.addButton(roomType.getName(),
                        Constant.COLOR_BLACK,
                        Constant.COLOR_WHITE,
                        (dialog, view) -> {
                            dialog.dismiss();
                            PreferenceManager.getInstance().setRoomType(roomType.getId());
                            typeView.setText(roomType.getName());
                        });
            }

            builder.build().show();
        });
    }
}
