package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.DimensUtil;

/**
 * Created by zhangsong on 19-4-8
 */
public class LayoutTalker extends FrameLayout implements IStateView {
    private static final String TAG = "LayoutTalkerMember";

    private StateHelper stateHelper;

    private View talkerView;
    private TextView nameView;
    private View talkingView;
    private LinearLayout btnContainer;

    public static LayoutTalker create(Context context) {
        return new LayoutTalker(context);
    }

    public LayoutTalker(Context context) {
        this(context, null);
    }

    public LayoutTalker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LayoutTalker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_talker_member, this);

        talkerView = findViewById(R.id.indicator_talker);
        nameView = findViewById(R.id.txt_name);
        talkingView = findViewById(R.id.indicator_talking);
        btnContainer = findViewById(R.id.container_btn);

        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);
    }

    @Override
    public LayoutTalker setState(State state) {
        stateHelper.changeState(this, state);
        return this;
    }

    @Override
    public State getState() {
        return stateHelper.getState();
    }

    public LayoutTalker setName(String name) {
        nameView.setText(name);
        return this;
    }

    public LayoutTalker canTalk(boolean can) {
        if (can) {
            talkerView.setBackgroundResource(R.drawable.em_dot_on);
        } else {
            talkerView.setBackgroundResource(R.drawable.em_dot_off);
        }
        return this;
    }

    public LayoutTalker addButton(String title, boolean enabled, View.OnClickListener listener) {
        StateTextButton button = new StateTextButton(getContext());
        button.setTextSize(10);
        int padding = DimensUtil.dp2px(getContext(), 4);
        button.setPadding(padding, padding, padding, padding);
        button.setText(title);
        if (enabled) {
            button.setState(State.ENABLEON);
        } else {
            button.setState(State.ENABLEOFF);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (btnContainer.getChildCount() > 0) {
            params.leftMargin = DimensUtil.dp2px(getContext(), 10);
        }
        button.setOnClickListener(listener);

        btnContainer.setVisibility(VISIBLE);
        btnContainer.addView(button, params);

        return this;
    }
}
