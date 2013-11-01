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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SideBar {
    static final String[] defaultBookmarks = {
            "http://fxexperience.com/",
            "http://jewelsea.wordpress.com/",
            "http://docs.oracle.com/javafx/",
            "http://docs.oracle.com/javafx/2/api/index.html",
            "https://forums.oracle.com/forums/forum.jspa?forumID=1385&start=0"
    };
    static final String[] canvasBookmarks = {
            "http://www.zynaps.com/site/experiments/environment.html?mesh=bart.wft",
            "http://andrew-hoyer.com/experiments/cloth/",
            "http://hakim.se/experiments/html5/sketch/#35313167",
            "http://www.effectgames.com/demos/canvascycle/"
//    "http://www.kevs3d.co.uk/dev/lsystems/",        rendering of half the fractals here fails, so displayed it.
//    "http://www.openrise.com/lab/FlowerPower/",     flower power appears to have stopped working in later webview builds, so disabled it.
//    "http://mugtug.com/sketchpad/",                 sketchpad doesn't work too well, so disabled it.
//    "http://radikalfx.com/files/collage/demo.html"  collage is pretty boring, so disabled it.
    };
    private final ScrollPane sideBarScroll;
    private final VBox bar;
    private final VBox progressHolder;
    /**
     * Create a private contructor so you can only create a sidebar via factory methods
     */
    private SideBar(final VBox bar, VBox progressHolder) {
        this.bar = bar;
        this.progressHolder = progressHolder;
        this.sideBarScroll = new ScrollPane(bar);
        sideBarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideBarScroll.getStyleClass().add("sidebar-scroll");
    }

    /**
     * Factory method for creating a new sidebar.
     *
     * @param chrome the chrome the sidebar will be placed into.
     * @return the new sidebar.
     */
    public static SideBar createSidebar(final Willow chrome) {
        // layout holder for the sidebar.
        final VBox bar = new VBox();
        bar.getStyleClass().add("sidebar-background");

        // create a spacer for the sidebar.
        final VBox spacer = new VBox();
        spacer.getStyleClass().add("sidebar-background");
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setAlignment(Pos.BOTTOM_CENTER);

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
                if (createBookmark(chrome, bookmarksMenu, bookmarkUrl)) return;
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

        // create a canvas demos button.
        final ContextMenu canvasMenu = new ContextMenu();
        final Button canvasButton = Util.createIconButton(
                "Canvas Demos",
                "canvas.jpg",
                "Things of beauty",
                null
        );
        canvasButton.setOnAction(actionEvent ->
               canvasMenu.show(canvasButton, Side.BOTTOM, 0, 0)
        );

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

        // create a firebug button.
        final Button firebugButton = Util.createIconButton(
                "Firebug",
                "firebug.png",
                "Discover your web page",
                actionEvent -> {
                    chrome.getBrowser().getView().getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
                }
        );

        // create a box for displaying navigation options.
        VBox navigationBox = new VBox();
        navigationBox.setSpacing(5);
        navigationBox.setStyle("-fx-padding: 5");
        navigationBox.getChildren().addAll(homeButton, historyButton, bookmarksButton, readerButton, fontsizer);
        final TitledPane navPanel = new TitledPane("Navigation", navigationBox);
        navPanel.getStyleClass().add("sidebar-panel");

        // create a box for development tools.
        VBox developmentBox = new VBox();  // todo generalize this title stuff creation for sidebar items.
        developmentBox.setSpacing(5);
        developmentBox.setStyle("-fx-padding: 5");
        developmentBox.getChildren().addAll(firebugButton);
        final TitledPane devPanel = new TitledPane("Development", developmentBox);
        devPanel.getStyleClass().add("sidebar-panel");
        devPanel.setExpanded(false);

        // create a box for demos.
        VBox demoBox = new VBox();  // todo generalize this title stuff creation for sidebar items.
        demoBox.setSpacing(5);
        demoBox.setStyle("-fx-padding: 5");
        demoBox.getChildren().addAll(canvasButton);
        final TitledPane demoPanel = new TitledPane("Demos", demoBox);
        demoPanel.getStyleClass().add("sidebar-panel");
        demoPanel.setExpanded(false);

        // create a box for benchmark control.
        final TitledPane benchPanel = BenchPanel.createPanel(chrome);
        benchPanel.setExpanded(false);

        // size all of the panes similarly.
        navPanel.prefWidthProperty().bind(benchPanel.prefWidthProperty());
        devPanel.prefWidthProperty().bind(benchPanel.prefWidthProperty());

        // put the panes inside the sidebar.
        bar.getChildren().addAll(navPanel, devPanel, demoPanel, benchPanel, spacer);

        // create an initial set of bookmarks.
        for (String url : defaultBookmarks) {
            createBookmark(chrome, bookmarksMenu, url);
        }
        for (String url : canvasBookmarks) {
            createBookmark(chrome, canvasMenu, url);
        }

        return new SideBar(bar, spacer);
    }

    /**
     * Creates a bookmarked url to navigate to.
     *
     * @param chrome        the browser the bookmark is to control.
     * @param bookmarksMenu the menu into which the bookmark is to be installed.
     * @param bookmarkUrl   the url of the bookmark.  // todo should also include the title.
     * @return true if the bookmark was installed in the chrome.
     */
    private static boolean createBookmark(final Willow chrome, ContextMenu bookmarksMenu, final String bookmarkUrl) {
        for (MenuItem item : bookmarksMenu.getItems()) {
            if (item.getText().equals(bookmarkUrl)) return false;
        }
        final MenuItem menuItem = new MenuItem(bookmarkUrl);
        menuItem.setOnAction(actionEvent -> chrome.getBrowser().navTo(bookmarkUrl));
        bookmarksMenu.getItems().add(menuItem);
        return true;
    }

    /**
     * Set the load control attached to the sidebar
     */
    public void setLoadControl(Node loadControl) {
        VBox.setMargin(loadControl, new Insets(5, 5, 10, 5));
        progressHolder.getChildren().clear();
        progressHolder.getChildren().add(loadControl);
    }

    /**
     * Returns the sidebar display
     */
    public VBox getBarDisplay() {
        return bar;
    }

    public ScrollPane getScroll() {
        return sideBarScroll;
    }
}

// todo add an autohide to the bar if it hasn't been used for a while.
// todo history in the sidebar should actually be chrome wide rather than browser tab specific.
// todo some kind of persistence framework is needed.

// todo file jira ability to set the initial offset of a slider
