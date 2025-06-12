package com.talenttracker.dao;

import com.talenttracker.model.User;
import com.talenttracker.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectArtistDAO {
    public void addArtistsToProject(int projectId, List<Integer> artistIds) throws SQLException {
        String sql = "INSERT INTO ProjectArtist (idProject, idArtist) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Integer artistId : artistIds) {
                pstmt.setInt(1, projectId);
                pstmt.setInt(2, artistId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public List<User> getArtistsForProject(int projectId) throws SQLException {
        List<User> artists = new ArrayList<>();
        String sql = "SELECT u.* FROM User u JOIN ProjectArtist pa ON u.idUser = pa.idArtist WHERE pa.idProject = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User artist = new User();
                artist.setId(rs.getInt("idUser"));
                artist.setFullName(rs.getString("fullName"));
                artist.setRole(rs.getString("role"));
                artists.add(artist);
            }
        }
        return artists;
    }

    public void removeArtistsFromProject(int projectId) throws SQLException {
        String sql = "DELETE FROM ProjectArtist WHERE idProject = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.executeUpdate();
        }
    }
} 