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

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JFrame;
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
import org.gofleet.context.GoClassLoader;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.BasicWindow;

public abstract class GenericDialog<T> extends JFrame {
	private static final int yPad = 18;
	private static final int xPad = 6;
	private static final int initialY = 6;
	private static final int initialX = 6;
	private static final long serialVersionUID = -5899890654719038356L;
	protected int rows = 0;
	protected int cols = 2;
	protected JPanel mid;
	protected static final org.apache.commons.logging.Log log = LogFactory
			.getLog(GenericDialog.class);
	protected Object id = null;
	protected LinkedList<Component> componentes = new LinkedList<Component>();
	protected ActionListener saveListener = saveListener();
	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(GenericDialog.class);
	protected final ActionListener closeListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};

	protected void setObject(Object o) {
		this.id = o;
	}

	protected Object getObject() {
		return this.id;
	}

	public GenericDialog(T i, final String titulo, final String icon) {
		super();
		log.trace("GenericDialog(" + i + ")");
		setAlwaysOnTop(true);
		setResizable(false);
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(500, 500));
		setTitle(titulo);
		try {
			setIconImage(((BasicWindow) GoClassLoader.getGoClassLoader().load(
					BasicWindow.class)).getFrame().getIconImage());
		} catch (Throwable e) {
			LOG.error("There is no icon image", e);
		}
		JPanel base = new JPanel();
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
		base.setBackground(Color.WHITE);

		JPanel title = new JPanel(new FlowLayout(FlowLayout.LEADING));

		final JLabel labelTitle = new JLabel(titulo,
				LogicConstants.getIcon(icon), JLabel.LEFT);
		labelTitle.setFont(LogicConstants.deriveBoldFont(12f));

		title.setBackground(Color.WHITE);

		title.add(labelTitle);

		base.add(title);

		mid = new JPanel(new SpringLayout());
		mid.setBackground(Color.WHITE);

		loadDialog(i);

		SpringUtilities.makeCompactGrid(mid, rows, cols, initialX, initialY,
				xPad, yPad);
		base.add(mid);

		JPanel buttons = new JPanel();

		buttons.setBackground(Color.WHITE);
		JButton accept = new JButton(getString("Buttons.ok"),
				LogicConstants.getIcon("button_accept"));
		accept.addActionListener(closeListener);
		accept.addActionListener(saveListener);
		buttons.add(accept);

		JButton cancel = new JButton(getString("Buttons.cancel"),
				LogicConstants.getIcon("button_cancel"));
		cancel.addActionListener(closeListener);
		buttons.add(cancel);

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

	protected abstract void loadDialog(T i);

	protected abstract ActionListener saveListener();

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
		mid.add(new JLabel(getString(pairs[0][0]), JLabel.RIGHT));

		for (int i = 1; i < pairs.length; i++) {
			String[] pair = pairs[i];
			if (pair.length != 2)
				log.error("Par desconocido");
			else {
				panel.add(new JLabel(getString(pair[0]), JLabel.RIGHT));
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
				mid.add(new JLabel(getString(pair[0]), JLabel.RIGHT));
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
		mid.add(new JLabel(getString(label), JLabel.RIGHT));
		JTextField jtextField = new JTextField(texto);
		jtextField.setEditable(true);
		jtextField.setName(label);
		mid.add(jtextField);
		componentes.add(jtextField);
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addString_Fixed(String texto, String label) {
		rows++;
		mid.add(new JLabel(getString(label), JLabel.RIGHT));
		JTextField jtextField = new JTextField(texto);
		jtextField.setEditable(false);
		mid.add(jtextField);
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addComboBox(Object selected, Object[] options, String title) {
		rows++;
		mid.add(new JLabel(getString(title), JLabel.RIGHT));
		JComboBox cb = new JComboBox(options);
		cb.setName(title);
		cb.setSelectedItem(selected);
		mid.add(cb);
		componentes.add(cb);
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addDate(Date fecha, String title, boolean editable) {
		rows++;
		// TODO editable
		mid.add(new JLabel(getString(title), JLabel.RIGHT));
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		JCalendarCombo calendar = new JCalendarCombo();
		calendar.setEditable(editable);
		calendar.setPreferredSize(new Dimension(300, 30));
		calendar.setDate(fecha);
		calendar.setName(title);
		componentes.add(calendar);
		panel.add(calendar);
		JSpinner res = new JSpinner(new SpinnerDateModel());
		res.setName(title);
		res.setPreferredSize(new Dimension(60, 30));
		JSpinner.DateEditor startEditor = new JSpinner.DateEditor(res,
				"HH:mm:ss");
		startEditor.setEnabled(editable);
		res.setEditor(startEditor);
		componentes.add(res);
		if (fecha != null)
			res.setValue(fecha);
		panel.add(res);
		mid.add(panel);
		for (int i = 3; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}

	protected void addList(Object[] lista, String label) {
		rows++;
		mid.add(new JLabel(getString(label), JLabel.RIGHT));
		if (lista != null) {
			JList jlist = new JList(lista);
			final JScrollPane jScrollPane = new JScrollPane(jlist);
			jScrollPane.getViewport().setPreferredSize(new Dimension(100, 100));
			mid.add(jScrollPane);
		}
		for (int i = 2; i < cols; i++)
			mid.add(Box.createHorizontalGlue());
	}
}
