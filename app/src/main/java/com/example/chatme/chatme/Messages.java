package com.example.chatme.chatme;

public class Messages {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    private String id;
    private String comment;
    private String usertype;

    public Messages()
    {

    }

    public Messages(String id, String comment, String usertype)
    {
        this.id = id;
        this.comment = comment;
        this.usertype = usertype;
    }
}