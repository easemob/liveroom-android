package com.hyphenate.liveroom.entities;

/**
 * Created by zhangsong on 19-3-30
 */
public class ChatRoom {
    private String roomId; // 服务端生成唯一的语聊室ID
    private String roomName;
    private String ownerName; // 创建这者name，jid中的username
    private String rtcConfrId; // 音视频会议ID
    private String rtcConfrPassword; // 会议密码
    private String rtcConfrCreateTime; // yyyy-MM-dd HH:mm:ss
    // 10: 普通互动会议
    // 11: 大会议（混音）
    // 12: 互动会议（混音）
    private int rtcConfrType;
    private boolean rtcConfrMixed; // 混音
    private int rtcConfrAudienceLimit; // 观众上限
    private int rtcConfrTalkerLimit; // 主播上限
    private boolean allowAudienceTalk; // 允许观众上麦

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getRtcConfrId() {
        return rtcConfrId;
    }

    public void setRtcConfrId(String rtcConfrId) {
        this.rtcConfrId = rtcConfrId;
    }

    public String getRtcConfrPassword() {
        return rtcConfrPassword;
    }

    public void setRtcConfrPassword(String rtcConfrPassword) {
        this.rtcConfrPassword = rtcConfrPassword;
    }

    public String getRtcConfrCreateTime() {
        return rtcConfrCreateTime;
    }

    public void setRtcConfrCreateTime(String rtcConfrCreateTime) {
        this.rtcConfrCreateTime = rtcConfrCreateTime;
    }

    public int getRtcConfrType() {
        return rtcConfrType;
    }

    public void setRtcConfrType(int rtcConfrType) {
        this.rtcConfrType = rtcConfrType;
    }

    public boolean isRtcConfrMixed() {
        return rtcConfrMixed;
    }

    public void setRtcConfrMixed(boolean rtcConfrMixed) {
        this.rtcConfrMixed = rtcConfrMixed;
    }

    public int getRtcConfrAudienceLimit() {
        return rtcConfrAudienceLimit;
    }

    public void setRtcConfrAudienceLimit(int rtcConfrAudienceLimit) {
        this.rtcConfrAudienceLimit = rtcConfrAudienceLimit;
    }

    public int getRtcConfrTalkerLimit() {
        return rtcConfrTalkerLimit;
    }

    public void setRtcConfrTalkerLimit(int rtcConfrTalkerLimit) {
        this.rtcConfrTalkerLimit = rtcConfrTalkerLimit;
    }

    public boolean isAllowAudienceTalk() {
        return allowAudienceTalk;
    }

    public void setAllowAudienceTalk(boolean allowAudienceTalk) {
        this.allowAudienceTalk = allowAudienceTalk;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", rtcConfrId='" + rtcConfrId + '\'' +
                ", rtcConfrPassword='" + rtcConfrPassword + '\'' +
                ", rtcConfrCreateTime='" + rtcConfrCreateTime + '\'' +
                ", rtcConfrType=" + rtcConfrType +
                ", rtcConfrMixed=" + rtcConfrMixed +
                ", rtcConfrAudienceLimit=" + rtcConfrAudienceLimit +
                ", rtcConfrTalkerLimit=" + rtcConfrTalkerLimit +
                ", allowAudienceTalk=" + allowAudienceTalk +
                '}';
    }
}
