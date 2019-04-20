package com.hyphenate.liveroom.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.liveroom.R;
import com.hyphenate.liveroom.utils.DimensUtil;

import java.util.ArrayList;
import java.util.List;

public class EaseTipDialog extends Dialog {
    public interface OnClickListener {
        void onClick(EaseTipDialog dialog, View v);
    }

    private ImageView ivTipIcon;
    private TextView tvTitle;
    private TextView tvMessage;
    private ImageView ivCancel;
    private LinearLayout btnContainer;

    private TipDialogStyle dialogStyle = TipDialogStyle.DEFAULT;
    private String title = "";
    private String content = "";

    private List<ButtonHolder> buttonHolders = new ArrayList<>();

    public static enum TipDialogStyle {
        DEFAULT,
        INFO,
        ERROR
    }

    public EaseTipDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(true);
    }

    public EaseTipDialog(Context context, TipDialogStyle dialogStyle) {
        super(context);
        setCanceledOnTouchOutside(true);
        this.dialogStyle = dialogStyle;
    }

    public void setStyle(TipDialogStyle dialogStyle) {
        this.dialogStyle = dialogStyle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String content) {
        this.content = content;
    }

    public void setButtonHolders(List<ButtonHolder> holders) {
        buttonHolders.clear();
        buttonHolders.addAll(holders);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ease_tip_dialog);
        initViews();
        if (dialogStyle != TipDialogStyle.DEFAULT) {
            switch (dialogStyle) {
                case INFO:
                    ivTipIcon.setImageResource(R.drawable.em_ic_tips);
                    break;
                case ERROR:
                    ivTipIcon.setImageResource(R.drawable.em_ic_error);
                    break;
            }
        }
        tvTitle.setText(title);
        tvMessage.setText(content);
        for (ButtonHolder holder : buttonHolders) {
            addButton(holder.title, holder.textColor, holder.bgColor, holder.listener);
        }

        ivCancel.setOnClickListener(v -> dismiss());
    }

    private void initViews() {
        ivTipIcon = findViewById(R.id.iv_tip_icon);
        tvTitle = findViewById(R.id.tv_title);
        tvMessage = findViewById(R.id.tv_message);
        ivCancel = findViewById(R.id.iv_cancel);
        btnContainer = findViewById(R.id.container_btn);
    }

    private EaseTipDialog addButton(String title, int textColor, int bgColor, OnClickListener listener) {
        View button = createButton(title, textColor, bgColor, v -> {
            if (listener != null) {
                listener.onClick(EaseTipDialog.this, v);
            }
        });

        if (btnContainer.getChildCount() == 0) {
            btnContainer.setVisibility(View.VISIBLE);
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
            params.topMargin = DimensUtil.dp2px(getContext(), 10);
        }

        btnContainer.addView(button);

        return this;
    }

    private View createButton(String title, int textColor, int bgColor, View.OnClickListener listener) {
        TextView button = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                DimensUtil.dp2px(getContext(), 40));
        button.setLayoutParams(params);
        button.setBackgroundColor(bgColor);
        button.setTextColor(textColor);
        button.setTextSize(14);
        button.setText(title);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(listener);
        return button;
    }

    public static class Builder {
        private Context context;
        private String title = "";
        private String content = "";
        private TipDialogStyle style = TipDialogStyle.DEFAULT;
        private List<ButtonHolder> buttonHolders = new ArrayList<>();

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setStyle(TipDialogStyle dialogStyle) {
            this.style = dialogStyle;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int resTitle) {
            this.title = context.getResources().getString(resTitle);
            return this;
        }

        public Builder setMessage(String message) {
            this.content = message;
            return this;
        }

        public Builder setMessage(int resMessage) {
            this.content = context.getResources().getString(resMessage);
            return this;
        }

        public Builder addButton(String title, int textColor, int bgColor, OnClickListener listener) {
            buttonHolders.add(new ButtonHolder(title, textColor, bgColor, listener));
            return this;
        }

        public EaseTipDialog build() {
            EaseTipDialog dialog = new EaseTipDialog(context, style);
            dialog.setMessage(content);
            dialog.setTitle(title);
            dialog.setButtonHolders(buttonHolders);
            return dialog;
        }
    }

    private static class ButtonHolder {
        private String title;
        private int textColor;
        private int bgColor;
        private OnClickListener listener;

        public ButtonHolder(String title, int textColor, int bgColor, OnClickListener listener) {
            this.title = title;
            this.textColor = textColor;
            this.bgColor = bgColor;
            this.listener = listener;
        }
    }
}
