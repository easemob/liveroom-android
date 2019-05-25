package com.hyphenate.liveroom.widgets.border;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import com.hyphenate.util.DensityUtil;

public class RectBorderDrawable extends GradientDrawable {

    public RectBorderDrawable(Context context, @ColorInt int filledColor, @ColorInt int borderColor) {
        super();
        setShape(RECTANGLE);
        setColor(filledColor);
        setStroke(dp2px(context, 1), borderColor);
    }

    private int dp2px(Context context, float dpVal) {
        return DensityUtil.dip2px(context, dpVal);
    }

}
