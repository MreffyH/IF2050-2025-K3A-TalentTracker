package com.talenttracker.dao;

import com.talenttracker.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StatsDAO {

    public void addVisitors(int count, int artistId) throws SQLException {
        String sql = "INSERT INTO Visitors (visitorsToday, idArtis, date) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE visitorsToday = visitorsToday + VALUES(visitorsToday)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.setInt(2, artistId);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
        }
    }

    public void addSales(int amount, int artistId) throws SQLException {
        addDailyStat("Sales", "salesToday", amount, artistId);
    }
    
    public void addAlbumsSold(int amount, int artistId) throws SQLException {
        String sql = "INSERT INTO AlbumSold (albumSoldToday, idArtis, date) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE albumSoldToday = albumSoldToday + VALUES(albumSoldToday)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setInt(2, artistId);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
        }
    }

    public double getVisitorsForDate(LocalDate date, int artistId) throws SQLException {
        return getTotalForDate("Visitors", "visitorsToday", date, artistId);
    }

    public double getSalesForDate(LocalDate date, int artistId) throws SQLException {
        return getTotalForDate("Sales", "salesToday", date, artistId);
    }

    public double getAlbumsSoldForDate(LocalDate date, int artistId) throws SQLException {
        return getTotalForDate("AlbumSold", "albumSoldToday", date, artistId);
    }

    public double getTotalVisitorsForArtist(int artistId) throws SQLException {
        return getTotalForArtist("Visitors", "visitorsToday", artistId);
    }

    public double getTotalSalesForArtist(int artistId) throws SQLException {
        return getTotalForArtist("Sales", "salesToday", artistId);
    }

    public double getTotalAlbumsSoldForArtist(int artistId) throws SQLException {
        return getTotalForArtist("AlbumSold", "albumSoldToday", artistId);
    }

    private void addDailyStat(String tableName, String valueColumnName, int value, int artistId) throws SQLException {
        String sql = String.format("INSERT INTO %s (%s, idArtis, date) VALUES (?, ?, ?)", tableName, valueColumnName);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, value);
            pstmt.setInt(2, artistId);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
        }
    }

    private double getTotalForDate(String tableName, String valueColumnName, LocalDate date, int artistId) throws SQLException {
        double total = 0;
        String sql = String.format("SELECT SUM(%s) FROM %s WHERE date = ? AND idArtis = ?", valueColumnName, tableName);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setInt(2, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        }

        if (total == 0) {
            // If no data for today, try yesterday
            LocalDate yesterday = date.minusDays(1);
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(yesterday));
                pstmt.setInt(2, artistId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getDouble(1);
                    }
                }
            }
        }
        return total;
    }

    private double getTotalForArtist(String tableName, String valueColumnName, int artistId) throws SQLException {
        double total = 0;
        String sql = String.format("SELECT SUM(%s) FROM %s WHERE idArtis = ?", valueColumnName, tableName);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        }
        return total;
    }
}
