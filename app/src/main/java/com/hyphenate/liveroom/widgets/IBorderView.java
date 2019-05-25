package com.hyphenate.liveroom.widgets;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.widgets.border.GrayBorderDrawable;
import com.hyphenate.liveroom.widgets.border.GreenBorderDrawable;
import com.hyphenate.liveroom.widgets.border.NoneBorderDrawable;
import com.hyphenate.liveroom.widgets.border.RedBorderDrawable;

/**
 * Created by zhangsong on 19-4-8
 */
public interface IBorderView {

    public enum Border {
        NONE,
        GRAY,
        GREEN,
        RED;

        public static Border from(int border) {
            if (border == 1) {
                return GRAY;
            }

            if (border == 2) {
                return GREEN;
            }

            if (border == 3) {
                return RED;
            }

            return NONE;
        }
    }

    public IBorderView setBorder(Border border);

    public Border getBorder();

    public static final class BorderHelper {
        private Border border = Border.NONE;

        public void changeBorder(View v, Border border) {
            changeBorder(v, border, v.getResources().getColor(R.color.color_gray));
        }

        public void changeBorder(View v, Border border,@ColorInt int bgColor) {
            this.border = border;
            switch (border) {
                case NONE:
                    v.setBackground(new NoneBorderDrawable(v.getContext(), bgColor));
                    break;
                case GRAY:
                    v.setBackground(new GrayBorderDrawable(v.getContext(), bgColor));
                    break;
                case GREEN:
                    v.setBackground(new GreenBorderDrawable(v.getContext(), bgColor));
                    break;
                case RED:
                    v.setBackground(new RedBorderDrawable(v.getContext(), bgColor));
                    break;
            }
        }

        public void init(View v, AttributeSet attrs) {
            if (v instanceof IBorderView) {
                if (attrs != null) {
                    TypedArray typedArray = v.getContext().getResources()
                            .obtainAttributes(attrs, R.styleable.BorderView);
                    int i = typedArray.getInt(R.styleable.BorderView_border, 0);
                    border = Border.from(i);
                    typedArray.recycle();
                }
                changeBorder(v, border);
            } else {
                throw new IllegalArgumentException("View not an IBorderView impl.");
            }
        }

        public Border getBorder() {
            return border;
        }
    }
}
