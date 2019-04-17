package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.hyphenate.liveroom.R;

/**
 * Created by zhangsong on 19-4-8
 */
public class MusicControllerView extends FrameLayout implements IBorderView {
    private static final String TAG = "LayoutMusicController";

    private StateHelper stateHelper;

    public MusicControllerView(Context context) {
        this(context, null);
    }

    public MusicControllerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_music_controller, this);
        stateHelper = new StateHelper();
        stateHelper.init(this, attrs);
    }

    @Override
    public MusicControllerView setBorder(Border state) {
        stateHelper.changeBorder(this, state);
        return this;
    }

    @Override
    public Border getBorder() {
        return stateHelper.getBorder();
    }
}
