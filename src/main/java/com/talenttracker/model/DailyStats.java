package com.talenttracker.model;

public class DailyStats {
    private final double totalSales;
    private final double albumsSold;
    private final double newVisitors;

    public DailyStats(double totalSales, double albumsSold, double newVisitors) {
        this.totalSales = totalSales;
        this.albumsSold = albumsSold;
        this.newVisitors = newVisitors;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public double getAlbumsSold() {
        return albumsSold;
    }

    public double getNewVisitors() {
        return newVisitors;
    }
} 