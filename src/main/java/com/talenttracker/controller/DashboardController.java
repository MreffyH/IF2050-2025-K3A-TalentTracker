package com.talenttracker.controller;

import com.talenttracker.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {
    @FXML private ImageView crownIconView;
    @FXML private ImageView artistImageView;
    @FXML private ImageView starIconView;
    @FXML private Label artistNameLabel;

    // Main Dashboard Elements
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private ImageView searchIconView;

    // Stat Card Labels
    @FXML private Label totalSalesLabel;
    @FXML private Label albumsSoldLabel;
    @FXML private Label newVisitorsLabel;
    @FXML private Label monthlyTotalLabel;
    @FXML private Label totalSalesChangeLabel;
    @FXML private Label albumsSoldChangeLabel;
    @FXML private Label newVisitorsChangeLabel;
    @FXML private Label monthlyTotalChangeLabel;

    // Stat Card Icons
    @FXML private ImageView dollarIconView;
    @FXML private ImageView bagIconView;
    @FXML private ImageView usersIconView;

    // Charts
    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private CategoryAxis topAlbumXAxis;
    @FXML private NumberAxis topAlbumYAxis;

    @FXML private LineChart<String, Number> socialInsightsChart;
    @FXML private CategoryAxis socialXAxis;
    @FXML private NumberAxis socialYAxis;

    @FXML private PieChart monthlyTotalChart;

    @FXML
    public void initialize() {
        loadDashboardData();
        setupSearch();
        initializeTopArtist();
        initializeStatIcons();
    }
    
    private void loadDashboardData() {
        loadTodayStats();
        loadMonthlyTotal();
        setupTopAlbumChart();
        setupSocialInsightsChart();
        setupMonthlyTotalChart(); // This might need data from monthly total
    }
    
    private void loadTodayStats() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Today's vs Yesterday's Sales
        double todaySales = getTotalForDate("Sales", "salesToday", today);
        double yesterdaySales = getTotalForDate("Sales", "salesToday", yesterday);
        totalSalesLabel.setText(currencyFormat.format(todaySales));
        updateChangeLabel(totalSalesChangeLabel, todaySales, yesterdaySales, true);

        // Today's vs Yesterday's Albums Sold
        double todayAlbums = getTotalForDate("AlbumSold", "albumSoldToday", today);
        double yesterdayAlbums = getTotalForDate("AlbumSold", "albumSoldToday", yesterday);
        albumsSoldLabel.setText(String.format("%,.0f", todayAlbums));
        updateChangeLabel(albumsSoldChangeLabel, todayAlbums, yesterdayAlbums, false);

        // Today's vs Yesterday's New Visitors
        double todayVisitors = getTotalForDate("Visitors", "visitorsToday", today);
        double yesterdayVisitors = getTotalForDate("Visitors", "visitorsToday", yesterday);
        newVisitorsLabel.setText(String.format("%,.0f", todayVisitors));
        updateChangeLabel(newVisitorsChangeLabel, todayVisitors, yesterdayVisitors, false);
    }

    private void loadMonthlyTotal() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        LocalDate today = LocalDate.now();
        
        double thisMonthSales = getMonthlyTotal("Sales", "salesToday", today.getYear(), today.getMonthValue());
        monthlyTotalLabel.setText(currencyFormat.format(thisMonthSales));

        LocalDate lastMonth = today.minusMonths(1);
        double lastMonthSales = getMonthlyTotal("Sales", "salesToday", lastMonth.getYear(), lastMonth.getMonthValue());
        updateChangeLabel(monthlyTotalChangeLabel, thisMonthSales, lastMonthSales, true);
    }

    private double getTotalForDate(String tableName, String columnName, LocalDate date) {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE date = ?", columnName, tableName);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getMonthlyTotal(String tableName, String columnName, int year, int month) {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE YEAR(date) = ? AND MONTH(date) = ?", columnName, tableName);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateChangeLabel(Label label, double current, double previous, boolean isCurrency) {
        if (previous == 0) {
            if (current > 0) {
                label.setText("+100%"); // Or "Infinite", or simply a large number
            } else {
                label.setText("N/A");
            }
            return;
        }
        double change = ((current - previous) / previous) * 100;
        String prefix = change >= 0 ? "+" : "";
        label.setText(String.format("%s%.0f%%", prefix, change));
    }

    private void setupTopAlbumChart() {
        topAlbumChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String sql = "SELECT albumName, SUM(sold) AS total_sold FROM TopAlbum GROUP BY albumName ORDER BY total_sold DESC LIMIT 5";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("albumName"), rs.getInt("total_sold")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        topAlbumChart.getData().add(series);
    }

    private void setupSocialInsightsChart() {
        socialInsightsChart.getData().clear();
        String sql = "SELECT date, SUM(todayFollowers) AS total_followers FROM Popularity WHERE date >= ? GROUP BY date ORDER BY date ASC";
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Followers");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(7))); // Last 7 days
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    series.getData().add(new XYChart.Data<>(date.format(formatter), rs.getInt("total_followers")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        socialInsightsChart.getData().add(series);
    }

    private void initializeTopArtist() {
        // This could be made dynamic in the future
        artistNameLabel.setText("Kana Arima");
        try {
            Image artistImg = new Image("file:img/KanaArimaProfile.png");
            artistImageView.setImage(artistImg);
            Image crownImg = new Image("file:img/IconCrown.png");
            crownIconView.setImage(crownImg);
            Image starImg = new Image("file:img/IconPartyL.png");
            starIconView.setImage(starImg);
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void initializeStatIcons() {
        try {
            dollarIconView.setImage(new Image("file:img/MoneyIcon.png"));
            bagIconView.setImage(new Image("file:img/BagIcon.png"));
            usersIconView.setImage(new Image("file:img/VisitorIcon.png"));
        } catch (Exception e) {
            System.err.println("Error loading stat icons: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchButton.setOnAction(event -> handleSearch());
        try {
            searchIconView.setImage(new Image("file:img/SearchIcon.png"));
        } catch (Exception e) {
            System.err.println("Error loading search icon: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String searchTerm = searchTextField.getText().trim();
        if (searchTerm.isEmpty()) return;

        String sql = "SELECT idUser FROM User WHERE fullName = ? AND role = 'Artist'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchTerm);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int artistId = rs.getInt("idUser");
                    
                    // Navigate to LaporanKinerjaView
                    BorderPane mainContainer = (BorderPane) searchTextField.getScene().getRoot();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LaporanKinerjaView.fxml"));
                    mainContainer.setCenter(loader.load());

                    // Pass the artist info to the controller
                    LaporanKinerjaController controller = loader.getController();
                    controller.setArtist(artistId, searchTerm);

                } else {
                    // Optional: handle case where artist is not found
                    System.out.println("Artist not found: " + searchTerm);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToStaffView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DashboardViewStaff.fxml"));
            Scene scene = searchTextField.getScene();
            scene.setRoot(loader.load());
        } catch (IOException e) {
            System.err.println("Error loading staff dashboard: " + e.getMessage());
        }
    }

    private void setupMonthlyTotalChart() {
        // This is decorative, as the main value is in the label.
        // You could tie this to a specific goal if you have one.
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Progress", 75),
            new PieChart.Data("Remaining", 25)
        );
        
        monthlyTotalChart.setData(pieChartData);
        monthlyTotalChart.setStartAngle(90);
    }
}

