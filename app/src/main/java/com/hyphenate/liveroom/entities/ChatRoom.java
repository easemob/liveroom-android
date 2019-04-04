package com.hyphenate.liveroom.entities;

/**
 * Created by zhangsong on 19-3-30
 */
public class ChatRoom {
    private String id;
    private String name;
    private String introduce;

    public String getId() {
        return id;
    }

    public ChatRoom setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ChatRoom setName(String name) {
        this.name = name;
        return this;
    }

    public String getIntroduce() {
        return introduce;
    }

    public ChatRoom setIntroduce(String introduce) {
        this.introduce = introduce;
        return this;
    }
}
