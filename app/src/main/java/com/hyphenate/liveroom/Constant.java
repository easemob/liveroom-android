package com.hyphenate.liveroom;

public class Constant {
    public static final String EXTRA_OWNER_NAME = "ownerName";
    public static final String EXTRA_ROOM_NAME = "roomName";
    public static final String EXTRA_PASSWORD = "password";

    public static final String EXTRA_CHAT_TYPE = "chatType";
    public static final String EXTRA_CHATROOM_ID = "chatroomId";
    public static final String EXTRA_CONFERENCE_ID = "conferenceId";

    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;

    public static final String EM_CONFERENCE_OP = "em_conference_op";
    public static final String OP_REQUEST_TOBE_SPEAKER = "request_tobe_speaker";
    public static final String OP_REQUEST_TOBE_AUDIENCE = "request_tobe_audience";
    public static final String OP_REQUEST_TOBE_REJECTED = "request_tobe_rejected";

    // 互动模式
    public static final String ROOM_TYPE_COMMUNICATION = "communication";
    // 主持模式
    public static final String ROOM_TYPE_HOST = "host";
    // 抢麦模式
    public static final String ROOM_TYPE_MONOPOLY = "monopoly";
}
