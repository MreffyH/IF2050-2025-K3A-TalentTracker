package com.talenttracker.dao;

import com.talenttracker.model.FanResponse;
import com.talenttracker.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FansDAO {

    public List<FanResponse> getFanResponses(int artistId, boolean monthFilter) throws SQLException {
        List<FanResponse> responses = new ArrayList<>();
        String sql;
        if (monthFilter) {
            sql = "SELECT source, comment, category FROM FansResponse WHERE idArtis = ? AND timestamp >= ? ORDER BY timestamp DESC";
        } else {
            sql = "SELECT source, comment, category FROM FansResponse WHERE idArtis = ? ORDER BY timestamp DESC";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            if (monthFilter) {
                pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDate.now().minusMonths(1).atStartOfDay()));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    responses.add(new FanResponse(
                        rowNum++,
                        rs.getString("source"),
                        rs.getString("comment"),
                        rs.getString("category")
                    ));
                }
            }
        }
        return responses;
    }

    // Methods for testing purposes
    public void addFanResponse(int idResponse, int artistId, String source, String comment, String category, java.sql.Timestamp timestamp) throws SQLException {
        String sql = "INSERT INTO FansResponse (idResponse, idArtis, source, comment, category, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idResponse);
            pstmt.setInt(2, artistId);
            pstmt.setString(3, source);
            pstmt.setString(4, comment);
            pstmt.setString(5, category);
            pstmt.setTimestamp(6, timestamp);
            pstmt.executeUpdate();
        }
    }

    public void removeFanResponsesByArtist(int artistId) throws SQLException {
        String sql = "DELETE FROM FansResponse WHERE idArtis = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            pstmt.executeUpdate();
        }
    }
} 