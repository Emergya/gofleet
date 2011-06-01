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
 * 22/07/2009
 */
package es.emergya.ui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.NavigatableComponent;

import es.emergya.ui.gis.CustomMapView;
import es.emergya.ui.gis.RotatableView;

/**
 * @author fario
 * 
 */
public class ZoomControlPanel extends JPanel implements ActionListener {
	private JButton in, out, rcw, rccw;
	private NavigatableComponent nav;

	/**
	 * Creates a zoom control panel that will zoom in and out of a
	 * {@link CustomMapView}
	 * 
	 * @param nav
	 *            A {@link NavigatableComponent} in which we'll do zoom
	 */
	public ZoomControlPanel(NavigatableComponent nav) {
		super();
		this.nav = nav;
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		in = new JButton(es.emergya.cliente.constants.LogicConstants
				.getIcon("viewmag+"));
		in.addActionListener(this);
		out = new JButton(es.emergya.cliente.constants.LogicConstants
				.getIcon("viewmag-"));
		out.addActionListener(this);

		// rccw = new JButton("<<");
		// rccw.addActionListener(this);
		// rcw = new JButton(">>");
		// rcw.addActionListener(this);

		add(out);
		// add(rccw);
		add(Box.createHorizontalGlue());
		// add(rcw);
		add(in);

		updateUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == in)
			nav.zoomTo(nav.getCenter(), nav.getScale() - 0.01);
		else if (e.getSource() == out)
			nav.zoomTo(nav.getCenter(), nav.getScale() + 0.01);
		else if (e.getSource() == rccw && nav instanceof RotatableView)
			((RotatableView) nav).rotate(Math.PI / 180);
		else if (e.getSource() == rcw && nav instanceof RotatableView)
			((RotatableView) nav).rotate(-(Math.PI / 180));

		if (nav instanceof ZoomPerformed)
			((ZoomPerformed) nav).zoomPerformed();
	}
}
