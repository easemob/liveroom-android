package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * Created by zhangsong on 19-4-8
 */
public class StateImageButton extends AppCompatImageButton implements IStateView {
    private static final String TAG = "StateImageButton";

    private StateHelper stateHelper;

    public StateImageButton(Context context) {
        this(context, null);
    }

    public StateImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);
    }

    @Override
    public StateImageButton setState(State state) {
        stateHelper.changeState(this, state);
        return this;
    }

    @Override
    public State getState() {
        return stateHelper.getState();
    }
}
