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
 * 08/07/2009
 */
package es.emergya.ui.gis.markers;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import es.emergya.ui.gis.paintingThreads.Updatable;

/**
 * @author fario
 * 
 */
public class CustomMovingMarker extends Marker implements Updatable {
	private LatLon[] positions = {};
	private int currentPositionIndex = 0;

	public CustomMovingMarker(LatLon ll, String text, String iconName,
			MarkerLayer parentLayer, LatLon[] positions) {
		super(ll, text, iconName, parentLayer, -1, 0);
		setPositions(positions);
	}

	public void setPositions(LatLon[] newPositions) {
		int fold = 10;
		positions = new LatLon[newPositions.length * fold];
		for (int i = 0; i < newPositions.length; i++) {
			LatLon p0 = newPositions[i];
			LatLon p1 = newPositions[0];
			if (i + 1 != newPositions.length)
				p1 = newPositions[i + 1];
			double dx = p1.lon() - p0.lon();
			double dy = p1.lat() - p0.lat();
			for (int j = 0; j < fold; j++) {
				positions[i * fold + j] = new LatLon(p0.lat()
						+ (dy * (j / ((double) fold))), p0.lon()
						+ (dx * (j / ((double) fold))));
			}
		}
		currentPositionIndex = 0;
	}

	public void update() {
		currentPositionIndex = (currentPositionIndex + 1) % positions.length;
		eastNorth = Main.proj.latlon2eastNorth(positions[currentPositionIndex]);
	}
}
