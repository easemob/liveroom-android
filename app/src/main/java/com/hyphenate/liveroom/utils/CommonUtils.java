/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.liveroom.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    /**
     * check if network avalable
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * check if sdcard exist
     *
     * @return
     */
    public static boolean isSdcardExist() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    /**
     * get top activity
     *
     * @param context
     * @return
     */
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity.getClassName();
        else
            return "";
    }


    public static EMConversation.EMConversationType getConversationType(int chatType) {
        if (chatType == 1) {
            return EMConversation.EMConversationType.Chat;
        } else if (chatType == 2) {
            return EMConversation.EMConversationType.GroupChat;
        } else {
            return EMConversation.EMConversationType.ChatRoom;
        }
    }

    /**
     * TextView 显示高亮
     * @param view
     * @param str1 要高亮显示的文字（输入的关键字）
     * @param str2 包含高亮文字的字符串
     */
    public static void initHightLight(TextView view, CharSequence str1, String str2) {
        SpannableString sp = new SpannableString(str2);
        if (str1 != null) {
            // 正则匹配
            Pattern p = Pattern.compile(str1.toString());
            Matcher m = p.matcher(sp);
            // 查找下一个
            while (m.find()) {
                // 字符开始下标
                int start = m.start();
                // 字符结束下标
                int end = m.end();
                try {
                    // 设置高亮
                    sp.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        view.setText(sp);
    }

    /**
     * 获取版本名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVersionName(Context context) {
        PackageInfo pInfo = getPackageInfo(context);
        return (pInfo == null) ? "" : pInfo.versionName;
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        PackageInfo pInfo = getPackageInfo(context);
        return pInfo == null ? -1 : pInfo.versionCode;
    }

    // 通过PackageInfo得到应用的包名
    private static PackageInfo getPackageInfo(Context context) {
        try {
            // 通过PackageManager可以得到PackageInfo
            PackageManager pManager = context.getPackageManager();
            return pManager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
