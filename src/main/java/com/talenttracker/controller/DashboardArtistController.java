package com.talenttracker.controller;

import com.talenttracker.DatabaseManager;
import com.talenttracker.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.ToggleButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class DashboardArtistController {

    private int artistId;
    private String artistFullName;

    @FXML private ImageView profileAvatarView;
    @FXML private ImageView downloadIcon;
    @FXML private ImageView salesIcon;
    @FXML private ImageView albumsIcon;
    @FXML private ImageView visitorsIcon;
    @FXML private Label profileNameLabel;
    @FXML private ToggleButton monthFilterButton;
    @FXML private ToggleButton allFilterButton;

    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private LineChart<String, Number> socialMediaChart;

    @FXML private TableView<FanResponse> responsesTable;
    @FXML private TableColumn<FanResponse, Integer> numberCol;
    @FXML private TableColumn<FanResponse, String> sourceCol;
    @FXML private TableColumn<FanResponse, String> commentCol;
    @FXML private TableColumn<FanResponse, String> categoryCol;

    @FXML private PieChart sentimentChart;
    @FXML private VBox legendPane;

    @FXML private Label socialPopularityChangeLabel;
    @FXML private Label socialTotalFollowersLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalSalesChangeLabel;
    @FXML private Label albumsSoldLabel;
    @FXML private Label albumsSoldChangeLabel;
    @FXML private Label visitorsLabel;
    @FXML private Label visitorsChangeLabel;

    public void setArtistId(int artistId) {
        this.artistId = artistId;
        this.artistFullName = Main.getLoggedInUserFullName(); // It's better to get the full name here as well
        loadDashboardData();
    }

    @FXML
    public void initialize() {
        if (this.artistId != 0) { // If artistId is already set, load data
            loadDashboardData();
        } else { // Otherwise, get it from the logged-in user (initial load)
            this.artistId = Main.getLoggedInUserId();
            if (this.artistId != 0) {
                loadDashboardData();
            }
        }
        
        this.artistFullName = Main.getLoggedInUserFullName();
        
        profileNameLabel.setText(this.artistFullName);
        String profileImageName = this.artistFullName.replaceAll("\\s+", "") + "Profile.png";
        try {
            profileAvatarView.setImage(new Image("file:img/" + profileImageName));
        } catch (Exception e) {
            System.err.println("Could not load profile image: " + profileImageName);
            profileAvatarView.setImage(new Image("file:img/DefaultArtist.png")); 
        }

        try {
            salesIcon.setImage(new Image("file:img/MoneyIcon.png"));
            albumsIcon.setImage(new Image("file:img/BagIcon.png"));
            visitorsIcon.setImage(new Image("file:img/VisitorIcon.png"));
        } catch (Exception e) {
            System.err.println("Could not load icons.");
        }
    }

    private void loadDashboardData() {
        if (artistId == 0) return; // Don't load data if no artist is set

        loadTopAlbumChart();
        loadSocialMediaChart();
        loadFansResponses();
        loadDashboardMetrics();
    }

    private void loadTopAlbumChart() {
        topAlbumChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? ORDER BY sold DESC LIMIT 7";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("albumName"), rs.getInt("sold")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        topAlbumChart.getData().add(series);
    }

    private void loadSocialMediaChart() {
        socialMediaChart.getData().clear();
        String sql = "SELECT socialMedia, todayFollowers, date FROM Popularity WHERE idArtis = ? AND date >= ? ORDER BY date ASC";
        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for(String platform : new String[]{"Instagram", "X", "TikTok"}){
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(platform);
            seriesMap.put(platform, series);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusMonths(1))); 
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                seriesMap.get(rs.getString("socialMedia")).getData().add(new XYChart.Data<>(rs.getDate("date").toLocalDate().format(formatter), rs.getInt("todayFollowers")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        socialMediaChart.getData().addAll(seriesMap.values());
    }

    private void loadFansResponses() {
        ObservableList<FanResponse> responses = FXCollections.observableArrayList();
        String sql = "SELECT source, comment, category FROM FansResponse WHERE idArtis = ? ORDER BY timestamp DESC LIMIT 5";
        int positive = 0, negative = 0, neutral = 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            int counter = 1;
            while(rs.next()) {
                String category = rs.getString("category");
                if (category != null) {
                    responses.add(new FanResponse(counter++, rs.getString("source"), rs.getString("comment"), category));
                    switch(category){
                        case "Positive": positive++; break;
                        case "Negative": negative++; break;
                        case "Neutral": neutral++; break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        responsesTable.setItems(responses);

        updateSentimentChart(positive, negative, neutral);
    }
    
    private void updateSentimentChart(int positive, int negative, int neutral) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        if(positive > 0) pieChartData.add(new PieChart.Data("Positive", positive));
        if(negative > 0) pieChartData.add(new PieChart.Data("Negative", negative));
        if(neutral > 0) pieChartData.add(new PieChart.Data("Neutral", neutral));
        
        sentimentChart.setData(pieChartData);
        
        legendPane.getChildren().clear();
        Map<String, String> colorMap = Map.of("Positive", "#28a745", "Negative", "#dc3545", "Neutral", "#ffc107");
        for(final PieChart.Data data : sentimentChart.getData()){
            HBox legendEntry = new HBox(5);
            Circle colorDot = new Circle(5, Color.web(colorMap.get(data.getName())));
            Label legendLabel = new Label(data.getName());
            legendEntry.getChildren().addAll(colorDot, legendLabel);
            legendPane.getChildren().add(legendEntry);
        }
    }

    private void loadDashboardMetrics() {
        if (artistId == 0) return;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // Sales
        double todaySales = getMetricForDate("Sales", "salesToday", today);
        double yesterdaySales = getMetricForDate("Sales", "salesToday", yesterday);
        totalSalesLabel.setText(String.format(new Locale("id", "ID"), "IDR %,.0f", todaySales));
        updateChangeLabel(totalSalesChangeLabel, todaySales, yesterdaySales);
        
        // Albums
        double todayAlbums = getMetricForDate("AlbumSold", "albumSoldToday", today);
        double yesterdayAlbums = getMetricForDate("AlbumSold", "albumSoldToday", yesterday);
        albumsSoldLabel.setText(String.format("%,.0f", todayAlbums));
        updateChangeLabel(albumsSoldChangeLabel, todayAlbums, yesterdayAlbums);

        // Visitors
        double todayVisitors = getMetricForDate("Visitors", "visitorsToday", today);
        double yesterdayVisitors = getMetricForDate("Visitors", "visitorsToday", yesterday);
        visitorsLabel.setText(String.format("%,.0f", todayVisitors));
        updateChangeLabel(visitorsChangeLabel, todayVisitors, yesterdayVisitors);

        // Social Media Followers
        loadSocialPopularityStats();
    }
    
    private double getMetricForDate(String tableName, String columnName, LocalDate date) {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE idArtis = ? AND date = ?", columnName, tableName);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.artistId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private void updateChangeLabel(Label changeLabel, double currentValue, double previousValue) {
        if (previousValue == 0) {
            changeLabel.setText(currentValue > 0 ? "+100%" : "N/A");
            return;
        }
        double percentChange = ((currentValue - previousValue) / previousValue) * 100;
        changeLabel.setText(String.format("%+.0f%% vs yesterday", percentChange));
    }

    private void loadSocialPopularityStats() {
        double currentFollowers = getFollowerTotalForDate(LocalDate.now());
        double lastMonthFollowers = getFollowerTotalForDate(LocalDate.now().minusMonths(1));

        socialTotalFollowersLabel.setText(String.format("%,.0f Followers", currentFollowers));
        if (lastMonthFollowers == 0) {
             socialPopularityChangeLabel.setText(currentFollowers > 0 ? "+100%" : "N/A");
             return;
        }
        double followerChange = ((currentFollowers - lastMonthFollowers) / lastMonthFollowers) * 100;
        socialPopularityChangeLabel.setText(String.format("%+.0f%% VS LAST MONTH", followerChange));
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

    public static class FanResponse {
        private final int number;
        private final String source;
        private final String comment;
        private final String category;

        public FanResponse(int number, String source, String comment, String category) {
            this.number = number;
            this.source = source;
            this.comment = comment;
            this.category = category;
        }
        public int getNumber() { return number; }
        public String getSource() { return source; }
        public String getComment() { return comment; }
        public String getCategory() { return category; }
    }
}
