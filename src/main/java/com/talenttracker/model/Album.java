package com.talenttracker.model;

public class Album {
    private String name;
    private int sales;

    public Album(String name, int sales) {
        this.name = name;
        this.sales = sales;
    }

    public String getName() {
        return name;
    }

    public int getSales() {
        return sales;
    }
} 