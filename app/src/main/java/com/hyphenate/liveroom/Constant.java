package com.hyphenate.liveroom;

public class Constant {
    public static final String EXTRA_ROOM_NAME = "roomName";
    public static final String EXTRA_CREATOR = "ownerName";
    public static final String EXTRA_PASSWORD = "password";

    public static final String EXTRA_CHAT_TYPE = "chatType";
    public static final String EXTRA_TEXT_CHATROOM_ID = "userId";
    public static final String EXTRA_VOICE_CONF_ID = "userId";

    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;

    // 互动模式
    public static final String ROOM_TYPE_COMMUNICATION = "communication";
    // 主持模式
    public static final String ROOM_TYPE_HOST = "host";
    // 抢麦模式
    public static final String ROOM_TYPE_MONOPOLY = "monopoly";
}
