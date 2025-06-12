package com.talenttracker.controller;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.User;
import com.talenttracker.dao.AttendanceDAO;
import com.talenttracker.model.Attendance;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;


public class AttendanceDashboardController implements Initializable {

    // FXML Fields - Common
    @FXML private Label digitalTimeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // FXML Fields - Staff View
    @FXML private VBox attendanceRecordsVBox;
    @FXML private Label salaryAmountLabel;
    @FXML private Button checkButton;
    @FXML private Label onTimePercentageLabel;
    @FXML private Label latePercentageLabel;
    @FXML private Canvas analogClockCanvas;
    @FXML private Label workingTimeLabel;
    @FXML private Button viewSalaryButton;
    @FXML private LineChart<String, Number> onTimeLineChart;
    @FXML private LineChart<String, Number> lateLineChart;
    @FXML private CategoryAxis onTimeXAxis;
    @FXML private CategoryAxis lateXAxis;
    @FXML private NumberAxis onTimeYAxis;
    @FXML private NumberAxis lateYAxis;

    // FXML Fields - CEO View
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private VBox staffTableContent;
    @FXML private Pagination tablePagination;

    // DAOs
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final UserDAO userDAO = new UserDAO();

    // State
    private User currentUser;
    private static final Time ON_TIME_CUTOFF = Time.valueOf("08:30:00");
    private boolean isSalaryVisible = false;

    // --- START: TEST CONFIGURATION ---
    // Change these values to configure the test scenario.
    private static final int TEST_CUTOFF_HOUR = 8;
    private static final int TEST_CUTOFF_MINUTE = 30;
    private static final int TEST_CHECKOUT_HOUR = 17;
    private static final int TEST_CHECKOUT_MINUTE = 00;
    // --- END: TEST CONFIGURATION ---

    // Working Hours State
    private Timeline workingHoursTimeline;
    private long elapsedSeconds = 0;
    private Attendance currentAttendanceRecord;
    private LocalTime testAutoCheckoutTime; // For testing dynamic checkout

    public void setUser(User user) {
        this.currentUser = user;
        // This check is important
        if (this.currentUser != null) {
            if (userNameLabel != null) userNameLabel.setText(currentUser.getFullName());
            if (userRoleLabel != null) userRoleLabel.setText(currentUser.getRole());
            
            if ("CEO".equalsIgnoreCase(currentUser.getRole())) {
                // Load CEO-specific data
                loadAllStaffData();
            } else {
                // Load Staff-specific data
                refreshDashboard();
                restoreCheckInState();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialization that does not depend on the user role
        if (onTimeLineChart != null) {
            configureChartAxes();
        }
        if (digitalTimeLabel != null) {
            initializeClock();
        }
        if (searchButton != null) {
            searchButton.setOnAction(event -> handleSearch());
        }
    }
    
    private void handleSearch() {
        // Implement search logic for CEO view
        String searchText = searchField.getText();
        // ... filter staff list based on search ...
    }

    private void loadAllStaffData() {
        // Implement data loading for CEO view
    }

    private void configureChartAxes() {
        if (onTimeYAxis == null || lateYAxis == null || onTimeXAxis == null || lateXAxis == null) return;
        // Configure Y-axes for 0-100 percentage range
        onTimeYAxis.setLowerBound(0);
        onTimeYAxis.setUpperBound(100);
        onTimeYAxis.setTickUnit(20);
        onTimeYAxis.setLabel("%");
        
        lateYAxis.setLowerBound(0);
        lateYAxis.setUpperBound(100);
        lateYAxis.setTickUnit(20);
        lateYAxis.setLabel("%");
        
        // Configure X-axes
        onTimeXAxis.setLabel("Day");
        lateXAxis.setLabel("Day");
    }

    private void refreshDashboard() {
        if (currentUser.getRole().equalsIgnoreCase("Staff")) {
            loadAndDisplayAttendance();
            updateSalaryDisplay();
            updateStats();
            updateLineCharts();
        }
    }

    private void updateStats() {
        if (onTimePercentageLabel == null || latePercentageLabel == null) return;
        try {
            List<Attendance> userAttendance = attendanceDAO.getAttendanceByUserId(currentUser.getId());
            if (userAttendance.isEmpty()) {
                onTimePercentageLabel.setText("0%");
                latePercentageLabel.setText("0%");
                return;
            }

            long onTimeCount = userAttendance.stream().filter(Attendance::isOnTime).count();
            long lateCount = userAttendance.size() - onTimeCount;

            double total = userAttendance.size();
            double onTimePercent = (onTimeCount / total) * 100;
            double latePercent = (lateCount / total) * 100;

            onTimePercentageLabel.setText(String.format("%.0f%%", onTimePercent));
            latePercentageLabel.setText(String.format("%.0f%%", latePercent));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAndDisplayAttendance() {
        if (attendanceRecordsVBox == null) return;
        attendanceRecordsVBox.getChildren().clear();
        try {
            List<Attendance> attendanceRecords = attendanceDAO.getAttendanceByUserId(currentUser.getId());
            System.out.println("Loaded attendance records: " + attendanceRecords.size());
            Platform.runLater(() -> {
                for (Attendance record : attendanceRecords) {
                    HBox recordRow = createAttendanceRow(record);
                    attendanceRecordsVBox.getChildren().add(recordRow);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createAttendanceRow(Attendance record) {
        System.out.println("Attendance Record: date=" + record.getDate() + ", time=" + record.getTime() + ", onTime=" + record.isOnTime());
        HBox recordRow = new HBox();
        recordRow.setPadding(new Insets(12, 0, 12, 0));
        recordRow.setSpacing(20);

        Label dateLabel = new Label(record.getDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        dateLabel.setTextFill(Color.BLACK);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Circle statusDot = new Circle(5);
        statusDot.getStyleClass().add(record.isOnTime() ? "status-dot-on-time" : "status-dot-absent");

        Label timeLabel = new Label(record.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.setTextFill(Color.BLACK);
        
        // FIXED: Improved alignment for time display
        HBox timeBox = new HBox(10, statusDot, timeLabel);
        timeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        recordRow.getChildren().addAll(dateLabel, spacer, timeBox);
        return recordRow;
    }

    private void updateSalaryDisplay() {
        if (salaryAmountLabel == null) return;
        if (isSalaryVisible) {
            salaryAmountLabel.setText("IDR " + String.format("%,d", currentUser.getSalary()));
        } else {
            salaryAmountLabel.setText("IDR ********");
        }
    }

    private void restoreCheckInState() {
        if (checkButton == null) return;
        try {
            currentAttendanceRecord = attendanceDAO.getLatestUnfinishedAttendance(currentUser.getId());
            if (currentAttendanceRecord != null) {
                // User was checked in but closed the app. Restore the state.
                checkButton.setText("Check Out");
                checkButton.getStyleClass().setAll("check-button-out");
                
                LocalTime checkInTime = currentAttendanceRecord.getTime().toLocalTime();
                elapsedSeconds = java.time.Duration.between(checkInTime, LocalTime.now()).getSeconds();
                startWorkingHoursTimer();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleCheckIn() {
        if (checkButton == null) return;
        if (currentAttendanceRecord == null) {
            // --- CHECK IN (TEST MODE) ---
            try {
                // 1. Check if user has already checked in today
                if (attendanceDAO.hasCheckedInToday(currentUser.getId())) {
                    checkButton.setText("Checked In Today");
                    checkButton.setDisable(true);
                    System.out.println("User has already checked in today. Cannot check in again.");
                    return;
                }

                // 2. Configure test times
                LocalTime now = LocalTime.now();
                testAutoCheckoutTime = LocalTime.of(TEST_CHECKOUT_HOUR, TEST_CHECKOUT_MINUTE);
                LocalTime onTimeCutoffForTest = LocalTime.of(TEST_CUTOFF_HOUR, TEST_CUTOFF_MINUTE);

                System.out.println("--- TEST MODE: CHECK-IN ---");
                System.out.println("Check-in time: " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                System.out.println("Late if after: " + onTimeCutoffForTest.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                System.out.println("Auto checkout at: " + testAutoCheckoutTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                boolean onTime = !now.isAfter(onTimeCutoffForTest);

                // 3. Create and save attendance record
                Attendance newAttendance = new Attendance();
                newAttendance.setIdStaff(currentUser.getId());
                newAttendance.setDate(Date.valueOf(java.time.LocalDate.now()));
                newAttendance.setTime(Time.valueOf(now));
                newAttendance.setOnTime(onTime);
                newAttendance.setWorkingHours(0); // Mark as unfinished

                attendanceDAO.addAttendance(newAttendance);
                
                // 4. Refresh user data to get updated salary from trigger
                currentUser = userDAO.getUserById(currentUser.getId());

                // 5. Update UI and start timer
                currentAttendanceRecord = attendanceDAO.getLatestUnfinishedAttendance(currentUser.getId());
                checkButton.setText("Check Out");
                checkButton.getStyleClass().setAll("check-button-out");
                elapsedSeconds = 0;
                startWorkingHoursTimer();
                refreshDashboard();

            } catch (SQLException e) {
                e.printStackTrace();
                // Handle DB error
            }
        } else {
            // --- CHECK OUT ---
            stopWorkingHoursTimer();
            testAutoCheckoutTime = null; // Reset test variable
            
            try {
                attendanceDAO.updateWorkingHoursForToday(currentAttendanceRecord.getId(), (int) elapsedSeconds);

                // Salary is now handled by the database trigger.
                // We just need to refresh the user data to get the latest salary.
                currentUser = userDAO.getUserById(currentUser.getId());

                currentAttendanceRecord = null;
                checkButton.setText("Checked In Today");
                checkButton.setDisable(true);
                checkButton.getStyleClass().setAll("check-button-in");
                refreshDashboard();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeClock() {
        if (digitalTimeLabel == null || analogClockCanvas == null) return;
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateDigitalClock();
            drawAnalogClock();
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        drawAnalogClock(); // Initial draw
    }

    private void updateDigitalClock() {
        if (digitalTimeLabel == null) return;
        LocalTime now = LocalTime.now();
        digitalTimeLabel.setText(now.format(DateTimeFormatter.ofPattern("hh : mm : ss a")));
    }
    
    private void drawAnalogClock() {
        if (analogClockCanvas == null) return;
        GraphicsContext gc = analogClockCanvas.getGraphicsContext2D();
        double width = analogClockCanvas.getWidth();
        double height = analogClockCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) / 2 * 0.85;
        
        gc.clearRect(0, 0, width, height);

        // Draw clock face
        gc.setStroke(Color.web("#9CA3AF"));
        gc.setLineWidth(3);
        
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            double startX = centerX + Math.sin(angle) * (radius - 10);
            double startY = centerY - Math.cos(angle) * (radius - 10);
            double endX = centerX + Math.sin(angle) * radius;
            double endY = centerY - Math.cos(angle) * radius;
            gc.strokeLine(startX, startY, endX, endY);
        }

        // Get current time
        LocalTime time = LocalTime.now();
        int hour = time.getHour() % 12;
        int minute = time.getMinute();
        int second = time.getSecond();

        // Calculate hand angles
        double hourAngle = Math.toRadians((hour * 30) + (minute * 0.5));
        double minuteAngle = Math.toRadians(minute * 6);
        double secondAngle = Math.toRadians(second * 6);

        // Draw hands
        drawClockHand(gc, centerX, centerY, hourAngle, radius * 0.5, 6, Color.BLACK);
        drawClockHand(gc, centerX, centerY, minuteAngle, radius * 0.75, 4, Color.BLACK);
        drawClockHand(gc, centerX, centerY, secondAngle, radius * 0.9, 2, Color.RED);

        // Draw center dot
        gc.setFill(Color.BLACK);
        gc.fillOval(centerX - 5, centerY - 5, 10, 10);
    }

    private void drawClockHand(GraphicsContext gc, double centerX, double centerY, double angle, double length, double width, Color color) {
        double endX = centerX + Math.sin(angle) * length;
        double endY = centerY - Math.cos(angle) * length;
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeLine(centerX, centerY, endX, endY);
    }

    @FXML
    public void handleViewSalary() {
        isSalaryVisible = !isSalaryVisible;
        updateSalaryDisplay();
    }

    // FIXED: Complete rewrite of updateLineCharts method
    private void updateLineCharts() {
        if (onTimeLineChart == null || lateLineChart == null) return;
        try {
            List<Attendance> userAttendance = attendanceDAO.getAttendanceByUserId(currentUser.getId());
            
            // Group by date and calculate on time/late percentage per day
            Map<String, List<Attendance>> byDate = userAttendance.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getDate().toLocalDate().toString()));

            XYChart.Series<String, Number> onTimeSeries = new XYChart.Series<>();
            onTimeSeries.setName("On Time %");
            XYChart.Series<String, Number> lateSeries = new XYChart.Series<>();
            lateSeries.setName("Late %");

            java.util.List<String> sortedDates = new java.util.ArrayList<>(byDate.keySet());
            java.util.Collections.sort(sortedDates);
            
            for (String date : sortedDates) {
                List<Attendance> records = byDate.get(date);
                long onTimeCount = records.stream().filter(Attendance::isOnTime).count();
                long total = records.size();
                double onTimePercent = total > 0 ? (onTimeCount * 100.0) / total : 0;
                double latePercent = 100.0 - onTimePercent;
                
                // FIXED: Extract only the day from the date (format: YYYY-MM-DD)
                String dayOnly = date.substring(date.lastIndexOf("-") + 1);
                
                onTimeSeries.getData().add(new XYChart.Data<>(dayOnly, onTimePercent));
                lateSeries.getData().add(new XYChart.Data<>(dayOnly, latePercent));
            }
            
            // Clear and set up the charts
            onTimeLineChart.getData().clear();
            onTimeLineChart.getData().add(onTimeSeries);
            
            lateLineChart.getData().clear();
            lateLineChart.getData().add(lateSeries);
            
            // FIXED: Ensure Y-axis is properly configured
            Platform.runLater(() -> {
                onTimeYAxis.setLowerBound(0);
                onTimeYAxis.setUpperBound(100);
                onTimeYAxis.setTickUnit(20);
                
                lateYAxis.setLowerBound(0);
                lateYAxis.setUpperBound(100);
                lateYAxis.setTickUnit(20);
                
                // Enable symbols for better visibility
                onTimeLineChart.setCreateSymbols(true);
                lateLineChart.setCreateSymbols(true);
                
                // Hide legend if not needed
                onTimeLineChart.setLegendVisible(false);
                lateLineChart.setLegendVisible(false);
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Timer logic
    private void startWorkingHoursTimer() {
        if (workingTimeLabel == null) return;
        stopWorkingHoursTimer(); // Ensure no multiple timers are running
        workingHoursTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            elapsedSeconds++;
            updateWorkingTimeLabel();

            // Auto checkout logic (modified for testing)
            LocalTime effectiveCheckoutTime = (testAutoCheckoutTime != null) 
                ? testAutoCheckoutTime 
                : LocalTime.of(17, 0);
            
            if (LocalTime.now().isAfter(effectiveCheckoutTime) && currentAttendanceRecord != null) {
                toggleCheckIn();
            }
        }));
        workingHoursTimeline.setCycleCount(Animation.INDEFINITE);
        workingHoursTimeline.play();
    }

    private void stopWorkingHoursTimer() {
        if (workingHoursTimeline != null) {
            workingHoursTimeline.stop();
        }
    }

    private void updateWorkingTimeLabel() {
        if (workingTimeLabel == null) return;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        workingTimeLabel.setText(String.format("%d Hr %02d Mins %02d Secs", hours, minutes, seconds));
    }
}
