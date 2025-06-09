package com.example.controller;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private Circle profileCircle;

    @FXML
    private GridPane tableHeader;

    private List<StaffMember> allStaffMembers = new ArrayList<>();
    private List<StaffMember> filteredStaffMembers = new ArrayList<>();
    private final int ITEMS_PER_PAGE = 5;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize profile circle with CEO initials
        profileCircle.setFill(Color.WHITE);
        
        // Load staff data
        loadStaffData();
        
        // Initialize pagination
        tablePagination.setPageCount((int) Math.ceil((double) allStaffMembers.size() / ITEMS_PER_PAGE));
        tablePagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayStaffPage(newIndex.intValue());
        });
        
        // Initialize search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterStaffMembers(newText);
        });
        
        // Display initial page
        displayStaffPage(0);
    }

    private void loadStaffData() {
        // Sample data - in a real app, this would come from a database
        allStaffMembers.add(new StaffMember("Moh Aqila", 80, 20, "30 Hr 40 Mins 56 Secs", 9000000));
        allStaffMembers.add(new StaffMember("Anwar Fawaz", 90, 10, "30 Hr 40 Mins 56 Secs", 10000000));
        allStaffMembers.add(new StaffMember("Reffy Aja", 76, 24, "30 Hr 40 Mins 56 Secs", 8000000));
        allStaffMembers.add(new StaffMember("Afnan Alif", 89, 11, "30 Hr 40 Mins 56 Secs", 10000000));
        allStaffMembers.add(new StaffMember("Kak Nana", 12, 88, "30 Hr 40 Mins 56 Secs", 1000000));
        
        // Add more sample data to demonstrate pagination
        for (int i = 0; i < 95; i++) {
            int attendPercent = (int) (Math.random() * 100);
            allStaffMembers.add(new StaffMember(
                "Staff " + (i + 6),
                attendPercent,
                100 - attendPercent,
                "30 Hr 40 Mins 56 Secs",
                (long) (Math.random() * 10000000) + 1000000
            ));
        }
        
        // Initialize filtered list
        filteredStaffMembers.addAll(allStaffMembers);
    }

    private void filterStaffMembers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredStaffMembers = new ArrayList<>(allStaffMembers);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredStaffMembers = allStaffMembers.stream()
                .filter(staff -> staff.getName().toLowerCase().contains(lowerCaseSearch))
                .collect(Collectors.toList());
        }
        
        // Update pagination
        tablePagination.setPageCount(Math.max(1, (int) Math.ceil((double) filteredStaffMembers.size() / ITEMS_PER_PAGE)));
        displayStaffPage(0);
    }

    private void displayStaffPage(int pageIndex) {
        staffTableContent.getChildren().clear();
        
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredStaffMembers.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            StaffMember staff = filteredStaffMembers.get(i);
            staffTableContent.getChildren().add(createStaffRow(staff));
        }
    }

    private GridPane createStaffRow(StaffMember staff) {
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
        Label nameLabel = new Label(staff.getName());
        nameLabel.getStyleClass().add("staff-name");
        GridPane.setHalignment(nameLabel, javafx.geometry.HPos.LEFT);
        row.add(nameLabel, 0, 0);
        
        // Attend column - left aligned to match header
        HBox attendBox = new HBox(10);
        attendBox.setAlignment(Pos.CENTER_LEFT);
        Circle attendDot = new Circle(8);
        attendDot.getStyleClass().add("attend-dot");
        Label attendLabel = new Label(staff.getAttendPercent() + "%");
        attendLabel.getStyleClass().add("percentage-label");
        attendBox.getChildren().addAll(attendDot, attendLabel);
        GridPane.setHalignment(attendBox, javafx.geometry.HPos.LEFT);
        row.add(attendBox, 1, 0);
        
        // Absent column - left aligned to match header
        HBox absentBox = new HBox(10);
        absentBox.setAlignment(Pos.CENTER_LEFT);
        Circle absentDot = new Circle(8);
        absentDot.getStyleClass().add("absent-dot");
        Label absentLabel = new Label(staff.getAbsentPercent() + "%");
        absentLabel.getStyleClass().add("percentage-label");
        absentBox.getChildren().addAll(absentDot, absentLabel);
        GridPane.setHalignment(absentBox, javafx.geometry.HPos.LEFT);
        row.add(absentBox, 2, 0);
        
        // Working hours column - center aligned to match header
        Label hoursLabel = new Label(staff.getWorkingHours());
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

    private String formatCurrency(long amount) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        return "IDR " + currencyFormat.format(amount).replace(",", ".");
    }

    // Staff Member model class
    private static class StaffMember {
        private String name;
        private int attendPercent;
        private int absentPercent;
        private String workingHours;
        private long salary;

        public StaffMember(String name, int attendPercent, int absentPercent, String workingHours, long salary) {
            this.name = name;
            this.attendPercent = attendPercent;
            this.absentPercent = absentPercent;
            this.workingHours = workingHours;
            this.salary = salary;
        }

        public String getName() {
            return name;
        }

        public int getAttendPercent() {
            return attendPercent;
        }

        public int getAbsentPercent() {
            return absentPercent;
        }

        public String getWorkingHours() {
            return workingHours;
        }

        public long getSalary() {
            return salary;
        }
    }
}
