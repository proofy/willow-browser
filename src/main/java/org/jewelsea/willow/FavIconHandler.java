package org.jewelsea.willow;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.sf.image4j.codec.ico.ICODecoder;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/* Helper class for handling favicons for sites */
public class FavIconHandler {
  /** @return singleton instance */
  public static FavIconHandler getInstance() { if (instance == null) instance = new FavIconHandler(); return instance; }
  private static FavIconHandler instance;

  /** least recently used cache of favicons */
  private HashMap<String, ImageView> faviconCache = new LruCache<String, ImageView>(50);

  /**
   * Fetch a favicon for a given location.
   * @param browserLoc the location of a browser for which a favicon is to be fetched.
   * @return the favicon for the browser location or null if no such favicon could be determined.
   */
  public ImageView fetchFavIcon(final String browserLoc) {
    String    faviconLoc = null;
    ImageView favicon    = null;

    try {
      final int protocolSepLoc = browserLoc.indexOf("://");
      if (protocolSepLoc > 0) {
        // workout the location of the favicon.
        final int pathSepLoc = browserLoc.indexOf("/", protocolSepLoc + 3);
        String rootLoc = (pathSepLoc > 0) ? browserLoc.substring(0, pathSepLoc) : browserLoc;
        faviconLoc = rootLoc + "/favicon.ico";

        // fetch the favicon from cache if it is there.
        favicon = faviconCache.get(faviconLoc);
        if (favicon != null) return favicon;

        // fetch the favicon from the server if we can.
        URL url = new URL(faviconLoc);
        List<BufferedImage> imgs = ICODecoder.read(url.openStream());
        if (imgs.size() > 0) {
          final Image fxImage = Util.bufferedImageToFXImage(imgs.get(0));
          favicon = new ImageView(fxImage);
          favicon.setFitHeight(16);
          favicon.setPreserveRatio(true);
        }
      }
    } catch (Exception e) {
      // no action required.
    }

    // if we determined a location where the favicon should be, save that in the cache,
    // the favicon will be null if we could not fetch the favicon from where we thought it might be.
    if (faviconLoc != null) {
      faviconCache.put(faviconLoc, favicon);
    }

    return favicon;
  }
  
}

// todo double check how pathing logic works for different string types.
// todo run the fetch async in a worker.
// todo think about other favicon types such as pngs, and jpgs and how they may be processed as well as favicons which could be specified inside html.