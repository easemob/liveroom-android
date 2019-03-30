package com.hyphenate.liveroom.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by zhangsong on 19-3-30
 */
public class BaseFragment extends Fragment {
    private InputMethodManager inputManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
