package com.hyphenate.liveroom.manager;

import android.os.CountDownTimer;
import android.util.Log;

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
    private int startSecond;

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

        startSecond = seconds;
        countDownCallback = callback;
        // 原生CountDownTimer会出现跳秒问题
        countDownTimer = new CountDownTimer(seconds * 2 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                startSecond--;

                if (countDownCallback != null) {
                    countDownCallback.onTick(startSecond * 1000);
                }

                if (startSecond == 0) {
                    onFinish();
                    cancel();
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
            countDownTimer = null;
        }
    }
}