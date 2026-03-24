package com.albionservant;

import com.albionservant.data.InvestmentData;
import com.albionservant.gui.CraftPanel;
import com.albionservant.gui.TopNavigationBar;
import com.albionservant.gui.HotInvestmentsPanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
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

        // Create all panels
        TopNavigationBar topBar = new TopNavigationBar();
        HotInvestmentsPanel hotPanel = new HotInvestmentsPanel();
        CraftPanel craftPanel = new CraftPanel();

        // Sample data for Hot Investments
        InvestmentData item1 = new InvestmentData(
                "Master's Battleaxe", 6, "Martlock → Blackmarket", 450249,
                2, 32, 239421, 210250, 87.8);

        InvestmentData item2 = new InvestmentData(
                "Grandmaster's Bow", 7, "Thetford → Blackmarket", 895000,
                1, 18, 620000, 275000, 44.3);

        InvestmentData item3 = new InvestmentData(
                "Expert's Plate Helmet", 5, "Lymhurst → Blackmarket", 124500,
                3, 45, 89000, 35500, 39.9);

        InvestmentData item4 = new InvestmentData(
                "Master's Spear", 6, "Bridgewatch → Blackmarket", 672300,
                0, 12, 480000, 192300, 40.1);

        hotPanel.setInvestments(List.of(item1, item2, item3, item4));

        // Initially show Hot Investments
        root.getChildren().addAll(topBar, hotPanel);

        // Apply dark background
        root.setBackground(new Background(new BackgroundFill(AppConfig.BACKGROUND_MAIN, null, null)));

        // === TAB SWITCHING LOGIC ===
        topBar.setOnCraftClicked(() -> {
            root.getChildren().remove(1);        // Remove current content panel
            root.getChildren().add(craftPanel);  // Show Craft panel
        });

        topBar.setOnOtherTabClicked(() -> {
            root.getChildren().remove(1);        // Remove current content panel
            root.getChildren().add(hotPanel);    // Show Hot Investments again
        });

        Scene scene = new Scene(root, 1280, 960);
        stage.setTitle("AlbionServant App");
        stage.setScene(scene);
        stage.show();
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