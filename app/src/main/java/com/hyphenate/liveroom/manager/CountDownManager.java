package com.hyphenate.liveroom.manager;

import android.os.CountDownTimer;

/**
 * Created by zhangsong on 19-4-24
 */
public class CountDownManager {
    public interface CountDownCallback {
        public void onTick(long millisUntilFinished);

        public void onCancel();

        public void onFinish();
    }

    private static final String TAG = "CountDownManager";

    private static CountDownManager INSTANCE;

    private CountDownTimer countDownTimer;
    private CountDownCallback countDownCallback;

    public static CountDownManager getInstance() {
        if (INSTANCE == null) {
            synchronized (CountDownManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CountDownManager();
                }
            }
        }
        return INSTANCE;
    }

    public void startCountDown(int seconds, final CountDownCallback callback) {
        if (countDownTimer != null) {
            return;
        }

        countDownCallback = callback;

        countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (countDownCallback != null) {
                    countDownCallback.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                if (countDownCallback != null) {
                    countDownCallback.onFinish();
                    countDownCallback = null;
                }
            }
        }.start();
    }

    public void stopCountDown() {
        if (countDownCallback != null) {
            countDownCallback.onCancel();
            countDownCallback = null;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = null;
    }
}
