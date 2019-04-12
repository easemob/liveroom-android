package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.DimensUtil;

/**
 * Created by zhangsong on 19-4-8
 */
public class TalkerView extends FrameLayout implements IStateView {
    private static final String TAG = "LayoutTalkerMember";

    private StateHelper stateHelper;

    private View talkerView;
    private TextView nameView;
    private ImageView kingView;
    private View talkingView;
    private LinearLayout btnContainer;

    public static TalkerView create(Context context) {
        return new TalkerView(context);
    }

    public static StateTextButton createButton(Context context, int btnId, String title, boolean enabled
            , StateTextButton.OnClickListener listener) {
        StateTextButton button = new StateTextButton(context);
        button.setTag(btnId);
        button.setTextSize(10);
        int padding = DimensUtil.dp2px(context, 4);
        button.setPadding(padding, padding, padding, padding);
        button.setText(title);
        if (enabled) {
            button.setState(State.ENABLEON);
        } else {
            button.setState(State.ENABLEOFF);
        }
        button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(button);
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
        talkingView = findViewById(R.id.indicator_talking);
        btnContainer = findViewById(R.id.container_btn);

        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);
    }

    @Override
    public TalkerView setState(State state) {
        stateHelper.changeState(this, state);
        return this;
    }

    @Override
    public State getState() {
        return stateHelper.getState();
    }

    public TalkerView setName(String name) {
        nameView.setText(name);
        return this;
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

    public TalkerView addButton(int btnId, String title, boolean enabled, StateTextButton.OnClickListener listener) {
        addButton(createButton(getContext(), btnId, title, enabled, listener));
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
