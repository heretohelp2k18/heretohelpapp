package com.example.chatme.chatme;

public class ChatRoom {

    private String userid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPsychoid() {
        return psychoid;
    }

    public void setPsychoid(String psychoid) {
        this.psychoid = psychoid;
    }

    public String getPsychoname() {
        return psychoname;
    }

    public void setPsychoname(String psychoname) {
        this.psychoname = psychoname;
    }

    private String username;
    private String psychoid;
    private String psychoname;
    public ChatRoom(){

    }

    public  ChatRoom(String userid, String username, String psychoid, String psychoname)
    {
        this.userid = userid;
        this.psychoid = psychoid;
        this.username = username;
        this.psychoname = psychoname;
    }
}
