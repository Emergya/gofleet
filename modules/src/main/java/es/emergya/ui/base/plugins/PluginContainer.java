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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.logging.LogFactory;

import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.i18n.Internacionalization;

/**
 * Container for plugins.
 * 
 */
public class PluginContainer extends AbstractPluggable {
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(PluginContainer.class);

	private static final long serialVersionUID = -494763572856676601L;
	private Modes mode = Modes.DESKTOP;

	public enum Modes {
		MOBILE, DESKTOP
	};

	public void setMode(Modes mode) {
		if (mode != null)
			this.mode = mode;
	}

	/**
	 * Contains all the plugins.
	 */
	protected PluggableJTabbedPane pane;

	public DetachedTab getDetachedTab(int i) {
		return pane.detached_tabs.get(i);
	}

	/**
	 * Default constructor.
	 */
	public PluginContainer() {
		this.setLayout(new GridLayout(1, 1));
	}

	/**
	 * Ordeno los plúgines. Compruebo cuáles son contenedores y cuáles no.
	 * Incrusto los no contenedores dentro de los contenedores del mismo tipo y
	 * los contenedores en mi panel.
	 */
	@Override
	public void setup() {
		if (getPane() != null) {
			this.remove(getPane());
		}
		this.pane = new PluggableJTabbedPane();
		getPane().setDoubleBuffered(true);
		this.add(getPane());
		Collections.sort(this.plugins);

		Map<PluginType, List<AbstractPlugin>> options = new HashMap<PluginType, List<AbstractPlugin>>();
		List<AbstractPluggable> tabs = new ArrayList<AbstractPluggable>();

		for (AbstractPlugin plugin : this.plugins) {
			if (plugin instanceof AbstractPluggable) {
				tabs.add((AbstractPluggable) plugin);
			} else {
				if (!options.containsKey(plugin.getType())) {
					options.put(plugin.getType(),
							new ArrayList<AbstractPlugin>());
				}
				options.get(plugin.getType()).add(plugin);
			}

			if (plugin instanceof Option)
				((Option) plugin).reboot();
		}
		Collections.sort(tabs);
		for (AbstractPluggable tab : tabs) {
			if (options.containsKey(tab.getType())) {
				for (AbstractPlugin plugin : options.get(tab.getType())) {
					tab.addPlugin(plugin);
				}
			}

			if (tab.isEnabled() && this.isAllowed(tab)) {
				tab.setup();
				getPane().add(tab);

				int index = getPane().getTabCount() - 1;

				getPane().setTitleAt(index, tab.getTitle());
				getPane().setToolTipTextAt(index, tab.getTip());
				getPane().setIconAt(index, tab.getIcon());
				getPane().setBackgroundAt(index, tab.getColor());
				getPane().setForegroundAt(index, Color.BLACK);

				// Detaches all the detachables panels
				if (tab.getDetachable() && mode == Modes.DESKTOP) {
					log.trace("tab detachable: " + tab);
					DetachedTab f = getPane()
							.detach(tab,
									Internacionalization
											.getString("Main.Administration.titleWindow"));
					f.setVisible(true);
					f.setRetatchOnClose(false);
				}

				// Deletes the admin tab if the user is not administrator
				if (PluginType.getType("ADMIN").equals(tab.getType())
						&& !Authentication.isAdministrator()) {
					getPane().remove(tab);
				}
			}
		}
	}

	private boolean isAllowed(AbstractPluggable tab) {
		Usuario u = Authentication.getUsuario();
		boolean isAdmin = (u != null && u.getAdministrador());
		return (PluginType.getType("ADMIN").equals(tab.getType()) || isAdmin);
	}

	@Override
	public void resize() {
		if ((getPane() != null) && (getPane().getComponents().length > 0)) {
			for (Component componente : getPane().getComponents()) {
				if (componente instanceof AbstractPlugin) {
					((AbstractPlugin) componente).resize();
				}
			}
		}
	}

	/**
	 * @return the selected component color
	 */
	public Color getBackgroundColor() {
		if (getPane() != null && getPane().getSelectedComponent() != null) {
			return getPane().getSelectedComponent().getBackground();
		}
		return Color.WHITE;
	}

	public PluggableJTabbedPane getPane() {
		return this.pane;
	}

	public void maximizeAllDetachedTabs() {
		for (DetachedTab tab : getPane().detached_tabs)
			tab.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
}
