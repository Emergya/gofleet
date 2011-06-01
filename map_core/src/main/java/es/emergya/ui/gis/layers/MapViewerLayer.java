/*
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jlrodriguez@emergya.es">Juan Luís Rodríguez</a>
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
 * @author <a href="mailto:fario@emergya.es">Félix del Río Beningno</a>
 *
 * This file is part of GoFleet
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
/*
 * 01/07/2009
 */
package es.emergya.ui.gis.layers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.gis.CustomMapView;

/**
 * Tries to add a raster tiles layer. Uses the code on {@link JMapViewer} by Jan
 * Peter Stotz
 * 
 * @author fario
 * 
 */
public class MapViewerLayer extends Layer implements TileLoaderListener {

	private static final Log LOG = LogFactory.getLog(MapViewerLayer.class);
	/**
	 * Vectors for clock-wise tile painting
	 */
	protected static final Point[] move = { new Point(1, 0), new Point(0, 1),
			new Point(-1, 0), new Point(0, -1) };
	protected OsmTileLoader tileLoader;
	protected TileCache tileCache;
	protected TileSource tileSource;
	protected boolean tileGridVisible;
	/**
	 * x- and y-position of the center of this map-panel on the world map
	 * denoted in screen pixel regarding the current zoom level.
	 */
	protected Point center;
	/**
	 * Current zoom level
	 */
	protected int zoom;
	JobDispatcher jobDispatcher;
	protected CustomMapView parent;

	/**
	 * @param name
	 */
	public MapViewerLayer(String name) {
		this(name, new MemoryTileCache(), 4);
	}

	public MapViewerLayer(String name, TileCache tileCache,
			int downloadThreadCount) {
		this(name, new OsmTileSource.Mapnik(), tileCache, downloadThreadCount);
	}

	public MapViewerLayer(String name, TileSource tileSource,
			TileCache tileCache, int downloadThreadCount) {
		super(name);
		if (tileCache instanceof MemoryTileCache)
			((MemoryTileCache) tileCache).setCacheSize(LogicConstants.getInt("MAXTILESCACHED", 50));
		this.tileSource = tileSource;
		tileLoader = new OsmTileLoader(this);
		this.tileCache = tileCache;
		jobDispatcher = JobDispatcher.getInstance();
		tileGridVisible = false;
		this.background = true;
	}

	public MapViewerLayer(String name, TileSource tileSource,
			TileCache tileCache, boolean osmFileCache) {
		super(name);
		this.tileSource = tileSource;
		if (osmFileCache) {
			this.tileLoader = new OsmFileCacheTileLoader(this);
		} else {
			this.tileLoader = new OsmTileLoader(this);
		}
		this.tileCache = tileCache;
		jobDispatcher = JobDispatcher.getInstance();
		tileGridVisible = false;
		this.background = true;
	}

	@Override
	public void paint(Graphics g, MapView mv) {
		parent = (CustomMapView) mv;

		int iMove = 0;

		zoom = mv.zoom();
		LatLon cen = Main.proj.eastNorth2latlon(mv.getCenter());

		center = new Point(OsmMercator.LonToX(cen.getX(), zoom), OsmMercator
				.LatToY(cen.getY(), zoom));

		int tilex = center.x / Tile.SIZE;
		int tiley = center.y / Tile.SIZE;
		int off_x = (center.x % Tile.SIZE);
		int off_y = (center.y % Tile.SIZE);

		int w2 = mv.getWidth() / 2;
		int h2 = mv.getHeight() / 2;
		int posx = w2 - off_x;
		int posy = h2 - off_y;

		int diff_left = off_x;
		int diff_right = Tile.SIZE - off_x;
		int diff_top = off_y;
		int diff_bottom = Tile.SIZE - off_y;

		boolean start_left = diff_left < diff_right;
		boolean start_top = diff_top < diff_bottom;

		if (start_top) {
			if (start_left) {
				iMove = 2;
			} else {
				iMove = 3;
			}
		} else {
			if (start_left) {
				iMove = 1;
			} else {
				iMove = 0;
			}
		} // calculate the visibility borders
		int x_min = -Tile.SIZE;
		int y_min = -Tile.SIZE;
		int x_max = mv.getWidth();
		int y_max = mv.getHeight();

		// paint the tiles in a spiral, starting from center of the map
		boolean painted = true;
		int x = 0;
		while (painted) {
			painted = false;
			for (int i = 0; i < 4; i++) {
				if (i % 2 == 0) {
					x++;
				}
				for (int j = 0; j < x; j++) {
					if (x_min <= posx && posx <= x_max && y_min <= posy
							&& posy <= y_max) {
						// tile is visible
						Tile tile = getTile(tilex, tiley, zoom);
						if (tile != null) {
							painted = true;
							tile.paint(g, posx, posy);
							if (tileGridVisible) {
								g.drawString(tile.getXtile() + ", "
										+ tile.getYtile(), posx, posy + 12);
								g.drawRect(posx, posy, Tile.SIZE, Tile.SIZE);
							}
						}
					}
					Point p = move[iMove];
					posx += p.x * Tile.SIZE;
					posy += p.y * Tile.SIZE;
					tilex += p.x;
					tiley += p.y;
				}
				iMove = (iMove + 1) % move.length;
			}
		}
		// outer border of the map
		int mapSize = Tile.SIZE << zoom;
		g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);

		if (LOG.isDebugEnabled()) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 15);
		}

		// g.fillRect(0, 100, 300, 55);
		// g.setColor(Color.BLACK);
		// g.drawString("center: " + center.x + ", " + center.y, 5, 120);
		// g.drawString("zoom: " + zoom + " for scale:" + mv.getScale(), 5,
		// 150);
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
	}

	/**
	 * retrieves a tile from the cache. If the tile is not present in the cache
	 * a load job is added to the working queue of
	 * {@link JobDispatcher.JobThread}.
	 * 
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 * @return specified tile from the cache or <code>null</code> if the tile
	 *         was not found in the cache.
	 */
	protected Tile getTile(int tilex, int tiley, int zoom) {
		int max = (1 << zoom);
		if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max) {
			return null;
		}
		Tile tile = tileCache.getTile(tileSource, tilex, tiley, zoom);
		if (tile == null) {
			tile = new Tile(tileSource, tilex, tiley, zoom);
			tileCache.addTile(tile);
			tile.loadPlaceholderFromCache(tileCache);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("Numero de intentos para " + tile.getKey() + ": "
					+ tile.getNumIntentos());
		}
		if (!tile.isLoaded() && tile.getNumIntentos() < Tile.MAX_NUM_INTENTOS) {
			jobDispatcher.addJob(tileLoader.createTileLoaderJob(tileSource,
					tilex, tiley, zoom));
		}
		return tile;
	}

	@Override
	public TileCache getTileCache() {
		return tileCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener#
	 * tileLoadingFinished(org.openstreetmap.gui.jmapviewer.Tile, boolean)
	 */
	@Override
	public void tileLoadingFinished(Tile tile, boolean success) {
		if (success) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					parent.repaint();
				}
			});
		}
	}

	/**
	 * @return The maximum zoom level that the associated tilesource offers
	 */
	public int getMaxZoomLevel() {
		return tileSource.getMaxZoom();
	}

	/**
	 * @return The minimum zoom level that the associated tilesource offers
	 */
	public int getMinZoomLevel() {
		return tileSource.getMinZoom();
	}

	// === Methods to add the editor user interface, that we won't use ===
	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public Object getInfoComponent() {
		return null;
	}

	@Override
	public Component[] getMenuEntries() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	// ======
	// === We wont let them merge with the background ===

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {
	}
	// ======
}
