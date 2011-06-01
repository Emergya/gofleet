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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.ExitHandler;
import es.emergya.ui.gis.IMapViewer;

public class PluggableJTabbedPane extends JTabbedPane implements MouseListener {
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(PluggableJTabbedPane.class);

	private static final long serialVersionUID = 1890890213870372119L;
	private Rectangle salir;
	private Integer min_height = 0;
	private DetachedTab lastDetachablePane = null;

	protected List<JComponent> botones_flotantes = new ArrayList<JComponent>();

	protected List<DetachedTab> detached_tabs = new ArrayList<DetachedTab>();

	public void addFloatingButton(JComponent c) {
		botones_flotantes.add(c);
	}

	public PluggableJTabbedPane() {
		super();
		addFloatingButtons();
		this.addMouseListener(this);
		this.setUI(new MyTabbedPaneUI(this));
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				addFloatingButtons();
			}

		});
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (PluggableJTabbedPane.this.salir.contains(e.getPoint())) {
			ExitHandler eh = new ExitHandler();
			eh.actionPerformed(null);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		for (JComponent bf : botones_flotantes) {
			try {
				g.translate(bf.getBounds().x, bf.getBounds().y);
				bf.paint(g);
				g.translate(-bf.getBounds().x, -bf.getBounds().y);
			} catch (Throwable t) {
			}
		}
	}

	private void addFloatingButtons() {
		JButton salir = new JButton();
		salir.addActionListener(new ExitHandler());

		Icon icon = LogicConstants.getIcon("header_button_exit");
		salir.setIcon(icon);
		if (icon != null)
			if (min_height < icon.getIconHeight())
				min_height = icon.getIconHeight();

		// AÃ±adimos el botÃ³n de Salir
		salir.setBounds(this.getWidth() - icon.getIconWidth() - 2, 2,
				icon.getIconWidth(), icon.getIconHeight());
		salir.setBorderPainted(false);
		PluggableJTabbedPane.this.salir = salir.getBounds();

		// Logo de la empresa
		JLabel logo = new JLabel();
		icon = LogicConstants.getIcon("header_logo_cliente");
		if (min_height < icon.getIconHeight())
			min_height = icon.getIconHeight();

		logo.setIcon(icon);
		logo.setBounds(salir.getBounds().x - icon.getIconWidth() - 2, 2,
				icon.getIconWidth(), icon.getIconHeight());

		JLabel companyLogo = new JLabel();
		icon = LogicConstants.getIcon("header_logo");
		if (icon != null)
			if (min_height < icon.getIconHeight())
				min_height = icon.getIconHeight();
		companyLogo.setIcon(icon);
		companyLogo.setBounds(logo.getBounds().x - icon.getIconWidth(), 2,
				icon.getIconWidth(), icon.getIconHeight());

		botones_flotantes = new ArrayList<JComponent>();
		addFloatingButton(companyLogo);
		addFloatingButton(logo);
		addFloatingButton(salir);

		repaint();
	}

	@Override
	protected void finalize() throws Throwable {
		for (JFrame f : detached_tabs)
			f.dispose();

		super.finalize();
	}

	/**
	 * Separa una pestaÃ±a en un nuevo jframe con el tÃ­tulo que tenga la
	 * pestaÃ±a original
	 * 
	 * @param index
	 *            el indice de la pestaÃ±a
	 * @return Un {@link DetachedTab} que representa la pestaÃ±a en un
	 *         {@link JFrame}
	 * @throws IndexOutOfBoundsException
	 *             Si el indice de la pestaÃ±a no existe
	 */
	public DetachedTab detach(int index) {
		String title = getTitleAt(index);
		return detach(index, title);

	}

	/**
	 * Separa una pestaÃ±a en un nuevo jframe con el tÃ­tulo pasado como
	 * parÃ¡metro.
	 * 
	 * @param index
	 *            el indice de la pestaÃ±a.
	 * @param title
	 *            El tÃ­tulo de la ventana.
	 * @return Un {@link DetachedTab} que representa la pestaÃ±a en un
	 *         {@link JFrame}
	 * @throws IndexOutOfBoundsException
	 *             Si el indice de la pestaÃ±a no existe
	 */
	public DetachedTab detach(int index, String title) {
		log.trace("Detaching " + title);
		String tip = getToolTipTextAt(index);
		Icon icon = getIconAt(index);
		Component tab = getComponentAt(index);
		if (lastDetachablePane == null)
			lastDetachablePane = new DetachedTab(this, title, tip, index, tab,
					icon);
		else {
			lastDetachablePane.addTab(tab.getName(), icon, tab);
			detached_tabs.add(lastDetachablePane);
		}
		return lastDetachablePane;
	}

	/**
	 * @see PluggableJTabbedPane#detach(int index, String title)
	 * @param index
	 * @param title
	 * @return
	 */
	public DetachedTab detach(AbstractPluggable tab, String title) {
		log.trace("Detaching " + tab);
		String tip = tab.getToolTipText();
		Icon icon = tab.getIcon();
		if (tab.getNewDetachablePane() || lastDetachablePane == null)
			lastDetachablePane = new DetachedTab(this, title, tip,
					tab.getOrder(), tab, icon);
		else {
			lastDetachablePane.addTab(tab.getTitle(), icon, tab);
			detached_tabs.add(lastDetachablePane);
		}
		return lastDetachablePane;
	}

	public List<DetachedTab> getDetachedTabs() {
		return Collections.unmodifiableList(detached_tabs);
	}

	public void reattach(DetachedTab t) {
		detached_tabs.remove(t);
		insertTab(t.getTitle(), t.getIcon(), t.getDetachedTab(), t.getTip(),
				t.getOriginalPositon());
	}

	public Integer getMin_height() {
		return min_height;
	}
}

class MyTabbedPaneUI extends MetalTabbedPaneUI {

	protected final Log log = LogFactory.getLog(getClass());
	private PluggableJTabbedPane pane = null;

	public MyTabbedPaneUI(PluggableJTabbedPane pane) {
		super();
		this.pane = pane;
	}

	@Override
	public void update(Graphics g, JComponent c) {
		try {
			super.update(g, c);
		} catch (Throwable t) {
			log.error("Error al hacer update en el PluggableJTabbedPane", t);
		}
	}

	@Override
	protected int calculateMaxTabHeight(int tabPlacement) {
		int height = super.calculateMaxTabHeight(tabPlacement);
		if (height < pane.getMin_height())
			height = pane.getMin_height();
		return height;
	}

	@Override
	protected void installListeners() {
		super.installListeners();
		this.tabPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				BasicWindow.recolorAlert();

				if (((PluggableJTabbedPane) e.getSource()).getSelectedIndex() == 0) {
					Component c = ((PluggableJTabbedPane) e.getSource())
							.getSelectedComponent();

					if (c instanceof IMapViewer)
						((IMapViewer) c).updateGv();
				}
			}
		});
	}
}