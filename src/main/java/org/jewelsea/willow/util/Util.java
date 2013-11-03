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
    /** turn an awt image into a JavaFX image. */
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

    /**
     * Copies an ImageView to a new ImageView, so that we can render multiple copies of the templated
     * ImageView in a scene.
     *
     * @param templateImageView an imageview containing an image and other import information to be copied.
     * @return a copy of the import parts of an ImageView
     */
    public static ImageView copyImageView(ImageView templateImageView) {
        ImageView xerox = new ImageView();
        xerox.setFitHeight(templateImageView.getFitHeight());
        xerox.setPreserveRatio(templateImageView.isPreserveRatio());
        xerox.imageProperty().bind(templateImageView.imageProperty());
        return xerox;
    }
}
