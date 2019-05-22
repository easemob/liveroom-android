package com.hyphenate.liveroom.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.hyphenate.liveroom.R;

/**
 * 播放音效工具类
 */
public class SoundPlayUtil {

    // SoundPool 对象
    public SoundPool mSoundPlayer;
    private boolean mInitialed;
    private int giftSoundId;
    private int likeSoundId;

    public SoundPlayUtil(Context context) {
        mSoundPlayer = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);

        giftSoundId = mSoundPlayer.load(context, R.raw.gift, 1);
        likeSoundId = mSoundPlayer.load(context, R.raw.like, 1);

        mSoundPlayer.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mInitialed = true;
            }
        });
    }

    /**
     * 播放礼物音效
     */
    public void playGift() {
        playSound(giftSoundId);
    }

    /**
     * 播放点赞音效
     */
    public void playLike() {
        playSound(likeSoundId);
    }

    private void playSound(int soundId) {
        if (!mInitialed) {
            return;
        }
        mSoundPlayer.play(soundId, 1, 1, 1, 0, 1);
    }

    public void release() {
        //销毁的时候释放SoundPool资源
        if (mSoundPlayer != null) {
            mSoundPlayer.release();
            mSoundPlayer = null;
        }
    }
}
