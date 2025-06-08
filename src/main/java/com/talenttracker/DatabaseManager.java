package com.talenttracker;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {

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

    public static boolean addUser(int id, String fullName, String email, String password, String role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO User (idUser, fullName, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, fullName);
            pstmt.setString(3, email);
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, role);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
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

    public static boolean addArtist(String fullName, String email, String password) {
        return addUser(fullName, email, password, "Artist");
    }

    public static void getAllUsers() {
        String query = "SELECT email, role, fullName FROM `user`";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Users in database:");
                while (rs.next()) {
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    String fullName = rs.getString("fullName");
                    System.out.println("Email: " + email + ", Role: " + role + ", Full Name: " + fullName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 