package com.hyphenate.liveroom.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SystemShareUtils {
    //微信朋友圈发送界面
    public static final String NAME_ACTIVITY_WECHAT_CIRCLE_PUBLISH = "com.tencent.mm.ui.tools.ShareToTimeLineUI";
    //微信好友发送界面
    public static final String NAME_ACTIVITY_WECHAT_FRIEND = "com.tencent.mm.ui.tools.ShareImgUI";


    public static void shareTextAndPicToWechat(Context context, String activityName, String text, List<String> files) {

        String packageName = "com.tencent.mm"; //微信包名

        if (isWeixinAvailable(context)) {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(packageName, activityName);
            intent.setComponent(comp);
            ArrayList<Uri> imageUris = new ArrayList<>();
            if (null != files) {
                for (String f : files) {
                    // 7.0以及以上需要使用MediaStore.Images.Media.insertImage转换一下uri
                    imageUris.add(Uri.fromFile(new File(f)));
                }
            }
            if (!imageUris.isEmpty()) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                intent.putExtra("Kdescription", text); // 作用域朋友圈，对好友不会有影响
            } else {
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_TEXT, text);
            }

            context.startActivity(intent);
        } else {
            Toast.makeText(context.getApplicationContext(), "请先安装微信APP", Toast.LENGTH_SHORT).show();
        }


    }

    // 判断手机内是否安装了微信APP
    private static boolean isWeixinAvailable(Context context) {
        PackageManager packageManager = context.getPackageManager(); // 获取packageManager
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0); // 获取所有已安装程序的包信息
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                if ("com.tencent.mm".equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }
}
