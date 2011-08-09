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
package es.emergya.cliente;

import static es.emergya.cliente.constants.LogicConstantsUI.deriveBoldFont;
import static es.emergya.cliente.constants.LogicConstantsUI.deriveLightFont;
import static es.emergya.cliente.constants.LogicConstantsUI.getInt;
import static es.emergya.cliente.constants.LogicConstantsUI.getLightFont;

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.UIManager;

import org.apache.commons.logging.LogFactory;

/**
 * Main class.
 * 
 * @author jlrodriguez
 * @author marias
 * 
 */
public final class Mobile extends Loader {

	private static final int UPDATE_MAPS_FREQUENCY = getInt(
			"UPDATE_MAPS_FREQUENCY", 10) * 1000;
	private static final int UPDATE_LISTADOS_FREQUENCY = getInt(
			"UPDATE_LISTADOS_FREQUENCY", 10) * 1000;
	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(Mobile.class);

	static {
		_this = new Mobile();
	}

	/** Constructor is private to avoid creating objects. */
	private Mobile() {
	}

	@Override
	protected void loadJobs() {
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 * 
	 * 
	 */
	@Override
	protected void createAndShowGUI() {
	}

	@Override
	protected void configureUI() {
		UIManager.put("swing.boldMetal", Boolean.FALSE); //$NON-NLS-1$

		UIManager.put("TabbedPane.selected", Color.decode("#B1BEF0"));
		final Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, getLightFont());
			}

		}
		UIManager.put("TableHeader.font", deriveBoldFont(10f));
		UIManager.put("TabbedPane.font", deriveLightFont(9f));
	}
}
