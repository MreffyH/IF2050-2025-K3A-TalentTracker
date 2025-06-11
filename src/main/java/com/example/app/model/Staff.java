package com.example.app.model;

public class Staff {
    private final String id;
    private final String name;

    public Staff(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name; // This is useful for displaying in ComboBoxes or ListViews
    }
} 