package com.example.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.app.model.User;
import com.example.app.util.Database;

public class ProjectArtistDAOImpl implements ProjectArtistDAO {

    @Override
    public void addArtistsToProject(int projectId, List<Integer> artistIds) {
        String sql = "INSERT INTO ProjectArtist (idProject, idArtist) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Integer artistId : artistIds) {
                pstmt.setInt(1, projectId);
                pstmt.setInt(2, artistId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getArtistsForProject(int projectId) {
        List<User> artists = new ArrayList<>();
        String sql = "SELECT u.* FROM User u JOIN ProjectArtist pa ON u.idUser = pa.idArtist WHERE pa.idProject = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User artist = new User();
                artist.setIdUser(rs.getInt("idUser"));
                artist.setFullName(rs.getString("fullName"));
                artist.setRole(rs.getString("role"));
                artists.add(artist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }
} 