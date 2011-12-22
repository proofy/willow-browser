package org.jewelsea.willow;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class BrowserWindow {
  private final WebView        view    = new WebView();
  private final History        history = new History(this);
  private final StringProperty status  = new SimpleStringProperty();
  private final TextField      locField = new TextField();    // the location the browser engine is currently pointing at (or where the user can type in where to go next).
  private final ReadOnlyObjectWrapper<ImageView> favicon = new ReadOnlyObjectWrapper<ImageView>();
  private final FavIconHandler favIconHandler = FavIconHandler.getInstance();

  public BrowserWindow() {
    // init the location text field.
    HBox.setHgrow(locField, Priority.ALWAYS);
    locField.setPromptText("Where do you want to go today?");
    locField.setTooltip(new Tooltip("Enter a location or find happiness."));
    locField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
          navTo(locField.getText());
        }
      }
    });
    locField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean from, Boolean to) {
        if (to) {
          Platform.runLater(new Runnable() {
            @Override public void run() {
              locField.selectAll();
            }
          });
        }
      }
    });

    // make the location field draggable.
    getLocField().getStyleClass().add("location-field");
    getLocField().setOnDragDetected(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        Dragboard db = getLocField().startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(getLocField().getText());
        db.setContent(content);
      }
    });

    // monitor the web view for when it's location changes, so we can update the history lists and other items correctly.
    final WebEngine engine = getView().getEngine();
    engine.locationProperty().addListener(new ChangeListener<String>() {
      @Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {
        getHistory().executeNav(newLoc); // update the history lists.
        getLocField().setText(newLoc);   // update the location field.
        favicon.set(copyImageView(favIconHandler.fetchFavIcon(newLoc)));
      }
    });
    
    // monitor the web views loading state so we can provide progress feedback.
    Worker worker = engine.getLoadWorker();
    worker.stateProperty().addListener(new ChangeListener<Worker.State>() {
      @Override public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
        if (newState == Worker.State.CANCELLED) {
          // todo possible hook here for implementing a download handler.
        }
      }
    });
    worker.exceptionProperty().addListener(new ChangeListener<Throwable>() {
      @Override public void changed(ObservableValue<? extends Throwable> observableValue, Throwable oldThrowable, Throwable newThrowable) {
        System.out.println("Browser encountered a load exception: " + newThrowable);
      }
    });

    // create handlers for javascript actions and status changes.
    engine.setPromptHandler(createPromptHandler());
    engine.setConfirmHandler(createConfirmHandler());
    engine.setOnAlert(createAlertHandler());
    engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
      @Override public void handle(WebEvent<String> stringWebEvent) {
        getStatus().setValue(stringWebEvent.getData());
      }
    });

    // monitor the location url, and if it is a pdf file, then create a pdf viewer for it.
    getLocField().textProperty().addListener(new ChangeListener<String>() {
      @Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {
        if (newLoc.endsWith(".pdf")) {
          try {
            final PDFViewer pdfViewer = new PDFViewer(false);  // todo try icepdf viewer instead...
            pdfViewer.openFile(new URL(newLoc));
          } catch (Exception ex) {
            // just fail to open a bad pdf url silently - no action required.
          }
        }
        String downloadableExtension = null;  // todo I wonder how to find out from WebView which documents it could not process so that I could trigger a save as for them?
        String[] downloadableExtensions = { ".doc", ".xls", ".zip", ".tgz", ".jar" };
        for (String ext: downloadableExtensions) {
          if (newLoc.endsWith(ext)) {
            downloadableExtension = ext;
            break;
          }
        }
        if (downloadableExtension != null) {  
          // create a file save option for performing a download.
          FileChooser chooser = new FileChooser();
          chooser.setTitle("Save " + newLoc);
          chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Downloadable File", downloadableExtension));
          int filenameIdx = newLoc.lastIndexOf("/") + 1;
          if (filenameIdx != 0) {
            File saveFile = chooser.showSaveDialog(view.getScene().getWindow());

            if (saveFile != null) {
              BufferedInputStream  is = null;
              BufferedOutputStream os = null;
              try {
                is = new BufferedInputStream(new URL(newLoc).openStream());
                os = new BufferedOutputStream(new FileOutputStream(saveFile));
                int b = is.read();
                while (b != -1) {
                  os.write(b);
                  b = is.read();
                }
              } catch (FileNotFoundException e) {
                System.out.println("Unable to save file: " + e);
              } catch (MalformedURLException e) {
                System.out.println("Unable to save file: " + e);
              } catch (IOException e) {
                System.out.println("Unable to save file: " + e);
              } finally {
                try { if (is != null) is.close(); } catch (IOException e) { /** no action required. */ }
                try { if (os != null) os.close(); } catch (IOException e) { /** no action required. */ }
              }
            }
            
            // todo provide feedback on the save function and provide a download list and download list lookup.
          }
        }
      }
    });

    // add an effect for disabling and enabling the view.
    getView().disabledProperty().addListener(new ChangeListener<Boolean>() {
      final BoxBlur     soften = new BoxBlur();
      final ColorAdjust dim    = new ColorAdjust();
      { dim.setInput(soften); dim.setBrightness(-0.5); }
      @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          getView().setEffect(dim);
        } else {
          getView().setEffect(null);
        }
      }
    });
  }

  /** 
   * Copies an ImageView to a new ImageView, so that we can render multiple copies of the templated
   * ImageView in a scene.
   * @param templateImageView an imageview containing an image and other import information to be copied.
   * @return a copy of the import parts of an ImageView
   */ 
  private ImageView copyImageView(ImageView templateImageView) {
    ImageView xerox = new ImageView();
    xerox.setFitHeight(templateImageView.getFitHeight());
    xerox.setPreserveRatio(templateImageView.isPreserveRatio());
    xerox.imageProperty().bind(templateImageView.imageProperty());
    return xerox;
  }

  private EventHandler<WebEvent<String>> createAlertHandler() {
    return new EventHandler<WebEvent<String>>() {
      @Override public void handle(WebEvent<String> stringWebEvent) {
        // add controls to the popup.
        final Label promptMessage = new Label(stringWebEvent.getData());
        final ImageView alertImage = new ImageView(new Image(Util.getResource("alert_48.png")));
        alertImage.setFitHeight(32);
        alertImage.setPreserveRatio(true);
        promptMessage.setGraphic(alertImage);
        promptMessage.setWrapText(true);
        promptMessage.setPrefWidth(350);

        // action button text setup.
        HBox buttonBar = new HBox(20);
        final Button confirmButton = new Button("Continue");
        confirmButton.setDefaultButton(true);

        buttonBar.getChildren().addAll(confirmButton);

        // layout the popup.
        final VBox promptLayout = new VBox(14);
        promptLayout.setPadding(new Insets(10));
        promptLayout.getStyleClass().add("alert-dialog");
        promptLayout.getChildren().addAll(promptMessage, buttonBar);

        final DropShadow dropShadow = new DropShadow();
        promptLayout.setEffect(dropShadow);
        overlayView(promptLayout);

        // confirm and close the popup.
        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            // todo block until the user accepts the alert.
            getView().setDisable(false);
            removeViewOverlay();
          }
        });
      }
    };
  }

  private Callback<String, Boolean> createConfirmHandler() {
    return new Callback<String, Boolean>() {
      @Override
      public Boolean call(String message) {
        // add controls to the popup.
        final Label promptMessage = new Label(message);
        promptMessage.setWrapText(true);
        promptMessage.setPrefWidth(350);

        // action button text setup.
        HBox buttonBar = new HBox(20);

        final ImageView confirmImage = new ImageView(new Image(Util.getResource("select_48.png")));
        confirmImage.setFitHeight(19);
        confirmImage.setPreserveRatio(true);

        final Button confirmButton = new Button("Confirm");
        confirmButton.setGraphic(confirmImage);
        confirmButton.setDefaultButton(true);

        final ImageView denyImage = new ImageView(new Image(Util.getResource("stop_48.png")));
        denyImage.setFitHeight(19);
        denyImage.setPreserveRatio(true);

        final Button denyButton = new Button("Deny");
        denyButton.setGraphic(denyImage);
        denyButton.setCancelButton(true);

        buttonBar.getChildren().addAll(confirmButton, denyButton);

        // layout the popup.
        final VBox promptLayout = new VBox(14);
        promptLayout.setPadding(new Insets(10));
        promptLayout.getStyleClass().add("alert-dialog");
        promptLayout.getChildren().addAll(promptMessage, buttonBar);

        final DropShadow dropShadow = new DropShadow();
        promptLayout.setEffect(dropShadow);
        overlayView(promptLayout);

        // confirm and close the popup.
        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            // todo actually modify the output of the prompt callback when the platform permits this to be possible.
            getView().setDisable(false);
            removeViewOverlay();
          }
        });

        // deny and close the popup.
        denyButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            // todo actually modify the output of the prompt callback when the platform permits this to be possible.
            getView().setDisable(false);
            removeViewOverlay();
          }
        });

        return true; // todo should block and return the actual value from the dialog.
      }
    };
  }

  private Callback<PromptData, String> createPromptHandler() {
    return new Callback<PromptData, String>() {
      @Override public String call(PromptData promptData) {
        // add controls to the popup.
        final Label promptMessage = new Label(promptData.getMessage());
        promptMessage.setWrapText(true);
        final ImageView promptImage = new ImageView(new Image(Util.getResource("help_64.png")));
        promptImage.setFitHeight(32);
        promptImage.setPreserveRatio(true);
        promptMessage.setGraphic(promptImage);
        promptMessage.setPrefWidth(350);
        final TextField inputField = new TextField(promptData.getDefaultValue());
        inputField.setTranslateY(-5);
        Platform.runLater(new Runnable() {
          @Override public void run() {
            inputField.selectAll();
          }
        });

        // action button text setup.
        HBox buttonBar = new HBox(20);
        final Button submitButton = new Button("Submit");
        submitButton.setDefaultButton(true);
        final Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        ColorAdjust bleach = new ColorAdjust();
        bleach.setSaturation(-0.6);
        cancelButton.setEffect(bleach);
        buttonBar.getChildren().addAll(submitButton, cancelButton);

        // layout the popup.
        final VBox promptLayout = new VBox(14);
        promptLayout.setPadding(new Insets(10));
        promptLayout.getStyleClass().add("alert-dialog");
        promptLayout.getChildren().addAll(promptMessage, inputField, buttonBar);

        final DropShadow dropShadow = new DropShadow();
        promptLayout.setEffect(dropShadow);
        overlayView(promptLayout);

        // submit and close the popup.
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            // todo actually modify the output of the prompt callback when the platform permits this to be possible.
            getView().setDisable(false);
            removeViewOverlay();
          }
        });

        // submit and close the popup.
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
            // todo actually modify the output of the prompt callback when the platform permits this to be possible.
            getView().setDisable(false);
            removeViewOverlay();
          }
        });

        return promptData.getDefaultValue();
      }
    };
  }

  private void removeViewOverlay() {
    BorderPane viewParent = (BorderPane) getView().getParent().getParent();
    viewParent.setCenter(getView());
  }

  public void navTo(String loc) {
    // modify the request location, to make it easier on the user for typing.
// todo we probably don't want this default nav for empty .... work out what to do instead ....
//    if (loc == null || loc.isEmpty()) { // go home if the location field is empty.
//      loc = chrome.homeLocationProperty.get();
//    }
    if (loc == null) loc = "";
    if (loc.startsWith("google")) { // search google
      loc = "http://www.google.com/search?q=" + loc.substring("google".length()).trim().replaceAll(" ", "+");
    } else if (loc.startsWith("bing")) { // search bing
      loc = "http://www.bing.com/search?q=" + loc.substring("bing".length()).trim().replaceAll(" ", "+");
    } else if (loc.startsWith("yahoo")) { // search yahoo
      loc = "http://search.yahoo.com/search?p=" + loc.substring("yahoo".length()).trim().replaceAll(" ", "+");
    } else if (loc.startsWith("wiki")) {
      loc = "http://en.wikipedia.org/w/index.php?search=" + loc.substring("wiki".length()).trim().replaceAll(" ", "+");
    } else if (loc.startsWith("find")) { // search default (google) due to keyword
      loc = "http://www.google.com/search?q=" + loc.substring("find".length()).trim().replaceAll(" ", "+");
    } else if (loc.startsWith("search")) { // search default (google) due to keyword
      loc = "http://www.google.com/search?q=" + loc.substring("search".length()).trim().replaceAll(" ", "+");
    } else if (loc.contains(" ")) { // search default (google) due to space
      loc = "http://www.google.com/search?q=" + loc.trim().replaceAll(" ", "+");
    } else if (!(loc.startsWith("http://") || loc.startsWith("https://")) && !loc.isEmpty()) {
      loc = "http://" + loc;  // default to http
    }

    // ask the webview to navigate to the given location.
    if (!loc.equals(getView().getEngine().getLocation())) {
      if (!loc.isEmpty()) {
        getView().getEngine().load(loc);
      } else {
        getView().getEngine().loadContent("");
      }
    } else {
      getView().getEngine().reload();
    }

    // webview will grab the focus if automatically if it has an html input control to display, but we want it
    // to always grab the focus and kill the focus which was on the input bar, so just set ask the platform to focus
    // the web view later (we do it later, because if we did it now, the default focus handling might kick in and override our request).
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        getView().requestFocus();
      }
    });
  }

  // create controls to monitor webview loading.
  public ProgressBar createLoadControl() {
    final Worker<Void> loadWorker = getView().getEngine().getLoadWorker();
    final ProgressBar progressBar = new ProgressBar();
    progressBar.setMaxWidth(Double.MAX_VALUE);
    ColorAdjust bleach = new ColorAdjust();
    bleach.setSaturation(-0.6);
    progressBar.setEffect(bleach);
    HBox.setHgrow(progressBar, Priority.ALWAYS);

    progressBar.visibleProperty().bind(loadWorker.runningProperty());

    // as the webview load progresses update progress.
    loadWorker.workDoneProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
        if (newNumber == null) newNumber = -1.0;
        final double newValue = newNumber.doubleValue();
        if (newValue < 0.0 || newValue > 100.0) {
          progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }
        progressBar.setProgress(newValue / 100.0);
      }
    });

    return progressBar;
  }

  /** @return a display to monitor status messages from the webview. */
  public HBox createStatusDisplay() {
    final HBox statusDisplay = new HBox();
    Text statusText = new Text();
    statusText.textProperty().bind(getStatus());
    HBox.setMargin(statusText, new Insets(1, 6, 3, 6));
    statusDisplay.setEffect(new DropShadow());
    statusDisplay.getStyleClass().add("status-background");
    statusDisplay.getChildren().add(statusText);
    statusDisplay.setVisible(false);
    statusText.textProperty().addListener(new ChangeListener<String>() {
      @Override public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        statusDisplay.setVisible(newValue != null && !newValue.equals(""));
      }
    });
    return statusDisplay;
  }

  public TextField getLocField() {
    return locField;
  }

  public StringProperty getStatus() {
    return status;
  }

  public ImageView getFavicon() {
    return favicon.get();
  }

  public ReadOnlyObjectProperty<ImageView> faviconProperty() {
    return favicon.getReadOnlyProperty();
  }

  public History getHistory() {
    return history;
  }

  public WebView getView() {
    return view;
  }

  /**
   * Overlay a dialog on top of the webview.
   * @param dialogNode the dialog to overlay on top of the view.
   */
  private void overlayView(Node dialogNode) {
    // if the view is already overlaid we will just ignore this overlay call silently . . . todo probably not the best thing to do, but ok for now.
    if (!(getView().getParent() instanceof BorderPane)) return;

    // record the view's parent.
    BorderPane viewParent = (BorderPane) getView().getParent();

    // create an overlayPane layering the popup on top of the webview
    StackPane overlayPane = new StackPane();
    overlayPane.getChildren().addAll(getView(), new Group(dialogNode));
    getView().setDisable(true);

    // overlay the popup on the webview.
    viewParent.setCenter(overlayPane);
  }
}

// todo cleanup the javascript prompt handlers as their code could be collapsed.
// todo log jira issue on browser load work progress not being updated.
// todo file rfe for icon support.
// todo cleanup history code.
// todo add better favicon support (not just for icos, but for pngs, etc.)
// todo how to set the save filename.
// todo file an jira bug request - modifying the list of items in a context menu makes the menus focus model go strange (doesn't appear to...)
// https://lh3.googleusercontent.com/BylHWQYWZPJtG8OHypz_rfWOEZS9nKh-96uCm-njWlS9vRxPIYOPJ-30XAdDf0U-_cfLz_S3Kg=s128-h128-e365
// http://www.mozilla.org/images/projects/firebug.png