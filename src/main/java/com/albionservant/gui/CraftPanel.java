package com.albionservant.gui;

import com.albionservant.data.CraftData;
import com.albionservant.elements.CraftCategory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class CraftPanel extends VBox {

    private final TextField searchField = new TextField();
    private final HBox mainView = new HBox(60);
    private final BorderPane splitView = new BorderPane();

    private final Deque<CraftCategory> path = new ArrayDeque<>();

    public CraftPanel() {
        setMaxWidth(1150);
        setAlignment(Pos.CENTER);

        setBackground(new Background(new BackgroundFill(Color.rgb(240, 240, 240), new CornerRadii(12), null)));
        setPadding(new Insets(25));
        setSpacing(15);

        buildSearchBar();
        buildMainCategories();

        getChildren().add(mainView);
    }

    private void buildSearchBar() {
        searchField.setPromptText("Search...");
        searchField.setPrefHeight(48);
        searchField.setStyle("-fx-background-radius: 10; -fx-font-size: 16px;");
        getChildren().add(searchField);
    }

    private void buildMainCategories() {
        mainView.setAlignment(Pos.CENTER);

        Button gearBtn   = createMainButton("Gear",   "⚒️");
        Button foodBtn   = createMainButton("Food",   "🍖");
        Button potionBtn = createMainButton("Potion", "🧪");

        gearBtn.setOnAction(e -> startGearFlow());

        mainView.getChildren().addAll(gearBtn, foodBtn, potionBtn);
    }

    private Button createMainButton(String name, String icon) {
        Button btn = new Button(icon + "\n\n" + name);
        btn.setPrefSize(200, 200);
        btn.setStyle("-fx-background-color: #e04e4e; -fx-text-fill: white; " +
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 20;");
        return btn;
    }

    private void startGearFlow() {
        path.clear();
        path.push(CraftData.getGearTree());

        getChildren().remove(mainView);
        getChildren().add(splitView);
        refreshSplitView();
    }

    private void refreshSplitView() {
        splitView.getChildren().clear();
        splitView.setMaxWidth(1100);

        // LEFT SIDE - navigation path (grayed items)
        VBox left = buildLeftPath();

        // RIGHT SIDE
        VBox right = new VBox(20);
        right.setPadding(new Insets(30));
        right.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        CraftCategory current = path.peek();

        Label header = new Label("Choose " + current.getName() + " type");
        header.setFont(Font.font("System", FontWeight.BOLD, 22));
        header.setTextFill(Color.rgb(60, 60, 60));
        right.getChildren().add(header);

        // === ONLY SHOW SUBCATEGORIES IF THERE ARE CHILDREN ===
        if (!current.getChildren().isEmpty()) {
            GridPane grid = buildOptionGrid(current.getChildren());
            right.getChildren().add(grid);
        }

        // Back arrow
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-font-size: 32px; -fx-background-color: transparent;");
        backBtn.setOnAction(e -> goBackOneLevel());

        HBox topRight = new HBox(backBtn);
        topRight.setAlignment(Pos.TOP_RIGHT);

        splitView.setTop(topRight);
        splitView.setLeft(left);
        splitView.setCenter(right);
    }

    private VBox buildLeftPath() {
        VBox left = new VBox(12);
        left.setPadding(new Insets(20));
        left.setPrefWidth(280);
        left.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15;");

        int index = 0;
        for (CraftCategory cat : path) {
            boolean isActive = (index == path.size() - 1);
            Button btn = createPathButton(cat.getIcon() + "  " + cat.getName(), isActive);
            int finalIndex = index;
            btn.setOnAction(e -> goBackToLevel(finalIndex));
            left.getChildren().add(btn);
            index++;
        }
        return left;
    }

    private Button createPathButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setStyle(active ?
                "-fx-background-color: #e04e4e; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;" :
                "-fx-background-color: #e0e0e0; -fx-text-fill: #555; -fx-font-size: 17px;");
        return btn;
    }

    private GridPane buildOptionGrid(List<CraftCategory> options) {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);

        int col = 0, row = 0;
        for (CraftCategory opt : options) {
            VBox box = createOptionBox(opt);
            box.setOnMouseClicked(e -> selectNextLevel(opt));
            grid.add(box, col++, row);
            if (col > 3) {
                col = 0;
                row++;
            }
        }
        return grid;
    }

    private VBox createOptionBox(CraftCategory cat) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(160, 160);
        box.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 15; -fx-padding: 15;");

        Label icon = new Label(cat.getIcon());
        icon.setFont(Font.font(52));

        Label name = new Label(cat.getName());
        name.setFont(Font.font("System", FontWeight.BOLD, 16));

        box.getChildren().addAll(icon, name);
        return box;
    }

    private void selectNextLevel(CraftCategory next) {
        path.push(next);
        refreshSplitView();
    }

    private void goBackOneLevel() {
        if (path.size() > 1) {
            path.pop();
            refreshSplitView();
        } else {
            // Back to main 3 buttons
            getChildren().remove(splitView);
            getChildren().add(mainView);
            path.clear();
        }
    }

    private void goBackToLevel(int level) {
        while (path.size() > level + 1) {
            path.pop();
        }
        refreshSplitView();
    }

    public void reset() {
        getChildren().remove(splitView);
        getChildren().add(mainView);
        path.clear();
    }
}