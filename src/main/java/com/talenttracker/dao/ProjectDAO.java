package com.talenttracker.dao;

import com.talenttracker.model.Project;
import com.talenttracker.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public boolean addProject(Project project) throws SQLException {
        String sql = "INSERT INTO Project(idProject, projectName, type, description, idCEO, idStaff, startDate, endDate) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, project.getIdProject());
            pstmt.setString(2, project.getProjectName());
            pstmt.setString(3, project.getType());
            pstmt.setString(4, project.getDescription());
            pstmt.setInt(5, project.getIdCEO());
            pstmt.setInt(6, project.getIdStaff());
            pstmt.setTimestamp(7, Timestamp.valueOf(project.getStartDate()));
            pstmt.setTimestamp(8, Timestamp.valueOf(project.getEndDate()));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<Project> getAllProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM Project";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Project project = new Project();
                project.setIdProject(rs.getInt("idProject"));
                project.setProjectName(rs.getString("projectName"));
                project.setType(rs.getString("type"));
                project.setDescription(rs.getString("description"));
                project.setIdCEO(rs.getInt("idCEO"));
                project.setIdStaff(rs.getInt("idStaff"));
                project.setStartDate(rs.getTimestamp("startDate").toLocalDateTime());
                project.setEndDate(rs.getTimestamp("endDate").toLocalDateTime());
                projects.add(project);
            }
        }
        return projects;
    }

    public int getNextProjectId() throws SQLException {
        String sql = "SELECT MAX(idProject) FROM Project";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        }
        return 1; // Default to 1 if table is empty
    }

    public Project getProjectById(int projectId) throws SQLException {
        // Implementation can be added later
        return null;
    }
} 