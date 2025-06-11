package com.talenttracker.controller;

import com.talenttracker.dao.DashboardDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Album;
import com.talenttracker.model.DailyStats;
import com.talenttracker.model.Popularity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DashboardController {

    @FXML private ImageView crownIconView;
    @FXML private ImageView artistImageView;
    @FXML private ImageView starIconView;
    @FXML private Label artistNameLabel;
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private ImageView searchIconView;
    @FXML private Label totalSalesLabel;
    @FXML private Label albumsSoldLabel;
    @FXML private Label newVisitorsLabel;
    @FXML private Label monthlyTotalLabel;
    @FXML private Label totalSalesChangeLabel;
    @FXML private Label albumsSoldChangeLabel;
    @FXML private Label newVisitorsChangeLabel;
    @FXML private Label monthlyTotalChangeLabel;
    @FXML private ImageView dollarIconView;
    @FXML private ImageView bagIconView;
    @FXML private ImageView usersIconView;
    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private LineChart<String, Number> socialInsightsChart;
    @FXML private PieChart monthlyTotalChart;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
            "#FEB95A", "#E89B2F", "#223055", "#FFB7CA", "#28AEF3",
            "#D7598B", "#A9DFD8", "#538EA5"
    );

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
        setupMonthlyTotalChart();
    }

    private void loadTodayStats() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        try {
            DailyStats todayStats = dashboardDAO.getDailyStatsForDate(today);
            DailyStats yesterdayStats = dashboardDAO.getDailyStatsForDate(yesterday);

            totalSalesLabel.setText(currencyFormat.format(todayStats.getTotalSales()));
            albumsSoldLabel.setText(String.format("%,.0f", (double) todayStats.getAlbumsSold()));
            newVisitorsLabel.setText(String.format("%,.0f", (double) todayStats.getNewVisitors()));

            updateChangeLabel(totalSalesChangeLabel, todayStats.getTotalSales(), yesterdayStats.getTotalSales(), true);
            updateChangeLabel(albumsSoldChangeLabel, todayStats.getAlbumsSold(), yesterdayStats.getAlbumsSold(), false);
            updateChangeLabel(newVisitorsChangeLabel, todayStats.getNewVisitors(), yesterdayStats.getNewVisitors(), false);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMonthlyTotal() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);

        try {
            double thisMonthSales = dashboardDAO.getMonthlyTotalSales(today.getYear(), today.getMonthValue());
            double lastMonthSales = dashboardDAO.getMonthlyTotalSales(lastMonth.getYear(), lastMonth.getMonthValue());
            monthlyTotalLabel.setText(currencyFormat.format(thisMonthSales));
            updateChangeLabel(monthlyTotalChangeLabel, thisMonthSales, lastMonthSales, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateChangeLabel(Label label, double current, double previous, boolean isCurrency) {
        if (previous == 0) {
            label.setText(current > 0 ? "+100%" : "N/A");
            return;
        }
        double change = ((current - previous) / previous) * 100;
        label.setText(String.format("%s%.0f%%", change >= 0 ? "+" : "", change));
    }

    private void setupTopAlbumChart() {
        topAlbumChart.getData().clear();
        try {
            List<Album> topAlbums = dashboardDAO.getTopAlbums();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Album album : topAlbums) {
                series.getData().add(new XYChart.Data<>(album.getName(), album.getSales()));
            }
            topAlbumChart.getData().add(series);
            topAlbumChart.applyCss(); 
            styleBarChart(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void styleBarChart(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                String color = barColors.get(random.nextInt(barColors.size()));
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }
        }
    }

    private void setupSocialInsightsChart() {
        socialInsightsChart.getData().clear();
        try {
            List<Popularity> socialData = dashboardDAO.getSocialInsights();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Followers");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            for (Popularity popularity : socialData) {
                series.getData().add(new XYChart.Data<>(popularity.getDate().format(formatter), popularity.getFollowers()));
            }
            socialInsightsChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeTopArtist() {
        artistNameLabel.setText("Kana Arima"); 
        try {
            Image artistImg = new Image("file:img/KanaArimaProfile.png");
            if (artistImg.isError()) {
                artistImageView.setImage(new Image("file:img/DefaultArtist.png"));
            } else {
                artistImageView.setImage(artistImg);
            }
            crownIconView.setImage(new Image("file:img/IconCrown.png"));
            starIconView.setImage(new Image("file:img/IconPartyL.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            artistImageView.setImage(new Image("file:img/DefaultArtist.png"));
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

        try {
            int artistId = userDAO.getArtistIdByName(searchTerm);
            if (artistId != -1) {
                BorderPane mainContainer = (BorderPane) searchTextField.getScene().getRoot();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LaporanKinerjaView.fxml"));
                mainContainer.setCenter(loader.load());

                LaporanKinerjaController controller = loader.getController();
                controller.setArtist(artistId, searchTerm);
            } else {
                System.out.println("Artist not found: " + searchTerm);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToStaffView() {
        try {
            BorderPane mainLayout = (BorderPane) searchButton.getScene().getRoot();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DashboardView.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupMonthlyTotalChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Progress", 75),
                new PieChart.Data("Remaining", 25));
        monthlyTotalChart.setData(pieChartData);
        monthlyTotalChart.setStartAngle(90);
    }
}
