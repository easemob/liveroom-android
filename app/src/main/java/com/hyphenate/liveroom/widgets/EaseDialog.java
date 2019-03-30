package com.hyphenate.liveroom.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.DimensUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangsong on 19-3-29
 */
public class EaseDialog {
    public interface OnClickListener {
        void onClick(EaseDialog dialog, View v);
    }

    private static final String TAG = "EaseDialog";

    private Context context;
    private Dialog dialog;

    private FrameLayout container;
    private LinearLayout btnContainer;

    private InputMethodManager inputManager;

    private List<View> buttons = new ArrayList<>();

    protected EaseDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context, R.style.MyDialogStyle);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth() / 4 * 3;

        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_ease, null);
        dialog.setContentView(contentView, new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));

        container = contentView.findViewById(R.id.container);
        btnContainer = contentView.findViewById(R.id.layout_btn);

        inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static EaseDialog create(Context context) {
        return new EaseDialog(context);
    }

    public EaseDialog setContentView(View v) {
        container.addView(v);
        return this;
    }

    public EaseDialog addButton(String title, int textColor, int bgColor, OnClickListener listener) {
        View button = createButton(title, textColor, bgColor, v -> {
            if (listener != null) {
                listener.onClick(EaseDialog.this, v);
            }
        });

        if (buttons.size() == 0) {
            btnContainer.setVisibility(View.VISIBLE);
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
            params.leftMargin = DimensUtil.dp2px(context, 16);
        }

        buttons.add(button);

        btnContainer.setWeightSum(buttons.size());
        btnContainer.addView(button);

        return this;
    }

    public String getText(int layoutId) {
        return ((TextView) container.findViewById(layoutId)).getText().toString();
    }

    public EaseDialog setText(int layoutId, String content) {
        ((TextView) container.findViewById(layoutId)).setText(content);
        return this;
    }

    public EaseDialog setImage(int layoutId, int resId) {
        ((ImageView) container.findViewById(layoutId)).setImageResource(resId);
        return this;
    }

    public EaseDialog setOnClickListener(int layoutId, OnClickListener listener) {
        container.findViewById(layoutId).setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(this, v);
            }
        });
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dismiss(true);
    }

    public void dismiss(boolean withKeyboard) {
        if (withKeyboard) {
            dismissKeyboard();
        }
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    private View createButton(String title, int textColor, int bgColor, View.OnClickListener listener) {
        TextView button = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        button.setLayoutParams(params);
        button.setBackgroundColor(bgColor);
        button.setTextColor(textColor);
        button.setTextSize(16);
        button.setText(title);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(listener);
        return button;
    }

    private void dismissKeyboard() {
        inputManager.hideSoftInputFromWindow(container.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
