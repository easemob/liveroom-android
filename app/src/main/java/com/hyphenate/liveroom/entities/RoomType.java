package com.hyphenate.liveroom.entities;

import com.hyphenate.liveroom.Constant;

/**
 * Created by zhangsong on 19-4-18
 */
public enum RoomType {
    COMMUNICATION(Constant.ROOM_TYPE_COMMUNICATION, "互动模式", "互动模式下所有主播可以自由发言"),
    HOST(Constant.ROOM_TYPE_HOST, "主持模式", "主持模式下管理员分配的主播获得发言权"),
    MONOPOLY(Constant.ROOM_TYPE_MONOPOLY, "抢麦模式", "抢麦模式下所有主播通过抢麦获得发言权");

    public static RoomType from(String id) {
        if (Constant.ROOM_TYPE_HOST.equals(id)) {
            return HOST;
        }
        if (Constant.ROOM_TYPE_MONOPOLY.equals(id)) {
            return MONOPOLY;
        }
        return COMMUNICATION;
    }

    private String id;
    private String name;
    private String desc;

    RoomType(String id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
