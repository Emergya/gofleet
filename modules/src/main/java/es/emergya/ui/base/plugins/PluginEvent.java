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
package es.emergya.ui.base.plugins;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

public class PluginEvent extends EventObject {

	public PluginEvent(PluginListener originalSource) {
		super(originalSource);
		this.source = originalSource;
	}

	private static final long serialVersionUID = 7383182229898306240L;

	final private PluginListener source;
	final private List<PluginListener> plugins = new LinkedList<PluginListener>();

	public PluginListener getSource() {
		return this.source;
	}

	public synchronized void add(PluginListener pl) {
		this.plugins.add(pl);
	}

	public synchronized Boolean contains(PluginListener pl) {
		return (this.source.equals(pl) || this.plugins.contains(pl));
	}
}
