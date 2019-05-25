package com.hyphenate.liveroom.widgets.border;

import android.content.Context;
import android.support.annotation.ColorInt;

import com.hyphenate.liveroom.R;

public class GrayBorderDrawable extends RectBorderDrawable {

    public GrayBorderDrawable(Context context, @ColorInt int filledColor) {
        super(context, filledColor, context.getResources().getColor(R.color.gray));
    }
}
