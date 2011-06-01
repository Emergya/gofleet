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

/**
 * 
 * @author jlrodriguez
 */
public class LatLongBoundingBox implements EmergyaBoundingBox {
	private EmergyaLatLon minxy;
	private EmergyaLatLon maxxy;

	public LatLongBoundingBox(EmergyaLatLon minlatlon, EmergyaLatLon maxlatlon) {
		this.minxy = minlatlon;
		this.maxxy = maxlatlon;
	}

	public LatLongBoundingBox(double minLon, double minLat, double maxLon,
			double maxLat) {
		this.minxy = new EmergyaLatLon(minLat, minLon);
		this.maxxy = new EmergyaLatLon(maxLat, maxLon);
	}

	@Override
	public double getMinX() {
		return minxy.getLon();
	}

	@Override
	public double getMinY() {
		return minxy.getLat();
	}

	@Override
	public double getMaxX() {
		return maxxy.getLon();
	}

	@Override
	public double getMaxY() {
		return maxxy.getLat();
	}

	public EmergyaLatLon getMinXY() {
		return minxy;
	}

	public EmergyaLatLon getMaxXY() {
		return maxxy;
	}

	public void setMinXY(EmergyaLatLon minxy) {
		this.minxy = minxy;
	}

	public void setMaxXY(EmergyaLatLon maxxy) {
		this.maxxy = maxxy;
	}

	public void setMinX(double minx) {
		this.minxy.setLon(minx);
	}

	public void setMinY(double miny) {
		this.minxy.setLat(miny);
	}

	public void setMaxX(double maxx) {
		this.maxxy.setLon(maxx);
	}

	public void setMaxY(double maxy) {
		this.maxxy.setLat(maxy);
	}

	@Override
	public String toString() {
		return getMinY() + "," + -getMaxX() + "," + getMaxY() + ","
				+ -getMinX();
	}

}
