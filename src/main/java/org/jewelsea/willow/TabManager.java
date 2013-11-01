package org.jewelsea.willow;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;

/**
 * Manages a set of active browser windows
 */
public class TabManager {

    public static final double TAB_PANE_WIDTH = 400;
    /**
     * representation of the current browser.
     */
    final private ReadOnlyObjectWrapper<BrowserWindow> browser = new ReadOnlyObjectWrapper<>();
    /**
     * browser tabs.
     */
    final private TabPane tabPane = new TabPane();
    /**
     * button to open a new tab
     */
    final private Button newTabButton = new Button();
    /**
     * a location field in the chrome representing the location of the current tab
     * (can be null if the location is not represented in the chrome but only in the browser in the tab itself).
     */
    final private TextField chromeLocField;

    public TabManager() {
        this(null);
    }

    public TabManager(TextField locField) {
        this.chromeLocField = locField;

        // create a browser tab pane with a custom tab closing policy which does not allow the last tab to be closed.
        tabPane.setTabMinWidth(50);
        tabPane.setTabMaxWidth(TAB_PANE_WIDTH);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            final ObservableList<Tab> tabs = tabPane.getTabs();
            tabs.get(0).setClosable(tabs.size() > 1);
            for (int i = 1; i < tabs.size(); i++) {
                tabs.get(i).setClosable(true);
            }
            tabPane.setTabMaxWidth(Math.max(50, TAB_PANE_WIDTH / Math.max(1, tabPane.getTabs().size() * 0.7)));  // todo work out a good max width // todo file jira setting max width on a initialTab pane is buggy as the close symbol is not usable if you change initialTab from closable to not closable. // todo file jira on initialTab pane set policy for closing icon display.
        });

        // monitor the selected tab in the tab pane so that we can set the TabManager's browser property appropriately.
        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) ->
                browser.set(((BrowserTab) newTab).getBrowser())
        );

        // add the initialTab to the tabset.
        addTab(new BrowserTab(this));

        // create a button for opening a new tab.
        newTabButton.setTooltip(new Tooltip("Tabulate"));
        final ImageView tabGraphic = new ImageView(new Image(Util.getResource("Plus.png")));
        final ColorAdjust tabColorAdjust = new ColorAdjust();
        tabColorAdjust.setContrast(-0.7);
        tabGraphic.setEffect(tabColorAdjust);
        tabGraphic.setPreserveRatio(true);
        tabGraphic.setFitHeight(14);
        newTabButton.setGraphic(tabGraphic);
        newTabButton.onActionProperty().set(actionEvent -> {
            final BrowserTab newTab = new BrowserTab(this);
            newTab.setText("New Tab");
            addTab(newTab);
        });
    }

    public BrowserWindow getBrowser() {
        return browser.get();
    }

    public ReadOnlyObjectProperty<BrowserWindow> browserProperty() {
        return browser.getReadOnlyProperty();
    }

    /**
     * @return the tabs which control the active browser window.
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * @return a button for opening a new tab.
     */
    public Button getNewTabButton() {
        return newTabButton;
    }

    void addTab(BrowserTab tab) {
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
        if (chromeLocField != null) {
            chromeLocField.requestFocus();
        }
    }

}
