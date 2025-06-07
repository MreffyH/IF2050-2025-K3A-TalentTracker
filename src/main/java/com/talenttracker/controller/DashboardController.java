package com.talenttracker.controller;

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
import java.io.IOException;

public class DashboardController {
    @FXML private ImageView crownIconView;
    @FXML private ImageView artistImageView;
    @FXML private ImageView starIconView;
    @FXML private Label artistNameLabel;

    // Main Dashboard Elements
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private ImageView searchIconView;

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
        setupCharts();
        setupSearch();
        initializeTopArtist();
        initializeStatIcons();
        setupMonthlyTotalChart();
    }

    private void setupCharts() {
        setupTopAlbumChart();
        setupSocialInsightsChart();
    }

    private void setupTopAlbumChart() {
        // Sample data for top albums
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Album 1", 150));
        series.getData().add(new XYChart.Data<>("Album 2", 120));
        series.getData().add(new XYChart.Data<>("Album 3", 90));
        series.getData().add(new XYChart.Data<>("Album 4", 80));
        series.getData().add(new XYChart.Data<>("Album 5", 60));
        
        topAlbumChart.getData().add(series);
    }

    private void setupSocialInsightsChart() {
        // Sample data for social media insights
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Mon", 1000));
        series.getData().add(new XYChart.Data<>("Tue", 1500));
        series.getData().add(new XYChart.Data<>("Wed", 1300));
        series.getData().add(new XYChart.Data<>("Thu", 1800));
        series.getData().add(new XYChart.Data<>("Fri", 2000));
        series.getData().add(new XYChart.Data<>("Sat", 2200));
        series.getData().add(new XYChart.Data<>("Sun", 1900));
        
        socialInsightsChart.getData().add(series);
    }

    private void initializeTopArtist() {
        artistNameLabel.setText("Kana Arima");
        try {
            // Load artist image
            Image artistImg = new Image("file:img/KanaArimaProfile.png");
            artistImageView.setImage(artistImg);

            // Load crown icon
            Image crownImg = new Image("file:img/IconCrown.png");
            crownIconView.setImage(crownImg);

            // Load star icon
            Image starImg = new Image("file:img/IconPartyL.png");
            starIconView.setImage(starImg);
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void initializeStatIcons() {
        try {
            // Load dollar icon
            Image dollarImg = new Image("file:img/MoneyIcon.png");
            dollarIconView.setImage(dollarImg);
        } catch (Exception e) {
            System.err.println("Error loading money icon: " + e.getMessage());
        }

        try {
            // Load bag icon
            Image bagImg = new Image("file:img/BagIcon.png");
            bagIconView.setImage(bagImg);
        } catch (Exception e) {
            System.err.println("Error loading bag icon: " + e.getMessage());
        }

        try {
            // Load users icon
            Image usersImg = new Image("file:img/VisitorIcon.png");
            usersIconView.setImage(usersImg);
        } catch (Exception e) {
            System.err.println("Error loading visitor icon: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchButton.setOnAction(event -> handleSearch());
        try {
            // Load search icon
            Image searchImg = new Image("file:img/SearchIcon.png");
            searchIconView.setImage(searchImg);
        } catch (Exception e) {
            System.err.println("Error loading search icon: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String searchTerm = searchTextField.getText().trim();
        
        if (searchTerm.equalsIgnoreCase("Kana Arima")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LaporanKinerjaView.fxml"));
                Scene scene = searchTextField.getScene();
                scene.setRoot(loader.load());
            } catch (IOException e) {
                System.err.println("Error loading performance report: " + e.getMessage());
            }
        } else {
            // TODO: Implement search for other artists
            System.out.println("Searching for: " + searchTerm);
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
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Progress", 48),
            new PieChart.Data("Remaining", 52)
        );
        
        monthlyTotalChart.setData(pieChartData);
        monthlyTotalChart.setStartAngle(90);
    }
}

