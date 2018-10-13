package com.example.chatme.chatme;

public class UserDataNotif {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getChatroom() {
        return chatroom;
    }

    public void setChatroom(String chatroom) {
        this.chatroom = chatroom;
    }

    private String name;
    private String id;
    private String gender;
    private Boolean read;
    private String chatroom;

    public UserDataNotif()
    {

    }

    public UserDataNotif(String name, String id, String gender, String chatroom)
    {
        this.name = name;
        this.id = id;
        this.gender = gender;
        this.read = false;
        this.chatroom = chatroom;
    }
}
