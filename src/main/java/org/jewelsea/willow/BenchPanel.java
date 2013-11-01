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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Sidebar panel for showing Benchmark information
 */
public class BenchPanel {
    public static TitledPane createPanel(final Willow chrome) {
        // create a layout container for the panel.
        VBox benchPanel = new VBox();
        benchPanel.setSpacing(5);
        benchPanel.setStyle("-fx-padding: 5");
        TitledPane benchTitle = new TitledPane("Benchmarks", benchPanel);
        benchTitle.setStyle("-fx-font-size: 16px;");

        // info on benchmarks.
        // format: name, link, icon, (if link and icon are empty, then defines a benchmark category).
        final String[][] benchmarkLinks = {
                {"Compliance", "", ""},
                {"HTML 5 Test", "http://www.html5test.com", "HTML5_Badge_32.png"},
                {"Acid 3 Test", "http://acid3.acidtests.org/", "acid.png"},
                {"JavaScript Performance", "", ""},
                {"WebKit SunSpider", "http://www.webkit.org/perf/sunspider-0.9.1/sunspider-0.9.1/driver.html", "webkit.png"},
                {"Google V8", "http://v8.googlecode.com/svn/data/benchmarks/v5/run.html", "google.png"},
                {"Mozilla Kraken", "http://krakenbenchmark.mozilla.org/kraken-1.0/driver.html", "firefox_32.png"},
                {"Rendering Performance", ""},
                {"Bubble Mark", "http://bubblemark.com/dhtml.htm", "ball.png"},
                {"Guimark", "http://www.craftymind.com/factory/guimark/GUIMark_HTML4.html", "guimark.png"}
        };

        // create the panel contents and insert it into the panel.
        ToggleGroup benchToggleGroup = new ToggleGroup();
        boolean firstCategory = true;
        for (final String[] link : benchmarkLinks) {
            if ("".equals(link[1])) {
                // a category of benchmarks.
                final Label categoryLabel = new Label(link[0]);
                categoryLabel.setStyle("-fx-text-fill: midnightblue; -fx-font-size: 16px;");
                VBox.setMargin(categoryLabel, new Insets(firstCategory ? 1 : 8, 0, 0, 0));
                benchPanel.getChildren().add(categoryLabel);
                firstCategory = false;
            } else {
                // create a toggle button to navigate to the given benchmark.
                final ToggleButton benchLink = new ToggleButton(link[0]);
                benchLink.getStyleClass().add("icon-button");
                benchLink.setAlignment(Pos.CENTER_LEFT);
                benchLink.setContentDisplay(ContentDisplay.LEFT);
                benchLink.setOnAction(actionEvent -> chrome.getBrowser().navTo(link[1]));
                benchPanel.getChildren().add(benchLink);
                benchLink.setMaxWidth(Double.MAX_VALUE);
                VBox.setMargin(benchLink, new Insets(0, 5, 0, 5));

                // place the link in a toggle group.
                benchLink.setToggleGroup(benchToggleGroup);

                // add a graphic to the link.
                if (!link[2].equals("")) {
                    final Image image = Util.getImage(link[2]);
                    final ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(16);
                    benchLink.setGraphic(imageView);
                }
            }
        }

        // add a spacer to pad out the panel.
        final Region spacer = new Region();
        spacer.setPrefHeight(5);
        benchPanel.getChildren().add(spacer);

        return benchTitle;
    }
}
