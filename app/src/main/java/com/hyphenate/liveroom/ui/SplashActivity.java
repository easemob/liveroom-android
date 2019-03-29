package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.liveroom.R;

/**
 * Created by zhangsong on 19-3-28
 */
public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    private static final int TIME_DELAY = 2000;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_splash);

        RelativeLayout rootLayout = findViewById(R.id.splash_root);
        TextView versionText = findViewById(R.id.tv_version);

        versionText.setText(EMClient.VERSION);
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1500);
        rootLayout.startAnimation(animation);

        new Handler().postDelayed(() -> {
            if (EMClient.getInstance().isLoggedInBefore()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, TIME_DELAY);

    }
}
