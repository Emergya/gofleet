/*
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jlrodriguez@emergya.es">Juan Luís Rodríguez</a>
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
 * @author <a href="mailto:fario@emergya.es">Félix del Río Beningno</a>
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
/*
 * 28/07/2009
 */
package es.emergya.ui.base.plugins;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Icon;
import javax.swing.JFrame;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;

import es.emergya.actions.Authentication;
import es.emergya.ui.base.BasicWindow;

/**
 * @author fario
 * 
 */
public class DetachedTab extends JFrame {
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(DetachedTab.class);

	private static final long serialVersionUID = 6912920011615598746L;
	private final PluggableJTabbedPane original_pane;
	private final String tip;
	private final int original_positon;
	private final Component detached_tab;
	private final Icon icon;
	private boolean retatch_on_close = true;

	private final PluggableJTabbedPane pane = new PluggableJTabbedPane();

	@GoWired
	public BasicWindow window;

	public void setWindow(BasicWindow window) {
		this.window = window;
	}

	/**
	 * @param original_pane
	 * @param title
	 * @param tip
	 * @param original_positon
	 * @param detached_tab
	 * @param icon
	 * @throws HeadlessException
	 */
	public DetachedTab(PluggableJTabbedPane original_pane, String title,
			String tip, int original_positon, Component detached_tab, Icon icon)
			throws HeadlessException {
		super(title);
		this.setIconImage(window.getFrame().getIconImage());
		this.original_pane = original_pane;
		this.tip = tip;
		this.original_positon = original_positon;
		this.detached_tab = detached_tab;
		this.icon = icon;

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (retatch_on_close) {
					DetachedTab.this.original_pane.reattach(DetachedTab.this);
					DetachedTab.this.dispose();
				}

				Authentication.logOut();
				super.windowClosing(e);
			}
		});

		addTab(detached_tab.getName(), icon, detached_tab);

		this.addWindowListener(new WindowAdapter() {
		});
		this.setMinimumSize(new Dimension(800, 600));

		// this.addComponentListener(new ComponentAdapter() {
		// @Override
		// public void componentResized(ComponentEvent e) {
		// super.componentResized(e);
		// Dimension d = DetachedTab.this.getSize();
		// DetachedTab.log.info("Size actual: " + d);
		// if (d.width < 800)
		// d.width = 800;
		// if (d.height < 200)
		// d.height = 200;
		// DetachedTab.log.info("Size after: " + d);
		// DetachedTab.this.setSize(d.width, d.height);
		// }
		// });
		this.add(pane);
		pack();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	public void addTab(String title, Icon icon, Component tab) {
		log.trace("addTab(" + title + ")");
		this.pane.addTab(title, icon, tab);
	}

	/**
	 * (Des)activa que la ventana vuelva a generar la pestaña de la que salió.
	 * El comportamiento estandar al desactivarlo es cerrar la aplicación. Para
	 * conseguir otro comportamiento basta con añadirle un
	 * {@link WindowListener} al frame generado.
	 * 
	 * @param retatch_on_close
	 */
	public void setRetatchOnClose(boolean retatch_on_close) {
		this.retatch_on_close = retatch_on_close;
		if (retatch_on_close) {
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		} else {
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

	public String getTip() {
		return tip;
	}

	public int getOriginalPositon() {
		return original_positon;
	}

	public Component getDetachedTab() {
		return detached_tab;
	}

	public Icon getIcon() {
		return icon;
	}
}
