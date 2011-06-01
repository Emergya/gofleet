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
 * 06/07/2009
 */
package es.emergya.ui.gis.layers;

import java.io.File;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

/**
 * Modification over MarkerLayer to be independent of a MapFrame
 * 
 * A layer holding markers.
 * 
 * Markers are GPS points with a name and, optionally, a symbol code attached;
 * marker layers can be created from waypoints when importing raw GPS data, but
 * they may also come from other sources.
 * 
 * The symbol code is for future use.
 * 
 * The data is read only.
 */
public class CustomMarkerLayer extends MarkerLayer {

	/**
	 * @param indata
	 * @param name
	 * @param associatedFile
	 * @param fromLayer
	 * @param mapView
	 */
	public CustomMarkerLayer(GpxData indata, String name, File associatedFile,
			GpxLayer fromLayer, MapView mapView) {
		super(indata, name, associatedFile, fromLayer, mapView);
	}

}
