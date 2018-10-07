package com.example.chatme.chatme;

public class Online {
    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    private Boolean online;
    private Boolean available; //If has chat session or none

    public Online(Boolean online, Boolean available){
        this.online = online;
        this.available = available;
    }

    public Online(){

    }
}
