package com.hyphenate.liveroom;

import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.liveroom.utils.PreferenceManager;

/**
 * Created by zhangsong on 19-3-28
 */
public class HomeApp extends Application {
    private static final String TAG = "HomeApp";

    private static Application app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // 初始化环信SDK
        EMClient.getInstance().init(app, new EMOptions());
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(BuildConfig.DEBUG);

        PreferenceManager.init(this);
    }

    public Application getApp() {
        return app;
    }
}
