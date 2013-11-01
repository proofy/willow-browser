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

package org.jewelsea.willow.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.Willow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Util {
    // create a button with an icon
    static public Button createIconButton(String buttonText, String imageLoc, String tooltipText, EventHandler<ActionEvent> actionEventHandler) {
        final Button button = new Button(buttonText);
        button.setTooltip(new Tooltip(tooltipText));
        button.getStyleClass().add("icon-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        final ImageView imageView = new ImageView(getImage(imageLoc));
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);
        button.setGraphic(imageView);
        button.setContentDisplay(ContentDisplay.LEFT);
        VBox.setMargin(button, new Insets(0, 5, 0, 5));
        button.setOnAction(actionEventHandler);

        return button;
    }

    // turn an awt image into a JavaFX image.
    public static javafx.scene.image.Image bufferedImageToFXImage(java.awt.Image image, double width, double height, boolean resize, boolean smooth) throws IOException {
        if (!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage =
                    new BufferedImage(
                            image.getWidth(null),
                            image.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB
                    );
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = bufferedImage;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in, width, height, resize, smooth);
    }

    /** get a resource relative to the application class. */
    static String getResource(String path) {
        return Willow.class.getResource(path).toExternalForm();
    }

    /** get a image resource in an images/ path relative to the application class. */
    public static Image getImage(String imageFilename) {
        return new Image(Util.getResource("images/" + imageFilename));
    }

    // debugging routine to dump the scene graph.
    public static void dump(Node n) {
        dump(n, 0);
    }

    private static void dump(Node n, int depth) {
        for (int i = 0; i < depth; i++) System.out.print("  ");
        System.out.println(n);
        if (n instanceof Parent) for (Node c : ((Parent) n).getChildrenUnmodifiable()) dump(c, depth + 1);
    }

    // debugging routine to highlight the borders of nodes.
    public static void highlight(Node n) {
        highlight(n, 0);
    }

    private static void highlight(Node n, int depth) {
        n.setStyle("-fx-stroke: red; -fx-stroke-width: 1; -fx-stroke-type: inside;");
        if (n instanceof Parent) for (Node c : ((Parent) n).getChildrenUnmodifiable()) highlight(c, depth + 1);
    }

}
