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
/**
 * 
 */
package es.emergya.ui.gis;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

import es.emergya.geo.util.UTM;

/**
 * 
 * Conversor entre distintos tipos de geometrias
 * 
 * @author marias
 * 
 */
public class GeometryConverter {
	private static final Log log = LogFactory.getLog(GeometryConverter.class);
	private static UTM UTMConverter = new UTM();

	/**
	 * <p>
	 * Ejemplos de sintaxis:
	 * </p>
	 * <ul>
	 * <li>Punto: POINT(30 50)</li>
	 * <li>Linea: LINESTRING(1 1, 5 5, 10 10, 20 20)</li>
	 * <li>Multilinea: LINESTRING( (1 1, 5 5, 10 10, 20 20),(20 30, 10 15, 40
	 * 5))</li>
	 * <li>Poligono simple: POLYGON (0 0, 10 0, 10 10, 0 0)</li>
	 * <li>Varios poligono en una sola geometria (multipoligono): POLYGON ( (0
	 * 0, 10 0, 10 10, 0 10, 0 0),( 20 20, 20 40, 40 40, 40 20, 20 20) )</li>
	 * <li>Geometrias de distinto tipo en un solo elemento:
	 * GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10))</li>
	 * <li>Punto vacio: POINT EMPTY</li>
	 * <li>Multipoligono vacio: MULTIPOLYGON EMPTY</li>
	 * </ul>
	 * 
	 * @param geometry
	 * @return
	 */
	public static OsmPrimitive wktToOsmPrimitive(String geometry) {
		OsmPrimitive resultado = null;

		if (geometry != null) {
			log.trace("Vamos a convertir " + geometry);
			String[] componentes = StringUtils.split(geometry, "(");

			String tipo = componentes[0];
			GeometryType type = null;

			if (tipo.equals("LINESTRING"))
				type = GeometryType.LINESTRING;
			else if (tipo.equals("MULTILINESTRING"))
				type = GeometryType.MULTILINESTRING;
			else if (tipo.equals("POINT"))
				type = GeometryType.POINT;
			else if (tipo.equals("MULTIPOINT"))
				type = GeometryType.MULTIPOINT;
			else if (tipo.equals("POLYGON"))
				type = GeometryType.POLYGON;
			else if (tipo.equals("MULTIPOLYGON"))
				type = GeometryType.MULTIPOLYGON;
			else
				type = GeometryType.UNKNOWN;

			switch (type) {
			case LINESTRING:
				log.trace("Reconocido a LINESTRING");
				resultado = new Way();
				if (componentes[1].length() < 1) {
					log.error("componentes erroneas");
				} else {
					String[] coordenadas = StringUtils.split(componentes[1]
							.substring(0, componentes[1].length() - 1), ",");
					if (coordenadas.length == 0)
						log.trace("No hay coordenadas, ¿geometria vacia?");
					long id = 0l;
					for (String coordenada : coordenadas) {
						log.trace("coordenadas: " + coordenada);
						String[] numeros = StringUtils.split(coordenada, " ");
						if (numeros.length != 2)
							log.error("Numero de dimensiones incorrecto: "
									+ numeros.length);
						else {
							try {
								LatLon latlon = extractLatLon(numeros);
								log.trace(latlon);
								Node n = new Node(id++);
								n.setCoor(latlon);
								((Way) resultado).addNode(n);
							} catch (Exception e) {
								log.error("Error calculando el nodo", e);
							}
						}
					}
				}
				break;
			case POINT:
				log.trace("Reconocido a POINT");
				if (componentes[1].length() < 1) {
					log.error("componentes erroneas");
				} else {
					String coordenada = componentes[1].substring(0,
							componentes[1].length() - 1);
					if (coordenada.length() == 0)
						log.trace("No hay coordenadas, ¿geometria vacia?");
					log.trace("coordenadas: " + coordenada);
					String[] numeros = StringUtils.split(coordenada, " ");
					if (numeros.length != 2)
						log.error("Numero de dimensiones incorrecto: "
								+ numeros.length);
					else {
						try {
							LatLon latlon = extractLatLon(numeros);
							resultado = new Node(latlon);
						} catch (Exception e) {
							log.error("Error calculando el nodo", e);
							e.printStackTrace();
						}
					}

				}
				break;
			default:
				throw new NotImplementedException(
						"Aun no tenemos implementado este tipo de geometria: "
								+ tipo);
			}
		}
		resultado.incomplete = false;
		return resultado;
	}

	/**
	 * <p>
	 * Ejemplos de sintaxis:
	 * </p>
	 * <ul>
	 * <li>Punto: POINT(30 50)</li>
	 * <li>Linea: LINESTRING(1 1, 5 5, 10 10, 20 20)</li>
	 * <li>Multilinea: LINESTRING( (1 1, 5 5, 10 10, 20 20),(20 30, 10 15, 40
	 * 5))</li>
	 * <li>Poligono simple: POLYGON (0 0, 10 0, 10 10, 0 0)</li>
	 * <li>Varios poligono en una sola geometria (multipoligono): POLYGON ( (0
	 * 0, 10 0, 10 10, 0 10, 0 0),( 20 20, 20 40, 40 40, 40 20, 20 20) )</li>
	 * <li>Geometrias de distinto tipo en un silo elemento:
	 * GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10))</li>
	 * <li>Punto vacio: POINT EMPTY</li>
	 * <li>Multipoligono vacio: MULTIPOLYGON EMPTY</li>
	 * </ul>
	 * 
	 * @param geometry
	 * @return
	 */
	public static WayPoint wktToWayPoint(String geometry) {
		WayPoint resultado = null;

		if (geometry != null) {
			log.trace("Vamos a convertir " + geometry);
			String[] componentes = StringUtils.split(geometry, "(");

			String tipo = componentes[0];
			GeometryType type = null;

			if (tipo.equals("LINESTRING"))
				type = GeometryType.LINESTRING;
			else if (tipo.equals("MULTILINESTRING"))
				type = GeometryType.MULTILINESTRING;
			else if (tipo.equals("POINT"))
				type = GeometryType.POINT;
			else if (tipo.equals("MULTIPOINT"))
				type = GeometryType.MULTIPOINT;
			else if (tipo.equals("POLYGON"))
				type = GeometryType.POLYGON;
			else if (tipo.equals("MULTIPOLYGON"))
				type = GeometryType.MULTIPOLYGON;
			else
				type = GeometryType.UNKNOWN;

			switch (type) {
			case POINT:
				log.trace("Reconocido a POINT");
				if (componentes[1].length() < 1) {
					log.error("componentes erroneas");
				} else {
					String coordenada = componentes[1].substring(0,
							componentes[1].length() - 1);
					if (coordenada.length() == 0)
						log.trace("No hay coordenadas, ¿geometria vacia?");
					log.trace("coordenadas: " + coordenada);
					String[] numeros = StringUtils.split(coordenada, " ");
					if (numeros.length != 2)
						log.error("Numero de dimensiones incorrecto: "
								+ numeros.length);
					else {
						try {
							LatLon latlon = extractLatLon(numeros);
							log.trace("LatLon del nuevo marcador: " + latlon);
							resultado = new WayPoint(latlon);
						} catch (Exception e) {
							log.error("Error calculando el nodo", e);
							e.printStackTrace();
						}
					}

				}
				break;
			default:
				throw new NotImplementedException(
						"Aun no tenemos implementado este tipo de geometria: "
								+ tipo);
			}
		}
		return resultado;
	}

	private static LatLon extractLatLon(String[] numeros) {
		Double x = new Double(numeros[0]);
		Double y = new Double(numeros[1]);
		EastNorth en = new EastNorth(x, y);

		LatLon latlon = UTMConverter.eastNorth2latlon(en);
		return latlon;
	}
}

enum GeometryType {
	POINT, LINESTRING, MULTILINESTRING, MULTIPOINT, POLYGON, MULTIPOLYGON, UNKNOWN;
}