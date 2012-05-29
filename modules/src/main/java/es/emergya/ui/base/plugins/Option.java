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
import java.util.Calendar;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import es.emergya.cliente.constants.LogicConstants;

/**
 * Base abstract example plugin.
 * 
 * @author marias
 * 
 */
public abstract class Option extends AbstractPlugin implements PluginListener {
	private static final long serialVersionUID = 1651295775437542935L;
	protected Calendar lastUpdated = Calendar.getInstance();

	// Le paso el título, tipo y orden para poder cambiarlos desde el Main,
	// pero los de la aplicación tendrán un ctor. sin parámetros y llevarán
	// los valores hardcodeados

	public Option(String title, PluginType type, int order, String url,
			JComponent content) {
		setTitle(title);
		this.type = type;
		this.order = order;
		this.tip = title;

		if (url != null)
			this.icon = LogicConstants.getIcon(url);

		BorderLayout b = new BorderLayout();
		b.setVgap(10);
		b.setHgap(10);

		this.setLayout(b);

		super.tab = content;
		if (content != null)
			this.add(content, BorderLayout.CENTER);
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		if (title.length() == 0)
			this.title = title;
		else
			this.title = StringUtils.rightPad(title, 25);
	}

	public Option(String title, PluginType type, int order, JComponent content) {
		this(title, type, order, null, content);
	}

	/**
	 * Overridear si queremos que el adminjob haga algo
	 */
	public void refresh(PluginEvent event) {
		lastUpdated = Calendar.getInstance();
	}

	/**
	 * Overridear si queremos que el adminjob detecte que necesita actualizacion
	 * 
	 * @return
	 */
	public boolean needsUpdating() {
		return false;
	}

	public abstract void reboot();
}
