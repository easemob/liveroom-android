package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by zhangsong on 19-4-8
 */
public class BorderTextButton extends AppCompatTextView implements IBorderView {
    public interface OnClickListener {
        void onClick(BorderTextButton btn);
    }
    private static final String TAG = "StateImageButton";

    private BorderHelper borderHelper;

    public BorderTextButton(Context context) {
        this(context, null);
    }

    public BorderTextButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        borderHelper = new BorderHelper();
        borderHelper.init(this, attrs);
    }

    @Override
    public BorderTextButton setBorder(Border state) {
        borderHelper.changeBorder(this, state);
        return this;
    }

    @Override
    public Border getBorder() {
        return borderHelper.getBorder();
    }
}
