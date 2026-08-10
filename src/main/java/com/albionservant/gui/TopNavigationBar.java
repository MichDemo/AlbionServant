package com.albionservant.gui;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Top-level navigation whose appearance is entirely controlled by CSS.
 * Java owns only structure, callbacks and the selected state.
 */
public class TopNavigationBar extends VBox {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private Button activeButton;

    public TopNavigationBar() {
        getStyleClass().add("top-navigation");
        buildHeaderLabel();
        buildTabButtons();
    }

    private void buildHeaderLabel() {
        Label title = new Label("Ekran Główny");
        title.getStyleClass().add("top-navigation-title");
        getChildren().add(title);
    }

    private void buildTabButtons() {
        HBox tabsContainer = new HBox();
        tabsContainer.getStyleClass().add("top-navigation-tabs");
        tabsContainer.setAlignment(Pos.CENTER);

        Button craftBtn = createTabButton("CRAFT");
        Button refineBtn = createTabButton("REFINE");
        Button specsBtn = createTabButton("SPECS");
        Button optionsBtn = createTabButton("OPTIONS");

        HBox.setHgrow(craftBtn, Priority.ALWAYS);
        HBox.setHgrow(refineBtn, Priority.ALWAYS);
        HBox.setHgrow(specsBtn, Priority.ALWAYS);
        HBox.setHgrow(optionsBtn, Priority.ALWAYS);

        craftBtn.setOnAction(e -> setActiveButton(craftBtn));
        refineBtn.setOnAction(e -> setActiveButton(refineBtn));
        specsBtn.setOnAction(e -> setActiveButton(specsBtn));
        optionsBtn.setOnAction(e -> setActiveButton(optionsBtn));

        tabsContainer.getChildren().addAll(craftBtn, refineBtn, specsBtn, optionsBtn);
        getChildren().add(tabsContainer);
    }

    private Button createTabButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("top-navigation-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void setActiveButton(Button newActive) {
        if (activeButton != null) {
            activeButton.pseudoClassStateChanged(SELECTED, false);
        }
        newActive.pseudoClassStateChanged(SELECTED, true);
        activeButton = newActive;
    }

    public void setOnCraftClicked(Runnable action) {
        Button craftBtn = tabButton(0);
        craftBtn.setOnAction(e -> {
            setActiveButton(craftBtn);
            action.run();
        });
    }

    public void setOnRefineClicked(Runnable action) {
        Button refineBtn = tabButton(1);
        refineBtn.setOnAction(e -> {
            setActiveButton(refineBtn);
            action.run();
        });
    }

    public void setOnSpecsClicked(Runnable action) {
        Button specsBtn = tabButton(2);
        specsBtn.setOnAction(e -> {
            setActiveButton(specsBtn);
            action.run();
        });
    }

    public void setOnOtherTabClicked(Runnable action) {
        HBox tabs = tabsContainer();
        for (int index = 3; index < tabs.getChildren().size(); index++) {
            Button button = (Button) tabs.getChildren().get(index);
            button.setOnAction(e -> {
                setActiveButton(button);
                action.run();
            });
        }
    }

    private HBox tabsContainer() {
        return (HBox) getChildren().get(1);
    }

    private Button tabButton(int index) {
        return (Button) tabsContainer().getChildren().get(index);
    }
}
