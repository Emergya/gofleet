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
class MercatorBoundingBox implements EmergyaBoundingBox {
	private EmergyaEastNorthing minxy;
	private EmergyaEastNorthing maxxy;

	public MercatorBoundingBox(EmergyaEastNorthing minxy,
			EmergyaEastNorthing maxxy) {
		this.minxy = minxy;
		this.maxxy = maxxy;
	}

	public MercatorBoundingBox(double minx, double miny, double maxx,
			double maxy) {
		this.minxy = new EmergyaEastNorthing(minx, miny);
		this.maxxy = new EmergyaEastNorthing(maxx, maxy);
	}

	@Override
	public double getMinX() {
		return minxy.getX();
	}

	@Override
	public double getMinY() {
		return minxy.getY();
	}

	@Override
	public double getMaxX() {
		return maxxy.getX();
	}

	@Override
	public double getMaxY() {
		return maxxy.getY();
	}

	public EmergyaEastNorthing getMinXY() {
		return minxy;
	}

	public EmergyaEastNorthing getMaxXY() {
		return maxxy;
	}

	public void setMinXY(EmergyaEastNorthing minxy) {
		this.minxy = minxy;
	}

	public void setMaxXY(EmergyaEastNorthing maxxy) {
		this.maxxy = maxxy;
	}

	public void setMinX(double minx) {
		this.minxy.setX(minx);
	}

	public void setMinY(double miny) {
		this.minxy.setY(miny);
	}

	public void setMaxX(double maxx) {
		this.maxxy.setX(maxx);
	}

	public void setMaxY(double maxy) {
		this.maxxy.setY(maxy);
	}

}
