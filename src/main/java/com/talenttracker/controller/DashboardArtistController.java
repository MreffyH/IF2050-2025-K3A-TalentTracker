package com.talenttracker.controller;

import com.talenttracker.dao.AlbumDAO;
import com.talenttracker.dao.FansDAO;
import com.talenttracker.dao.SocialMediaDAO;
import com.talenttracker.dao.StatsDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Album;
import com.talenttracker.model.FanResponse;
import com.talenttracker.model.Popularity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardArtistController {
    private int artistId;
    private String artistFullName;

    @FXML private Button reportButton;
    @FXML private ImageView profileAvatarView;
    @FXML private ImageView downloadIcon;
    @FXML private ImageView salesIcon;
    @FXML private ImageView albumsIcon;
    @FXML private ImageView visitorsIcon;
    @FXML private Label profileNameLabel;
    @FXML private ToggleButton monthFilterButton;
    @FXML private ToggleButton allFilterButton;
    @FXML private ToggleGroup filterToggleGroup;

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

    private final AlbumDAO albumDAO = new AlbumDAO();
    private final SocialMediaDAO socialMediaDAO = new SocialMediaDAO();
    private final StatsDAO statsDAO = new StatsDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FansDAO fansDAO = new FansDAO();
    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
            "#FEB95A", "#E89B2F", "#223055", "#FFB7CA", "#28AEF3",
            "#D7598B", "#A9DFD8", "#538EA5"
    );

    public void setArtistId(int artistId) {
        this.artistId = artistId;
        try {
            this.artistFullName = userDAO.getArtistNameById(artistId);

            if (this.artistFullName != null) {
                profileNameLabel.setText(this.artistFullName);
                String profileImageName = this.artistFullName.replaceAll("\\s+", "") + "Profile.png";
                Image profileImage = new Image("file:img/" + profileImageName);

                if (profileImage.isError()) {
                    System.err.println("Could not load profile image: " + profileImageName + ". Using default.");
                    profileAvatarView.setImage(new Image("file:img/DefaultArtist.png"));
                } else {
                    profileAvatarView.setImage(profileImage);
                }
            } else {
                profileNameLabel.setText("Unknown Artist");
                profileAvatarView.setImage(new Image("file:img/DefaultArtist.png"));
            }

            loadDashboardData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        filterToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                loadDashboardData();
            }
        });
        monthFilterButton.setSelected(true);
        try {
            downloadIcon.setImage(new Image("file:img/DownloadIcon.png"));
            salesIcon.setImage(new Image("file:img/MoneyIcon.png"));
            albumsIcon.setImage(new Image("file:img/BagIcon.png"));
            visitorsIcon.setImage(new Image("file:img/VisitorIcon.png"));
        } catch (Exception e) {
            System.err.println("Could not load icons.");
        }
        reportButton.setOnAction(event -> handleReportButton());
    }

    @FXML
    private void handleReportButton() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PDF", "PDF", "CSV");
        dialog.setTitle("Export Report");
        dialog.setHeaderText("Choose the format for your report.");
        dialog.setContentText("Format:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(format -> {
            if ("PDF".equals(format)) {
                exportAsPdf();
            } else if ("CSV".equals(format)) {
                exportAsCsv();
            }
        });
    }

    private void exportAsPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.setInitialFileName(artistFullName.replaceAll("\\s+", "_") + "_Performance_Report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(reportButton.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("Performance Report for " + artistFullName);
                    contentStream.endText();
                    addPdfContent(contentStream);
                }
                document.save(file);
                showAlert("Success", "PDF report saved successfully.");
            } catch (IOException | SQLException e) {
                showAlert("Error", "Failed to save PDF report: " + e.getMessage());
            }
        }
    }

    private void addPdfContent(PDPageContentStream contentStream) throws IOException, SQLException {
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        float y = 700;

        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Total Visitors: " + visitorsLabel.getText());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Total Sales: " + totalSalesLabel.getText());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Albums Sold: " + albumsSoldLabel.getText());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Total Followers: " + socialTotalFollowersLabel.getText());
        contentStream.endText();
        y -= 100;

        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Top Albums:");
        contentStream.endText();
        y -= 20;
        boolean monthFilter = monthFilterButton.isSelected();
        List<Album> albums = albumDAO.getArtistAlbums(artistId, monthFilter);
        for (Album album : albums) {
            contentStream.beginText();
            contentStream.newLineAtOffset(60, y);
            contentStream.showText("- " + album.getName() + ": " + album.getSales() + " sold");
            contentStream.endText();
            y -= 20;
        }
    }

    private void exportAsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.setInitialFileName(artistFullName.replaceAll("\\s+", "_") + "_Performance_Report.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(reportButton.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Metric,Value\n");
                writer.append("Total Visitors,").append(visitorsLabel.getText()).append("\n");
                writer.append("Total Sales,").append(totalSalesLabel.getText()).append("\n");
                writer.append("Albums Sold,").append(albumsSoldLabel.getText()).append("\n");
                writer.append("Total Followers,").append(socialTotalFollowersLabel.getText()).append("\n\n");

                writer.append("Album,Sales\n");
                boolean monthFilter = monthFilterButton.isSelected();
                List<Album> albums = albumDAO.getArtistAlbums(artistId, monthFilter);
                for (Album album : albums) {
                    writer.append(album.getName()).append(",").append(String.valueOf(album.getSales())).append("\n");
                }
                writer.append("\n");

                writer.append("Platform,Date,Followers\n");
                List<Popularity> socialData = socialMediaDAO.getSocialMediaData(artistId);
                for (Popularity p : socialData) {
                    writer.append(p.getPlatform()).append(",").append(p.getDate().toString()).append(",").append(String.valueOf(p.getFollowers())).append("\n");
                }

                showAlert("Success", "CSV report saved successfully.");
            } catch (IOException | SQLException e) {
                showAlert("Error", "Failed to save CSV report: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void loadDashboardData() {
        boolean monthFilter = monthFilterButton.isSelected();
        loadTopAlbumChart(monthFilter);
        loadSocialMediaChart(monthFilter);
        loadFansResponses(monthFilter);
        loadDashboardMetrics(monthFilter);
        loadSocialPopularityStats(monthFilter);
    }

    private void loadTopAlbumChart(boolean monthFilter) {
        topAlbumChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try {
            List<Album> albums = albumDAO.getArtistAlbums(artistId, monthFilter);
            for (Album album : albums) {
                series.getData().add(new XYChart.Data<>(album.getName(), album.getSales()));
            }
            topAlbumChart.getData().add(series);
            topAlbumChart.applyCss(); // Ensure nodes are created
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

    private void loadSocialMediaChart(boolean monthFilter) {
        socialMediaChart.getData().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        try {
            List<Popularity> popularityData = socialMediaDAO.getSocialMediaData(artistId);
            Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();

            for (Popularity p : popularityData) {
                seriesMap.computeIfAbsent(p.getPlatform(), k -> {
                    XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                    newSeries.setName(k);
                    socialMediaChart.getData().add(newSeries);
                    return newSeries;
                }).getData().add(new XYChart.Data<>(p.getDate().format(formatter), p.getFollowers()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFansResponses(boolean monthFilter) {
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        try {
            List<FanResponse> responses = fansDAO.getFanResponses(artistId, monthFilter);
            responsesTable.setItems(FXCollections.observableArrayList(responses));

            long positive = responses.stream().filter(r -> "Positive".equalsIgnoreCase(r.getCategory())).count();
            long negative = responses.stream().filter(r -> "Negative".equalsIgnoreCase(r.getCategory())).count();
            long neutral = responses.stream().filter(r -> "Neutral".equalsIgnoreCase(r.getCategory())).count();
            updateSentimentChart((int)positive, (int)negative, (int)neutral);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSentimentChart(int positive, int negative, int neutral) {
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Positive", positive),
                        new PieChart.Data("Negative", negative),
                        new PieChart.Data("Neutral", neutral));

        sentimentChart.setData(pieChartData);
        legendPane.getChildren().clear();

        final Map<String, Color> sentimentColors = new HashMap<>();
        sentimentColors.put("Positive", Color.rgb(46, 204, 113)); // Green
        sentimentColors.put("Negative", Color.rgb(231, 76, 60));   // Red
        sentimentColors.put("Neutral", Color.rgb(189, 195, 199));  // Gray

        int i = 0;
        for (PieChart.Data data : pieChartData) {
            Color color = sentimentColors.get(data.getName());
            data.getNode().setStyle("-fx-pie-color: " + toRgbString(color) + ";");

            HBox legendEntry = new HBox(5);
            Circle circle = new Circle(5, color);
            legendEntry.getChildren().addAll(circle, new Label(data.getName()));
            legendPane.getChildren().add(legendEntry);
            i++;
        }
    }

    private String toRgbString(Color c) {
        return String.format("rgb(%d, %d, %d)",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    private void loadDashboardMetrics(boolean monthFilter) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        try {
            if (monthFilter) {
                LocalDate today = LocalDate.now();
                double todaySales = statsDAO.getSalesForDate(today, artistId);
                double todayAlbums = statsDAO.getAlbumsSoldForDate(today, artistId);
                double todayVisitors = statsDAO.getVisitorsForDate(today, artistId);

                totalSalesLabel.setText(currencyFormat.format(todaySales));
                albumsSoldLabel.setText(String.format("%,.0f", todayAlbums));
                visitorsLabel.setText(String.format("%,.0f", todayVisitors));

                LocalDate yesterday = today.minusDays(1);
                double yesterdaySales = statsDAO.getSalesForDate(yesterday, artistId);
                double yesterdayAlbums = statsDAO.getAlbumsSoldForDate(yesterday, artistId);
                double yesterdayVisitors = statsDAO.getVisitorsForDate(yesterday, artistId);

                updateChangeLabel(totalSalesChangeLabel, todaySales, yesterdaySales);
                updateChangeLabel(albumsSoldChangeLabel, todayAlbums, yesterdayAlbums);
                updateChangeLabel(visitorsChangeLabel, todayVisitors, yesterdayVisitors);
            } else {
                double totalSales = statsDAO.getTotalSalesForArtist(artistId);
                double totalAlbums = statsDAO.getTotalAlbumsSoldForArtist(artistId);
                double totalVisitors = statsDAO.getTotalVisitorsForArtist(artistId);

                totalSalesLabel.setText(currencyFormat.format(totalSales));
                albumsSoldLabel.setText(String.format("%,.0f", totalAlbums));
                visitorsLabel.setText(String.format("%,.0f", totalVisitors));

                totalSalesChangeLabel.setText("");
                albumsSoldChangeLabel.setText("");
                visitorsChangeLabel.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateChangeLabel(Label changeLabel, double currentValue, double previousValue) {
        if (previousValue == 0) {
            changeLabel.setText(currentValue > 0 ? "+100%" : "N/A");
            return;
        }
        double change = ((currentValue - previousValue) / previousValue) * 100;
        changeLabel.setText(String.format("%+.0f%%", change));
    }

    private void loadSocialPopularityStats(boolean monthFilter) {
        try {
            Map<String, Double> followerStats = socialMediaDAO.getMostRecentFollowerStats(artistId);
            double currentFollowers = followerStats.get("current");
            double previousFollowers = followerStats.get("previous");

            socialTotalFollowersLabel.setText(String.format("%,.0f", currentFollowers));
            updateChangeLabel(socialPopularityChangeLabel, currentFollowers, previousFollowers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
