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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gofleet.module.IModule;

import es.emergya.utils.ExtensionClassLoader;
import es.emergya.utils.LogicConstants;

@SuppressWarnings("serial")
/*
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public abstract class AbstractPlugin extends JPanel implements
		Comparable<AbstractPlugin>, IModule {
	protected PluginType type;
	protected String title;
	protected Icon icon;
	protected String tip;
	protected Color color = Color.WHITE;
	protected JComponent tab;
	protected Integer order = 0;
	protected Boolean enabled = true;
	protected String id;

	public String getId() {
		return this.id;
	}
	

	protected void loadProperties(String file) {
		Properties p = new Properties();

		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			InputStream is = ecl.getResourceAsStream(file);
			if (is == null)
				is = LogicConstants.class.getResourceAsStream(file);
			p.load(is);

			this.title = p.getProperty("TITLE");
			this.type = PluginType.getType(p.getProperty("TYPE", "UNKNOWN"));
			this.order = new Integer(p.getProperty("ORDER", "0"));
			this.tip = p.getProperty("TIP", this.title);
			
		} catch (Throwable e) {
			Logger.getLogger(this.getClass()).error(
					"Couldn't load property file", e);
			this.title = StringUtils.rightPad("Unknown Module", 25);
			this.type = null;
			this.order = new Integer(0);
			this.tip = title;
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public PluginType getType() {
		return this.type;
	}

	public String getTitle() {
		return this.title;
	}

	public Icon getIcon() {
		return this.icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public String getTip() {
		return this.tip;
	}

	public Component getTab() {
		return this.tab;
	}

	public int getOrder() {
		return this.order;
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color newColor) {
		this.color = newColor;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public int compareTo(AbstractPlugin o) {
		return Integer.valueOf(this.order).compareTo(o.getOrder());
	}

	public void resize() {
		this.repaint();
		if (this.tab != null)
			this.tab.repaint();
	}

	public List<String> getDependencies() {
		return new ArrayList<String>(0);
	}
}
