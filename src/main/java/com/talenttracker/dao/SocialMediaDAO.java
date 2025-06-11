package com.talenttracker.dao;

import com.talenttracker.util.DatabaseUtil;
import com.talenttracker.model.Popularity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialMediaDAO {

    public List<Popularity> getSocialMediaData(int artistId) throws SQLException {
        List<Popularity> socialMediaData = new ArrayList<>();
        String sql = "SELECT socialMedia, todayFollowers, date FROM Popularity WHERE idArtis = ? ORDER BY date ASC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String platform = rs.getString("socialMedia");
                    int followers = rs.getInt("todayFollowers");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    
                    socialMediaData.add(new Popularity(platform, followers, date));
                }
            }
        }
        return socialMediaData;
    }

    public void addSocialMediaData(String platform, int followers, int artistId) throws SQLException {
        addSocialMedia(platform, followers, artistId);
    }

    public void addSocialMedia(String platform, int followers, int artistId) throws SQLException {
        String sql = "INSERT INTO Popularity (socialMedia, todayFollowers, idArtis, date) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE todayFollowers = todayFollowers + VALUES(todayFollowers)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platform);
            pstmt.setInt(2, followers);
            pstmt.setInt(3, artistId);
            pstmt.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.executeUpdate();
        }
    }

    public double getFollowerTotalForDate(LocalDate date, int artistId) throws SQLException {
        double totalFollowers = 0;
        String sql = "SELECT SUM(todayFollowers) FROM Popularity WHERE date = ? AND idArtis = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setInt(2, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalFollowers = rs.getDouble(1);
                }
            }
        }
        return totalFollowers;
    }

    public Map<String, Double> getMostRecentFollowerStats(int artistId) throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        stats.put("current", 0.0);
        stats.put("previous", 0.0);

        String sql = "SELECT date, SUM(todayFollowers) AS total " +
                     "FROM Popularity " +
                     "WHERE idArtis = ? " +
                     "GROUP BY date " +
                     "ORDER BY date DESC " +
                     "LIMIT 2";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("current", rs.getDouble("total"));
                }
                if (rs.next()) {
                    stats.put("previous", rs.getDouble("total"));
                }
            }
        }
        return stats;
    }
} 