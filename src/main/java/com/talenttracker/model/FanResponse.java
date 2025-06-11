package com.talenttracker.model;

public class FanResponse {
    private final int number;
    private final String source;
    private final String comment;
    private final String category;

    public FanResponse(int number, String source, String comment, String category) {
        this.number = number;
        this.source = source;
        this.comment = comment;
        this.category = category;
    }

    public int getNumber() { return number; }
    public String getSource() { return source; }
    public String getComment() { return comment; }
    public String getCategory() { return category; }
} 