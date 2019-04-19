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
package com.hyphenate.liveroom.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.hyphenate.liveroom.Constant;

public class PreferenceManager {
    /**
     * name of preference
     */
    public static final String PREFERENCE_NAME = "saveInfo";
    private static SharedPreferences mSharedPreferences;
    private static PreferenceManager mPreferencemManager;
    private static SharedPreferences.Editor editor;

    private static final String SHARED_KEY_CURRENTUSER_USERNAME = "SHARED_KEY_CURRENTUSER_USERNAME";
    private static final String SHARED_KEY_ROOM_TYPE = "shared_key_room_type";
    private static final String SHARED_KEY_ALLOW_REQUEST = "shared_key_allow_request";
    private static final String SHARED_KEY_AUTO_AGREE = "shared_key_auto_agree";
    private static final String SHARED_KEY_BG_MUSIC = "shared_key_bg_music";

    @SuppressLint("CommitPrefEdits")
    private PreferenceManager(Context cxt) {
        mSharedPreferences = cxt.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }

    public static synchronized void init(Context cxt) {
        if (mPreferencemManager == null) {
            mPreferencemManager = new PreferenceManager(cxt);
        }
    }

    /**
     * get instance of PreferenceManager
     *
     * @param
     * @return
     */
    public synchronized static PreferenceManager getInstance() {
        if (mPreferencemManager == null) {
            throw new RuntimeException("please init first!");
        }

        return mPreferencemManager;
    }

    public void setCurrentUserName(String username) {
        editor.putString(SHARED_KEY_CURRENTUSER_USERNAME, username);
        editor.apply();
    }

    public String getCurrentUsername() {
        return mSharedPreferences.getString(SHARED_KEY_CURRENTUSER_USERNAME, null);
    }

    /**
     * ------------------------------ voice chat room -----------------------------
     */

    public String getRoomType() {
        return mSharedPreferences.getString(SHARED_KEY_ROOM_TYPE, Constant.ROOM_TYPE_COMMUNICATION);
    }

    public void setRoomType(String type) {
        editor.putString(SHARED_KEY_ROOM_TYPE, type);
        editor.apply();
    }

    public boolean isAllowRequest() {
        return mSharedPreferences.getBoolean(SHARED_KEY_ALLOW_REQUEST, true);
    }

    public void setAllowRequest(boolean allow) {
        editor.putBoolean(SHARED_KEY_ALLOW_REQUEST, allow);
        editor.apply();
    }

    public boolean isAutoAgree() {
        return mSharedPreferences.getBoolean(SHARED_KEY_AUTO_AGREE, false);
    }

    public void setAutoAgree(boolean auto) {
        editor.putBoolean(SHARED_KEY_AUTO_AGREE, auto);
        editor.apply();
    }

    public boolean withBgMusic() {
        return mSharedPreferences.getBoolean(SHARED_KEY_BG_MUSIC, false);
    }

    public void setBgMusic(boolean with) {
        editor.putBoolean(SHARED_KEY_BG_MUSIC, with);
        editor.apply();
    }
}
