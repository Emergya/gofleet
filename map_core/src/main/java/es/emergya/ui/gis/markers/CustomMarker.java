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
 * 07/07/2009
 */
package es.emergya.ui.gis.markers;

import java.awt.Color;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

/**
 * Vale para asociarle un objeto a un marcador. Puedes referenciarlo por el id o
 * por el objeto. O por ambos. Pongo los dos por si hay casos en los que
 * conviene refrescar el objeto y casos en los que no.
 * 
 * @author marias
 */
public class CustomMarker<K, T> extends Marker {

	public enum Type {
		UNKNOWN, RESOURCE, INCIDENCE
	};

	protected K id = null;
	protected Type type = Type.UNKNOWN;
	protected T object = null;

	public CustomMarker(LatLon ll, String text, String iconName,
			MarkerLayer parentLayer, double time, double offset, K id, Type type) {
		super(ll, text, iconName, parentLayer, time, offset);
		this.id = id;
		this.type = type;
	}

	public CustomMarker(WayPoint w, MarkerLayer layer, K id, Type type) {
		this(w.latlon, w.getString("name"), w.getString("symbol"), layer, -1,
				0, id, type);
		if (w.attr.containsKey("color"))
			this.color = Color.decode(w.getString("color"));
		if (w.attr.containsKey("bgcolor"))
			this.bgcolor = Color.decode(w.getString("bgcolor"));
	}

	public K getId() {
		return id;
	}

	public void setId(K id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

}
