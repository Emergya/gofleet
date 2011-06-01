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
 * 14/07/2009
 */
package es.emergya.ui.gis.layers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;

import es.emergya.ui.gis.CustomMapView;
import es.emergya.ui.gis.RotatableView;

/**
 * @author fario
 * 
 */
public class RotatableMapViewLayer extends MapViewerLayer {

	/**
	 * @param name
	 * @param tileCache
	 * @param downloadThreadCount
	 */
	public RotatableMapViewLayer(String name, TileCache tileCache,
			int downloadThreadCount) {
		super(name, tileCache, downloadThreadCount);

	}

	/**
	 * @param name
	 * @param tileSource
	 * @param tileCache
	 * @param downloadThreadCount
	 */
	public RotatableMapViewLayer(String name, TileSource tileSource,
			TileCache tileCache, int downloadThreadCount) {
		super(name, tileSource, tileCache, downloadThreadCount);
	}

	/**
	 * @param name
	 */
	public RotatableMapViewLayer(String name) {
		super(name);
	}

	public RotatableMapViewLayer(String name, TileSource tileSource,
			TileCache tileCache, boolean osmFileCache) {
		super(name, tileSource, tileCache, osmFileCache);
	}

	@Override
	public void paint(Graphics gr, MapView mv) {
		parent = (CustomMapView) mv;
		Rectangle bbox = parent.getBoundingBox();
		double angle = 0;
		if (mv instanceof RotatableView)
			angle = ((RotatableView) mv).getAngle();

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
			if (start_left)
				iMove = 2;
			else
				iMove = 3;
		} else {
			if (start_left)
				iMove = 1;
			else
				iMove = 0;
		} // calculate the visibility borders
		// bbox.x += 250;
		// bbox.y += 250;
		// bbox.width -= 500;
		// bbox.height -= 500;
		int x_min = bbox.x - Tile.SIZE;
		int y_min = bbox.y - Tile.SIZE;
		int x_max = bbox.x + bbox.width;
		int y_max = bbox.y + bbox.height;

		// Apply a rotation transform to the graphics
		Graphics2D g = (Graphics2D) gr;
		g.rotate(-angle, mv.getWidth() / 2, mv.getHeight() / 2);

		// paint the tiles in a spiral, starting from center of the map
		boolean painted = true;
		int ntiles = 0;
		int x = 0;
		while (painted) {
			painted = false;
			for (int i = 0; i < 4; i++) {
				if (i % 2 == 0)
					x++;
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
						ntiles++;
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
		// g.setColor(Color.RED);
		// g.drawRect(bbox.x, bbox.y, bbox.width, bbox.height);

		g.rotate(angle, mv.getWidth() / 2, mv.getHeight() / 2);

		g.setColor(Color.DARK_GRAY);
		g.drawString("Tiles in cache: " + tileCache.getTileCount()
				+ " - Tiles drawn: " + ntiles, 50, 15);
	}
}