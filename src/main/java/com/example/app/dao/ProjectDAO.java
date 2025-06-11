package com.example.app.dao;

import com.example.app.model.Project;
import java.util.List;

public interface ProjectDAO {
    boolean addProject(Project project);
    // updateProject and deleteProject can be re-implemented later if needed
    // void updateProject(Project project);
    // void deleteProject(int projectId);
    Project getProjectById(int projectId);
    List<Project> getAllProjects();
} 