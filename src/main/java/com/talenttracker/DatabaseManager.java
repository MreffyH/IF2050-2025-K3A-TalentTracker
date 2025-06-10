package com.talenttracker;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {

    public static class Album {
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

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            properties.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            properties.getProperty("db.url"), 
            properties.getProperty("db.user"), 
            properties.getProperty("db.password")
        );
    }

    public static boolean addUser(String fullName, String email, String password, String role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "INSERT INTO `user` (idUser, fullName, email, password, role) VALUES (?, ?, ?, ?, ?)";
        int nextId = 1;

        try (Connection conn = getConnection()) {
            // Get the next available ID
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(idUser) FROM `user`")) {
                if (rs.next()) {
                    nextId = rs.getInt(1) + 1;
                }
            }

            // Insert the new user
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, nextId);
                pstmt.setString(2, fullName);
                pstmt.setString(3, email);
                pstmt.setString(4, hashedPassword);
                pstmt.setString(5, role);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] verifyUser(String email, String password) {
        String query = "SELECT idUser, password, role, fullName FROM `user` WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        String idUser = rs.getString("idUser");
                        String role = rs.getString("role");
                        String fullName = rs.getString("fullName");
                        return new String[]{idUser, role, fullName}; // Return user ID, role, and full name
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null on failure
    }

    public static List<Album> getArtistAlbums(int artistId, boolean monthFilter) {
        List<Album> albums = new ArrayList<>();
        String sql;
        if (monthFilter) {
            sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? AND date >= ? ORDER BY sold DESC";
        } else {
            sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? ORDER BY sold DESC";
        }
        
        try (Connection conn = getConnection();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public static int getArtistByName(String artistName) {
        String sql = "SELECT idUser FROM User WHERE fullName = ? AND role = 'Artist'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artistName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idUser");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if not found
    }

    public static String getArtistNameById(int artistId) {
        String sql = "SELECT fullName FROM User WHERE idUser = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("fullName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
