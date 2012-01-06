package org.jewelsea.willow;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public class Willow extends Application {
  public static final String DEFAULT_HOME_LOCATION = "http://docs.oracle.com/javafx/2.0/get_started/jfxpub-get_started.htm";
  public StringProperty homeLocationProperty = new SimpleStringProperty(DEFAULT_HOME_LOCATION);
  private SideBar    sidebar;                              // sidebar for controlling the app.
  private TabManager tabManager;                           // tab manager for managing browser tabs.
  private BorderPane mainLayout = new BorderPane();        // layout of the browser application.
  private TextField  chromeLocField = new TextField();     // current location of the current browser or a value being updated by the user to change the current browser's location.

  // change listeners to tie the location of the current browser to the chromeLocField and vice versa (binding cannot be used because both these values must be read/write).
  private ChangeListener<String> browserLocFieldChangeListener;
  private ChangeListener<String> chromeLocFieldChangeListener;

  public static void main(String[] args) { Application.launch(args); }
  @Override public void start(final Stage stage) throws MalformedURLException, UnsupportedEncodingException {
    // set the title bar to the title of the web page (if there is one).
    stage.setTitle("Willow");

    // initialize the stuff which can't be initialized in the init method due to stupid threading issues.
    tabManager = new TabManager(chromeLocField);
    sidebar    = SideBar.createSidebar(this);

    // initialize the location field in the Chrome.
    chromeLocField.setStyle("-fx-font-size: 14;");
    chromeLocField.setPromptText("Where do you want to go today?");
    chromeLocField.setTooltip(new Tooltip("Enter a location or find happiness."));
    chromeLocField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
          getBrowser().navTo(chromeLocField.getText());
        }
      }
    });

    // setup the main layout.
    HBox.setHgrow(chromeLocField, Priority.ALWAYS);
    final Pane navPane = NavTools.createNavPane(this);
    mainLayout.setTop(navPane);
    mainLayout.setLeft(getSidebarDisplay());

    // add an overlay layer over the main layout for effects and status messages.
    final AnchorPane overlayLayer = new AnchorPane();
    final StackPane overlaidLayout = new StackPane();
    overlaidLayout.getChildren().addAll(mainLayout, overlayLayer);
    overlayLayer.setPickOnBounds(false);

    // monitor the tab manager for a change in the browser window and update the display appropriately.
    tabManager.browserProperty().addListener(new ChangeListener<BrowserWindow>() {
      @Override public void changed(ObservableValue<? extends BrowserWindow> observableValue, final BrowserWindow oldBrowser, final BrowserWindow newBrowser) {
        browserChanged(oldBrowser, newBrowser, stage, overlayLayer);
      }
    });

    // we need to manually handle the change from no browser at all to an initial browser.
    browserChanged(null, getBrowser(), stage, overlayLayer);

    // create the scene.
    final Scene scene = new Scene(overlaidLayout, 1121, 600);
    scene.getStylesheets().add("org/jewelsea/willow/willow.css");
    overlaidLayout.setStyle("-fx-background: rgba(100, 0, 0, 0)");

    // set some sizing constraints on the scene.
    overlayLayer.prefHeightProperty().bind(scene.heightProperty());
    overlayLayer.prefWidthProperty().bind(scene.widthProperty());

    // show the scene.
    stage.setScene(scene);
    stage.show();

    // nav to the home location
    getBrowser().navTo(homeLocationProperty.get());

    // highlight the entire text if we click on the chromeLocField so that it can be easily changed.
    chromeLocField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean from, Boolean to) {
        if (to) {
          Platform.runLater(new Runnable() { // run later used here to override the default selection rules for the textfield.
            @Override public void run() {
              chromeLocField.selectAll();
            }
          });
        }
      }
    });

    // make the chrome location field draggable.
    chromeLocField.getStyleClass().add("location-field");
    chromeLocField.setOnDragDetected(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        Dragboard db = chromeLocField.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(chromeLocField.getText());
        db.setContent(content);
      }
    });

    // automatically hide and show the sidebar and navbar as we transition in and out of fullscreen.
    final Button navPaneButton = createNavPaneButton(navPane);
    stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
      @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
        if (( stage.isFullScreen() &&  getSidebarDisplay().isVisible()) ||
            (!stage.isFullScreen() && !getSidebarDisplay().isVisible())) {
          ((Button) scene.lookup("#sidebarButton")).fire();
        }
        if (( stage.isFullScreen() &&  navPane.isVisible()) ||
            (!stage.isFullScreen() && !navPane.isVisible())) {
          navPaneButton.fire();
        }
      }
    });

    // create a new tab when the user presses Ctrl+T
    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override public void handle(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.T)) {
          tabManager.getNewTabButton().fire();
        }
      }
    });

    // add an icon for the application.
    stage.getIcons().add(new Image(Util.getResource("WillowTreeIcon.png")));

    // set the focus (do it later, so that our request has a better chance
    // of being actioned and the default focus positioning does not override it).
    Platform.runLater(new Runnable() {
      @Override public void run() {
        getChromeLocField().requestFocus();
      }
    });

    // debugging routine.
//    Platform.runLater(new Runnable() {
//      @Override public void run() {
//        Util.dump(scene.getRoot());
//      }
//    });
  }

  // creates a button to hide and show the navigation pane.
  private Button createNavPaneButton(final Pane navPane) {
    final Button navPaneButton = new Button();
    navPaneButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        // hide sidebar.
        final double startHeight = navPane.getHeight();
        final Animation hideNavPane = new Transition() {
          { setCycleDuration(Duration.millis(250)); }
          protected void interpolate(double frac) {
            final double curHeight = startHeight * (1.0 - frac);
            navPane.setPrefHeight(curHeight);
            navPane.setTranslateY(-startHeight + curHeight);
          }
        };
        hideNavPane.onFinishedProperty().set(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            navPane.setVisible(false);
          }
        });

        // show sidebar.
        final Animation showNavPane = new Transition() {
          { setCycleDuration(Duration.millis(250)); }
          protected void interpolate(double frac) {
            navPane.setVisible(true);
            final double curHeight = startHeight * frac;
            navPane.setPrefHeight(curHeight);
            navPane.setTranslateY(-startHeight + curHeight);
          }
        };

        if (showNavPane.statusProperty().get().equals(Animation.Status.STOPPED) && hideNavPane.statusProperty().get().equals(Animation.Status.STOPPED)) {
          if (navPane.isVisible()) {
            hideNavPane.play();
          } else {
            showNavPane.play();
          }
        }
      }
    });
    return navPaneButton;
  }

  /**
   * Handler for when a new browser is switched into the chrome.
   * @param oldBrowser the old browser we were to displaying (or none if there is no such thing).
   * @param newBrowser the new browser we are to display.
   * @param stage the stage displaying the chrome.
   * @param overlayLayer the overlay layer for status and other information in the chrome.
   */
  private void browserChanged(final BrowserWindow oldBrowser, final BrowserWindow newBrowser, final Stage stage, AnchorPane overlayLayer) {
    // cleanup the links between the chrome's location field and the old browser's location field.
    if (oldBrowser != null && browserLocFieldChangeListener != null) {
      oldBrowser.getLocField().textProperty().removeListener(browserLocFieldChangeListener);
    }
    if (chromeLocFieldChangeListener != null) {
      chromeLocField.textProperty().removeListener(chromeLocFieldChangeListener);
    }

    // update the stage title to monitor the page displayed in the selected browser.
    newBrowser.getView().getEngine().titleProperty().addListener(new ChangeListener<String>() {  // todo hmm I wonder how the listeners ever get removed...
      @Override public void changed(ObservableValue<? extends String> observableValue, String oldTitle, String newTitle) {
        if (newTitle != null && !"".equals(newTitle)) {
          stage.setTitle("Willow - " + newTitle);
        } else {
          // necessary because when the browser is in the process of loading a new page, the title will be empty.  todo I wonder if the title would be reset correctly if the page has no title.
          if (!newBrowser.getView().getEngine().getLoadWorker().isRunning()) {
            stage.setTitle("Willow");
          }
        }
      }
    });

    // monitor the status of the selected browser.
    overlayLayer.getChildren().clear();
    final HBox statusDisplay = newBrowser.createStatusDisplay();
    statusDisplay.translateXProperty().bind(getSidebarDisplay().widthProperty().add(20).add(getSidebarDisplay().translateXProperty()));
    statusDisplay.translateYProperty().bind(overlayLayer.heightProperty().subtract(30));
    overlayLayer.getChildren().add(statusDisplay);

    // monitor the loading progress of the selected browser.
    sidebar.setLoadControl(newBrowser.createLoadControl());

    // make the chrome's location field respond to changes in the new browser's location.
    browserLocFieldChangeListener = new ChangeListener<String>() {
      @Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {
        if (!chromeLocField.getText().equals(newLoc)) {
          chromeLocField.setText(newLoc);
        }
      }
    };
    newBrowser.getLocField().textProperty().addListener(browserLocFieldChangeListener);

    // make the new browser respond to changes the user makes to the chrome's location.
    chromeLocFieldChangeListener = new ChangeListener<String>() {
      @Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {
        if (!newBrowser.getLocField().getText().equals(newLoc)) {
          newBrowser.getLocField().setText(newLoc);
        }
      }
    };
    chromeLocField.textProperty().addListener(chromeLocFieldChangeListener);
    chromeLocField.setText(newBrowser.getLocField().getText());

    // display the selected browser.
    mainLayout.setCenter(newBrowser.getView());
  }

  public BrowserWindow getBrowser() {
    return tabManager.getBrowser();
  }

  public VBox getSidebarDisplay() {
    return sidebar.getBarDisplay();
  }

  public TextField getChromeLocField() {
    return chromeLocField;
  }

  public TabManager getTabManager() {
    return tabManager;
  }
}
