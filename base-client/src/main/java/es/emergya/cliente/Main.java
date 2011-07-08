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

import static es.emergya.cliente.constants.LogicConstants.deriveBoldFont;
import static es.emergya.cliente.constants.LogicConstants.deriveLightFont;
import static es.emergya.cliente.constants.LogicConstants.getInt;
import static es.emergya.cliente.constants.LogicConstants.getLightFont;

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.UIManager;

import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import es.emergya.i18n.Internacionalization;
import es.emergya.scheduler.CustomScheduler;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.base.plugins.Tab;

/**
 * Main class.
 * 
 * @author jlrodriguez
 * @author marias
 * 
 */
public final class Main extends Loader {

	private static final int LOGIN_FREQUENCY = getInt("LOGIN_FREQUENCY", 10) * 1000;
	private static final int UPDATE_MAPS_FREQUENCY = getInt(
			"UPDATE_MAPS_FREQUENCY", 10) * 1000;
	private static final int UPDATE_LISTADOS_FREQUENCY = getInt(
			"UPDATE_LISTADOS_FREQUENCY", 10) * 1000;
	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(Main.class);
	
	static {
		_this = new Main(); 
	}

	/** Constructor is private to avoid creating objects. */
	private Main() {
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

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	@Override
	protected void createAndShowGUI() {

		// Mapas
		FleetControlMapViewer fleetcontrol = new FleetControlMapViewer(
				Internacionalization.getString("Main.FleetControl"),
				PluginType.GPS, 1, "tab_icon_controlflota");
		container.addPlugin(fleetcontrol);
		container.addPlugin(new HistoryMapViewer(Internacionalization
				.getString("Main.History"), PluginType.GPS, 2,
				"tab_icon_historico"));

		// Administracion
		final Tab administracion = new Tab(Internacionalization
				.getString("Main.Administration"), PluginType.ADMIN, 2,
				"tab_icon_administracion", Color.LIGHT_GRAY);
		administracion.setDetachable(true);
		container.addPlugin(administracion);
		final AdminFleets adminFleets = new AdminFleets(1);
		container.addPlugin(adminFleets);
		final AdminRoles adminRoles = new AdminRoles(2);
		container.addPlugin(adminRoles);
		final AdminSquads adminSquads = new AdminSquads(3);
		container.addPlugin(adminSquads);
		final AdminResources adminResources = new AdminResources(4);
		container.addPlugin(adminResources);
		final AdminUsers adminUsers = new AdminUsers(5);
		container.addPlugin(adminUsers);
		final AdminLayers adminLayers = new AdminLayers(6);
		container.addPlugin(adminLayers);

		// Listados
		final Tab listados = new Tab(Internacionalization
				.getString("Main.Listados"), PluginType.LIST, 3,
				"tab_icon_listados", Color.LIGHT_GRAY);
		listados.setDetachable(true);
		container.addPlugin(listados);
		final ListIncidences listIncidences = new ListIncidences(1);
		container.addPlugin(listIncidences);

		// Formularios
		final Tab formularios = new Tab(Internacionalization
				.getString("Main.Formularios"), PluginType.FORMS, 3,
				"tab_icon_formularios", Color.LIGHT_GRAY);
		formularios.setDetachable(true);
		container.addPlugin(formularios);
		final FormIncidencia formulario = new FormIncidencia(1);
		container.addPlugin(formulario);

		// Eventos de refresco
		PluginEventHandler.register(adminUsers, adminRoles);
		PluginEventHandler.register(adminRoles, adminUsers);
		PluginEventHandler.register(adminUsers, adminLayers);
		PluginEventHandler.register(adminLayers, adminUsers);
		PluginEventHandler.register(adminResources, adminFleets);
		PluginEventHandler.register(adminResources, adminSquads);
		PluginEventHandler.register(adminFleets, adminResources);
		PluginEventHandler.register(adminSquads, adminResources);
		PluginEventHandler.register(adminRoles, adminFleets);
		PluginEventHandler.register(adminFleets, adminRoles);
	}

	@Override
	protected void loadJobs() {
		CustomScheduler scheduler;
		try {
			scheduler = new CustomScheduler();

			scheduler.addJob(Main.UPDATE_MAPS_FREQUENCY, "updateMaps",
					UpdateMapsJob.class);

			scheduler.addJob(Main.UPDATE_LISTADOS_FREQUENCY, "updateListados",
					UpdateAdminJob.class);
			scheduler.start();
		} catch (SchedulerException e) {
			LOG.error(e, e);
			showError(e);
		}
	}
}
