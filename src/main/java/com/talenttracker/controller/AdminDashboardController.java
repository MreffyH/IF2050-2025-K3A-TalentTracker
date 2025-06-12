package com.talenttracker.controller;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.talenttracker.dao.AttendanceDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Attendance;
import com.talenttracker.model.User;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AdminDashboardController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private VBox staffTableContent;

    @FXML
    private Pagination tablePagination;

    @FXML
    private GridPane tableHeader;

    private UserDAO userDAO = new UserDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    private List<User> allStaffMembers = new ArrayList<>();
    private List<User> filteredStaffMembers = new ArrayList<>();
    private final int ITEMS_PER_PAGE = 8;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load staff data
        loadStaffData();
        
        // Initialize pagination
        setupPagination();
        
        // Initialize search functionality
        setupSearch();
        
        // Display initial page
        displayStaffPage(0);
    }

    public void refreshStaffList() {
        loadStaffData();
        setupPagination();
        displayStaffPage(0);
    }

    private void loadStaffData() {
        try {
            allStaffMembers = userDAO.getAllStaff();
            filteredStaffMembers.clear();
            filteredStaffMembers.addAll(allStaffMembers);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error, maybe show a dialog
        }
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) filteredStaffMembers.size() / ITEMS_PER_PAGE);
        tablePagination.setPageCount(Math.max(1, pageCount));
        tablePagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayStaffPage(newIndex.intValue());
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterStaffMembers(newText);
        });
    }

    private void filterStaffMembers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredStaffMembers = new ArrayList<>(allStaffMembers);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredStaffMembers = allStaffMembers.stream()
                .filter(staff -> staff.getFullName().toLowerCase().contains(lowerCaseSearch))
                .collect(Collectors.toList());
        }
        setupPagination();
        displayStaffPage(0);
    }

    private void displayStaffPage(int pageIndex) {
        staffTableContent.getChildren().clear();
        
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredStaffMembers.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            User staff = filteredStaffMembers.get(i);
            staffTableContent.getChildren().add(createStaffRow(staff));
        }
    }

    private GridPane createStaffRow(User staff) {
        GridPane row = new GridPane();
        row.getStyleClass().add("staff-row");
        
        // Configure columns to match header exactly
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(18.75);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(18.75);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(18.75);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(18.75);
        
        row.getColumnConstraints().addAll(col1, col2, col3, col4, col5);
        
        // Name column - left aligned
        Label nameLabel = new Label(staff.getFullName());
        nameLabel.getStyleClass().add("staff-name");
        GridPane.setHalignment(nameLabel, javafx.geometry.HPos.LEFT);
        row.add(nameLabel, 0, 0);
        
        // Calculate attendance percentages and working hours
        int onTimePercent = 0;
        int latePercent = 0;
        long totalWorkingSeconds = 0;
        try {
            List<Attendance> attendanceRecords = attendanceDAO.getAttendanceByUserId(staff.getId());
            if (!attendanceRecords.isEmpty()) {
                // More efficient calculation from the fetched list
                long onTimeCount = attendanceRecords.stream().filter(Attendance::isOnTime).count();
                onTimePercent = (int) ((onTimeCount * 100.0) / attendanceRecords.size());
                latePercent = 100 - onTimePercent;
                
                // Sum up total working hours
                totalWorkingSeconds = attendanceRecords.stream()
                        .mapToLong(Attendance::getWorkingHours)
                        .sum();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Attend column
        HBox attendBox = createPercentageBox(onTimePercent + "%", "attend-dot");
        GridPane.setHalignment(attendBox, javafx.geometry.HPos.LEFT);
        row.add(attendBox, 1, 0);
        
        // Absent/Late column
        HBox absentBox = createPercentageBox(latePercent + "%", "absent-dot");
        GridPane.setHalignment(absentBox, javafx.geometry.HPos.LEFT);
        row.add(absentBox, 2, 0);
        
        // Working hours
        long hours = totalWorkingSeconds / 3600;
        long minutes = (totalWorkingSeconds % 3600) / 60;
        Label hoursLabel = new Label(String.format("%d Hr %02d Min", hours, minutes));
        hoursLabel.getStyleClass().add("working-hours");
        GridPane.setHalignment(hoursLabel, javafx.geometry.HPos.CENTER);
        row.add(hoursLabel, 3, 0);
        
        // Salary column - right aligned to match header
        Label salaryLabel = new Label(formatCurrency(staff.getSalary()));
        salaryLabel.getStyleClass().add("salary-label");
        GridPane.setHalignment(salaryLabel, javafx.geometry.HPos.RIGHT);
        row.add(salaryLabel, 4, 0);
        
        return row;
    }

    private HBox createPercentageBox(String text, String styleClass) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(8);
        dot.getStyleClass().add(styleClass);
        Label label = new Label(text);
        label.getStyleClass().add("percentage-label");
        box.getChildren().addAll(dot, label);
        return box;
    }

    private String formatCurrency(long amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return "IDR " + currencyFormat.format(amount);
    }
}