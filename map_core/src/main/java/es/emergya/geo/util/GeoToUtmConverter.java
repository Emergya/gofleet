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
package es.emergya.geo.util;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.tools.ExtensionClassLoader;

/**
 * Utility class for transform geographical coordinates to UTM reference system.
 * It reads parameters from utm.properties file.
 * 
 * @author jlrodriguez
 * 
 */
public class GeoToUtmConverter {
	/** Logger. */
	private static final Log LOG = LogFactory.getLog(GeoToUtmConverter.class);
	private static final Properties props;
	private static final String utmProperties = "/conf/utm.properties";
	/** Hemisphere property key. */
	private static final String HEMISPHERE_KEY = "geoToUtmConverter.hemisphere";
	/** UTM zone property key. */
	private static final String UTM_ZONE_KEY = "geoToUtmConverter.zone";
	/** Ellipsoid property key. */
	private static final String ELLIPSOID_KEY = "geoToUtmConverter.ellipsoid";
	/** Default UTM zone. */
	private static final String DEFAULT_ZONE = "30";
	/** Default ellipsoid. */
	private static final String DEFAULT_ELLIPSOID = "WSG84";
	/** Default hemisphere. */
	private static final String DEFAULT_HEMISPHERE = "N";

	static {
		props = new Properties();
		// load and read properties
		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			InputStream is = ecl.getResourceAsStream(GeoToUtmConverter.utmProperties);
			if (is == null)
				is = GeoToUtmConverter.class.getResourceAsStream(GeoToUtmConverter.utmProperties);
			GeoToUtmConverter.props.load(is);
		} catch (FileNotFoundException fnf) {
			GeoToUtmConverter.LOG.error("Fichero de configuarcion " + GeoToUtmConverter.utmProperties
					+ " no encontrado", fnf);
		} catch (IOException e) {
			GeoToUtmConverter.LOG.error("No se pudo cargar el fichero de propiedades "
					+ GeoToUtmConverter.utmProperties, e);
		} catch (Exception e) {
			GeoToUtmConverter.LOG.error("NO se pudo cargar el fichero de propiedades "
					+ GeoToUtmConverter.utmProperties, e);
		}
	}

	/** Private constructor. */
	private GeoToUtmConverter() {

	}

	/**
	 * Transform a pair latitude, longitude in UTM coordinates. The zone is
	 * defined in utm.properties
	 * 
	 * @param latitude
	 *            latitude in degrees. North latitudes are positives and South
	 *            latitudes are negatives.
	 * @param longitude
	 *            longitude in degrees. East longitudes are positives and West
	 *            longitudes are negatives.
	 * @return a point where x is longitude and y is latitude.
	 */
	public static Point2D toUTM(final double latitude, final double longitude) {
//
//		Ellipsoid ellipsoid;
//		Ellipsoid targetEllipsoid;
//		int hemisphere;
//		int zone;
//
//		String hemisphereProp = GeoToUtmConverter.props.getProperty(GeoToUtmConverter.HEMISPHERE_KEY,
//				GeoToUtmConverter.DEFAULT_HEMISPHERE);
//		String zoneProp = GeoToUtmConverter.props.getProperty(GeoToUtmConverter.UTM_ZONE_KEY, GeoToUtmConverter.DEFAULT_ZONE);
//		String ellipsoidProp = GeoToUtmConverter.props.getProperty(GeoToUtmConverter.ELLIPSOID_KEY,
//				GeoToUtmConverter.DEFAULT_ELLIPSOID);
//
//		if (ellipsoidProp.toUpperCase().equals("WGS84")) {
//			ellipsoid = Ellipsoid.wgs84;
//			targetEllipsoid = Ellipsoid.ed50;
//		} else {
//			ellipsoid = Ellipsoid.ed50;
//			targetEllipsoid = Ellipsoid.wgs84;
//		}
//
//		zone = Integer.valueOf(zoneProp);
//		if (hemisphereProp.toUpperCase().equals("N"))
//			hemisphere = Projection.NORTH;
//		else
//			hemisphere = Projection.SOUTH;
//
//		// Do the transformation.
//		// UtmZone utm = new UtmZone(ellipsoid, zone, hemisphere, 0.0d);
//
//		GeodesicPosition original = new GeodesicPosition(Math
//				.toRadians(longitude), Math.toRadians(latitude), 0.0d);
//		// GeodesicPosition result = transform(original, 6378137,
//		// 1/298.257223563d,
//		// 6.69437999014E-3, 6377563.396-6378137.0,
//		// 1/297 - 1/298.257223563 , -87.0d, -98.0d, -121.0d);
//		GeodesicPosition result = GeoToUtmConverter.transform(original, Ellipsoid.wgs84
//				.getESemiMajorAxis(), 1.0d / Ellipsoid.wgs84.getEIFlattening(),
//				Ellipsoid.wgs84.e2, Ellipsoid.ed50.getESemiMajorAxis()
//				- Ellipsoid.wgs84.getESemiMajorAxis(), 1.0d
//				/ Ellipsoid.ed50.getEIFlattening() - 1.0d
//				/ Ellipsoid.wgs84.getEIFlattening(), 86d, 107d, 121d);
//
//		UtmZone targetUtm = new UtmZone(targetEllipsoid, zone, hemisphere, 0.0d);
//		GeoPoint gp = new GeoPoint(targetUtm, Math.toDegrees(result.lon), Math
//				.toDegrees(result.lat));
//		UtmPoint up = new UtmPoint(targetUtm);
//
//		return targetUtm.fromGeo(gp, up, targetUtm);
		
		throw new NotImplementedException("Función no implementada");

	}

	/*
	 * transform
	 * 
	 * Parameters: from: The geodetic position to be translated. from_a: The
	 * semi-major axis of the "from" ellipsoid. from_f: Flattening of the "from"
	 * ellipsoid. from_esq: Eccentricity-squared of the "from" ellipsoid. da:
	 * Change in semi-major axis length (meters); "to" minus "from" df: Change
	 * in flattening; "to" minus "from" dx: Change in x between "from" and "to"
	 * datum. dy: Change in y between "from" and "to" datum. dz: Change in z
	 * between "from" and "to" datum.
	 */
	public static GeodesicPosition transform(GeodesicPosition from,
			double from_a, double from_f,

			double from_esq, double da, double df, double dx, double dy,
			double dz) {
		double slat = Math.sin(from.lat);
		double clat = Math.cos(from.lat);
		double slon = Math.sin(from.lon);
		double clon = Math.cos(from.lon);
		double ssqlat = slat * slat;
		double adb = 1.0 / (1.0 - from_f); // "a divided by b"
		double dlat, dlon, dh;

		double rn = from_a / Math.sqrt(1.0 - from_esq * ssqlat);
		double rm = from_a * (1. - from_esq)
		/ Math.pow((1.0 - from_esq * ssqlat), 1.5);

		dlat = (((((-dx * slat * clon - dy * slat * slon) + dz * clat) + (da * ((rn
				* from_esq * slat * clat) / from_a))) + (df
						* (rm * adb + rn / adb) * slat * clat)))
						/ (rm + from.h);

		dlon = (-dx * slon + dy * clon) / ((rn + from.h) * clat);

		dh = (dx * clat * clon) + (dy * clat * slon) + (dz * slat)
		- (da * (from_a / rn)) + ((df * rn * ssqlat) / adb);

		return new GeodesicPosition(from.lon + dlon, from.lat + dlat, from.h
				+ dh);
	}

}
