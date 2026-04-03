package com.albionservant.gui;

import com.albionservant.data.InvestmentData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class HotInvestmentsPanel extends VBox {

    private final VBox itemsContainer = new VBox(18);
    private final HBox paginationBox = new HBox(15);
    private Button prevButton;
    private Button nextButton;
    private Label pageLabel;
    private int currentPage = 0;
    private List<InvestmentData> allInvestments = List.of();
    private static final int ITEMS_PER_PAGE = 3;

    private static final double MIN_ITEMS_HEIGHT = 420;   // height for exactly 3 rows

    public HotInvestmentsPanel() {
        setBackground(new Background(new BackgroundFill(Color.rgb(224, 78, 78), new CornerRadii(12), null)));
        setPadding(new Insets(25, 30, 25, 30));
        setSpacing(20);
        setAlignment(Pos.TOP_CENTER);

        buildHeader();
        buildItemsContainer();
        buildPaginationControls();

        getChildren().addAll(itemsContainer, paginationBox);
    }

    private void buildHeader() {
        Label title = new Label("🔥 Hot Investments");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Best crafting / flipping opportunities right now");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.rgb(255, 255, 255, 0.85));

        VBox headerBox = new VBox(5, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        getChildren().add(0, headerBox);
    }

    private void buildItemsContainer() {
        itemsContainer.setAlignment(Pos.TOP_CENTER);
        itemsContainer.setMinHeight(MIN_ITEMS_HEIGHT);   // ← THIS PREVENTS SHRINKING
    }

    private void buildPaginationControls() {
        prevButton = new Button("← Previous");
        prevButton.setStyle("""
            -fx-background-color: #4ade80;
            -fx-text-fill: #111;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            """);
        prevButton.setOnAction(e -> previousPage());

        nextButton = new Button("Next →");
        nextButton.setStyle("""
            -fx-background-color: #4ade80;
            -fx-text-fill: #111;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            """);
        nextButton.setOnAction(e -> nextPage());

        pageLabel = new Label("1 / 1");
        pageLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        pageLabel.setTextFill(Color.WHITE);

        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(20);
        paginationBox.getChildren().addAll(prevButton, pageLabel, nextButton);
    }

    public void setInvestments(List<InvestmentData> investments) {
        this.allInvestments = investments != null ? investments : List.of();
        currentPage = 0;
        updateDisplay();
        updateButtonStates();
    }

    private void updateDisplay() {
        itemsContainer.getChildren().clear();

        if (allInvestments.isEmpty()) {
            Label empty = new Label("No investments loaded yet.");
            empty.setTextFill(Color.WHITE);
            empty.setFont(Font.font("System", FontWeight.BOLD, 18));
            itemsContainer.getChildren().add(empty);
            return;
        }

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allInvestments.size());

        for (int i = start; i < end; i++) {
            HBox row = createInvestmentRow(allInvestments.get(i));
            itemsContainer.getChildren().add(row);
        }

        // If we have fewer items, the minHeight keeps everything stable
    }

    private HBox createInvestmentRow(InvestmentData data) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle("""
            -fx-background-color: rgba(255,255,255,0.12);
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: rgba(255,255,255,0.2);
            """);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(data.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 17));
        nameLabel.setTextFill(Color.WHITE);

        Label tierLabel = new Label("T" + data.getTier());
        tierLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        tierLabel.setTextFill(Color.rgb(74, 222, 128));
        nameBox.getChildren().addAll(nameLabel, tierLabel);

        Label routeLabel = new Label(data.getLocation());
        routeLabel.setFont(Font.font("System", FontWeight.NORMAL, 15));
        routeLabel.setTextFill(Color.rgb(255, 255, 255, 0.9));

        Label buyLabel = new Label(String.format("%,d", data.getPrice()));
        buyLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        buyLabel.setTextFill(Color.WHITE);

        Label profitLabel = new Label("+" + String.format("%,d", data.getProfit()));
        profitLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        profitLabel.setTextFill(Color.rgb(74, 222, 128));

        Label roiLabel = new Label(String.format("%.1f%%", data.getRoi()));
        roiLabel.setFont(Font.font("System", FontWeight.BOLD, 17));
        roiLabel.setTextFill(Color.rgb(250, 204, 21));

        HBox.setHgrow(nameBox, Priority.ALWAYS);
        HBox.setHgrow(routeLabel, Priority.ALWAYS);

        row.getChildren().addAll(nameBox, routeLabel, buyLabel, profitLabel, roiLabel);
        return row;
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateDisplay();
            updateButtonStates();
        }
    }

    private void nextPage() {
        int totalPages = (int) Math.ceil((double) allInvestments.size() / ITEMS_PER_PAGE);
        if (currentPage + 1 < totalPages) {
            currentPage++;
            updateDisplay();
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        int totalPages = (int) Math.ceil((double) allInvestments.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
        pageLabel.setText((currentPage + 1) + " / " + totalPages);
    }
}