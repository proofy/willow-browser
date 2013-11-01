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

package org.jewelsea.willow.sidebar;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.Willow;
import org.jewelsea.willow.util.Util;

/**
 * Sidebar panel for showing demos
 */
public class DemoPanel extends TitledPane {
    final ContextMenu canvasMenu = new ContextMenu();

    static final String[] canvasBookmarks = {
            "http://www.zynaps.com/site/experiments/environment.html?mesh=bart.wft",
            "http://andrew-hoyer.com/experiments/cloth/",
            "http://hakim.se/experiments/html5/sketch/#35313167",
            "http://www.effectgames.com/demos/canvascycle/"
//    "http://www.kevs3d.co.uk/dev/lsystems/",        rendering of half the fractals here fails, so displayed it.
//    "http://www.openrise.com/lab/FlowerPower/",     flower power appears to have stopped working in later webview builds, so disabled it.
//    "http://mugtug.com/sketchpad/",                 sketchpad doesn't work too well, so disabled it.
//    "http://radikalfx.com/files/collage/demo.html"  collage is pretty boring, so disabled it.
    };

    public DemoPanel(final Willow chrome) {
        // create a canvas demos button.
        final Button canvasButton = Util.createIconButton(
                "Canvas Demos",
                "canvas.jpg",
                "Things of beauty",
                null
        );
        canvasButton.setOnAction(actionEvent ->
                canvasMenu.show(canvasButton, Side.BOTTOM, 0, 0)
        );
        for (String url : canvasBookmarks) {
            SideBar.createBookmark(chrome, canvasMenu, url);
        }

        // create a box for demos.
        VBox demoBox = new VBox();  // todo generalize this title stuff creation for sidebar items.
        demoBox.setSpacing(5);
        demoBox.setStyle("-fx-padding: 5");
        demoBox.getChildren().addAll(canvasButton);

        setText("Demos");
        setContent(demoBox);
        getStyleClass().add("sidebar-panel");
        setExpanded(false);
    }
}
