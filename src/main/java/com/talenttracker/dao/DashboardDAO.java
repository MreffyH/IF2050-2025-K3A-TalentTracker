package com.talenttracker.dao;

import com.talenttracker.model.Album;
import com.talenttracker.model.Popularity;
import com.talenttracker.util.DatabaseUtil;
import com.talenttracker.model.DailyStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public DailyStats getDailyStatsForDate(LocalDate date) throws SQLException {
        double sales = getTotalForDate("Sales", "salesToday", date);
        double albumsSold = getTotalForDate("AlbumSold", "albumSoldToday", date);
        double visitors = getTotalForDate("Visitors", "visitorsToday", date);
        return new DailyStats(sales, albumsSold, visitors);
    }

    private double getTotalForDate(String tableName, String columnName, LocalDate date) throws SQLException {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE date = ?", columnName, tableName);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }

    public double getMonthlyTotal(String tableName, String columnName, int year, int month) throws SQLException {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE YEAR(date) = ? AND MONTH(date) = ?", columnName, tableName);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }

    public double getMonthlyTotalSales(int year, int month) throws SQLException {
        return getMonthlyTotal("Sales", "salesToday", year, month);
    }

    public List<Album> getTopAlbums() throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT albumName, SUM(sold) AS total_sold FROM TopAlbum GROUP BY albumName ORDER BY total_sold DESC LIMIT 5";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next()) {
                albums.add(new Album(rs.getString("albumName"), rs.getInt("total_sold")));
            }
        }
        return albums;
    }

    public List<Popularity> getSocialInsights() throws SQLException {
        List<Popularity> popularities = new ArrayList<>();
        String sql = "SELECT date, SUM(todayFollowers) AS total_followers FROM Popularity WHERE date >= ? GROUP BY date ORDER BY date ASC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(7))); // Last 7 days
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    popularities.add(new Popularity(null, rs.getInt("total_followers"), rs.getDate("date").toLocalDate()));
                }
            }
        }
        return popularities;
    }
} 