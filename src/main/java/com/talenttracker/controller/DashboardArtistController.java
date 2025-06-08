package com.talenttracker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DashboardArtistController {

    // --- Injected Header Controller ---
    @FXML private HeaderController headerComponentController;

    // --- UI Elements ---
    @FXML private ImageView profileAvatarView;
    @FXML private Label profileNameLabel;
    @FXML private ToggleGroup filterToggleGroup;
    @FXML private ToggleButton monthFilterButton;
    @FXML private ToggleButton allFilterButton;
    @FXML private Button reportButton;

    // Charts
    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private CategoryAxis topAlbumXAxis;
    @FXML private NumberAxis topAlbumYAxis;
    @FXML private LineChart<String, Number> socialMediaChart;
    @FXML private CategoryAxis socialMediaXAxis;
    @FXML private NumberAxis socialMediaYAxis;
    @FXML private PieChart sentimentChart;

    // Fans Responses Table
    @FXML private TableView<FanResponse> responsesTable;
    @FXML private TableColumn<FanResponse, String> numberCol;
    @FXML private TableColumn<FanResponse, String> sourceCol;
    @FXML private TableColumn<FanResponse, String> commentCol;
    @FXML private TableColumn<FanResponse, String> categoryCol;

    // Sentiment Legend
    @FXML private VBox legendPane;

    // Metric Icons
    @FXML private ImageView salesIcon;
    @FXML private ImageView albumsIcon;
    @FXML private ImageView visitorsIcon;

    // --- Data Series for Charts ---
    private XYChart.Series<String, Number> topAlbumSeries;
    private XYChart.Series<String, Number> socialMediaSeries;
    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
            "#FFB74D", "#FFA726", "#EC407A", "#42A5F5",
            "#AB47BC", "#81C784", "#4DB6AC", "#64B5F6", "#90A4AE"
    );

    public void initialize() {
        // Setup UI components
        initializeImageViews();
        setupCharts();
        setupResponsesTable();
        createSentimentLegend();
        setupButtonHandlers();
    }

    private void initializeImageViews() {
        try {
            profileAvatarView.setImage(new Image("file:img/KanaArimaProfile.png"));
            visitorsIcon.setImage(new Image("file:img/VisitorIcon.png"));
            salesIcon.setImage(new Image("file:img/MoneyIcon.png"));
            albumsIcon.setImage(new Image("file:img/BagIcon.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void setupCharts() {
        // --- Top Album Chart ---
        topAlbumSeries = new XYChart.Series<>();
        topAlbumSeries.getData().addAll(
                new XYChart.Data<>("Gnarly", 300), new XYChart.Data<>("Afnan", 150),
                new XYChart.Data<>("Swicy", 250), new XYChart.Data<>("Idol", 800),
                new XYChart.Data<>("Fun", 550), new XYChart.Data<>("No Way", 750),
                new XYChart.Data<>("Robloks", 850), new XYChart.Data<>("OMG", 200),
                new XYChart.Data<>("Love", 250), new XYChart.Data<>("Moreg", 100)
        );
        topAlbumChart.getData().add(topAlbumSeries);
        styleBarChartNodes();

        // --- Social Media Chart ---
        socialMediaSeries = new XYChart.Series<>();
        socialMediaSeries.getData().addAll(
                new XYChart.Data<>("W1", 1000), new XYChart.Data<>("W2", 1500),
                new XYChart.Data<>("W3", 2500), new XYChart.Data<>("W4", 4000)
        );
        socialMediaChart.getData().add(socialMediaSeries);

        // --- Sentiment Pie Chart ---
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Positive", 60),
                new PieChart.Data("Negative", 20),
                new PieChart.Data("Neutral", 20)
        );
        sentimentChart.setData(pieChartData);
        sentimentChart.setStartAngle(90); // Adjust start angle for aesthetics
    }

    private void setupResponsesTable() {
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("sentiment"));

        // Custom cell factory for the category column to display colored badges
        categoryCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-padding: 4 10; -fx-background-radius: 15; -fx-font-weight: bold; -fx-text-fill: white;");
                    switch (item.toLowerCase()) {
                        case "positive":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #8bc34a;");
                            break;
                        case "negative":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #f44336;");
                            break;
                        case "neutral":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #607d8b;");
                            break;
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        responsesTable.setItems(getFanResponses());
    }

    private void styleBarChartNodes() {
        // This needs to be called after data is added to the chart.
        topAlbumChart.getData().forEach(series -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> data = series.getData().get(i);
                if (data.getNode() != null) {
                    String color = barColors.get(i % barColors.size());
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                } else {
                    // Node might not be created immediately, add a listener.
                    final int index = i;
                    data.nodeProperty().addListener((ov, oldNode, newNode) -> {
                        if (newNode != null) {
                            String color = barColors.get(index % barColors.size());
                            newNode.setStyle("-fx-bar-fill: " + color + ";");
                        }
                    });
                }
            }
        });
    }

    private ObservableList<FanResponse> getFanResponses() {
        return FXCollections.observableArrayList(
                new FanResponse("01", "Instagram", "\"She's the queen\"", "Positive"),
                new FanResponse("02", "X", "\"Ugh, why is Kana even still relevant? She's so annoying now\"", "Negative"),
                new FanResponse("03", "Instagram", "\"I love how she connects with the audience. Such a natural talent\"", "Positive"),
                new FanResponse("04", "X", "\"Angel, Cute, OMG I cant even bear my happiness when I met here yesterday\"", "Positive"),
                new FanResponse("05", "Instagram", "\"She's alright I guess, nothing too special though\"", "Neutral")
        );
    }

    private void createSentimentLegend() {
        List<PieChart.Data> data = sentimentChart.getData();
        for (PieChart.Data entry : data) {
            HBox legendItem = new HBox(10);
            legendItem.setAlignment(Pos.CENTER_LEFT);

            Circle colorCircle = new Circle(6);
            Label label = new Label(entry.getName());
            Label percentage = new Label(String.format("%.0f%%", entry.getPieValue()));
            percentage.setStyle("-fx-font-weight: bold;");

            // Match color with pie chart
            String colorHex = "";
            switch (entry.getName().toLowerCase()) {
                case "positive":
                    colorHex = "#8bc34a";
                    break;
                case "negative":
                    colorHex = "#f44336";
                    break;
                case "neutral":
                    colorHex = "#607d8b";
                    break;
            }
            colorCircle.setStyle("-fx-fill: " + colorHex + ";");

            HBox labelBox = new HBox(5, colorCircle, label);
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            legendItem.getChildren().addAll(labelBox, spacer, percentage);
            legendPane.getChildren().add(legendItem);
        }
    }

    private void setupButtonHandlers() {
        reportButton.setOnAction(event -> {
            // Placeholder for report download logic
            System.out.println("Report Summary button clicked.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Summary");
            alert.setHeaderText(null);
            alert.setContentText("Downloading report summary...");
            alert.showAndWait();
        });

        filterToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                // Keep one selected
                if (oldValue != null) {
                    oldValue.setSelected(true);
                } else {
                    monthFilterButton.setSelected(true);
                }
            } else {
                System.out.println("Filter changed to: " + ((ToggleButton) newValue).getText());
                // Add data filtering logic here based on the selected toggle
            }
        });
    }

    // --- Data Model for TableView ---
    public static class FanResponse {
        private final String number;
        private final String source;
        private final String comment;
        private final String sentiment;

        public FanResponse(String number, String source, String comment, String sentiment) {
            this.number = number;
            this.source = source;
            this.comment = comment;
            this.sentiment = sentiment;
        }

        public String getNumber() {
            return number;
        }

        public String getSource() {
            return source;
        }

        public String getComment() {
            return comment;
        }

        public String getSentiment() {
            return sentiment;
        }
    }
}
