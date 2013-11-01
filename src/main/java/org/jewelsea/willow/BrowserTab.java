package org.jewelsea.willow;

import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/** Tab associated with a browser window */
public class BrowserTab extends Tab {
    /** @return the browser window associated with this tab */
    public BrowserWindow getBrowser() {
        return browser; }
    private BrowserWindow browser = new BrowserWindow();
    public BrowserTab(TabManager tabManager) {
      super(""); // todo if we don't have some value set in here, we can never set the tab value (jira?)

      // set the new browser to open any pop-up windows in a new tab.
      browser.getView().getEngine().setCreatePopupHandler(popupFeatures -> {
        final BrowserTab browserTab = new BrowserTab(tabManager);
        tabManager.addTab(browserTab);
        return browserTab.browser.getView().getEngine();
      });

      // put some dummy invisible content in the tab otherwise it doesn't show because it has no dimensions.  // todo file jira?
      Pane spacer = new StackPane();
      spacer.setMinWidth(TabManager.TAB_PANE_WIDTH + 35);
      spacer.setMaxWidth(TabManager.TAB_PANE_WIDTH + 35);
      setContent(spacer);

      // add the tab
      graphicProperty().bind(getBrowser().faviconProperty());
      getBrowser().getView().getEngine().titleProperty().addListener((observableValue, oldValue, newTitle) -> {
       // todo we already have a listener for the title, might want to repurpose it...
        if (newTitle != null && !"".equals(newTitle)) {  // todo I wonder if the title would be reset correctly if the page has no title.
          setText(newTitle);
        }
      });
    }
}

