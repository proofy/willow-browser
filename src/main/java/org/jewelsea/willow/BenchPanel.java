package org.jewelsea.willow;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Sidebar panel for showing Benchmark information */
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
      { "Compliance", "", "" },
      { "HTML 5 Test",       "http://www.html5test.com", "HTML5_Badge_32.png"},
      { "Acid 3 Test",       "http://acid3.acidtests.org/", "acid.png"},
      { "JavaScript Performance", "", "" },
      { "WebKit SunSpider",  "http://www.webkit.org/perf/sunspider-0.9.1/sunspider-0.9.1/driver.html", "webkit.png"},
      { "Google V8",         "http://v8.googlecode.com/svn/data/benchmarks/v5/run.html", "google.png"},
      { "Mozilla Kraken",    "http://krakenbenchmark.mozilla.org/kraken-1.0/driver.html", "firefox_32.png"},
      { "Rendering Performance", "" },
      { "Bubble Mark",       "http://bubblemark.com/dhtml.htm", "ball.png"},
      { "Guimark",           "http://www.craftymind.com/factory/guimark/GUIMark_HTML4.html", "guimark.png"}
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
        benchLink.setOnAction(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            chrome.getBrowser().navTo(link[1]);
          }
        });
        benchPanel.getChildren().add(benchLink);
        benchLink.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(benchLink, new Insets(0, 5, 0, 5));
  
        // place the link in a toggle group.
        benchLink.setToggleGroup(benchToggleGroup);
  
        // add a graphic to the link.
        if (!link[2].equals("")) {
          final Image image = new Image(Util.getResource(link[2]));
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
