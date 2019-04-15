package com.hyphenate.liveroom.widgets;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.hyphenate.liveroom.R;

/**
 * Created by zhangsong on 19-4-8
 */
public interface IStateView {

    public enum State {
        DISABLE,
        ENABLEOFF,
        ENABLEON;

        public static State from(int state) {
            if (state == 1) {
                return ENABLEOFF;
            }

            if (state == 2) {
                return ENABLEON;
            }

            return DISABLE;
        }
    }

    public IStateView setState(State state);

    public State getState();

    public static final class StateHelper {
        private State state = State.DISABLE;

        public void changeState(View v, State state) {
            this.state = state;
            switch (state) {
                case DISABLE:
                    v.setBackgroundResource(R.drawable.em_bg_state_disable);
                    break;
                case ENABLEON:
                    v.setBackgroundResource(R.drawable.em_bg_state_enable_on);
                    break;
                case ENABLEOFF:
                    v.setBackgroundResource(R.drawable.em_bg_state_enable_off);
                    break;
            }
        }

        public void init(View v, AttributeSet attrs) {
            if (v instanceof IStateView) {
                if (attrs != null) {
                    TypedArray typedArray = v.getContext().getResources()
                            .obtainAttributes(attrs, R.styleable.StateView);
                    Integer i = typedArray.getInteger(R.styleable.StateView_state, 0);
                    state = State.from(i);
                }

                changeState(v, state);
            } else {
                throw new IllegalArgumentException("View not a IStateView impl.");
            }
        }

        public State getState() {
            return state;
        }
    }
}
