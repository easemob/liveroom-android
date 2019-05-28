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

    private static volatile CountDownManager INSTANCE;

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

    private CountDownManager() {
    }

    public void startCountDown(int seconds, final CountDownCallback callback) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (countDownCallback != null) {
            countDownCallback.onCancel();
        }

        countDownCallback = callback;
        // 原生CountDownTimer会出现跳秒问题
        countDownTimer = new CountDownTimer(seconds * 1000, 200) {
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (countDownCallback != null) {
            countDownCallback.onCancel();
            countDownCallback = null;
        }
    }
}