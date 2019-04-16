package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by zhangsong on 19-4-8
 */
public class StateTextButton extends AppCompatTextView implements IBorderView {
    public interface OnClickListener {
        void onClick(StateTextButton btn);
    }
    private static final String TAG = "StateImageButton";

    private StateHelper stateHelper;

    public StateTextButton(Context context) {
        this(context, null);
    }

    public StateTextButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);
    }

    @Override
    public StateTextButton setBorder(Border state) {
        stateHelper.changeBorder(this, state);
        return this;
    }

    @Override
    public Border getBorder() {
        return stateHelper.getBorder();
    }
}
