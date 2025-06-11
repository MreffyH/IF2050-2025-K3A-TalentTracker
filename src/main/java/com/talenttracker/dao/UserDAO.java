package com.talenttracker.dao;

import com.talenttracker.model.User;
import com.talenttracker.util.DatabaseUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean addUser(String fullName, String email, String password, String role) throws SQLException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "INSERT INTO `user` (idUser, fullName, email, password, role) VALUES (?, ?, ?, ?, ?)";
        int nextId = 1;

        try (Connection conn = DatabaseUtil.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(idUser) FROM `user`")) {
                if (rs.next()) {
                    nextId = rs.getInt(1) + 1;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, nextId);
                pstmt.setString(2, fullName);
                pstmt.setString(3, email);
                pstmt.setString(4, hashedPassword);
                pstmt.setString(5, role);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        }
    }

    public User verifyUser(String email, String password) throws SQLException {
        String query = "SELECT idUser, password, role, fullName FROM `user` WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        int id = rs.getInt("idUser");
                        String role = rs.getString("role");
                        String fullName = rs.getString("fullName");
                        return new User(id, fullName, email, role);
                    }
                }
            }
        }
        return null;
    }

    public int getArtistIdByName(String artistName) throws SQLException {
        String sql = "SELECT idUser FROM User WHERE fullName = ? AND role = 'Artist'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artistName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idUser");
                }
            }
        }
        return -1; // Return -1 if not found
    }

    public String getArtistNameById(int artistId) throws SQLException {
        String sql = "SELECT fullName FROM User WHERE idUser = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("fullName");
                }
            }
        }
        return null;
    }

    public User getArtistByName(String artistName) throws SQLException {
        String sql = "SELECT idUser, fullName, email, role FROM User WHERE fullName = ? AND role = 'Artist'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artistName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("idUser");
                    String fullName = rs.getString("fullName");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    return new User(id, fullName, email, role);
                }
            }
        }
        return null;
    }

    public List<User> getAllStaff() throws SQLException {
        List<User> staffList = new ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE role = 'Staff' ORDER BY idUser";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("idUser"));
                user.setFullName(rs.getString("fullName"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setAttendancePercentage(rs.getDouble("attendancePercentage"));
                user.setSalary(rs.getInt("salary"));
                staffList.add(user);
            }
        }
        return staffList;
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE email = ?";
        User user = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("idUser"));
                user.setFullName(rs.getString("fullName"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setAttendancePercentage(rs.getDouble("attendancePercentage"));
                user.setSalary(rs.getInt("salary"));
            }
        }
        return user;
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE idUser = ?";
        User user = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("idUser"));
                user.setFullName(rs.getString("fullName"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setAttendancePercentage(rs.getDouble("attendancePercentage"));
                user.setSalary(rs.getInt("salary"));
            }
        }
        return user;
    }

    public List<User> getUsersByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM User WHERE role = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("idUser"));
                user.setFullName(rs.getString("fullName"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        }
        return users;
    }
} 