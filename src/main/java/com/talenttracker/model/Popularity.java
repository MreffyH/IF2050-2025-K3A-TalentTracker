package com.talenttracker.model;

import java.time.LocalDate;

public class Popularity {
    private String platform;
    private int followers;
    private LocalDate date;

    public Popularity(String platform, int followers, LocalDate date) {
        this.platform = platform;
        this.followers = followers;
        this.date = date;
    }

    public String getPlatform() {
        return platform;
    }

    public int getFollowers() {
        return followers;
    }

    public LocalDate getDate() {
        return date;
    }
} 