package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * Created by zhangsong on 19-4-8
 */
public class BorderImageButton extends AppCompatImageButton implements IBorderView {
    private static final String TAG = "StateImageButton";

    private BorderHelper borderHelper;

    public BorderImageButton(Context context) {
        this(context, null);
    }

    public BorderImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        borderHelper = new BorderHelper();
        borderHelper.init(this, attrs);
    }

    @Override
    public BorderImageButton setBorder(Border state) {
        borderHelper.changeBorder(this, state);
        return this;
    }

    @Override
    public Border getBorder() {
        return borderHelper.getBorder();
    }
}
