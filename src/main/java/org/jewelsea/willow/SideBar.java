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
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SideBar {
  /** Create a private contructor so you can only create a sidebar via factory methods */
  private SideBar(VBox bar, VBox progressHolder) { 
    this.bar            = bar; 
    this.progressHolder = progressHolder; 
  }
  private final VBox bar;
  private final VBox progressHolder;
  
  /** Set the load control attached to the sidebar */
  public void setLoadControl(Node loadControl) {
    VBox.setMargin(loadControl, new Insets(5, 5, 10, 5));
    progressHolder.getChildren().clear();
    progressHolder.getChildren().add(loadControl);
  }

  /** Returns the sidebar display */
  public VBox getBarDisplay() { return bar; }

  /**
   * Factory method for creating a new sidebar.
   * @param chrome the chrome the sidebar will be placed into.
   * @return the new sidebar.
   */
  public static SideBar createSidebar(final Willow chrome) {
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
      new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent actionEvent) {
          chrome.getBrowser().navTo(chrome.homeLocationProperty.get());
        }
      }
    );
    homeButton.setOnDragOver(new EventHandler<DragEvent>() {
      @Override public void handle(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasString()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
      }
    });
    homeButton.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override public void handle(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasString()) {
          chrome.homeLocationProperty.set(db.getString());
          success = true;
        }
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
      }
    });

    // create a history button to show the history.
    final Button historyButton = Util.createIconButton(
      "History",
      "History.png",
      "Where did you go?",
      null
    );
    historyButton.setOnAction(chrome.getBrowser().getHistory().createShowHistoryActionEvent(historyButton));

    // create a bookmarksButton.
    final ContextMenu bookmarksMenu = new ContextMenu();
    final Button bookmarksButton = Util.createIconButton(
      "Bookmarks",
      "1714696718.png",
      "Drag a location here to remember it and click to recall your remembrance",
      null
    );
    bookmarksButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        bookmarksMenu.show(bookmarksButton, Side.BOTTOM, 0, 0);
      }
    });
    bookmarksButton.setOnDragOver(new EventHandler<DragEvent>() {
      @Override public void handle(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasString()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
      }
    });
    bookmarksButton.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override public void handle(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasString()) {
          // add the dragged url to the bookmarks menu (if it wasn't already there).
          final String bookmarkUrl = db.getString();
          for (MenuItem item : bookmarksMenu.getItems()) {
            if (item.getText().equals(bookmarkUrl)) return;
          }
          final MenuItem menuItem = new MenuItem(bookmarkUrl);
          menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
              chrome.getBrowser().navTo(bookmarkUrl);
            }
          });
          bookmarksMenu.getItems().add(menuItem);
          success = true;
        }
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
      }
    });

    // create a slider to manage the fontSize
    final Slider fontSize = new Slider(0.5, 2.015, 1.0);
    fontSize.setTooltip(new Tooltip("Make it easier or harder to read"));
    fontSize.setMajorTickUnit(0.5);
    fontSize.setMinorTickCount(1);
    fontSize.setShowTickMarks(true);
    fontSize.setBlockIncrement(0.1);
    fontSize.valueProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
        chrome.getBrowser().getView().setFontScale(newValue.doubleValue());
      }
    });
    final ImageView fontSizeIcon = new ImageView(new Image(Util.getResource("rsz_2fontsize.png")));
    fontSizeIcon.setPreserveRatio(true);
    fontSizeIcon.setFitHeight(32);
    ColorAdjust fontSizeColorAdjust = new ColorAdjust();
    fontSizeColorAdjust.setBrightness(0.25);
    fontSizeIcon.setEffect(fontSizeColorAdjust);
    final HBox fontsizer = HBoxBuilder.create().children(
      fontSizeIcon,
      fontSize
    ).build();
    HBox.setMargin(fontSizeIcon, new Insets(0, 0, 0, 8));

    // create a reader button.
    final Button readerButton = Util.createIconButton(
      "Read",
      "readability.png",
      "Make the current page easier to read",
      new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent actionEvent) {
          chrome.getBrowser().getView().getEngine().executeScript(
            "window.readabilityUrl='" + chrome.getBrowser().getLocField().getText() + "';var s=document.createElement('script');s.setAttribute('type','text/javascript');s.setAttribute('charset','UTF-8');s.setAttribute('src','http://www.readability.com/bookmarklet/read.js');document.documentElement.appendChild(s);"
          );
        }
      }
    );
    
    // create a firebug button.
    final Button firebugButton = Util.createIconButton(
      "Firebug",
      "firebug.png",
      "Discover your web page",
      new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent actionEvent) {
          chrome.getBrowser().getView().getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
        }
      }
    );
    
    // create a box for displaying navigation options.
    VBox navigationBox = new VBox();
    navigationBox.setSpacing(5);
    navigationBox.setStyle("-fx-padding: 5");
    navigationBox.getChildren().addAll(homeButton, historyButton, bookmarksButton, readerButton, fontsizer); // todo fontSize disabled until it is working.
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

    // create a box for benchmark control.
    final TitledPane benchPanel = BenchPanel.createPanel(chrome);
    benchPanel.setExpanded(false);
    
    // size all of the panes similarly.
    navPanel.prefWidthProperty().bind(benchPanel.prefWidthProperty());
    devPanel.prefWidthProperty().bind(benchPanel.prefWidthProperty());

    // put the panes inside the sidebar.
    bar.getChildren().addAll(navPanel, devPanel, benchPanel, spacer);

    return new SideBar(bar, spacer);
  }
}

// todo add an autohide to the bar if it hasn't been used for a while.
// todo add a full screen browsing mode.
// todo fully open sidebar makes the navbar scroll off the top of the screen.
// todo history in the sidebar should actually be chrome wide rather than browser tab specific.
// todo some kind of persistence framework is needed.

// todo file jira ability to set the initial offset of a slider
// todo file jira custom slider formatting does not work.