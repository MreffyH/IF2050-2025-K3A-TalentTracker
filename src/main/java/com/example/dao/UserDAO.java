package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.example.model.User;
import com.example.util.DatabaseConnection;

public class UserDAO {

    public void addUser(User user) throws SQLException {
        String getMaxIdSql = "SELECT MAX(idUser) FROM `user`";
        String insertSql = "INSERT INTO `user` (idUser, fullName, email, password, role, attendancePercentage, salary) VALUES (?, ?, ?, ?, ?, 0.0, 0)";
        int nextId = 1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get the next available ID
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(getMaxIdSql)) {
                if (rs.next()) {
                    nextId = rs.getInt(1) + 1;
                }
            }

            // Insert the new user
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, nextId);
                pstmt.setString(2, user.getFullName());
                pstmt.setString(3, user.getEmail());
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                pstmt.setString(4, hashedPassword);
                pstmt.setString(5, user.getRole());
                pstmt.executeUpdate();
            }
        }
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE email = ?";
        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
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
        try (Connection conn = DatabaseConnection.getConnection();
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

    public List<User> getAllStaff() throws SQLException {
        List<User> staffList = new ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE role = 'Staff' ORDER BY idUser";
        try (Connection conn = DatabaseConnection.getConnection();
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
} 