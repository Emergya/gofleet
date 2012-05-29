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
package es.emergya.ui.plugins.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpringLayout;

import org.apache.commons.logging.LogFactory;
import org.freixas.jcalendar.JCalendarCombo;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginType;

public abstract class FormGeneric extends Option {

	private static final int yPad = 18;
	private static final int xPad = 6;
	private static final int initialY = 6;
	private static final int initialX = 6;
	private static final long serialVersionUID = -5899890654927038356L;
	protected int rows = 0;
	protected int cols = 2;
	protected JPanel mid;
	protected static final org.apache.commons.logging.Log log = LogFactory
			.getLog(FormGeneric.class);
	private JPanel buttons;
	protected JPanel title;
	protected LinkedList<Component> componentes = new LinkedList<Component>();

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

	public FormGeneric(String title, int order) {
		super("", PluginType.getType("FORMS"), order, null);
		setTitle(i18n.getString(title));
		generatePanel();
	}

	/**
	 * Use to add the fields and labels to the form.
	 */
	protected abstract void initializeFields();

	/**
	 * Use to add "do" to the "ok" action
	 */
	protected abstract void doAction();

	public void generatePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);

		generateTitle();
		add(title);

		generateMiddle();
		add(mid);

		generateButtons();
		add(buttons);
	}

	protected void generateTitle() {
		title = new JPanel(new FlowLayout(FlowLayout.LEADING));
		final JLabel labelTitle = new JLabel(this.getTitle(), this.getIcon(),
				JLabel.LEFT);
		labelTitle.setFont(LogicConstants.deriveBoldFont(12f));
		title.setBackground(Color.WHITE);
		title.add(labelTitle);
	}

	protected void generateMiddle() {
		mid = new JPanel(new SpringLayout());
		mid.setBackground(Color.WHITE);
		initializeFields();
		SpringUtilities.makeCompactGrid(mid, rows, cols, initialX, initialY,
				xPad, yPad);
	}

	protected void generateButtons() {
		buttons = new JPanel();

		buttons.setBackground(Color.WHITE);
		JButton accept = new JButton(i18n.getString("Buttons.ok"),
				LogicConstants.getIcon("button_accept"));

		accept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FormGeneric.this.doAction();
				FormGeneric.this.reboot();
			}
		});

		JButton cancel = new JButton(i18n.getString("Buttons.cancel"),
				LogicConstants.getIcon("button_cancel"));
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FormGeneric.this.reboot();
			}
		});

		buttons.add(accept);
		buttons.add(cancel);
	}

	protected void addJoinedRow(String[][] pairs, Integer[] colsLength) {
		if (pairs == null || pairs.length == 0
				|| pairs.length != colsLength.length)
			return;
		rows++;

		int contador = 0;
		for (int i : colsLength)
			contador += i;
		final FlowLayout flowLayout = new FlowLayout();
		JPanel panel = new JPanel(flowLayout);
		panel.setOpaque(false);
		JTextField jtextField = new JTextField(pairs[0][1]);
		jtextField.setEditable(true);
		jtextField.setColumns(colsLength[0]);
		panel.add(jtextField);
		mid.add(new JLabel(i18n.getString(pairs[0][0]), JLabel.RIGHT));

		for (int i = 1; i < pairs.length; i++) {
			String[] pair = pairs[i];
			if (pair.length != 2)
				log.error("Par desconocido");
			else {
				panel.add(new JLabel(i18n.getString(pair[0]), JLabel.RIGHT));
				jtextField = new JTextField(pair[1]);
				jtextField.setEditable(true);
				panel.add(jtextField);
				jtextField.setColumns(colsLength[i]);
			}
		}
		if (contador < 100)
			for (int i = contador; i < 100; i += 5)
				panel.add(new JLabel("        "));

		mid.add(panel);

		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addRow(String[][] pairs) {
		rows++;
		if (pairs.length > cols)
			log.error("Se va a descuadrar");

		int columnas = 0;
		for (String[] pair : pairs) {
			if (pair.length != 2)
				log.error("Par desconocido");
			else {
				columnas += 2;
				mid.add(new JLabel(i18n.getString(pair[0]), JLabel.RIGHT));
				JTextField jtextField = new JTextField(pair[1]);
				jtextField.setEditable(true);
				mid.add(jtextField);
			}
		}
		while (columnas < cols) {
			mid.add(Box.createHorizontalGlue());
			columnas++;
		}
	}

	protected void addString(String texto, String label) {
		rows++;
		mid.add(new JLabel(i18n.getString(label), JLabel.RIGHT));
		JTextField jtextField = new JTextField(texto);
		jtextField.setName(label);
		jtextField.setEditable(true);
		componentes.add(jtextField);
		mid.add(jtextField);
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addString_Fixed(String texto, String label) {
		rows++;
		mid.add(new JLabel(i18n.getString(label), JLabel.RIGHT));
		JTextField jtextField = new JTextField(texto);
		jtextField.setEditable(false);
		mid.add(jtextField);
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addComboBox(Object selected, Object[] options, String title) {
		rows++;
		mid.add(new JLabel(i18n.getString(title), JLabel.RIGHT));
		JComboBox cb = new JComboBox(options);
		cb.setSelectedItem(selected);
		cb.setName(title);
		componentes.add(cb);
		mid.add(cb);
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addDate(Date fecha, String title, boolean editable) {
		rows++;
		// TODO editable
		mid.add(new JLabel(i18n.getString(title), JLabel.RIGHT));
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		JCalendarCombo calendar = new JCalendarCombo();
		calendar.setName(title);
		calendar.setEditable(editable);
		calendar.setPreferredSize(new Dimension(300, 30));
		calendar.setDate(fecha);
		panel.add(calendar);
		componentes.add(calendar);
		JSpinner res = new JSpinner(new SpinnerDateModel());
		res.setName(title);
		res.setPreferredSize(new Dimension(60, 30));
		res.setName(title);
		JSpinner.DateEditor startEditor = new JSpinner.DateEditor(res,
				"HH:mm:ss");
		startEditor.setEnabled(editable);
		res.setEditor(startEditor);
		if (fecha != null)
			res.setValue(fecha);
		componentes.add(res);
		panel.add(res);
		mid.add(panel);
		for (int i = 3; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addList(Object[] lista, String label) {
		rows++;
		mid.add(new JLabel(i18n.getString(label), JLabel.RIGHT));
		if (lista != null) {
			JList jlist = new JList(lista);
			final JScrollPane jScrollPane = new JScrollPane(jlist);
			jScrollPane.getViewport().setPreferredSize(new Dimension(100, 100));
			mid.add(jScrollPane);
		}
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	@Override
	public void reboot() {
		this.rows = 0;
		this.componentes.clear();
		this.remove(mid);
		this.remove(buttons);
		this.generateMiddle();
		this.add(mid);
		this.add(buttons);
		this.validate();
	}
}
