/*
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jlrodriguez@emergya.es">Juan Luís Rodríguez</a>
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
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
package es.emergya.ui.gis;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import es.emergya.cliente.constants.LogicConstants;

public class CustomTileSource {

	public static final String MAP_MAPNIK = LogicConstants.getTileUri();

	protected static abstract class AbstractOsmTileSource implements TileSource {
		private int maxzoom = -1, minzoom = -1;

		public int getMaxZoom() {
			if (maxzoom < 0)
				maxzoom = LogicConstants.getMaxTileZoom();
			return maxzoom;
		}

		public int getMinZoom() {
			if (minzoom < 0)
				minzoom = LogicConstants.getMinTileZoom();
			return minzoom;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public String toString() {
			return getName();
		}

		public String getTileType() {
			return "png";
		}

	}

	public static class Mapnik extends AbstractOsmTileSource {

		public static String NAME = "Mapnik";

		public String getName() {
			return NAME;
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.IfNoneMatch;
		}

	}
}
