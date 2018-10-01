package com.example.chatme.chatme;

public class Messages {

    private String name;
    private String comment;
    private String photo;

    public Messages()
    {

    }

    public Messages(String name, String comment, String photo)
    {
        this.name = name;
        this.comment = comment;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getPhoto() { return photo; }
}