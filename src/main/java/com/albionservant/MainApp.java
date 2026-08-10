package com.albionservant;

import com.albionservant.data.InvestmentData;
import com.albionservant.gui.CraftPanel;
import com.albionservant.gui.HotInvestmentsPanel;
import com.albionservant.gui.RefinePanel;
import com.albionservant.gui.SpecsPanel;
import com.albionservant.gui.TopNavigationBar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

public class MainApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(AppConfig.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.setSpacing(0);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(screenBounds.getWidth() * 0.9, 1400);
        double height = screenBounds.getHeight() * 0.95;

        TopNavigationBar topBar = new TopNavigationBar();
        VBox.setVgrow(topBar, Priority.NEVER);
        HotInvestmentsPanel hotPanel = new HotInvestmentsPanel();
        CraftPanel craftPanel = new CraftPanel();
        RefinePanel refinePanel = new RefinePanel();
        SpecsPanel specsPanel = new SpecsPanel();
        // Sample data
        InvestmentData item1 = new InvestmentData(
                "Master's Battleaxe",
                6,
                "Martlock → Blackmarket",
                450249,
                2,
                32,
                239421,
                210250,
                87.8
        );

        InvestmentData item2 = new InvestmentData(
                "Grandmaster's Bow",
                7,
                "Thetford → Blackmarket",
                895000,
                1,
                18,
                620000,
                275000,
                44.3
        );

        InvestmentData item3 = new InvestmentData(
                "Expert's Plate Helmet",
                5,
                "Lymhurst → Blackmarket",
                124500,
                3,
                45,
                89000,
                35500,
                39.9
        );

        InvestmentData item4 = new InvestmentData(
                "Master's Spear",
                6,
                "Bridgewatch → Blackmarket",
                672300,
                0,
                12,
                480000,
                192300,
                40.1
        );

        hotPanel.setInvestments(List.of(item1, item2, item3, item4));

        VBox contentArea = new VBox(hotPanel);
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setFillWidth(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        root.getChildren().addAll(topBar, contentArea);
        root.getStyleClass().add("app-root");

        // ── Detail mode listeners — hide top bar when in leaf panels ─────────
        craftPanel.setOnDetailModeListener(isDetail -> {
            topBar.setVisible(true);
            topBar.setManaged(true);
            VBox.setVgrow(contentArea, Priority.ALWAYS);
        });

        craftPanel.setSpecsPanel(specsPanel);

        refinePanel.setOnDetailModeListener(isDetail -> {
            topBar.setVisible(true);
            topBar.setManaged(true);
            VBox.setVgrow(contentArea, Priority.ALWAYS);
        });

        // ── Tab routing ──────────────────────────────────────────────────────
        topBar.setOnCraftClicked(() -> {
            contentArea.getChildren().set(0, craftPanel);
            VBox.setVgrow(contentArea, Priority.ALWAYS);
            topBar.setVisible(true);
        });

        topBar.setOnRefineClicked(() -> {
            contentArea.getChildren().set(0, refinePanel);
            VBox.setVgrow(contentArea, Priority.ALWAYS);
            topBar.setVisible(true);
        });

        topBar.setOnSpecsClicked(() -> {
            contentArea.getChildren().set(0, specsPanel);
            VBox.setVgrow(contentArea, Priority.ALWAYS);
            topBar.setVisible(true);
        });

        topBar.setOnOtherTabClicked(() -> {
            contentArea.getChildren().set(0, hotPanel);
            VBox.setVgrow(contentArea, Priority.ALWAYS);
            topBar.setVisible(true);
        });


        Scene scene = new Scene(root, width, height);


        // ALBIONSERVANT_CSS_ARCHITECTURE_V1
        root.getStyleClass().add("albion-minimal-root");
        var applicationStylesheet = MainApp.class.getResource(AppConfig.MAIN_STYLESHEET);
        if (applicationStylesheet == null) {
            throw new IllegalStateException(
                    "Missing application stylesheet: " + AppConfig.MAIN_STYLESHEET
            );
        }
        scene.getStylesheets().add(applicationStylesheet.toExternalForm());
        /*
         * Fix: JavaFX potrafi automatycznie przewinąć ScrollPane, kiedy ComboBox
         * dostaje focus albo otwiera popup. W craftingu powodowało to "skakanie"
         * i przesuwanie interfejsu po klikaniu list rozwijanych.
         *
         * Ten stabilizator zapamiętuje pozycję najbliższego ScrollPane przed
         * interakcją z ComboBoxem i przywraca ją po tym, jak JavaFX przeliczy layout.
         */
        installComboBoxScrollStabilizer(scene);

        stage.setTitle(AppConfig.APP_TITLE);
        stage.setScene(scene);

        // ALBIONSERVANT_HOT_INVESTMENTS_STARTUP_THEME_FIX_V1
        // Resolve author CSS before the first visible pulse, avoiding startup colour flashes.
        root.applyCss();
        root.layout();
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setResizable(true);
        stage.show();
        stage.setMaximized(true);
    }

    /**
     * Prevents ScrollPane from jumping when ComboBox/ChoiceBox-like controls
     * inside it receive focus or open their dropdown popup.
     */
    private void installComboBoxScrollStabilizer(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            Node target = event.getPickResult().getIntersectedNode();
            ComboBoxBase<?> comboBox = findParentOfType(target, ComboBoxBase.class);

            if (comboBox != null) {
                preserveNearestScrollPanePosition(comboBox);
            }
        });

        scene.focusOwnerProperty().addListener((observable, oldFocusOwner, newFocusOwner) -> {
            ComboBoxBase<?> comboBox = findParentOfType(newFocusOwner, ComboBoxBase.class);

            if (comboBox != null) {
                preserveNearestScrollPanePosition(comboBox);
            }
        });
    }

    private void preserveNearestScrollPanePosition(Node node) {
        ScrollPane scrollPane = findParentOfType(node, ScrollPane.class);

        if (scrollPane == null) {
            return;
        }

        double hValue = scrollPane.getHvalue();
        double vValue = scrollPane.getVvalue();

        Platform.runLater(() -> {
            scrollPane.setHvalue(hValue);
            scrollPane.setVvalue(vValue);

            Platform.runLater(() -> {
                scrollPane.setHvalue(hValue);
                scrollPane.setVvalue(vValue);
            });
        });
    }

    private <T> T findParentOfType(Node node, Class<T> type) {
        Node current = node;

        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }

            current = current.getParent();
        }

        return null;
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }

        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}