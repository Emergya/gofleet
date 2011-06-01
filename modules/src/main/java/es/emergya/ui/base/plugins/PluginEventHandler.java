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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PluginEventHandler {
	private static final Log log = LogFactory.getLog(PluginEventHandler.class);

	private static Map<PluginListener, Set<PluginListener>> observables = new LinkedHashMap<PluginListener, Set<PluginListener>>();

	public static void fireChange(PluginListener source) {
		log.debug("fireChange(" + source + ")");
		try {
			PluginEvent event = new PluginEvent(source);
			fireChange(source, event);
		} catch (Throwable t) {
			log.error(t, t);
		}
	}

	private static void fireChange(PluginListener source,
			final PluginEvent event) {
		log.debug("fireChange(" + source + "," + event + ")");
		try {
			Set<PluginListener> observers = getObservables(source);
			if (observers != null)
				for (final PluginListener pl : observers) {
					try {
						if (!event.contains(pl)) {
							event.add(pl);
							// SwingWorker<Object, Object> sw = new
							// SwingWorker<Object, Object>() {
							// @Override
							// protected Object doInBackground() {
							try {
								pl.refresh(event);
							} catch (Throwable t) {
								log.error("Error refrescando vista", t);
							}
							// return null;
							// }
							// };
							// SwingUtilities.invokeLater(sw);
							fireChange(pl, event);
						}
					} catch (Throwable t) {
						log.error(t, t);
					}
				}
		} catch (Throwable t) {
			log.error(t, t);
		}
	}

	private synchronized static Set<PluginListener> getObservables(
			PluginListener source) {
		return observables.get(source);
	}

	/**
	 * Registra a observer para avisarle cuando observable se modifique
	 * 
	 * @param observer
	 * @param observable
	 */
	public static synchronized void register(PluginListener observer,
			PluginListener observable) {
		Set<PluginListener> observers = getObservables(observable);
		if (observers == null)
			observers = new HashSet<PluginListener>();
		observers.add(observer);
		observables.put(observable, observers);
	}

	/**
	 * Deregistra a observer para no volverle a avisar cuando observable se
	 * modifique
	 * 
	 * @param observer
	 * @param observable
	 */
	public static synchronized void deregister(PluginListener observer,
			PluginListener observable) {
		Set<PluginListener> observers = getObservables(observable);
		if (observers == null)
			observers = new HashSet<PluginListener>();
		observers.remove(observer);
		observables.put(observable, observers);
	}

}
