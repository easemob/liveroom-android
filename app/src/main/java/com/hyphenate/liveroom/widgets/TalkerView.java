package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.os.CountDownTimer;
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
import java.util.TimeZone;

/**
 * Created by zhangsong on 19-4-8
 */
public class TalkerView extends FrameLayout implements IBorderView {
    public interface OnClickListener {
        void onClick(TalkerView talkerView, StateTextButton stateTextButton);
    }

    public interface CountDownCallback {
        void onFinish();
    }

    private static final String TAG = "LayoutTalkerMember";

    private StateHelper stateHelper;
    private String username;

    private View talkerView;
    private TextView nameView;
    private ImageView kingView;
    private TextView countDownTimeView;
    private View talkingView;
    private LinearLayout btnContainer;

    private CountDownTimer countDownTimer;
    private SimpleDateFormat dateFormat;

    public static TalkerView create(Context context) {
        return new TalkerView(context);
    }

    public StateTextButton createButton(Context context, int btnId, String title, Border border
            , OnClickListener listener) {
        StateTextButton button = new StateTextButton(context);
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

        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);

        dateFormat = new SimpleDateFormat("mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

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
        if (can) {
            talkerView.setBackgroundResource(R.drawable.em_dot_on);
        } else {
            talkerView.setBackgroundResource(R.drawable.em_dot_off);
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
        if (talking) {
            talkingView.setVisibility(VISIBLE);
        } else {
            talkingView.setVisibility(GONE);
        }
        return this;
    }

    public TalkerView showCountDownTime(long millisUntilFinished) {
        countDownTimeView.setVisibility(VISIBLE);
        countDownTimeView.setText(dateFormat.format(millisUntilFinished));
        return this;
    }

    public TalkerView dismissCountDownTime() {
        countDownTimeView.setVisibility(GONE);
        countDownTimeView.setText("00:00");
        return this;
    }

    public TalkerView startCountDown(int seconds, final CountDownCallback callback) {
        countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownTimeView.setVisibility(VISIBLE);
                countDownTimeView.setText(dateFormat.format(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                countDownTimeView.setVisibility(GONE);
                if (callback != null) {
                    callback.onFinish();
                }
            }
        }.start();
        return this;
    }

    public TalkerView stopCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimeView.setVisibility(GONE);
        return this;
    }

    public TalkerView addButton(StateTextButton button) {
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

    public StateTextButton removeButton(int btnId) {
        StateTextButton button = findButton(btnId);
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

    public StateTextButton findButton(int btnId) {
        View child = null;
        for (int i = 0; i < btnContainer.getChildCount(); ++i) {
            if ((int) btnContainer.getChildAt(i).getTag() == btnId) {
                child = btnContainer.getChildAt(i);
                break;
            }
        }
        return child != null ? (StateTextButton) child : null;
    }
}
