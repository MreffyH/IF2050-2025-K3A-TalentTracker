package com.talenttracker.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.talenttracker.DatabaseManager;
import com.talenttracker.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class LaporanKinerjaController {

    // --- Injected Header Controller ---
    @FXML private HeaderController headerComponentController;

    // --- UI Elements ---
    @FXML private Label successMessageLabel;
    @FXML private ImageView artistAvatarView;
    @FXML private Label artistNameLabel;

    // Charts
    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private CategoryAxis topAlbumXAxis;
    @FXML private NumberAxis topAlbumYAxis;
    @FXML private LineChart<String, Number> socialMediaChart;
    @FXML private CategoryAxis socialMediaXAxis;
    @FXML private NumberAxis socialMediaYAxis;

    // Album Form
    @FXML private TextField albumNameField;
    @FXML private TextField albumSoldField;
    @FXML private Button addAlbumButton;

    // Social Media Form
    @FXML private ComboBox<String> socialMediaComboBox;
    @FXML private TextField followersField;
    @FXML private Button addSocialButton;

    // Visitors Elements
    @FXML private Label visitorsCountLabel;
    @FXML private TextField visitorsField;
    @FXML private Button addVisitorsButton;
    @FXML private ImageView visitorsIconView;

    // Sales Elements
    @FXML private Label salesAmountLabel;
    @FXML private TextField salesTodayField;
    @FXML private Button addSalesButton;
    @FXML private ImageView salesIconView;

    // Album Sold Elements
    @FXML private Label albumsSoldCountLabel;
    @FXML private ImageView albumsSoldIconView;

    // Social Popularity Elements
    @FXML private Label socialPopularityChangeLabel;
    @FXML private Label socialTotalFollowersLabel;
    @FXML private Label visitorsChangeLabel;
    @FXML private Label salesChangeLabel;
    @FXML private Label albumsSoldChangeLabel;

    // --- Data Series for Charts ---
    private XYChart.Series<String, Number> topAlbumSeries;
    private XYChart.Series<String, Number> socialMediaSeries;

    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
        "#FFB74D", "#FFA726", "#EC407A", "#42A5F5",
        "#AB47BC", "#81C784", "#4DB6AC", "#64B5F6", "#90A4AE"
    );

    private int artistId;

    public void setArtist(int artistId, String artistName) {
        this.artistId = artistId;
        this.artistNameLabel.setText(artistName);
        
        // Update profile image
        try {
            String profileImageName = artistName.replaceAll("\\s+", "") + "Profile.png";
            Image profileImage = new Image("file:img/" + profileImageName);
            if (!profileImage.isError()) {
                artistAvatarView.setImage(profileImage);
            } else {
                 System.err.println("Could not load profile image: " + profileImageName);
            }
        } catch (Exception e) {
            System.err.println("Error creating image for: " + artistName);
        }

        // Reload all data for the newly set artist
        loadAllDataFromDatabase();
    }
    
    @FXML
    public void initialize() {
        // Data will be loaded when setArtist is called.
        // We can initialize UI components that don't depend on the artist.
        initializeImageViews();
        setupComboBox();
        setupButtonHandlers();
        
        // Initialize chart series
        topAlbumSeries = new XYChart.Series<>();
        topAlbumChart.getData().add(topAlbumSeries);
        socialMediaChart.getData().clear();
        socialMediaChart.setAnimated(true);

        if ("Artist".equalsIgnoreCase(Main.getLoggedInUserRole())) {
            setArtist(Main.getLoggedInUserId(), Main.getLoggedInUserFullName());
        }
    }

    private void initializeImageViews() {
        try {
            visitorsIconView.setImage(new Image("file:img/VisitorIcon.png"));
            salesIconView.setImage(new Image("file:img/MoneyIcon.png"));
            albumsSoldIconView.setImage(new Image("file:img/BagIcon.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void setupComboBox() {
        socialMediaComboBox.setItems(FXCollections.observableArrayList(
            "Instagram", "X", "TikTok"
        ));
    }

    private void setupButtonHandlers() {
        addAlbumButton.setOnAction(e -> handleAddAlbum());
        addSocialButton.setOnAction(e -> handleAddSocial());
        addVisitorsButton.setOnAction(e -> handleAddVisitors());
        addSalesButton.setOnAction(e -> handleAddSales());
    }

    private void loadAllDataFromDatabase() {
        loadStats();
        loadTopAlbumChart();
        loadSocialMediaChart();
        loadSocialPopularityStats();
    }

    private void loadStats() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Load and display today's stats directly
        double todayVisitors = getTotalForDate("Visitors", "visitorsToday", today, this.artistId);
        visitorsCountLabel.setText(String.format("%,.0f", todayVisitors));
        double todaySales = getTotalForDate("Sales", "salesToday", today, this.artistId);
        salesAmountLabel.setText(currencyFormat.format(todaySales));
        double todayAlbums = getTotalForDate("AlbumSold", "albumSoldToday", today, this.artistId);
        albumsSoldCountLabel.setText(String.format("%,.0f", todayAlbums));

        // Calculate and display percentage change from yesterday
        double yesterdayVisitors = getTotalForDate("Visitors", "visitorsToday", yesterday, this.artistId);
        updateChangeLabel(visitorsChangeLabel, todayVisitors, yesterdayVisitors, false);
        
        double yesterdaySales = getTotalForDate("Sales", "salesToday", yesterday, this.artistId);
        updateChangeLabel(salesChangeLabel, todaySales, yesterdaySales, true);
        
        double yesterdayAlbums = getTotalForDate("AlbumSold", "albumSoldToday", yesterday, this.artistId);
        updateChangeLabel(albumsSoldChangeLabel, todayAlbums, yesterdayAlbums, false);
    }

    private void loadTopAlbumChart() {
        topAlbumSeries.getData().clear();
        String sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? ORDER BY sold DESC LIMIT 7";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topAlbumSeries.getData().add(new XYChart.Data<>(rs.getString("albumName"), rs.getInt("sold")));
                }
            }
            styleBarChartNodes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSocialMediaChart() {
        socialMediaChart.getData().clear();
        socialMediaChart.layout();

        String sql = "SELECT socialMedia, todayFollowers, date FROM Popularity WHERE idArtis = ? ORDER BY date ASC";

        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        List<String> platforms = Arrays.asList("Instagram", "X", "TikTok");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for(String platform : platforms) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(platform);
            seriesMap.put(platform, series);
        }

         try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    String platform = rs.getString("socialMedia");
                    int followers = rs.getInt("todayFollowers");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    
                    XYChart.Series<String, Number> series = seriesMap.get(platform);
                    if (series != null) {
                        series.getData().add(new XYChart.Data<>(date.format(formatter), followers));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        for (String platform : platforms) {
            socialMediaChart.getData().add(seriesMap.get(platform));
        }
    }

    private void loadSocialPopularityStats() {
        double currentFollowers = getFollowerTotalForDate(LocalDate.now());
        double lastMonthFollowers = getFollowerTotalForDate(LocalDate.now().minusMonths(1));

        socialTotalFollowersLabel.setText(String.format("%,.0f Followers", currentFollowers));
        updateChangeLabel(socialPopularityChangeLabel, currentFollowers, lastMonthFollowers, false);
    }

    private double getFollowerTotalForDate(LocalDate date) {
        double totalFollowers = 0;
        String sql = "SELECT SUM(todayFollowers) as total FROM Popularity WHERE idArtis = ? AND date <= ? " + 
                     "AND (socialMedia, date) IN " +
                     "(SELECT socialMedia, MAX(date) FROM Popularity WHERE idArtis = ? AND date <= ? GROUP BY socialMedia)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setInt(3, artistId);
            pstmt.setDate(4, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalFollowers = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalFollowers;
    }

    private double getTotalForDate(String tableName, String columnName, LocalDate date, int artistId) {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE date = ? AND idArtis = ?", columnName, tableName);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setInt(2, artistId);
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
            if (current > 0) label.setText("+100%"); // Infinite growth
            else label.setText("N/A");
            return;
        }
        double change = ((current - previous) / previous) * 100;
        String prefix = change >= 0 ? "+" : "";
        label.setText(String.format("%s%.0f%%", prefix, change));
    }

    private void handleAddAlbum() {
        try {
            String name = albumNameField.getText();
            int sold = Integer.parseInt(albumSoldField.getText());
            if (name == null || name.trim().isEmpty()) return;

            // Insert into TopAlbum
            String topAlbumSql = "INSERT INTO TopAlbum (albumName, sold, date, idArtis) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(topAlbumSql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, sold);
                pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(4, artistId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Update AlbumSold
            String albumSoldSql = "INSERT INTO AlbumSold (idArtis, date, albumSoldToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE albumSoldToday = albumSoldToday + VALUES(albumSoldToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(albumSoldSql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(3, sold);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            albumNameField.clear();
            albumSoldField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for album sold.");
        }
    }

    private void handleAddSocial() {
        try {
            int followers = Integer.parseInt(followersField.getText());
            String platform = socialMediaComboBox.getValue();
            if (platform == null) return;

            String sql = "INSERT INTO Popularity (idArtis, date, socialMedia, todayFollowers) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE todayFollowers = VALUES(todayFollowers)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setString(3, platform);
                pstmt.setInt(4, followers);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            followersField.clear();
            socialMediaComboBox.getSelectionModel().clearSelection();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for followers.");
        }
    }

    private void handleAddVisitors() {
        try {
            int newVisitors = Integer.parseInt(visitorsField.getText());

            String sql = "INSERT INTO Visitors (idArtis, date, visitorsToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE visitorsToday = visitorsToday + VALUES(visitorsToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(3, newVisitors);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            visitorsField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for visitors.");
        }
    }

    private void handleAddSales() {
        try {
            double salesToday = Double.parseDouble(salesTodayField.getText());

            String sql = "INSERT INTO Sales (idArtis, date, salesToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE salesToday = salesToday + VALUES(salesToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setDouble(3, salesToday);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            salesTodayField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for sales.");
        }
    }

    private void styleBarChartNodes() {
        // Must be called after data is added
        for (int i = 0; i < topAlbumSeries.getData().size(); i++) {
            XYChart.Data<String, Number> data = topAlbumSeries.getData().get(i);
            if (data.getNode() != null) {
                 data.getNode().setStyle("-fx-bar-fill: " + barColors.get(i % barColors.size()) + ";");
            } else {
                // If the node is not yet created, we can't style it directly.
                // We rely on the fact that this is called after new data is added and rendered.
            }
        }
    }

    private void showSuccessMessage() {
        successMessageLabel.setVisible(true);
        successMessageLabel.setManaged(true);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), successMessageLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), successMessageLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            successMessageLabel.setVisible(false);
            successMessageLabel.setManaged(false);
        });

        fadeIn.setOnFinished(event -> pause.play());
        pause.setOnFinished(event -> fadeOut.play());
        fadeIn.play();
    }
}
