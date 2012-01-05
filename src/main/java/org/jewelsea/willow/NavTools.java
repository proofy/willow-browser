package org.jewelsea.willow;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NavTools {
  public static Pane createNavPane(final Willow chrome) {
    // create a back button.
    final Button backButton = new Button();
    backButton.setTooltip(new Tooltip("Go back or right click for history"));
    final ImageView backGraphic = new ImageView(new Image(Util.getResource("239706184.png")));
    final ColorAdjust backColorAdjust = new ColorAdjust();
    backColorAdjust.setBrightness(-0.1);
    backColorAdjust.setContrast(-0.1);
    backGraphic.setEffect(backColorAdjust);
    backButton.setGraphic(backGraphic);
    backGraphic.setPreserveRatio(true);
    backGraphic.setFitHeight(32);
    backButton.onActionProperty().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        if (chrome.getBrowser().getHistory().canNavBack()) {
          chrome.getBrowser().navTo(chrome.getBrowser().getHistory().requestNavBack());
        }
      }
    });
    backButton.setOnMouseReleased(chrome.getBrowser().getHistory().createShowHistoryMouseEvent(backButton));

    // create a forward button.
    final Button forwardButton = new Button();
    forwardButton.setTranslateX(-2);
    final ImageView forwardGraphic = new ImageView(new Image(Util.getResource("1813406178.png")));
    final ColorAdjust forwardColorAdjust = new ColorAdjust();
    forwardColorAdjust.setBrightness(-0.1);
    forwardColorAdjust.setContrast(-0.1);
    forwardGraphic.setEffect(forwardColorAdjust);
    forwardGraphic.setPreserveRatio(true);
    forwardGraphic.setFitHeight(20);
    forwardButton.setGraphic(forwardGraphic);
    forwardButton.setTooltip(new Tooltip("Go forward"));
    forwardButton.onActionProperty().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        if (chrome.getBrowser().getHistory().canNavForward()) {
          chrome.getBrowser().navTo(chrome.getBrowser().getHistory().requestNavForward());
        }
      }
    });
    forwardButton.setOnMouseReleased(chrome.getBrowser().getHistory().createShowHistoryMouseEvent(backButton));

    // create a navigate button.
    final Button navButton = new Button();
    navButton.setTooltip(new Tooltip("Go to or rejuvenate the location"));
    final ImageView navGraphic = new ImageView(new Image(Util.getResource("Forward Arrow.png")));
    final ColorAdjust navColorAdjust = new ColorAdjust();
    navColorAdjust.setContrast(-0.7);
    navGraphic.setEffect(navColorAdjust);
    navGraphic.setPreserveRatio(true);
    navGraphic.setFitHeight(14);
    navButton.setGraphic(navGraphic);
    navButton.onActionProperty().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        chrome.getBrowser().navTo(chrome.getBrowser().getLocField().getText());
      }
    });

    // create a button to hide and show the sidebar.
    final Button sidebarButton = new Button();
    final ImageView sidebarGraphic = new ImageView(new Image(Util.getResource("Down Arrow.png")));
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
    sidebarButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        // hide sidebar.
        final double startWidth = chrome.getSidebarDisplay().getWidth();
        final Animation hideSidebar = new Transition() {
          { setCycleDuration(Duration.millis(250)); }
          protected void interpolate(double frac) {
            final double curWidth = startWidth * (1.0 - frac);
            chrome.getSidebarDisplay().setPrefWidth(curWidth);
            chrome.getSidebarDisplay().setTranslateX(-startWidth + curWidth);
          }
        };
        hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            chrome.getSidebarDisplay().setVisible(false);
          }
        });

        // show sidebar.
        final Animation showSidebar = new Transition() {
          { setCycleDuration(Duration.millis(250)); }
          protected void interpolate(double frac) {
            chrome.getSidebarDisplay().setVisible(true);
            final double curWidth = startWidth * frac;
            chrome.getSidebarDisplay().setPrefWidth(curWidth);
            chrome.getSidebarDisplay().setTranslateX(-startWidth + curWidth);
          }
        };

        if (showSidebar.statusProperty().get().equals(Animation.Status.STOPPED) && hideSidebar.statusProperty().get().equals(Animation.Status.STOPPED)) {
          if (chrome.getSidebarDisplay().isVisible()) {
            hideSidebar.play();
          } else {
            showSidebar.play();
          }
        }
      }
    });
    
    final Button fullscreenButton = new Button("Fullscreen");
    fullscreenButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        final Stage stage = (Stage) fullscreenButton.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
      }
    });

    // align all of the navigation widgets in a horizontal toolbar.
    final HBox navPane = new HBox();
    navPane.setAlignment(Pos.CENTER);
    navPane.getStyleClass().add("toolbar");
    navPane.setSpacing(5);
    navPane.getChildren().addAll(sidebarButton, backButton, forwardButton, chrome.getChromeLocField(), chrome.getTabManager().getTabPane(), chrome.getTabManager().getNewTabButton(), navButton, fullscreenButton);
    navPane.setFillHeight(false);
    Platform.runLater(new Runnable() {
      @Override public void run() {
        navPane.setMinHeight(navPane.getHeight());
      }
    });

    final InnerShadow innerShadow = new InnerShadow();
    innerShadow.setColor(Color.ANTIQUEWHITE);
    navPane.setEffect(innerShadow);
    
    return navPane;
  }
}

// todo while a page is loading we might want to switch the favicon to a loading animation...
// todo may also want to add the following ot the navbar... /* createLoadIndicator(), */ /* browser.favicon, */