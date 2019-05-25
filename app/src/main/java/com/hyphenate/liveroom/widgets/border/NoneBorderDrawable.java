package com.hyphenate.liveroom.widgets.border;

import android.content.Context;
import android.support.annotation.ColorInt;

public class NoneBorderDrawable extends RectBorderDrawable {

    public NoneBorderDrawable(Context context, @ColorInt int filledColor) {
        super(context, filledColor, filledColor);
    }

}
