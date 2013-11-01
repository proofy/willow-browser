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

package org.jewelsea.willow.sidebar;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.Willow;
import org.jewelsea.willow.util.Util;

/**
 * Sidebar panel for showing demos
 */
public class NavigationPanel extends TitledPane {
    final ContextMenu canvasMenu = new ContextMenu();

    static final String[] defaultBookmarks = {
            "http://fxexperience.com/",
            "http://jewelsea.wordpress.com/",
            "http://docs.oracle.com/javafx/",
            "http://docs.oracle.com/javafx/2/api/index.html",
            "https://forums.oracle.com/forums/forum.jspa?forumID=1385&start=0"
    };

    public NavigationPanel(final Willow chrome) {
        // create a home button to navigate home.
        final Button homeButton = Util.createIconButton(
                "Home",
                "Fairytale_folder_home.png",
                "Click to go home or drag the location here to change house",
                actionEvent -> chrome.getBrowser().navTo(chrome.homeLocationProperty.get())
        );
        homeButton.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        homeButton.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                chrome.homeLocationProperty.set(db.getString());
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });

        // create a history button to show the history.
        final Button historyButton = Util.createIconButton(
                "History",
                "History.png",
                "Where did you go?",
                null
        );
        historyButton.setOnAction(e ->
                chrome.getBrowser().getHistory().showMenu(historyButton)
        );

        // create a bookmarksButton.
        final ContextMenu bookmarksMenu = new ContextMenu();
        final Button bookmarksButton = Util.createIconButton(
                "Bookmarks",
                "1714696718.png",
                "Drag a location here to remember it and click to recall your remembrance",
                null
        );
        bookmarksButton.setOnAction(actionEvent ->
                bookmarksMenu.show(bookmarksButton, Side.BOTTOM, 0, 0)
        );
        bookmarksButton.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        bookmarksButton.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                // add the dragged url to the bookmarks menu (if it wasn't already there).
                final String bookmarkUrl = db.getString();
                if (SideBar.createBookmark(chrome, bookmarksMenu, bookmarkUrl)) return;
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });

        // create a slider to manage the fontSize
        final Slider fontSize = new Slider(0.75, 1.515, 1.0);
        fontSize.setTooltip(new Tooltip("Make it easier or harder to read"));
        fontSize.setMajorTickUnit(0.25);
        fontSize.setMinorTickCount(0);
        fontSize.setShowTickMarks(true);
        fontSize.setBlockIncrement(0.1);
        fontSize.valueProperty().addListener((observableValue, oldValue, newValue) ->
                chrome.getBrowser().getView().setFontScale(newValue.doubleValue())
        );
        final ImageView fontSizeIcon = new ImageView(Util.getImage("rsz_2fontsize.png"));
        fontSizeIcon.setPreserveRatio(true);
        fontSizeIcon.setFitHeight(32);
        ColorAdjust fontSizeColorAdjust = new ColorAdjust();
        fontSizeColorAdjust.setBrightness(0.25);
        fontSizeIcon.setEffect(fontSizeColorAdjust);
        final HBox fontsizer = new HBox(
                fontSizeIcon,
                fontSize
        );
        HBox.setMargin(fontSizeIcon, new Insets(0, 0, 0, 8));

        // create a reader button.
        final Button readerButton = Util.createIconButton(
                "Read",
                "readability.png",
                "Make the current page easier to read",
                actionEvent -> {
                    chrome.getBrowser().getView().getEngine().executeScript(
                            "window.readabilityUrl='" + chrome.getBrowser().getLocField().getText() + "';var s=document.createElement('script');s.setAttribute('type','text/javascript');s.setAttribute('charset','UTF-8');s.setAttribute('src','http://www.readability.com/bookmarklet/read.js');document.documentElement.appendChild(s);"
                    );
                }
        );

        // create a box for displaying navigation options.
        VBox navigationBox = new VBox();
        navigationBox.setSpacing(5);
        navigationBox.setStyle("-fx-padding: 5");
        navigationBox.getChildren().addAll(homeButton, historyButton, bookmarksButton, readerButton, fontsizer);
        final TitledPane navPanel = new TitledPane("Navigation", navigationBox);
        navPanel.getStyleClass().add("sidebar-panel");

        // create an initial set of bookmarks.
        for (String url : defaultBookmarks) {
            SideBar.createBookmark(chrome, bookmarksMenu, url);
        }

        setText("Navigation");
        setContent(navigationBox);
        getStyleClass().add("sidebar-panel");
        setExpanded(true);
    }
}
