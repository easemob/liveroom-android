package com.hyphenate.liveroom.widgets;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.liveroom.R;
import com.hyphenate.util.EMLog;


public class EaseChatInputMenu extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "EaseChatInputMenu";
    protected LayoutInflater inflater;
    private Handler handler = new Handler();
    private ChatInputMenuListener listener;
    private Context context;
    private boolean inited;
    private EditText editText;
    private ImageButton btnHeart;
    private ImageButton btnGift;
    private boolean ctrlPress = false;

    public EaseChatInputMenu(Context context) {
        this(context, null);
    }

    public EaseChatInputMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatInputMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.ease_widget_chat_input_menu, this);
        editText = findViewById(R.id.et_sendmessage);
        btnHeart = findViewById(R.id.btn_heart);
        btnGift = findViewById(R.id.btn_gift);

        btnHeart.setOnClickListener(this);
        btnGift.setOnClickListener(this);
        editText.setOnClickListener(this);
        editText.requestFocus();
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EMLog.d(TAG, "keyCode:" + keyCode + ", action:" + event.getAction());

                if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        ctrlPress = true;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        ctrlPress = false;
                    }
                }
                return false;
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                EMLog.i(TAG, "actionId:" + actionId);
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN && ctrlPress)) {
                    String s = editText.getText().toString();
                    editText.setText("");
                    if (listener != null) {
                        listener.onSendMessage(s);
                    }
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_heart) {
            if (listener != null) {
                listener.onHeartClicked(v);
            }
        } else if (id == R.id.btn_gift) {
            if (listener != null) {
                listener.onGiftClicked(v);
            }
        } else if (id == R.id.et_sendmessage) {
            if (listener != null) {
                listener.onEditTextClicked();
            }
        }
    }

    public void setChatInputMenuListener(ChatInputMenuListener listener) {
        this.listener = listener;
    }

    public interface ChatInputMenuListener {
        void onSendMessage(String content);

        void onHeartClicked(View view);

        void onGiftClicked(View view);

        void onEditTextClicked();
    }


}
