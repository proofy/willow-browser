package org.jewelsea.willow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/** manages history for a browser tab. */
public class History {
  private static final int MAX_HISTORY_SIZE = 100; // max number of locations we will store in the history.

  // history items - higher indexed items are later in the history.
  private final ObservableList<String> items = FXCollections.observableArrayList();
  private       int     pointer = 0;        // index into the history list for the currently displayed page from the history.
  private       Integer navPointer = null;  // index into the history list for a new page to be displayed page in the history.
  private final BrowserWindow browser;      // browser window (contains WebView) managed by this history.

  /** create a new history tracker for the given browser window */
  public History(BrowserWindow browser) {
    this.browser = browser;
  }

  /** @return true if the current browser location is not at the end of the history list. */
  public boolean canNavForward() { return pointer < items.size() - 1; }
  /** @return true if the current browser location is not at the beginning of the history list. */
  public boolean canNavBack()    { return pointer > 0; }

  /**
   * @return the location of the provided history index
   *          or the current location if the provided index is out of the current history index range
   */
  public String requestNav(Integer index) {
    if (index >= 0 && index <= items.size())  {
      this.navPointer = index;
    }

    return items.get(navPointer);
  }

  /** @return the next location in the history list or the current location if there is no such element */
  public String requestNavForward() {
    if (canNavForward()) {
      navPointer = pointer + 1;
    }

    return items.get(navPointer);
  }

  /** @return the previous location in the history list or the current location if there is no such element */
  public String requestNavBack() {
    if (canNavBack()) {
      navPointer = pointer - 1;
    }

    return items.get(navPointer);
  }

  /** updates the history list to reflect a navigation to the given location. */
  public void executeNav(String newLoc) { // todo add some validation that this is the request nav, so that we ensure all updates occur correctly.
    if (navPointer == null) { // standard navPointer.
      if (pointer < items.size() - 1) { // wipe any forward button history.
        items.remove(pointer + 1, items.size());
      }
      items.add(newLoc);
      if (items.size() >= MAX_HISTORY_SIZE) {
        items.remove(0);
      }
      pointer = items.size() - 1;
    } else { // navPointer using history list.
      pointer = navPointer;
      navPointer = null;
    }
  }

  /**
   * Show a history menu when the user right clicks.
   * @param displayNode the node under which the history menu is to be displayed.
   * @return a right click mouse button event handler which will show a history context menu.
   */
  public EventHandler<MouseEvent> createShowHistoryMouseEvent(final Node displayNode) {
    return new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
          createMenu().show(displayNode, Side.BOTTOM, 0, 0); // show the history menu below the provided node (back button).
        }
      }
    };
  }

  /**
   * Show a history menu when the clicks a history button
   * @param displayNode the node under which the history menu is to be displayed.
   * @return an action event handler which will show a history context menu.
   */
  public EventHandler<ActionEvent> createShowHistoryActionEvent(final Node displayNode) {
    return new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        createMenu().show(displayNode, Side.BOTTOM, 0, 0); // show the history menu below the provided node (history button).
      }
    };
  }

  /** @return a new context menu for a range of history items. */
  private ContextMenu createMenu() {
    // a menu of history items.
    final ContextMenu historyMenu = new ContextMenu();

    // determine an appropriate subset range of the history list to display.
    int minIdx = Math.max(0,            pointer - 8); // min range (inclusive) of history items to show.
    int maxIdx = Math.min(items.size(), pointer + 6); // min range (exclusive) of history items to show.

    // add menu items to the history list.
    for (int i = maxIdx - 1; i >= minIdx; i--) {
      final MenuItem nextMenu = createMenuItem(items.get(i), i);
      historyMenu.getItems().add(nextMenu);
      if (i == pointer) {
        nextMenu.getStyleClass().add("current-menu");
      }
    }

    return historyMenu;
  }

  /**
   * Create a new history menu item.
   * @param loc the location the new menu item is to navigate to.
   * @param navPointer the index in the history list at which the location is located.
   * @return a menu item.
   */
  private MenuItem createMenuItem(final String loc, final int navPointer) {
    final MenuItem nextMenuItem = new MenuItem(loc);
    nextMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        browser.navTo(requestNav(navPointer));
      }
    });
    return nextMenuItem;
  }
}
