package com.hyphenate.liveroom.manager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.liveroom.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangsong on 19-5-8
 */
public class ConferenceAttributesManager {
    public interface OnAttributesUpdateListener {
        public void onAttributesUpdated(Entry[] entries);
    }

    public static class Entry {
        public EMConferenceListener.EMAttributeAction action;
        public String key;
        public String value;

        public Entry(EMConferenceListener.EMAttributeAction action, String key, String value) {
            this.action = action;
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "action=" + action +
                    ", key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    private static final String TAG = "ConferenceAttributesMan";

    private EMConferenceManager conferenceManager;
    private OnAttributesUpdateListener listener;

    public ConferenceAttributesManager(OnAttributesUpdateListener listener) {
        conferenceManager = EMClient.getInstance().conferenceManager();
        this.listener = listener;
    }

    // Map<key, value>
    private Map<String, String> attrsMap = new HashMap<>();
    // Map<key, value>
    private Map<String, String> senderMap;

    public ConferenceAttributesManager addOrUpdateConferenceAttribute(@NonNull String key, @NonNull String value) {
        if (senderMap == null) {
            senderMap = new HashMap<>(attrsMap);
        }
        senderMap.put(key, value);
        return this;
    }

    public ConferenceAttributesManager removeConferenceAttribute(@NonNull String key) {
        if (senderMap == null) {
            senderMap = new HashMap<>(attrsMap);
        }
        senderMap.remove(key);
        return this;
    }

    public void send(EMValueCallBack<Void> callBack) {
        conferenceManager.setConferenceAttribute(Constant.PROPERTY_ATTRS, getAttrValue(), callBack);
        senderMap.clear();
        senderMap = null;
    }

    public void parse(String json) {
        List<Entry> results = new ArrayList<>();
        // Parse new kay-values.
        Map<String, String> newAttrsMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                newAttrsMap.put(key, jsonObject.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 找出原始数据中需要删除的属性.
        Set<String> originalKeySet = attrsMap.keySet();
        for (String key : originalKeySet) {
            if (!newAttrsMap.containsKey(key)) {
                results.add(new Entry(EMConferenceListener.EMAttributeAction.DELETE, key, null));
            }
        }

        // 找出新增或者更新的属性
        Set<String> newKeySet = newAttrsMap.keySet();
        for (String key : newKeySet) {
            if (attrsMap.containsKey(key)) {
                String originalValue = attrsMap.get(key);
                String newValue = newAttrsMap.get(key);
                boolean isValueNoChange = (originalValue == newValue) || (originalValue != null && originalValue.equals(newValue));

                if (!isValueNoChange) { // 属性更新
                    results.add(new Entry(EMConferenceListener.EMAttributeAction.UPDATE, key, newAttrsMap.get(key)));
                }
            } else { // 新增属性
                results.add(new Entry(EMConferenceListener.EMAttributeAction.ADD, key, newAttrsMap.get(key)));
            }
        }

        attrsMap = newAttrsMap;

        if (listener != null) {
            listener.onAttributesUpdated(results.toArray(new Entry[0]));
        }

        Log.i(TAG, "parse, attrsMap: " + attrsMap);
    }

    public String getAttribute(@NonNull String key) {
        return attrsMap.get(key);
    }

    private String getAttrValue() {
        if (senderMap == null) {
            return null;
        }

        Set<Map.Entry<String, String>> set = senderMap.entrySet();
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : set) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
