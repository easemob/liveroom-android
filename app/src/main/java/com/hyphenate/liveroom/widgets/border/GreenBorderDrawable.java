package com.hyphenate.liveroom.widgets.border;

import android.content.Context;
import android.support.annotation.ColorInt;

import com.hyphenate.liveroom.R;

public class GreenBorderDrawable extends RectBorderDrawable {
    public GreenBorderDrawable(Context context, @ColorInt int filledColor) {
        super(context, filledColor, context.getResources().getColor(R.color.color_green));
    }
}
