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

public class BoundingBox {
	double north;
	double south;
	double east;
	double west;

	public BoundingBox(final int x, final int y, final int zoom) {
		tile2boundingBox(x, y, zoom);
	}

	public double getNorth() {
		return north;
	}

	public double getSouth() {
		return south;
	}

	public double getEast() {
		return east;
	}

	public double getWest() {
		return west;
	}

	/**
	 * Dadas las coordenadas en formato tile, devuele en boundingbox
	 * correspondiente a esa tile
	 * 
	 * @param x
	 *            coordenada X de la tile
	 * @param y
	 *            coordenada Y de la tile
	 * @param zoom
	 *            nivel de zoom
	 * @return
	 */
	private void tile2boundingBox(final int x, final int y, final int zoom) {
		north = tile2lat(y, zoom);
		south = tile2lat(y + 1, zoom);
		west = tile2lon(x, zoom);
		east = tile2lon(x + 1, zoom);
	}

	private static double tile2lon(int x, int z) {
		return ((double) x) / Math.pow(2.0d, (double) z) * 360.0d - 180.0d;
	}

	private static double tile2lat(int y, int z) {
		double n = Math.PI - (2.0d * Math.PI * (double) y)
				/ Math.pow(2.0d, (double) z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(west).append(",").append(south).append(",").append(east)
				.append(",").append(north);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(east);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(north);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(south);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(west);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(east) != Double
				.doubleToLongBits(other.east))
			return false;
		if (Double.doubleToLongBits(north) != Double
				.doubleToLongBits(other.north))
			return false;
		if (Double.doubleToLongBits(south) != Double
				.doubleToLongBits(other.south))
			return false;
		if (Double.doubleToLongBits(west) != Double
				.doubleToLongBits(other.west))
			return false;
		return true;
	}

}