package com.example.app.dao;

import com.example.app.model.Project;
import com.example.app.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAOImpl implements ProjectDAO {

    @Override
    public boolean addProject(Project project) {
        String sql = "INSERT INTO Project(idProject, projectName, type, description, idCEO, idStaff, startDate, endDate) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Project getProjectById(int projectId) {
        // Implementation can be added later
        return null;
    }

    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM Project";
        try (Connection conn = Database.getConnection();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }
}