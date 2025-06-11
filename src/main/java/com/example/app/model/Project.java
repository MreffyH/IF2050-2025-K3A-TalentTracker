package com.example.app.model;

import java.time.LocalDate;
import java.util.List;

public class Project {
    private String title;
    private String description;
    private LocalDate deadline;
    private String projectType;
    private List<Staff> assignedStaff;

    public Project(String title, String description, LocalDate deadline, String projectType, List<Staff> assignedStaff) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.projectType = projectType;
        this.assignedStaff = assignedStaff;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public List<Staff> getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(List<Staff> assignedStaff) {
        this.assignedStaff = assignedStaff;
    }
} 