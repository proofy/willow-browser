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

package org.jewelsea.willow.navigation;

import javafx.application.Platform;
import javafx.scene.web.WebView;

/**
 * Navigates a WebView to a provided location.
 */
public class NavigationHandler {

    private final WebView webView;

    public NavigationHandler(WebView webView) {
        this.webView = webView;
    }

    /**
     * Navigates a WebView to a provided location.
     *
     * In addition to navigating to full URLs, this routine provides a shorthand for certain navigation actions.
     *
     * For example:
     *   a loc of "google <text>" will map to a google search.
     *   a loc of "bing <text>" will map to a bing search.
     *   etc.
     *
     * @param loc the location the webview is to navigate to.
     */
    public void navTo(String loc) {
        // modify the request location, to make it easier on the user for typing.
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
        if (!loc.equals(webView.getEngine().getLocation())) {
            if (!loc.isEmpty()) {
                webView.getEngine().load(loc);
            } else {
                webView.getEngine().loadContent("");
            }
        } else {
            webView.getEngine().reload();
        }

        // webview will grab the focus if automatically if it has an html input control to display, but we want it
        // to always grab the focus and kill the focus which was on the input bar, so just set ask the platform to focus
        // the web view later (we do it later, because if we did it now, the default focus handling might kick in and override our request).
        Platform.runLater(webView::requestFocus);
    }

}
