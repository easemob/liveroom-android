package com.hyphenate.liveroom;

import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.liveroom.manager.HttpRequestManager;
import com.hyphenate.liveroom.manager.PreferenceManager;

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

        EMOptions options = new EMOptions();
        options.setAppKey("1100181024084247#voicechatroom"); //1100181024084247#voicechatroom  easemob-demo#chatdemoui
        // 初始化环信SDK
        EMClient.getInstance().init(app, options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(BuildConfig.DEBUG);

        PreferenceManager.init(this);
        HttpRequestManager.getInstance().init(this);
    }

    public Application getApp() {
        return app;
    }
}
