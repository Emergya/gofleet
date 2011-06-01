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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.emergya.ui.gis;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Parser;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource.TileUpdate;
import org.openstreetmap.josm.Main;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import es.emergya.ui.gis.CustomTileSource.AbstractOsmTileSource;

/**
 * 
 * @author jlrodriguez
 */
public class WMS2TileSource extends AbstractOsmTileSource {

	static MathTransform transform;

	static {
		try {
			Parser parser = new Parser();

			String googleWkt = "PROJCS[\"unnamed\","
					+ "GEOGCS[\"unnamed ellipse\"," + "DATUM[\"unknown\","
					+ "SPHEROID[\"unnamed\",6378137,0]],"
					+ "PRIMEM[\"Greenwich\",0],"
					+ "UNIT[\"degree\",0.0174532925199433]],"
					+ "PROJECTION[\"Mercator_2SP\"],"
					+ "PARAMETER[\"standard_parallel_1\",0],"
					+ "PARAMETER[\"central_meridian\",0],"
					+ "PARAMETER[\"false_easting\",0],"
					+ "PARAMETER[\"false_northing\",0]," + "UNIT[\"Meter\",1]"
					+ "]";

			CoordinateReferenceSystem epsg900913 = org.geotools.referencing.FactoryFinder
					.getCRSFactory(null).createFromWKT(googleWkt);

			transform = CRS.findMathTransform(epsg900913, CRS
					.decode("EPSG:4326"), true);
		} catch (NoSuchAuthorityCodeException ex) {
			Logger.getLogger(WMS2TileSource.class.getName()).log(Level.SEVERE,
					null, ex);

		} catch (FactoryException ex) {
			Logger.getLogger(WMS2TileSource.class.getName()).log(Level.SEVERE,
					null, ex);

		}
	}
	private final static Log log = LogFactory.getLog(WMS2TileSource.class);
	private final static String NAME = "WMS2TileSource";
	private String baseUrl;
	private GlobalMercator globalMercator;

	public WMS2TileSource(String baseUrl) {
		this.baseUrl = baseUrl;
		this.globalMercator = new GlobalMercator(256);
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		try {
			TileCoordinates googleTile = globalMercator.googleTile(tilex,
					tiley, zoom);
			// MercatorBoundingBox tileBounds =
			// globalMercator.tileBounds(googleTile.getTx(), googleTile.getTy(),
			// zoom);
			// LatLongBoundingBox latLon =
			// globalMercator.tileLatLongBounds(tilex, tiley, zoom);
			MercatorBoundingBox tileBounds = globalMercator.tileBounds(tilex,
					tiley, zoom);
			Envelope env = new com.vividsolutions.jts.geom.Envelope(
					new Coordinate(tileBounds.getMinX(), tileBounds.getMinY()),
					new Coordinate(tileBounds.getMaxX(), tileBounds.getMaxY()));
			Envelope transform1 = JTS.transform(env, transform);
			LatLongBoundingBox latlon = new LatLongBoundingBox(transform1
					.getMinX(), transform1.getMinY(), transform1.getMaxX(),
					transform1.getMaxY());
			BoundingBox bb = new BoundingBox(tilex, tiley, zoom);

			if (baseUrl.contains("{0}")) {
				return urlFromPattern(latlon);
			} else {
				return url(latlon);
			}
		} catch (TransformException ex) {
			Logger.getLogger(WMS2TileSource.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return null;
	}

	private String urlFromPattern(LatLongBoundingBox bb) {
		String proj = Main.proj.toCode();
		if (Main.proj instanceof org.openstreetmap.josm.data.projection.Mercator) // don't
																					// use
																					// mercator
																					// code
																					// directly
		{
			proj = "EPSG:4326";
		}

		log.trace("TileUrl: "
				+ MessageFormat.format(baseUrl, proj, bb.toString(), 256, 256));
		return MessageFormat.format(baseUrl, proj, bb.toString(), 256, 256);
	}

	private String url(LatLongBoundingBox bb) {
		StringBuilder b = new StringBuilder(baseUrl);
		b.append("&");
		b.append("BBOX=").append(bb.toString());
		b.append("&");
		b.append("WIDTH=").append(256);
		b.append("&");
		b.append("HEIGHT=").append(256);

		log.debug("TileUrl: " + b);
		return b.toString();
	}

	@Override
	public String getName() {
		return WMS2TileSource.NAME;
	}

	@Override
	public TileUpdate getTileUpdate() {
		return TileUpdate.None;
	}
}
