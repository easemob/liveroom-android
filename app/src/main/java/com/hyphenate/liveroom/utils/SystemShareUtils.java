package com.hyphenate.liveroom.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.util.List;

public class SystemShareUtils {

    // 判断手机内是否安装了微信APP
    private static boolean isAvailable(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager(); // 获取packageManager
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0); // 获取所有已安装程序的包信息
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                if (packageName != null && packageName.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void shareQQFriend(Context context, String msgTitle, String msgText) {
        shareMsg(context, "com.tencent.mobileqq",
                "com.tencent.mobileqq.activity.JumpActivity", "QQ", msgTitle, msgText);
    }

    public static void shareWeChatFriend(Context context, String msgTitle, String msgText) {
        shareMsg(context, "com.tencent.mm",
                "com.tencent.mm.ui.tools.ShareImgUI", "微信", msgTitle, msgText);
    }


    private static void shareMsg(Context context, String packageName, String activityName, String appName, String msgTitle, String msgText) {
        if (!packageName.isEmpty() && !isAvailable(context, packageName)) {
            Toast.makeText(context, "请先安装" + appName, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent("android.intent.action.SEND");
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        if (!packageName.isEmpty()) {
            intent.setComponent(new ComponentName(packageName, activityName));
            context.startActivity(intent);
        } else {
            context.startActivity(Intent.createChooser(intent, msgTitle));
        }
    }
}
