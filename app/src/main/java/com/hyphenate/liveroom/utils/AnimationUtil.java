package com.hyphenate.liveroom.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

/**
 * Created by zhangsong on 19-4-12
 */
public class AnimationUtil {
    public static AnimationSet create() {
        // 组合动画设置
        AnimationSet setAnimation = new AnimationSet(true);

        setAnimation.setRepeatMode(Animation.RESTART);
        setAnimation.setRepeatCount(1);// 设置了循环一次,但无效
        setAnimation.setFillAfter(true);

        // 旋转动画
        Animation rotate = new RotateAnimation(0, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1250);

        // 透明度动画
        Animation alpha1 = new AlphaAnimation(0.5f, 1f);
        alpha1.setDuration(250);

        // 缩放动画
        Animation scale1 = new ScaleAnimation(1.5f, 1f, 1.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale1.setDuration(250);

        // 透明度动画
        Animation alpha2 = new AlphaAnimation(1f, 0f);
        alpha2.setDuration(250);
        alpha2.setStartOffset(1000);

        // 缩放动画
        Animation scale2 = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale2.setDuration(250);
        scale2.setStartOffset(1000);

        // 将创建的子动画添加到组合动画里
        setAnimation.addAnimation(rotate);
        setAnimation.addAnimation(alpha1);
        setAnimation.addAnimation(scale1);
        setAnimation.addAnimation(alpha2);
        setAnimation.addAnimation(scale2);

        return setAnimation;
    }
}
