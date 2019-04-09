package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.hyphenate.liveroom.R;

/**
 * Created by zhangsong on 19-4-8
 */
public class LayoutMusicController extends FrameLayout implements IStateView {
    private static final String TAG = "LayoutMusicController";

    private StateHelper stateHelper;

    public LayoutMusicController(Context context) {
        this(context, null);
    }

    public LayoutMusicController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LayoutMusicController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_music_controller, this);
        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);
    }

    @Override
    public LayoutMusicController setState(State state) {
        stateHelper.changeState(this, state);
        return this;
    }

    @Override
    public State getState() {
        return stateHelper.getState();
    }
}
