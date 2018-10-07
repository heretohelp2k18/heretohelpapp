package com.example.chatme.chatme;

public class UserDataNotif {
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
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

    private String firstname;
    private String id;
    private String gender;
    private Boolean read;
    private String chatroom;

    public UserDataNotif()
    {

    }

    public UserDataNotif(String fname, String id, String gender, String chatroom)
    {
        this.firstname = fname;
        this.id = id;
        this.gender = gender;
        this.read = false;
        this.chatroom = chatroom;
    }
}
