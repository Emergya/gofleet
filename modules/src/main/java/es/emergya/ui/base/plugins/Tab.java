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

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collections;
import java.util.Random;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import es.emergya.cliente.constants.LogicConstants;

public class Tab extends AbstractPluggable {
	private static final long serialVersionUID = -6691454170753397497L;
	private JTabbedPane tabs;
	private static Random random = new Random();

	/**
	 * Le paso el título, tipo y orden para poder cambiarlos desde el Main, pero
	 * los de la aplicación tendrán un ctor. sin parámetros y llevarán los
	 * valores hardcodeados.
	 * */
	public Tab(String title, PluginType type, int order, String icon,
			Color color) {
		this.title = title;
		this.type = type;
		this.order = order;
		this.tip = title;
		this.setName(title);

		if (!icon.equals("")) {
			this.icon = LogicConstants.getIcon(icon);
		}
		this.color = color;

		this.tabs = new JTabbedPane();
		this.tabs.setTabPlacement(SwingUtilities.LEFT);

		this.tabs.setUI(new MyTabPaneUI());

		this.tabs.setBorder(new MatteBorder(4, 0, 0, 0, color));
		this.tabs.setBackground(color);
		this.tabs.setTabPlacement(SwingConstants.LEFT);

		this.setLayout(new BorderLayout());

		this.add(this.tabs, BorderLayout.CENTER);

		resize();
	}

	public Tab(String title, PluginType type, int order) {
		this(title, type, order, "", getRandomColor());
	}

	private static Color getRandomColor() {
		return new Color(random.nextFloat(), random.nextFloat(),
				random.nextFloat());
	}

	public Tab(String title, PluginType type, int order, Color color) {
		this(title, type, order, "", color);
	}

	public Tab(String title, PluginType type, int order, String icon) {
		this(title, type, order, icon, getRandomColor());
	}

	// Ordeno los plúgines ;) e incrusto los que estén habilitados.
	@Override
	public void setup() {
		this.tabs.setFont(LogicConstants.deriveBoldFont(12.0f));
		this.tabs.removeAll();

		this.setBackground(this.color);
		Collections.sort(this.plugins);

		for (final AbstractPlugin plugin : this.plugins)
			if (plugin.isEnabled()) {
				int tabIndex = ((plugin.getOrder() > this.tabs.getTabCount()) ? this.tabs
						.getTabCount() : plugin.getOrder());
				this.tabs.insertTab(plugin.getTitle(), plugin.getIcon(),
						plugin, plugin.getTip(), tabIndex);

				// JLabel l = new JLabel(plugin.getTitle(), plugin.getIcon(),
				// JLabel.RIGHT);
				// l.setVerticalTextPosition(JLabel.BOTTOM);
				// l.setHorizontalTextPosition(JLabel.CENTER);
				// this.tabs.setTabComponentAt(tabIndex, l);

				this.tabs.setBackground(plugin.getColor());

			}
	}

	/**
	 * Resize the inner AbstractPlugin.
	 */
	@Override
	public void resize() {
		if (this.tabs != null)
			for (java.awt.Component c : this.tabs.getComponents())
				if (c instanceof AbstractPlugin)
					((AbstractPlugin) c).resize();

	}
}