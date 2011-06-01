package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM via HTTP.
 * 
 * @author Jan Peter Stotz
 * @author Maria Arias de Reyna
 */
public class OsmTileLoader implements TileLoader {

    private static final Log LOG = LogFactory.getLog(OsmTileLoader.class);
    /**
     * Holds the used user agent used for HTTP requests. If this field is
     * <code>null</code>, the default Java user agent is used.
     */
    public static String USER_AGENT = null;
    public static String ACCEPT = "text/html, image/png, image/jpeg, image/gif, */*";
    protected TileLoaderListener listener;

    //number of tiles per axis
    protected int tilesX;
    protected int tilesY;

    //origin total boundingbox
    protected int minX;
    protected int minY;
    
    public OsmTileLoader(TileLoaderListener listener) {
        this.listener = listener;
    }

    @Override
    public Runnable createTileLoaderJob(final TileSource source,
            final int tilex, final int tiley, final int zoom) {
        return new Runnable() {

            InputStream input = null;

            @Override
            public void run() {
                TileCache cache = listener.getTileCache();
                Tile tile;
                synchronized (cache) {
                    tile = cache.getTile(source, tilex, tiley, zoom);
                    if (tile == null || tile.isLoaded() || tile.loading) {
                        return;
                    }
                    tile.loading = true;
                }
                try {
                    // Thread.sleep(500);
                    input = loadTileFromOsm(tile);
                    tile.loadImage(input);
                    tile.setLoaded(true);
                    listener.tileLoadingFinished(tile, true);
                    input.close();
                    input = null;
                    if (tile.getNumIntentos() >= Tile.MAX_NUM_INTENTOS) {
                        throw new TileLoadingException("Número de intentos superados para la tile " + tile);
                    }
                } catch (Exception e) {
                    tile.setImage(Tile.ERROR_IMAGE);
                    listener.tileLoadingFinished(tile, false);
                    if (input == null) {
                        LOG.error("failed loading " + zoom + "/" + tilex + "/"
                                + tiley + " " + e.getMessage());
                    }
                } finally {
                    tile.loading = false;
                    tile.setLoaded(true);
                }
            }
        };
    }

    protected InputStream loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(tile.getUrl());
        if (url.getProtocol().equals("file") || url.getProtocol().isEmpty()) {
            InputStream is = new FileInputStream(tile.getUrl());
            return is;
        }
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        prepareHttpUrlConnection(urlConn);
        urlConn.setReadTimeout(30000); // 30 seconds read timeout
        return urlConn.getInputStream();
    }

    protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
        if (USER_AGENT != null) {
            urlConn.setRequestProperty("User-agent", USER_AGENT);
        }
        urlConn.setRequestProperty("Accept", ACCEPT);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

	public int getTilesX() {
		return tilesX;
	}

	public void setTilesX(int tilesX) {
		this.tilesX = tilesX;
	}

	public int getTilesY() {
		return tilesY;
	}

	public void setTilesY(int tilesY) {
		this.tilesY = tilesY;
	}
}
