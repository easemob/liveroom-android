package com.hyphenate.liveroom.manager;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.liveroom.Constant;
import com.hyphenate.liveroom.entities.ChatRoom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangsong on 19-4-9
 */
public class HttpRequestManager {
    public interface IRequestListener<T> {
        public void onSuccess(T t);

        public void onFailed(int errCode, String desc);
    }

    private static final String TAG = "HttpRequestManager";

    private static final String BASEURL = "http://tcapp.easemob.com";
    private static final int ERR_INTERNAL = -1;

    private static volatile HttpRequestManager INSTANCE;

    private RequestQueue requestQueue;

    public static HttpRequestManager getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpRequestManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpRequestManager();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void createChatRoom(String roomName, String password, String desc,
                               boolean allowAudienceTalk, IRequestListener<ChatRoom> listener) {
        JSONObject requestObj = new JSONObject();
        try {
            requestObj.put("roomName", roomName);
            requestObj.put("password", password);
            requestObj.put("desc", desc);
            requestObj.put("allowAudienceTalk", allowAudienceTalk);
            requestObj.put("imChatRoomMaxusers", 50);
            requestObj.put("confrDelayMillis", 60000);
            requestObj.put("memRole", EMConferenceManager.EMConferenceRole.Audience.code); // 1 - 观众，3 - 主播，7 - Admin
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = String.format("%s/app/%s/create/talk/room",
                BASEURL,
                PreferenceManager.getInstance().getCurrentUsername());
        final Request request = new JsonObjectRequest(Request.Method.POST, url, requestObj.toString(), response -> {
            Log.i(TAG, "createChatRoom onResponse: " + response);
            if (listener == null) return;
            ChatRoom chatRoom = new Gson().fromJson(response.toString(), ChatRoom.class);
            listener.onSuccess(chatRoom);
        }, error -> {
            if (listener == null) return;
            Pair<Integer, String> result = handleError(error);
            listener.onFailed(result.first, result.second);
        });

        requestQueue.add(request);
    }

    public void deleteChatRoom(String roomId, IRequestListener<Void> listener) {
        String url = String.format("%s/app/%s/delete/talk/room/%s",
                BASEURL,
                PreferenceManager.getInstance().getCurrentUsername(),
                roomId);

        Request request = new StringRequest(Request.Method.DELETE, url, response -> {
            Log.i(TAG, "deleteChatRoom onResponse: " + response);
            if (listener != null) {
                listener.onSuccess(null);
            }
        }, error -> {
            if (listener == null) return;
            Pair<Integer, String> result = handleError(error);
            listener.onFailed(result.first, result.second);
        });

        requestQueue.add(request);
    }

    public void getChatRooms(int start, int count, final IRequestListener<List<ChatRoom>> listener) {
        String url = String.format("%s/app/talk/rooms/%s/%s",
                BASEURL,
                start,
                count);

        Log.i(TAG, "getChatRooms url: " + url);

        Request request = new StringRequest(url, response -> {
            Log.i(TAG, "getChatRooms onResponse: " + response);
            try {
                JSONObject object = new JSONObject(response);
                JSONArray array = object.optJSONArray("list");

                List<ChatRoom> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    ChatRoom chatRoom = new Gson().fromJson(obj.toString(), ChatRoom.class);
                    list.add(chatRoom);
                }

                if (listener != null) {
                    listener.onSuccess(list);
                }

                Log.i(TAG, "getChatRooms onResponse list: " + Arrays.toString(list.toArray()));
            } catch (JSONException e) {
                if (listener == null) return;
                listener.onFailed(ERR_INTERNAL, e.getMessage());
            }
        }, error -> {
            if (listener == null) return;
            Pair<Integer, String> result = handleError(error);
            listener.onFailed(result.first, result.second);
        });

        requestQueue.add(request);
    }

    public void occupyMic(String roomId, String username, final IRequestListener<Void> listener) {
        String url = String.format("%s/app/mic/%s/%s",
                BASEURL,
                roomId,
                username);

        Log.i(TAG, "occupyMic, url: " + url);

        Request request = new StringRequest(url, response -> {
            Log.i(TAG, "occupyMic onResponse: " + response);
            if (listener == null) return;
            try {
                JSONObject object = new JSONObject(response);
                boolean result = object.optBoolean("status", false);
                if (result) {
                    listener.onSuccess(null);
                } else {
                    listener.onFailed(Constant.ERROR_MIC_OCCUPY_FAILED, "Occupy mic failed.");
                }
            } catch (JSONException e) {
                listener.onFailed(-1, e.getMessage());
            }
        }, error -> {
            if (listener == null) return;
            Pair<Integer, String> result = handleError(error);
            Log.i(TAG, "occupyMic onError: " + result.first + " - " + result.second);
            listener.onFailed(result.first, result.second);
        });

        requestQueue.add(request);
    }

    public void releaseMic(String roomId, String username, final IRequestListener<Void> listener) {
        String url = String.format("%s/app/discardmic/%s/%s",
                BASEURL,
                roomId,
                username);

        Log.i(TAG, "releaseMic, url: " + url);

        Request request = new StringRequest(Request.Method.DELETE, url, response -> {
            Log.i(TAG, "releaseMic onResponse: " + response);
            if (listener == null) return;
            listener.onSuccess(null);
        }, error -> {
            if (listener == null) return;
            Pair<Integer, String> result = handleError(error);
            Log.i(TAG, "releaseMic onError: " + result.first + " - " + result.second);
            listener.onFailed(result.first, result.second);
        });

        requestQueue.add(request);
    }

    private HttpRequestManager() {
    }

    private Pair<Integer, String> handleError(VolleyError error) {
        if (error.networkResponse == null) {
            return new Pair<>(ERR_INTERNAL, "");
        }
        int errCode = error.networkResponse.statusCode;
        String msg = error.networkResponse.data == null ? null : new String(error.networkResponse.data);
        return new Pair<>(errCode, msg);
    }
}
