package com.talenttracker.dao;

import com.talenttracker.model.Album;
import com.talenttracker.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {

    public List<Album> getArtistAlbums(int artistId, boolean monthFilter) throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql;
        if (monthFilter) {
            sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? AND date >= ? ORDER BY sold DESC";
        } else {
            sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? ORDER BY sold DESC";
        }
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            if (monthFilter) {
                pstmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now().minusMonths(1)));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    albums.add(new Album(rs.getString("albumName"), rs.getInt("sold")));
                }
            }
        }
        return albums;
    }

    public List<String> getAllAlbumNamesForArtist(int artistId) throws SQLException {
        List<String> albumNames = new ArrayList<>();
        String sql = "SELECT albumName FROM TopAlbum WHERE idArtis = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    albumNames.add(rs.getString("albumName"));
                }
            }
        }
        return albumNames;
    }

    public void addAlbum(String albumName, int initialSold, int artistId) throws SQLException {
        String sql = "INSERT INTO TopAlbum (albumName, sold, idArtis, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, albumName);
            pstmt.setInt(2, initialSold);
            pstmt.setInt(3, artistId);
            pstmt.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.executeUpdate();
        }
    }

    public void addAlbumSold(String albumName, int amount, int artistId) throws SQLException {
        String sql = "UPDATE TopAlbum SET sold = sold + ? WHERE albumName = ? AND idArtis = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, albumName);
            pstmt.setInt(3, artistId);
            pstmt.executeUpdate();
        }
    }

    public void removeAlbum(String albumName, int artistId) throws SQLException {
        String sql = "DELETE FROM TopAlbum WHERE albumName = ? AND idArtis = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, albumName);
            pstmt.setInt(2, artistId);
            pstmt.executeUpdate();
        }
    }
} 