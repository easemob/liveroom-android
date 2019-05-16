package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.DimensUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by zhangsong on 19-4-8
 */
public class TalkerView extends FrameLayout implements IBorderView {
    public interface OnClickListener {
        void onClick(TalkerView talkerView, BorderTextButton stateTextButton);
    }

    private static final String TAG = "LayoutTalkerMember";

    private BorderHelper stateHelper;
    private String username;
    private boolean canTalk;

    private View talkerView;
    private TextView nameView;
    private ImageView kingView;
    private TextView countDownTimeView;
    private ImageView talkingView;
    private LinearLayout btnContainer;

    private int[] voiceAnimSrc = {R.drawable.em_ic_speaking_1, R.drawable.em_ic_speaking_2, R.drawable.em_ic_speaking_3};
    private static final int VOICE_INTERVAL_TIME = 150; //ms

    public static TalkerView create(Context context) {
        return new TalkerView(context);
    }

    public BorderTextButton createButton(Context context, int btnId, String title, Border border
            , OnClickListener listener) {
        BorderTextButton button = new BorderTextButton(context);
        button.setTag(btnId);
        button.setTextSize(10);
        int padding = DimensUtil.dp2px(context, 4);
        button.setPadding(padding, padding, padding, padding);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(DimensUtil.dp2px(context, 60));
        button.setText(title);
        button.setBorder(border);
        button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(this, button);
            }
        });
        return button;
    }

    public TalkerView(Context context) {
        this(context, null);
    }

    public TalkerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TalkerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_talker_member, this);

        talkerView = findViewById(R.id.indicator_talker);
        nameView = findViewById(R.id.txt_name);
        kingView = findViewById(R.id.iv_king);
        countDownTimeView = findViewById(R.id.tv_time_countdown);
        talkingView = findViewById(R.id.indicator_talking);
        btnContainer = findViewById(R.id.container_btn);

        stateHelper = new BorderHelper();
        stateHelper.init(this, attrs);

    }

    private void showVoiceAnimation(){
        realTalkingAnim();
        postDelayed(voiceAnimThread, VOICE_INTERVAL_TIME);
    }

    private void stopVoiceAnimation(){
        removeCallbacks(voiceAnimThread);
    }

    private void realTalkingAnim(){
        talkingView.setImageResource(voiceAnimSrc[new Random().nextInt(3)]);
    }

    Runnable voiceAnimThread = new Runnable() {
        @Override
        public void run() {
            realTalkingAnim();
            postDelayed(voiceAnimThread, VOICE_INTERVAL_TIME);
        }
    };


    @Override
    public TalkerView setBorder(Border state) {
        stateHelper.changeBorder(this, state);
        return this;
    }

    @Override
    public Border getBorder() {
        return stateHelper.getBorder();
    }

    public TalkerView setName(String name) {
        username = name;
        nameView.setText(name);
        return this;
    }

    public String getName() {
        return username;
    }

    public TalkerView canTalk(boolean can) {
        canTalk = can;
        if (can) {
            talkerView.setBackgroundResource(R.drawable.em_dot_on);
            showVoiceAnimation();
        } else {
            talkerView.setBackgroundResource(R.drawable.em_dot_off);
            talkingView.setVisibility(GONE);
            stopVoiceAnimation();
        }
        return this;
    }

    public TalkerView setKing(boolean king) {
        if (king) {
            kingView.setVisibility(VISIBLE);
        } else {
            kingView.setVisibility(GONE);
        }
        return this;
    }

    public TalkerView setTalking(boolean talking) {
        if (!canTalk) return this;

        if (talking) {
            talkingView.setVisibility(VISIBLE);
        } else {
            talkingView.setVisibility(GONE);
        }
        return this;
    }

    public TalkerView setCountDown(long millisUntilFinished) {
        countDownTimeView.setVisibility(VISIBLE);
        countDownTimeView.setText(String.format(Locale.getDefault(), "%ds", millisUntilFinished/1000));
        return this;
    }

    public TalkerView stopCountDown() {
        countDownTimeView.setText("");
        countDownTimeView.setVisibility(GONE);
        return this;
    }

    public TalkerView addButton(BorderTextButton button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (btnContainer.getChildCount() == 0) {
            btnContainer.setVisibility(VISIBLE);
        } else {
            params.leftMargin = DimensUtil.dp2px(getContext(), 10);
        }
        btnContainer.addView(button, params);
        return this;
    }

    public TalkerView addButton(int btnId, String title, Border border, OnClickListener listener) {
        addButton(createButton(getContext(), btnId, title, border, listener));
        return this;
    }

    public BorderTextButton removeButton(int btnId) {
        BorderTextButton button = findButton(btnId);
        if (button == null) {
            return null;
        }
        btnContainer.removeView(button);
        return button;
    }

    public TalkerView clearButtons() {
        btnContainer.removeAllViews();
        btnContainer.setVisibility(GONE);
        return this;
    }

    public BorderTextButton findButton(int btnId) {
        View child = null;
        for (int i = 0; i < btnContainer.getChildCount(); ++i) {
            if ((int) btnContainer.getChildAt(i).getTag() == btnId) {
                child = btnContainer.getChildAt(i);
                break;
            }
        }
        return child != null ? (BorderTextButton) child : null;
    }
}
