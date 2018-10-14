package com.example.chatme.chatme;

public class ChatHistory {
    private String chatroom;
    private  String chatdate;
    private  String chatmate;
    private  String gender;

    public String getChatroom() {
        return chatroom;
    }

    public void setChatroom(String chatroom) {
        this.chatroom = chatroom;
    }

    public String getChatdate() {
        return chatdate;
    }

    public void setChatdate(String chatdate) {
        this.chatdate = chatdate;
    }

    public String getChatmate() {
        return chatmate;
    }

    public void setChatmate(String chatmate) {
        this.chatmate = chatmate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
