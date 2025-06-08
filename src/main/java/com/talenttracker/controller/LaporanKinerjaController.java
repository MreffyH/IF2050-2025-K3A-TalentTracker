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

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.talenttracker.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

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

    // --- Data Series for Charts ---
    private XYChart.Series<String, Number> topAlbumSeries;
    private XYChart.Series<String, Number> socialMediaSeries;

    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
        "#FFB74D", "#FFA726", "#EC407A", "#42A5F5",
        "#AB47BC", "#81C784", "#4DB6AC", "#64B5F6", "#90A4AE"
    );

    @FXML
    public void initialize() {
        
        // Setup UI and Event Handlers
        initializeImageViews();
        setupComboBox();
        setupButtonHandlers();
        setupInitialChartData();
    }

    private void initializeImageViews() {
        try {
            artistAvatarView.setImage(new Image("file:img/KanaArimaProfile.png"));
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

    private void setupInitialChartData() {
        // --- Top Album Chart ---
        topAlbumSeries = new XYChart.Series<>();
        topAlbumSeries.getData().add(new XYChart.Data<>("Gnarly", 300));
        topAlbumSeries.getData().add(new XYChart.Data<>("Afnan", 150));
        topAlbumSeries.getData().add(new XYChart.Data<>("Swicy", 250));
        topAlbumSeries.getData().add(new XYChart.Data<>("Idol", 800));
        topAlbumSeries.getData().add(new XYChart.Data<>("Fun", 550));
        topAlbumSeries.getData().add(new XYChart.Data<>("No Way", 750));
        topAlbumSeries.getData().add(new XYChart.Data<>("Robloks", 850));
        topAlbumChart.getData().add(topAlbumSeries);
        styleBarChartNodes();

        // --- Social Media Chart ---
        socialMediaSeries = new XYChart.Series<>();
        socialMediaSeries.getData().add(new XYChart.Data<>("W1", 1000));
        socialMediaSeries.getData().add(new XYChart.Data<>("W2", 1500));
        socialMediaSeries.getData().add(new XYChart.Data<>("W3", 2500));
        socialMediaSeries.getData().add(new XYChart.Data<>("W4", 4000));
        socialMediaChart.getData().add(socialMediaSeries);
    }

    private void handleAddAlbum() {
        try {
            String name = albumNameField.getText();
            int sold = Integer.parseInt(albumSoldField.getText());
            if (name == null || name.trim().isEmpty()) return;

            // Update Album Sales Stat Card
            int currentAlbumsSold = Integer.parseInt(albumsSoldCountLabel.getText());
            albumsSoldCountLabel.setText(String.valueOf(currentAlbumsSold + sold));

            // Update Bar Chart
            topAlbumSeries.getData().add(new XYChart.Data<>(name, sold));
            styleBarChartNodes();

            albumNameField.clear();
            albumSoldField.clear();
            showSuccessMessage();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for album sold.");
        }
    }

    private void handleAddSocial() {
        try {
            int followers = Integer.parseInt(followersField.getText());
            String platform = socialMediaComboBox.getValue();
            if (platform == null) return;

            // Database insert
            String sql = "INSERT INTO Popularity (socialMedia, todayFollowers, date, idArtis) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE todayFollowers = VALUES(todayFollowers)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, platform);
                pstmt.setInt(2, followers);
                pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(4, 1); // Assuming artist ID for Kana Arima is 1
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // For simplicity, we add a new week to the chart
            String nextWeek = "W" + (socialMediaSeries.getData().size() + 1);
            socialMediaSeries.getData().add(new XYChart.Data<>(nextWeek, followers));

            followersField.clear();
            socialMediaComboBox.getSelectionModel().clearSelection();
            showSuccessMessage();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for followers.");
        }
    }

    private void handleAddVisitors() {
        try {
            int newVisitors = Integer.parseInt(visitorsField.getText());

            // Database insert
            String sql = "INSERT INTO Visitors (visitorsToday, date, idArtis) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE visitorsToday = visitorsToday + VALUES(visitorsToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newVisitors);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(3, 1); // Assuming artist ID for Kana Arima is 1
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int currentVisitors = Integer.parseInt(visitorsCountLabel.getText());
            visitorsCountLabel.setText(String.valueOf(currentVisitors + newVisitors));
            visitorsField.clear();
            showSuccessMessage();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for visitors.");
        }
    }

    private void handleAddSales() {
        try {
            double salesToday = Double.parseDouble(salesTodayField.getText());

            // Database insert
            String sql = "INSERT INTO Sales (salesToday, date, idStaff) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE salesToday = salesToday + VALUES(salesToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, salesToday);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(3, 2); // Assuming staff ID is 2
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Use NumberFormat for locale-specific parsing
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            double currentSales = 0;
            try {
                // Remove the currency symbol and parse
                String salesText = salesAmountLabel.getText().replace("IDR", "").trim();
                currentSales = currencyFormat.parse(salesText).doubleValue();
            } catch (java.text.ParseException e) {
                // Fallback for plain numbers if parsing fails
                try {
                    currentSales = Double.parseDouble(salesAmountLabel.getText());
                } catch (NumberFormatException nfe) {
                    System.err.println("Could not parse current sales amount: " + salesAmountLabel.getText());
                    nfe.printStackTrace();
                    return; // Exit if parsing fails
                }
            }

            double newTotal = currentSales + salesToday;
            salesAmountLabel.setText(currencyFormat.format(newTotal));
            
            salesTodayField.clear();
            showSuccessMessage();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for sales.");
        }
    }

    private void styleBarChartNodes() {
        for (int i = 0; i < topAlbumSeries.getData().size(); i++) {
            XYChart.Data<String, Number> data = topAlbumSeries.getData().get(i);
            if(data.getNode() != null) {
                String color = barColors.get(i % barColors.size());
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
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
