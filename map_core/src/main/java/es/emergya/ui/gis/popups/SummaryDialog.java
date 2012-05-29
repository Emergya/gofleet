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
package es.emergya.ui.gis.popups;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoClassLoader;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.HistoricoGPSConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.BasicWindow;

public class SummaryDialog extends JFrame {
	private static final String FORMAT = "%0" + LogicConstants.LONGITUD_ISSI
			+ "d";
	private static final long serialVersionUID = -299088272363122282L;

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(SummaryDialog.class);

	@GoWired
	public I18n i18n;

	/**
	 * @return the i18n
	 */
	public I18n getI18n() {
		return i18n;
	}

	/**
	 * @param i18n
	 *            the i18n to set
	 */
	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}

	public SummaryDialog(Recurso r) {

		super();
		r = (r == null) ? null : RecursoConsultas.getByIdentificador(r
				.getIdentificador());
		setAlwaysOnTop(true);
		setResizable(false);
		setName(r.getIdentificador());
		setBackground(Color.WHITE);
		setSize(600, 400);
		setTitle(i18n.getString("Resources.summary.titleWindow") + " "
				+ r.getIdentificador());
		try {
			setIconImage(((BasicWindow) GoClassLoader.getGoClassLoader().load(
					BasicWindow.class)).getFrame().getIconImage());
		} catch (Throwable e) {
			LOG.error("There is no icon image", e);
		}
		JPanel base = new JPanel();
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
		base.setBackground(Color.WHITE);

		r = RecursoConsultas.getByIdentificador(r.getIdentificador());

		// Icono del titulo
		JPanel title = new JPanel(new FlowLayout(FlowLayout.LEADING));

		final JLabel labelTitle = new JLabel(
				i18n.getString("Resources.summary.title") + " "
						+ r.getIdentificador(),
				LogicConstants.getIcon("tittleficha_icon_recurso"), JLabel.LEFT);
		labelTitle.setFont(LogicConstants.deriveBoldFont(12f));

		title.setBackground(Color.WHITE);

		title.add(labelTitle);

		base.add(title);

		// Nombre
		JPanel mid = new JPanel(new SpringLayout());
		mid.setBackground(Color.WHITE);
		mid.add(new JLabel(i18n.getString("Resources.name"), JLabel.RIGHT));
		JTextField name = new JTextField(25);
		if (r != null)
			name.setText(r.getIdentificador());
		name.setEditable(false);
		mid.add(name);
		// Patrulla
		mid.add(new JLabel(i18n.getString("Resources.squad"), JLabel.RIGHT));
		// JComboBox squads = new
		// JComboBox(PatrullaConsultas.getAll().toArray());
		JTextField squads = new JTextField(r.getPatrullas() == null ? null : r
				.getPatrullas().getNombre());
		// squads.setSelectedItem(r.getPatrullas());
		squads.setEditable(false);
		squads.setColumns(21);
		// squads.setEnabled(false);
		mid.add(squads);

		// Tipo
		mid.add(new JLabel(i18n.getString("Resources.type"), JLabel.RIGHT));
		JTextField types = new JTextField(r.getTipo());
		types.setEditable(false);
		mid.add(types);

		// Estado Eurocop
		mid.add(new JLabel(i18n.getString("Resources.status"), JLabel.RIGHT));
		JTextField status = new JTextField();
		if (r.getEstadoEurocop() != null)
			status.setText(r.getEstadoEurocop().getIdentificador());
		status.setEditable(false);
		mid.add(status);

		// Subflota
		mid.add(new JLabel(i18n.getString("Resources.subfleet"), JLabel.RIGHT));
		JTextField subfleets = new JTextField(r.getFlotas() == null ? null : r
				.getFlotas().getNombre());
		subfleets.setEditable(false);
		mid.add(subfleets);

		// Referencia Humana
		mid.add(new JLabel(i18n.getString("Resources.incidences"), JLabel.RIGHT));
		JTextField rHumana = new JTextField();
		// if (r.getIncidencias() != null)
		// rHumana.setText(r.getIncidencias().getReferenciaHumana());
		rHumana.setEditable(false);
		mid.add(rHumana);
		// dispositivo
		mid.add(new JLabel(i18n.getString("Resources.device"), JLabel.RIGHT));
		JTextField issi = new JTextField();
		issi.setEditable(false);
		mid.add(issi);
		mid.add(new JLabel(i18n.getString("Resources.enabled"), JLabel.RIGHT));
		JCheckBox enabled = new JCheckBox("", true);
		enabled.setEnabled(false);
		enabled.setOpaque(false);
		if (r.getDispositivo() != null) {
			final String valueOf = String.valueOf(r.getDispositivo());
			try {
				issi.setText(String.format(FORMAT, r.getDispositivo()));
			} catch (Throwable t) {
				issi.setText(valueOf);
			}
			enabled.setSelected(r.getHabilitado());
		}
		mid.add(enabled);

		// Fecha ultimo gps
		mid.add(new JLabel(i18n.getString("Resources.lastPosition"),
				JLabel.RIGHT));
		JTextField lastGPS = new JTextField();

		final Date lastGPSDateForRecurso = HistoricoGPSConsultas
				.lastGPSDateForRecurso(r);
		if (lastGPSDateForRecurso != null) {
			lastGPS.setText(SimpleDateFormat.getDateTimeInstance().format(
					lastGPSDateForRecurso));
		}
		lastGPS.setEditable(false);
		mid.add(lastGPS);

		// Espacio en blanco
		mid.add(Box.createHorizontalGlue());
		mid.add(Box.createHorizontalGlue());

		// Espacio en blanco
		mid.add(Box.createHorizontalGlue());
		mid.add(Box.createHorizontalGlue());

		SpringUtilities.makeCompactGrid(mid, 5, 4, 6, 6, 6, 18);
		base.add(mid);

		// informacion adicional
		JPanel infoPanel = new JPanel(new SpringLayout());
		JTextField info = new JTextField(25);
		info.setText(r.getInfoAdicional());
		info.setEditable(false);
		infoPanel.setOpaque(false);
		infoPanel.add(new JLabel(i18n.getString("Resources.info")));
		infoPanel.add(info);
		SpringUtilities.makeCompactGrid(infoPanel, 1, 2, 6, 6, 6, 18);
		base.add(infoPanel);

		JPanel buttons = new JPanel();

		buttons.setBackground(Color.WHITE);
		JButton accept = new JButton(i18n.getString("Buttons.ok"),
				LogicConstants.getIcon("button_accept"));

		accept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttons.add(accept);
		base.add(buttons);

		getContentPane().add(base);
		pack();
		int x;
		int y;

		Container myParent;
		try {
			myParent = ((BasicWindow) GoClassLoader.getGoClassLoader().load(
					BasicWindow.class)).getFrame().getContentPane();
			java.awt.Point topLeft = myParent.getLocationOnScreen();
			Dimension parentSize = myParent.getSize();

			Dimension mySize = getSize();

			if (parentSize.width > mySize.width)
				x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
			else
				x = topLeft.x;

			if (parentSize.height > mySize.height)
				y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
			else
				y = topLeft.y;

			setLocation(x, y);
		} catch (Throwable e1) {
			LOG.error("There is no basic window!", e1);
		}
	}
}
