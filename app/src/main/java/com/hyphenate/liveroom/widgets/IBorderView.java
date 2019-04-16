package com.hyphenate.liveroom.widgets;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.hyphenate.liveroom.R;

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

    public static final class StateHelper {
        private Border border = Border.NONE;

        public void changeBorder(View v, Border border) {
            this.border = border;
            switch (border) {
                case NONE:
                    v.setBackgroundResource(R.drawable.em_bg_border_none);
                    break;
                case GRAY:
                    v.setBackgroundResource(R.drawable.em_bg_border_gray);
                    break;
                case GREEN:
                    v.setBackgroundResource(R.drawable.em_bg_border_green);
                    break;
                case RED:
                    v.setBackgroundResource(R.drawable.em_bg_border_red);
                    break;
            }
        }

        public void init(View v, AttributeSet attrs) {
            if (v instanceof IBorderView) {
                if (attrs != null) {
                    TypedArray typedArray = v.getContext().getResources()
                            .obtainAttributes(attrs, R.styleable.BorderView);
                    Integer i = typedArray.getInteger(R.styleable.BorderView_border, 0);
                    border = Border.from(i);
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
