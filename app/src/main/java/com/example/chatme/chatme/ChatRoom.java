package com.example.chatme.chatme;

public class ChatRoom {
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPsychoid() {
        return psychoid;
    }

    public void setPsychoid(String psychoid) {
        this.psychoid = psychoid;
    }

    private String userid;
    private String psychoid;
    public ChatRoom(){

    }

    public  ChatRoom(String userid, String psychoid)
    {
        this.userid = userid;
        this.psychoid = psychoid;
    }
}
