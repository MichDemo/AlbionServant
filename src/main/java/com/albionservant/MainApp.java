package com.albionservant;

import com.albionservant.data.InvestmentData;
import com.albionservant.gui.CraftPanel;
import com.albionservant.gui.HotInvestmentsPanel;
import com.albionservant.gui.TopNavigationBar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
        double height = Math.min(screenBounds.getHeight() * 0.9, 920);

        TopNavigationBar topBar = new TopNavigationBar();
        HotInvestmentsPanel hotPanel = new HotInvestmentsPanel();
        CraftPanel craftPanel = new CraftPanel();

        // Sample data
        InvestmentData item1 = new InvestmentData("Master's Battleaxe", 6, "Martlock → Blackmarket", 450249, 2, 32, 239421, 210250, 87.8);
        InvestmentData item2 = new InvestmentData("Grandmaster's Bow", 7, "Thetford → Blackmarket", 895000, 1, 18, 620000, 275000, 44.3);
        InvestmentData item3 = new InvestmentData("Expert's Plate Helmet", 5, "Lymhurst → Blackmarket", 124500, 3, 45, 89000, 35500, 39.9);
        InvestmentData item4 = new InvestmentData("Master's Spear", 6, "Bridgewatch → Blackmarket", 672300, 0, 12, 480000, 192300, 40.1);
        hotPanel.setInvestments(List.of(item1, item2, item3, item4));

        // contentArea does NOT fill height — panel stays its natural size, top-aligned
        VBox contentArea = new VBox(hotPanel);
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setFillWidth(true);
        // Do NOT setVgrow here — that was stretching hotPanel to fill the window
        VBox.setVgrow(contentArea, Priority.NEVER);

        root.getChildren().addAll(topBar, contentArea);
        root.setBackground(new Background(new BackgroundFill(AppConfig.BACKGROUND_MAIN, null, null)));

        craftPanel.setOnDetailModeListener(isDetail -> topBar.setVisible(!isDetail));

        topBar.setOnCraftClicked(() -> {
            contentArea.getChildren().set(0, craftPanel);
            // CraftPanel needs to grow to fill available space
            VBox.setVgrow(contentArea, Priority.ALWAYS);
            contentArea.setAlignment(Pos.TOP_CENTER);
            topBar.setVisible(true);
        });

        topBar.setOnOtherTabClicked(() -> {
            contentArea.getChildren().set(0, hotPanel);
            // HotPanel should NOT grow — stays natural height
            VBox.setVgrow(contentArea, Priority.NEVER);
            topBar.setVisible(true);
        });

        Scene scene = new Scene(root, width, height);
        stage.setTitle("AlbionServant");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) context.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}