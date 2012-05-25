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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoClassLoader;

public class PluginType implements Comparable<PluginType> {
	private static final String UNKNOWN = "UNKNOWN";
	final static private PluginTypeContainer ptc = new PluginTypeContainer();
	String type = UNKNOWN;
	Boolean detachable = false;

	public Boolean isDetachable() {
		return detachable;
	}

	PluginType(String type) {
		super();
		this.type = type;
	}

	@Override
	public int compareTo(PluginType o) {
		return this.type.compareTo(o.type);
	}

	public static PluginType getType(String type) {
		return ptc.get(type);
	}

	public static Set<String> getAll() {
		Set<String> res = ptc.keySet();
		res.remove(UNKNOWN);
		return res;
	}

	public static PluginType getDefault() {
		return getType(UNKNOWN);
	}
}

class PluginTypeContainer extends HashMap<String, PluginType> {
	private static final long serialVersionUID = -8773223245356383977L;

	PluginTypeContainer() {
		super();
		// default
		put("UNKNOWN", new PluginType("UNKNOWN"));
		PluginType pluginType = new PluginType("ADMIN");
		pluginType.detachable = true;
		put("ADMIN", pluginType);
		put("MAP", new PluginType("MAP"));

		final Properties p = new Properties();
		try {
			InputStream resourceAsStream = GoClassLoader.getGoClassLoader()
					.getResourceAsStream("moduletypes");
			if (resourceAsStream == null)
				throw new NullPointerException(
						"No se encontró el fichero de moduletypes");
			p.load(resourceAsStream);
			for (Object k : p.keySet()) {
				put(k.toString(), new PluginType(k.toString()));
			}
		} catch (Exception e) {
			LogFactory.getLog(PluginType.class).error(
					"moduletypes file not found. Using default.");
			put("MESSAGES", new PluginType("MESSAGES"));
			put("FORMS", new PluginType("FORMS"));
			put("BUTTON", new PluginType("BUTTON"));
			put("LIST", new PluginType("LIST"));
		}

	}
}
