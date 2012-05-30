package org.jewelsea.willow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.sf.image4j.codec.ico.ICODecoder;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/* Helper class for handling favicons for sites */
public class FavIconHandler {
  /** @return singleton instance */
  public  static FavIconHandler  getInstance() { if (instance == null) instance = new FavIconHandler(); return instance; }
  private static FavIconHandler  instance;

  /** max number of threads we will use to simultaneously fetch favicons. */
  private static final int N_FETCH_THREADS = 4;

  /** prefix used for the favicon fetching thread. */
  public static final String FAVICON_FETCH_THREAD_PREFIX = "favicon-fetcher-";

  /** a threadpool for fetching favicons. */
  private final  ExecutorService threadpool;

  /** least recently used cache of favicons */
  private Map<String, ImageView> faviconCache =
    new ConcurrentHashMap<String, ImageView>(
      new LruCache<String, ImageView>(200)
    );

  /** constructor. */
  public FavIconHandler() {
    // initialize the favicon threadpool to the specified number of threads.
    // the name of the threads are customized so they are easy to recognize.
    // the status of the threads are set to daemon, so that the application can
    // exit even if a favicon fetch is in progress or stalled.
    threadpool = Executors.newFixedThreadPool(N_FETCH_THREADS, new ThreadFactory() {
      ThreadFactory defaultFactory = Executors.defaultThreadFactory(); 
      @Override public Thread newThread(Runnable r) {
        Thread newThread = defaultFactory.newThread(r);
        newThread.setName(FAVICON_FETCH_THREAD_PREFIX + newThread.getName());
        newThread.setDaemon(true);
        return newThread;
      }
    });
  }

  /**
   * Fetch a favicon for a given location.
   * @param browserLoc the location of a browser for which a favicon is to be fetched.
   * @return the favicon for the browser location or null if no such favicon could be determined.
   */
  public ImageView fetchFavIcon(final String browserLoc) {
    // fetch the favicon from cache if it is there.
    final String serverRoot = findRootLoc(browserLoc);
    ImageView cachedFavicon = faviconCache.get(serverRoot);
    if (cachedFavicon != null) return cachedFavicon;
    
    // ok, it wasn't in the cache, create a placeholder, to be used if the site doesn't have a favicon.
    final ImageView favicon = new ImageView();

    // if the serverRoot of the location cannot be determined, just return the placeholder.
    if (serverRoot == null) return favicon;
    
    // store the new favicon placeholder in the cache.
    faviconCache.put(serverRoot, favicon);
    
    // lazily fetch the real favicon.
    final Task<Image> task = new Task<Image>() {
      @Override protected Image call() throws Exception {
        // fetch the favicon from the server if we can.
        URL url = new URL(serverRoot + "/favicon.ico");

        // decode the favicon into an awt image.
        List<BufferedImage> imgs = ICODecoder.read(url.openStream());

        // if the decoding was successful convert to a JavaFX image and return it.
        if (imgs.size() > 0) {
          return Util.bufferedImageToFXImage(imgs.get(0), 0, 16, true, true);
        } else {
          return null;
        }
      }
    };

    // replace the placeholder in a favicon whenever the lazy fetch completes.
    task.valueProperty().addListener(new ChangeListener<Image>() {
      @Override public void changed(ObservableValue<? extends Image> observableValue, Image oldImage, Image newImage) {
        if (newImage != null) {
          favicon.setImage(newImage);
        }  
      }
    });

    threadpool.execute(task);

    return favicon;
  }

  /**
   * Determines the root location for a server.
   * For example http://www.yahoo.com/games => http://www.yahoo.com
   * @param browserLoc the location string of a browser window.
   * @return the computed server root url or null if the browser location does not represent a server.
   */    
  private String findRootLoc(String browserLoc) {
    final int protocolSepLoc = browserLoc.indexOf("://");
    if (protocolSepLoc > 0) {
      // workout the location of the favicon.
      final int pathSepLoc = browserLoc.indexOf("/", protocolSepLoc + 3);
      return (pathSepLoc > 0) ? browserLoc.substring(0, pathSepLoc) : browserLoc;
    }
    
    return null;
  }
}

// todo think about other favicon types such as pngs, and jpgs and how they may be processed as well as favicons which could be specified inside html.