/*
 * Copyright 2013 John Smith
 *
 * This file is part of Willow.
 *
 * Willow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Willow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Willow. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact details: http://jewelsea.wordpress.com
 */

package org.jewelsea.willow;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NavTools {
    public static Pane createNavPane(final Willow chrome) {
        // create a back button.
        final Button backButton = new Button();
        backButton.setId("backButton"); // todo I don't like this id set just for lookup - reference would be better
        backButton.setTooltip(new Tooltip("Go back or right click for history"));
        final ImageView backGraphic = new ImageView(Util.getImage("239706184.png"));
        final ColorAdjust backColorAdjust = new ColorAdjust();
        backColorAdjust.setBrightness(-0.1);
        backColorAdjust.setContrast(-0.1);
        backGraphic.setEffect(backColorAdjust);
        backButton.setGraphic(backGraphic);
        backGraphic.setPreserveRatio(true);
        backGraphic.setFitHeight(32);
        backButton.onActionProperty().set(actionEvent -> {
            if (chrome.getBrowser().getHistory().canNavBack()) {
                chrome.getBrowser().navTo(chrome.getBrowser().getHistory().requestNavBack());
            }
        });
        backButton.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                chrome.getBrowser().getHistory().showMenu(backButton);
            }
        });

        // create a forward button.
        final Button forwardButton = new Button();
        forwardButton.setId("forwardButton"); // todo I don't like this id set just for lookup - reference would be better
        forwardButton.setTranslateX(-2);
        final ImageView forwardGraphic = new ImageView(Util.getImage("1813406178.png"));
        final ColorAdjust forwardColorAdjust = new ColorAdjust();
        forwardColorAdjust.setBrightness(-0.1);
        forwardColorAdjust.setContrast(-0.1);
        forwardGraphic.setEffect(forwardColorAdjust);
        forwardGraphic.setPreserveRatio(true);
        forwardGraphic.setFitHeight(20);
        forwardButton.setGraphic(forwardGraphic);
        forwardButton.setTooltip(new Tooltip("Go forward"));
        forwardButton.onActionProperty().set(actionEvent -> {
            if (chrome.getBrowser().getHistory().canNavForward()) {
                chrome.getBrowser().navTo(chrome.getBrowser().getHistory().requestNavForward());
            }
        });
        forwardButton.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                chrome.getBrowser().getHistory().showMenu(backButton);
            }
        });

        // create a navigate button.
        final Button navButton = new Button();
        navButton.setTooltip(new Tooltip("Go to or rejuvenate the location"));
        final ImageView navGraphic = new ImageView(Util.getImage("Forward Arrow.png"));
        final ColorAdjust navColorAdjust = new ColorAdjust();
        navColorAdjust.setContrast(-0.7);
        navGraphic.setEffect(navColorAdjust);
        navGraphic.setPreserveRatio(true);
        navGraphic.setFitHeight(14);
        navButton.setGraphic(navGraphic);
        navButton.onActionProperty().set(actionEvent ->
                chrome.getBrowser().navTo(chrome.getBrowser().getLocField().getText())
        );

        // create a button to hide and show the sidebar.
        final Button sidebarButton = new Button();
        sidebarButton.setId("sidebarButton");
        final ImageView sidebarGraphic = new ImageView(Util.getImage("Down Arrow.png"));
        final ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(-0.7);
        sidebarGraphic.setFitHeight(10);
        sidebarGraphic.setPreserveRatio(true);
        sidebarButton.setScaleX(0.8);
        sidebarButton.setScaleY(0.8);
        sidebarGraphic.setEffect(colorAdjust);
        sidebarButton.setGraphic(sidebarGraphic);
        sidebarButton.setTooltip(new Tooltip("Play sidebar hide and seek"));
        sidebarButton.setStyle("-fx-font-weight: bold;");
        final DoubleProperty startWidth = new SimpleDoubleProperty();

        // todo java 8 has a weird background issue on resize - file bug
        // hide sidebar.
        final Animation hideSidebar = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            protected void interpolate(double frac) {
                final double curWidth = startWidth.get() * (1.0 - frac);
                chrome.getSidebarDisplay().setPrefWidth(curWidth);   // todo resize a spacing underlay to allow the scene to adjust.
                chrome.getSidebarDisplay().setTranslateX(-startWidth.get() + curWidth);
            }
        };
        hideSidebar.onFinishedProperty().set(actionEvent -> chrome.getSidebarDisplay().setVisible(false));

        // show sidebar.
        final Animation showSidebar = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            protected void interpolate(double frac) {
                final double curWidth = startWidth.get() * frac;
                chrome.getSidebarDisplay().setPrefWidth(curWidth);
                chrome.getSidebarDisplay().setTranslateX(-startWidth.get() + curWidth);
            }
        };

        sidebarButton.setOnAction(actionEvent -> {
            chrome.getSidebarDisplay().setMinWidth(Control.USE_PREF_SIZE);

            if (showSidebar.statusProperty().get().equals(Animation.Status.STOPPED) && hideSidebar.statusProperty().get().equals(Animation.Status.STOPPED)) {
                if (chrome.getSidebarDisplay().isVisible()) {
                    startWidth.set(chrome.getSidebarDisplay().getWidth());
                    hideSidebar.play();
                } else {
                    chrome.getSidebarDisplay().setVisible(true);
                    showSidebar.play();
                }
            }
        });

        final Button fullscreenButton = new Button();
        fullscreenButton.setTooltip(new Tooltip("Go huge"));
        final ImageView fullscreenGraphic = new ImageView(Util.getImage("1325834738_gtk-fullscreen.png"));
        fullscreenGraphic.setEffect(colorAdjust);
        fullscreenGraphic.setPreserveRatio(true);
        fullscreenGraphic.setFitHeight(14);
        fullscreenButton.setGraphic(fullscreenGraphic);
        fullscreenButton.setOnAction(actionEvent -> {
            final Stage stage = (Stage) fullscreenButton.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        });

        // align all of the navigation widgets in a horizontal toolbar.
        final HBox navPane = new HBox();
        navPane.setAlignment(Pos.CENTER);
        navPane.getStyleClass().add("toolbar");
        navPane.setSpacing(5);
        navPane.getChildren().addAll(sidebarButton, backButton, forwardButton, chrome.getChromeLocField(), chrome.getTabManager().getTabPane(), chrome.getTabManager().getNewTabButton(), navButton, fullscreenButton);
        navPane.setFillHeight(false);
        Platform.runLater(() -> navPane.setMinHeight(navPane.getHeight()));

        final InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.ANTIQUEWHITE);
        navPane.setEffect(innerShadow);

        return navPane;
    }
}

// todo while a page is loading we might want to switch the favicon to a loading animation...
// todo may also want to add the following ot the navbar... /* createLoadIndicator(), */ /* browser.favicon, */
// todo add ability to save pdfs and other docs.