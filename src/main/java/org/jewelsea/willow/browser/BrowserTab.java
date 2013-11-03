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

package org.jewelsea.willow.browser;

import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Tab associated with a browser window.
 */
public class BrowserTab extends Tab {
    /**
     * @return The browser window associated with this tab
     */
    public BrowserWindow getBrowser() {
        return browser;
    }

    private BrowserWindow browser = new BrowserWindow();

    public BrowserTab(TabManager tabManager) {
        // set the new browser to open any pop-up windows in a new tab.
        browser.getView().getEngine().setCreatePopupHandler(popupFeatures -> {
            final BrowserTab browserTab = new BrowserTab(tabManager);
            tabManager.addTab(browserTab);
            return browserTab.browser.getView().getEngine();
        });

        // put some dummy invisible content in the tab otherwise it doesn't show because it has no dimensions.
        Pane spacer = new StackPane();
        spacer.setMinWidth(TabManager.TAB_PANE_WIDTH + 35);
        spacer.setMaxWidth(TabManager.TAB_PANE_WIDTH + 35);
        setContent(spacer);

        // add the tab
        graphicProperty().bind(getBrowser().faviconProperty());
        getBrowser().getView().getEngine().titleProperty().addListener((observableValue, oldValue, newTitle) -> {
            // todo we already have a listener for the title, might want to repurpose it...
            // todo I wonder if the title would be reset correctly if the page has no title.
            if (newTitle != null && !"".equals(newTitle)) {
                setText(newTitle);
            }
        });
    }
}

