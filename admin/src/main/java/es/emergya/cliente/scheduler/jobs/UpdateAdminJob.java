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
package es.emergya.cliente.scheduler.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.logging.LogFactory;
import org.gofleet.scheduler.Job;
import org.quartz.Scheduler;

import edu.emory.mathcs.backport.java.util.Collections;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.base.plugins.PluginListener;

/**
 * Recorre todos los Option para ver si necesitan ser actualizados
 * 
 * @see Option#needsUpdating()
 * @see Option#refresh()
 * @see PluginEventHandler
 * 
 * @author marias
 * @see Scheduler
 */
public class UpdateAdminJob extends Job {
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(UpdateAdminJob.class);

	private static List<PluginListener> updatables = new ArrayList<PluginListener>();

	public UpdateAdminJob() {
		super();
	}

	public synchronized static void register(PluginListener o) {
		updatables.add(o);
	}

	@Override
	public String getName() {
		return "UpdateAdminJob";
	}

	@Override
	public String getDescription() {
		return "Job to check if the user is legitimate";
	}

	@Override
	public Integer getFrequency() {
		return 1000 * 30;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			for (final PluginListener o : (List<PluginListener>) Collections
					.synchronizedList(updatables))
				try {
					if (!(o instanceof Option) || ((Option) o).needsUpdating()) {
						SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
							@Override
							protected Object doInBackground() throws Exception {
								PluginEventHandler.fireChange(o);
								return null;
							}

							@Override
							protected void done() {
								super.done();
								o.refresh(null);
							}
						};
						sw.execute();
					}
				} catch (Throwable t) {
					log.error("Error al actualizar " + o.toString()
							+ " debido a " + t.toString());
				}
		} catch (Throwable e) {
			log.fatal(
					"Error al ejecutar la actualización de adminjob "
							+ e.toString(), e);
		}
	}
}
