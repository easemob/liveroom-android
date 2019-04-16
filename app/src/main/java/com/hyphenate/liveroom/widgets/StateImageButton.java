package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * Created by zhangsong on 19-4-8
 */
public class StateImageButton extends AppCompatImageButton implements IBorderView {
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
    public StateImageButton setBorder(Border state) {
        stateHelper.changeBorder(this, state);
        return this;
    }

    @Override
    public Border getBorder() {
        return stateHelper.getBorder();
    }
}
