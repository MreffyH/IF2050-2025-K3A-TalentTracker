package com.talenttracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.talenttracker.util.DatabaseUtil;
import com.talenttracker.model.Attendance;

public class AttendanceDAO {

    public void addAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (idstaff, `date`, `time`, attendance, working_hours) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attendance.getIdStaff());
            pstmt.setDate(2, attendance.getDate());
            pstmt.setTime(3, attendance.getTime());
            pstmt.setBoolean(4, attendance.isOnTime());
            pstmt.setInt(5, attendance.getWorkingHours());
            pstmt.executeUpdate();
        }
    }

    public void updateWorkingHoursForToday(int attendanceId, int workingSeconds) throws SQLException {
        String sql = "UPDATE attendance SET working_hours = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, workingSeconds);
            pstmt.setInt(2, attendanceId);
            pstmt.executeUpdate();
        }
    }

    public long getWorkingHoursForToday(int userId) throws SQLException {
        String sql = "SELECT working_hours FROM attendances WHERE id_staff = ? AND attendance_date = CURDATE()";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getLong("working_hours"); // Sesuaikan nama kolom jika berbeda
            }
        }
        return 0; // Return 0 if no record found for today
    }

    public List<Attendance> getAttendanceByUserId(int userId) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE idstaff = ? ORDER BY `date` DESC, `time` DESC";
        System.out.println("Executing SQL: " + sql); // <-- Add this line
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setIdStaff(rs.getInt("idstaff"));
                attendance.setDate(rs.getDate("date"));
                attendance.setTime(rs.getTime("time"));
                attendance.setOnTime(rs.getBoolean("attendance"));
                attendance.setWorkingHours(rs.getInt("working_hours"));
                attendanceList.add(attendance);
            }
        }
        return attendanceList;
    }

    public int getOnTimeCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE idstaff = ? AND attendance = 1";
        System.out.println("Executing SQL: " + sql); // <-- Add this line
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
     public boolean hasCheckedInToday(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE idstaff = ? AND `date` = CURDATE()";
        System.out.println("Executing SQL: " + sql); // <-- Add this line
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public Attendance getLatestUnfinishedAttendance(int userId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE idstaff = ? AND `date` = CURDATE() AND working_hours = 0 ORDER BY `time` DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setIdStaff(rs.getInt("idstaff"));
                attendance.setDate(rs.getDate("date"));
                attendance.setTime(rs.getTime("time"));
                attendance.setOnTime(rs.getBoolean("attendance"));
                attendance.setWorkingHours(rs.getInt("working_hours"));
                return attendance;
            }
        }
        return null;
    }
} 