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
package es.emergya.ui.plugins.admin.aux1;

import static es.emergya.cliente.constants.LogicConstants.getIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.FlotaConsultas;
import es.emergya.consultas.RolConsultas;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.plugins.AdminPanel.SaveOrUpdateAction;

/**
 * 
 * @author fario
 * @author marias
 * 
 */
public abstract class SummaryAction extends AbstractAction {

	private static final Dimension DIMENSION_COMBO = new Dimension(250, 22);
	static final Log log = LogFactory.getLog(SummaryAction.class);
	private static final int FILTER_HEIGHT = 70;
	private static final int HEIGHT = 250;
	private static final int BUTTON_WIDTH = 45;
	private static final int PANEL_WIDTH = 240;
	private final int textfieldSize = 40;
	private static final long serialVersionUID = -2871120480603090949L;
	protected boolean cambios = false;
	protected List<Object> leftItems = new ArrayList<Object>();
	protected List<Object> rightItems = new ArrayList<Object>();
	private DocumentListener changeListener = new DocumentListener() {

		@Override
		public void changedUpdate(DocumentEvent arg0) {
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			cambios = true;
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			cambios = true;
		}
	};
	final ActionListener changeSelectionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			cambios = true;
		}
	};
	final protected JTextField textfieldCabecera = new JTextField();
	final protected JTextField textfieldPie = new JTextField();
	final protected JList left = new JList();
	final protected JList right = new JList();
	final protected JComboBox iconos = new JComboBox(new DefaultComboBoxModel());
	protected boolean isNew = false;
	private JLabel i = new JLabel();
	protected final JCheckBox habilitado = new JCheckBox();
	protected final JCheckBox administrador = new JCheckBox();
	protected final JComboBox rol = new JComboBox(new DefaultComboBoxModel());
	protected final JTextField apellidos = new JTextField(textfieldSize);
	protected final JTextField nombre = new JTextField(textfieldSize);
	protected final JPasswordField contrasenya = new JPasswordField(
			textfieldSize / 3);
	protected final JPasswordField repetir = new JPasswordField(
			textfieldSize / 3);
	protected final JComboBox comboTipoCapa = new JComboBox(
			new DefaultComboBoxModel());
	protected final JButton izquierda = new JButton(
			LogicConstants.getIcon("button_left"));
	protected boolean abriendo = false;

	@GoWired
	private BasicWindow basicWindow;

	/**
	 * @return the basicWindow
	 */
	public BasicWindow getBasicWindow() {
		return basicWindow;
	}

	/**
	 * @param basicWindow
	 *            the basicWindow to set
	 */
	public void setBasicWindow(BasicWindow basicWindow) {
		this.basicWindow = basicWindow;
	}

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

	/**
	 * 
	 * @param o
	 *            objeto a borrar
	 */
	public SummaryAction(Object o) {
		super(null, getIcon("Admin.summary"));
		iconos.setPreferredSize(DIMENSION_COMBO);
		rol.setPreferredSize(DIMENSION_COMBO);
		comboTipoCapa.setPreferredSize(DIMENSION_COMBO);
		isNew = (o == null);
		textfieldPie.getDocument().addDocumentListener(changeListener);
		textfieldCabecera.getDocument().addDocumentListener(changeListener);
		nombre.getDocument().addDocumentListener(changeListener);
		apellidos.getDocument().addDocumentListener(changeListener);
		contrasenya.getDocument().addDocumentListener(changeListener);
		administrador.addActionListener(changeSelectionListener);
		habilitado.addActionListener(changeSelectionListener);
		rol.addActionListener(changeSelectionListener);
		habilitado.setOpaque(false);
		administrador.setOpaque(false);
		comboTipoCapa.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cambios = true;
			}
		});
	}

	protected JFrame d = null;

	@Override
	public void actionPerformed(ActionEvent e) {
		// Utilizamos un lock sobre el objeto para evitar abrir varias fichas
		// del mismo objeto ya que las acciones a realizar se lanzan en un
		// SwingWorker
		// sobre el que no tenemos garantizado el instante de ejecución, lo que
		// puede hacer que se llame varias veces a getSummaryDialog().
		synchronized (this) {
			if (abriendo) {
				if (log.isTraceEnabled()) {
					log.trace("Ya hay otra ventana "
							+ this.getClass().getName() + " abriéndose...");
				}

				return;
			}
			abriendo = true;
		}
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				try {
					if (d == null || !d.isShowing()) {
						d = getSummaryDialog();
					}
					if (d != null) {
						d.pack();
						int x;
						int y;

						Container myParent = getBasicWindow()
								.getPluginContainer().getDetachedTab(0);
						Point topLeft = myParent.getLocationOnScreen();
						Dimension parentSize = myParent.getSize();

						Dimension mySize = d.getSize();

						if (parentSize.width > mySize.width) {
							x = ((parentSize.width - mySize.width) / 2)
									+ topLeft.x;
						} else {
							x = topLeft.x;
						}

						if (parentSize.height > mySize.height) {
							y = ((parentSize.height - mySize.height) / 2)
									+ topLeft.y;
						} else {
							y = topLeft.y;
						}

						d.setLocation(x, y);
					} else {
						log.error("No pude abrir la ficha por un motivo desconocido");
					}
					return null;
				} catch (Throwable t) {
					log.error("Error al abrir la ficha", t);
					return null;
				}
			}

			@Override
			protected void done() {
				if (d != null) {
					d.setVisible(true);
					d.setExtendedState(JFrame.NORMAL);
					d.setAlwaysOnTop(true);
					d.requestFocus();
				}
				abriendo = false;
				if (log.isTraceEnabled()) {
					log.info("Swingworker " + this.getClass().getName()
							+ " finalizado. Abriendo = false");
				}
			}
		};

		sw.execute();
	}

	protected abstract JFrame getSummaryDialog();

	@SuppressWarnings("unchecked")
	protected JFrame generateSimpleDialog(final String label_cabecera,
			final String label_pie, final String titulo,
			final SaveOrUpdateAction guardar, final Icon icono,
			final String title) {
		try {
			final JFrame d = createJDialog(titulo);

			JPanel cabecera = buildCabecera(label_cabecera, textfieldSize,
					icono, title, null);
			d.add(cabecera, BorderLayout.NORTH);

			JPanel central = buildSimpleCentral(label_pie, guardar, d);
			d.add(central, BorderLayout.CENTER);

			guardar.setFrame(d);
			return d;
		} catch (Throwable t) {
			log.error("Error al cargar el summary dialog", t);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private JPanel buildSimpleCentral(final String labelPie,
			final SaveOrUpdateAction guardar, final JFrame d) {
		JPanel central = new JPanel(new GridBagLayout());
		central.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 1, 2, 1);
		gbc.gridwidth = 1;
		central.add(Box.createVerticalStrut(10), gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel nombreLbl = new JLabel("Nombre:", SwingConstants.RIGHT);
		central.add(nombreLbl, gbc);
		gbc.gridx++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(nombre, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel apellidosLbl = new JLabel("Apellidos: ", SwingConstants.RIGHT);
		central.add(apellidosLbl, gbc);
		gbc.gridx++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(apellidos, gbc);
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel rolLbl = new JLabel("Rol:", SwingConstants.RIGHT);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		central.add(rolLbl, gbc);
		gbc.gridx++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(rol, gbc);
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel contrasenyaLbl = new JLabel("Contraseña:", SwingConstants.RIGHT);
		gbc.gridy++;
		gbc.gridx = 0;
		central.add(contrasenyaLbl, gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(contrasenya, gbc);
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel contrasenya2Lbl = new JLabel("Repetir Contraseña:",
				SwingConstants.RIGHT);
		gbc.gridx++;
		central.add(contrasenya2Lbl, gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(repetir, gbc);
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel administradorLbl = new JLabel("Administrador:",
				SwingConstants.RIGHT);
		gbc.gridy++;
		gbc.gridx = 0;
		central.add(administradorLbl, gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(administrador, gbc);
		administrador.setOpaque(false);
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel habilitadoLbl = new JLabel("Habilitado", SwingConstants.RIGHT);
		gbc.gridx++;
		central.add(habilitadoLbl, gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(habilitado, gbc);
		habilitado.setOpaque(false);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel labl_pie = new JLabel(labelPie, JLabel.LEFT);
		central.add(labl_pie, gbc);
		textfieldPie.setColumns(textfieldSize);
		labl_pie.setLabelFor(textfieldPie);
		gbc.gridx++;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.LINE_START;
		central.add(textfieldPie, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		central.add(Box.createVerticalStrut(10), gbc);

		gbc.gridwidth = 2;
		gbc.gridy++;
		gbc.gridx = 1;
		JPanel botones = getBotonesSalir(guardar, d, 250);
		central.add(botones, gbc);

		((DefaultComboBoxModel) rol.getModel()).removeAllElements();
		for (String r : RolConsultas.getAllNames()) {
			((DefaultComboBoxModel) rol.getModel()).addElement(r);
		}

		return central;
	}

	private JPanel getBotonesSalir(final SaveOrUpdateAction guardar,
			final JFrame d, int width) {
		JPanel botones = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		botones.setPreferredSize(new Dimension(width, 40));
		botones.setOpaque(false);
		botones.add(getGuardarBtn(guardar));
		botones.add(getCancelBtn(d));
		return botones;
	}

	private JButton getCancelBtn(final JFrame d) {
		JButton cancelar = new JButton("Cancelar",
				LogicConstants.getIcon("button_cancel"));
		cancelar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (cambios) {
					int res = JOptionPane
							.showConfirmDialog(
									d,
									"Existen cambios sin guardar. ¿Seguro que desea cerrar la ventana?",
									"Cambios sin guardar",
									JOptionPane.OK_CANCEL_OPTION);
					if (res != JOptionPane.CANCEL_OPTION) {
						d.dispose();
					}
				} else {
					d.dispose();
				}
			}
		});
		return cancelar;
	}

	@SuppressWarnings("unchecked")
	protected JFrame generateUrlDialog(final String label_cabecera,
			final String label_pie, final String centered_label,
			final String titulo, final Object[] left_items,
			final Object[] right_items, final String left_label,
			final String right_label, final SaveOrUpdateAction guardar,
			final Icon icono, final String title, final Boolean habilitada,
			final Boolean tipoCapa, final String url) {
		int width_listas = SummaryAction.PANEL_WIDTH;
		if (left_items == null)
			width_listas = width_listas * 3 / 2;
		final Dimension dimensionList = new Dimension(width_listas,
				SummaryAction.HEIGHT);
		final Dimension dimensionPanel = new Dimension(
				SummaryAction.PANEL_WIDTH + 15, SummaryAction.HEIGHT
						+ SummaryAction.FILTER_HEIGHT + 15);
		final Dimension dimensionButtonPanel = new Dimension(
				SummaryAction.BUTTON_WIDTH, SummaryAction.HEIGHT);
		final Dimension dimensionFilterLateral = new Dimension(
				SummaryAction.PANEL_WIDTH, SummaryAction.FILTER_HEIGHT);

		if (d != null) {
			d.setVisible(true);
			return d;
		}
		d = createJDialog(titulo);

		JPanel cabecera = buildCabecera(label_cabecera, textfieldSize, icono,
				title, null);
		JPanel caracteristicas = new JPanel(new FlowLayout());
		caracteristicas.setOpaque(false);
		caracteristicas.add(new JLabel("Tipo de Capa: "));
		((DefaultComboBoxModel) comboTipoCapa.getModel()).removeAllElements();
		((DefaultComboBoxModel) comboTipoCapa.getModel()).addElement("Base");
		((DefaultComboBoxModel) comboTipoCapa.getModel())
				.addElement("Opcional");
		caracteristicas.add(comboTipoCapa);
		caracteristicas.add(new JLabel("Habilitada:"));
		caracteristicas.add(habilitado);
		cabecera.add(caracteristicas, BorderLayout.SOUTH);
		d.add(cabecera, BorderLayout.NORTH);

		JPanel urlPanel = new JPanel(new FlowLayout());
		urlPanel.setOpaque(false);

		urlPanel.add(new JLabel("URL:"));
		urlPanel.add(nombre);

		JPanel central = buildCentralUrl(centered_label, left_items,
				right_items, left_label, right_label, textfieldSize,
				dimensionList, dimensionPanel, dimensionButtonPanel,
				dimensionFilterLateral, urlPanel);

		d.add(central, BorderLayout.CENTER);

		JPanel pie = buildPie(label_pie, guardar, textfieldSize, d);
		d.add(pie, BorderLayout.SOUTH);

		if (guardar != null) {
			guardar.setFrame(d);
		}

		return d;
	}

	@SuppressWarnings("unchecked")
	protected JFrame generateIconDialog(final String label_cabecera,
			final String label_pie, final String centered_label,
			final String titulo, final Object[] left_items,
			final Object[] right_items, final String left_label,
			final String right_label, final SaveOrUpdateAction guardar,
			final Icon icono, final String title,
			final String icono_seleccionado) {
		final Dimension dimensionList = new Dimension(
				SummaryAction.PANEL_WIDTH, SummaryAction.HEIGHT);
		final Dimension dimensionPanel = new Dimension(
				SummaryAction.PANEL_WIDTH + 5, SummaryAction.HEIGHT
						+ SummaryAction.FILTER_HEIGHT + 5);
		final Dimension dimensionButtonPanel = new Dimension(
				SummaryAction.BUTTON_WIDTH, SummaryAction.HEIGHT);
		final Dimension dimensionFilterLateral = new Dimension(
				SummaryAction.PANEL_WIDTH, SummaryAction.FILTER_HEIGHT);

		final JFrame d = createJDialog(titulo);
		d.setResizable(false);
		JPanel cabecera = buildCabecera(label_cabecera, textfieldSize, icono,
				title, icono_seleccionado);
		d.add(cabecera, BorderLayout.NORTH);

		JPanel central = buildCentral(centered_label, left_items, right_items,
				left_label, right_label, textfieldSize, dimensionList,
				dimensionPanel, dimensionButtonPanel, dimensionFilterLateral);
		d.add(central, BorderLayout.CENTER);

		JPanel pie = buildPie(label_pie, guardar, textfieldSize, d);
		d.add(pie, BorderLayout.SOUTH);

		if (guardar != null) {
			guardar.setFrame(d);
		}

		return d;
	}

	private JFrame createJDialog(final String titulo) {
		final JFrame d = new JFrame(titulo);
		d.setResizable(false);
		d.setAlwaysOnTop(true);
		d.setIconImage(getBasicWindow().getIconImage());
		d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		d.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				if (cambios) {
					int res = JOptionPane
							.showConfirmDialog(
									d,
									"Existen cambios sin guardar. ¿Seguro que desea cerrar la ventana?",
									"Cambios sin guardar",
									JOptionPane.OK_CANCEL_OPTION);
					if (res != JOptionPane.CANCEL_OPTION) {
						e.getWindow().dispose();
					}
				} else {
					e.getWindow().dispose();
				}
			}
		});
		d.setLayout(new BorderLayout(5, 5));
		d.setBackground(Color.WHITE);
		d.getContentPane().setBackground(Color.WHITE);
		return d;
	}

	private JPanel buildCentralUrl(final String centered_label,
			final Object[] left_items, final Object[] right_items,
			final String left_label, final String right_label,
			final int textfieldSize, final Dimension dimensionList,
			final Dimension dimensionPanel,
			final Dimension dimensionButtonPanel,
			final Dimension dimensionFilterLateral, JPanel cabecera) {

		JPanel central = buildCenter(centered_label);
		GridBagConstraints gbc = new GridBagConstraints();
		buildJList(dimensionList, left_items, left, false);
		JScrollPane scrollleft = addScrollPane(dimensionList, left);
		buildJList(dimensionList, right_items, right, false);
		JScrollPane scrollright = addScrollPane(dimensionList, right);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(10, 10, 10, 10);

		gbc.gridwidth = 5;
		central.add(cabecera, gbc);
		gbc.gridwidth = 1;
		gbc.gridy++;

		JPanel leftPanel = null;
		if (left_items != null) {
			leftPanel = buildLateral(left_label, dimensionPanel,
					dimensionFilterLateral, scrollleft, left, true);

			leftItems.clear();
			for (Object o : ((DefaultListModel) left.getModel()).toArray()) {
				leftItems.add(o);
			}
			central.add(leftPanel, gbc);

			gbc.gridx++;
			JPanel botones = buildBotones(dimensionButtonPanel, left, right);
			central.add(botones, gbc);

			gbc.gridx++;
			right.setEnabled(true);
			comboTipoCapa.setEnabled(true);
			habilitado.setEnabled(true);
		} else {
			right.setEnabled(false);
		}
		JPanel rightPanel = buildLateral(right_label, dimensionPanel,
				dimensionFilterLateral, scrollright, right, false);
		rightItems.clear();
		for (Object o : ((DefaultListModel) right.getModel()).toArray()) {
			rightItems.add(o);
		}

		central.add(rightPanel, gbc);

		if (left_items != null) {
			final JButton up = new JButton(LogicConstants.getIcon("button_up"));
			up.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					reorder(0, right.getModel().getSize() - 1);
				}
			});
			up.setBorderPainted(false);
			up.setOpaque(false);
			up.setContentAreaFilled(false);
			final JButton down = new JButton(
					LogicConstants.getIcon("button_down"));
			down.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					reorder(right.getModel().getSize() - 1, 0);
				}
			});
			down.setBorderPainted(false);
			down.setOpaque(false);
			down.setContentAreaFilled(false);

			JPanel upDown = new JPanel(new FlowLayout());
			upDown.setOpaque(false);
			upDown.add(up);
			upDown.add(down);
			upDown.setPreferredSize(new Dimension(SummaryAction.BUTTON_WIDTH,
					100));

			gbc.gridx++;
			central.add(upDown, gbc);
		}
		JPanel resultado = new JPanel(new BorderLayout(10, 5));
		final TitledBorder titledBorder = new TitledBorder(centered_label);
		resultado.setBorder(titledBorder);
		resultado.setOpaque(false);
		resultado.add(central, BorderLayout.CENTER);

		JPanel res = new JPanel(new BorderLayout());
		res.setOpaque(false);
		res.setBorder(new EmptyBorder(0, 15, 0, 15));
		res.add(resultado);
		return res;
	}

	private void reorder(int inicio, int fin) {

		boolean sentido = inicio < fin;

		LinkedList<Object> aSubir = new LinkedList<Object>();
		LinkedList<Object> resultado = new LinkedList<Object>();

		for (Object o : right.getSelectedValues()) {
			aSubir.add(o);
		}

		final DefaultListModel defaultListModel = (DefaultListModel) right
				.getModel();

		if (log.isTraceEnabled()) {
			log.trace("Elementos seleccionados:");
			for (Object o : aSubir) {
				log.trace(o + " " + o.getClass());
			}
		}

		for (int i = inicio; (sentido ? i <= fin : fin <= i); i = (sentido ? i + 1
				: i - 1)) {
			Object o = defaultListModel.get(i);
			if (aSubir.contains(o) && i != inicio) {
				Object siguiente = resultado.pollLast();
				log.trace("Cambiamos " + o + " por " + siguiente);
				resultado.add(o);
				resultado.add(siguiente);
			} else {
				log.trace("Añadimos " + o);
				resultado.add(o);
			}
		}

		((DefaultListModel) right.getModel()).removeAllElements();
		log.trace("Nueva lista: ");

		int inicio2 = (sentido ? 0 : resultado.size() - 1);
		int fin2 = (sentido ? resultado.size() - 1 : 0);
		for (int i = inicio2; (sentido ? i <= fin2 : fin2 <= i); i = (sentido ? i + 1
				: i - 1)) {
			Object o = resultado.get(i);
			log.trace("Nueva lista >" + o);
			((DefaultListModel) right.getModel()).addElement(o);
		}

		int seleccion[] = new int[aSubir.size()];
		int k = 0;
		for (Integer i = 0; i < right.getModel().getSize(); i++) {
			if (aSubir.contains(right.getModel().getElementAt(i))) {
				seleccion[k++] = i;
			}
		}

		right.setSelectedIndices(seleccion);

		right.updateUI();
	}

	private JPanel buildCentral(final String centered_label,
			final Object[] left_items, final Object[] right_items,
			final String left_label, final String right_label,
			final int textfieldSize, final Dimension dimension,
			final Dimension dimension1, final Dimension dimension2,
			final Dimension dimension3) {
		JPanel central = buildCenter(centered_label);
		GridBagConstraints gbc = new GridBagConstraints();
		buildJList(dimension, left_items, left);
		JScrollPane scrollleft = addScrollPane(dimension, left);
		buildJList(dimension, right_items, right);
		JScrollPane scrollright = addScrollPane(dimension, right);

		gbc.gridx = 0;
		gbc.insets = new Insets(10, 10, 10, 10);
		JPanel leftPanel = null;
		if (left_items != null) {
			leftPanel = buildLateral(left_label, dimension1, dimension3,
					scrollleft, left, true);

			leftItems.clear();
			for (Object o : ((DefaultListModel) left.getModel()).toArray()) {
				leftItems.add(o);
			}
			central.add(leftPanel, gbc);

			gbc.gridx++;
			JPanel botones = buildBotones(dimension2, left, right);
			central.add(botones, gbc);

			gbc.gridx++;
			right.setEnabled(true);
			comboTipoCapa.setEnabled(true);
			habilitado.setEnabled(true);
		} else {
			right.setEnabled(false);
		}
		JPanel rightPanel = buildLateral(right_label, dimension1, dimension3,
				scrollright, right, false);
		rightItems.clear();
		for (Object o : ((DefaultListModel) right.getModel()).toArray()) {
			rightItems.add(o);
		}

		central.add(rightPanel, gbc);

		JPanel resultado = new JPanel(new BorderLayout(10, 5));
		final TitledBorder titledBorder = new TitledBorder(centered_label);
		resultado.setBorder(titledBorder);
		resultado.setOpaque(false);
		resultado.add(central, BorderLayout.CENTER);

		JPanel res = new JPanel(new BorderLayout());
		res.setOpaque(false);
		res.setBorder(new EmptyBorder(0, 15, 0, 15));
		res.add(resultado);
		return res;
	}

	private JPanel buildLateral(final String topLabel,
			final Dimension dimension1, final Dimension dimensionFilterLateral,
			JScrollPane scrollList, JList list, boolean left) {
		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setOpaque(false);
		leftPanel.setMinimumSize(dimension1);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		JPanel left_filtro = buildPanelFilter(topLabel, textfieldSize - 21,
				dimensionFilterLateral, list, left);
		leftPanel.add(left_filtro, gbc);
		gbc.gridy++;

		leftPanel.add(scrollList, gbc);

		return leftPanel;
	}

	private JPanel buildPanelFilter(final String topLabel,
			final int textfieldSize, final Dimension dimension,
			final JList list, final boolean left) {
		JPanel left_filtro = new JPanel(new GridBagLayout());
		left_filtro.setPreferredSize(dimension);
		left_filtro.setOpaque(false);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		left_filtro.add(new JLabel(topLabel, JLabel.LEFT), gbc);

		final JTextField filtro = new JTextField(textfieldSize);
		gbc.gridy++;
		left_filtro.add(filtro, gbc);

		AbstractAction actionStartFilter = new AbstractAction(null,
				getIcon(i18n.getString("Buttons.noFiltrar"))) {

			private static final long serialVersionUID = -4737487889360372801L;

			@Override
			public void actionPerformed(ActionEvent e) {
				((DefaultListModel) list.getModel()).removeAllElements();
				filtro.setText(null);
				if (left) {
					for (Object obj : leftItems) {
						((DefaultListModel) list.getModel()).addElement(obj);
					}
				} else {
					for (Object obj : rightItems) {
						((DefaultListModel) list.getModel()).addElement(obj);
					}
				}

			}
		};
		AbstractAction actionStopFilter = new AbstractAction(null,
				getIcon(i18n.getString("Buttons.filtrar"))) {

			private static final long serialVersionUID = 6570608476764008290L;

			@Override
			public void actionPerformed(ActionEvent e) {
				((DefaultListModel) list.getModel()).removeAllElements();
				if (left) {
					for (Object obj : leftItems) {
						if (compare(filtro, obj)) {
							((DefaultListModel) list.getModel())
									.addElement(obj);
						}
					}
				} else {
					for (Object obj : rightItems) {
						if (compare(filtro, obj)) {
							((DefaultListModel) list.getModel())
									.addElement(obj);
						}
					}
				}
			}

			private boolean compare(final JTextField filtro, Object obj) {
				final String elemento = obj.toString().toUpperCase().trim();
				final String text = filtro.getText().toUpperCase().trim();

				final String pattern = text.replace("*", ".*");
				boolean res = Pattern.matches(pattern, elemento);

				return res;// || elemento.indexOf(text) >= 0;
			}
		};
		JButton jButton = new JButton(actionStartFilter);
		JButton jButton2 = new JButton(actionStopFilter);
		jButton.setBorderPainted(false);
		jButton2.setBorderPainted(false);
		jButton.setContentAreaFilled(false);
		jButton2.setContentAreaFilled(false);
		jButton.setPreferredSize(new Dimension(
				jButton.getIcon().getIconWidth(), jButton.getIcon()
						.getIconHeight()));
		jButton2.setPreferredSize(new Dimension(jButton2.getIcon()
				.getIconWidth(), jButton2.getIcon().getIconHeight()));

		gbc.gridx++;
		left_filtro.add(jButton2, gbc);
		gbc.gridx++;
		left_filtro.add(jButton, gbc);
		return left_filtro;
	}

	private JScrollPane addScrollPane(final Dimension dimension,
			final JList list) {
		JScrollPane scrollleft = new JScrollPane(list);
		scrollleft.setOpaque(false);
		scrollleft
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollleft.getViewport().setOpaque(false);
		scrollleft.setPreferredSize(dimension);
		return scrollleft;
	}

	private JList buildJList(final Dimension dimension, final Object[] items,
			final JList list) {
		return buildJList(dimension, items, list, true);
	}

	private JList buildJList(final Dimension dimension, final Object[] items,
			final JList list, final boolean sort) {
		if (items == null) {
			return null;
		}
		list.setModel(new DefaultListModel() {

			@Override
			public void addElement(Object obj) {
				boolean inserted = false;
				if (sort) {
					for (int i = 0; i < this.getSize() && !inserted; i++) {
						try {
							final String comparator = ((obj == null) ? "" : obj
									.toString());
							if (this.get(i).toString().compareTo(comparator) > 0) {
								this.add(i, obj);
								inserted = true;
							}
						} catch (Throwable t) {
							log.error("Error al ordenar a " + obj
									+ " y no lo incluimos", t);
							inserted = true;
						}
					}
				}
				if (!inserted) {
					super.addElement(obj);
				}
			}
		});
		log.trace("Lista con " + items.length + " objetos");
		for (Object obj : items) {
			((DefaultListModel) list.getModel()).addElement(obj);
		}
		list.setCellRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = -987995602141400182L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel c = new JLabel();
				c.setText(value.toString());

				if (isSelected) {
					c.setOpaque(true);
					c.setBackground(Color.YELLOW);
				}
				return c;
			}
		});
		list.setMinimumSize(dimension);
		list.setFixedCellHeight(22);
		return list;
	}

	@SuppressWarnings("unchecked")
	private JPanel buildPie(final String label_pie,
			final SaveOrUpdateAction guardar, final int textfieldSize,
			final JFrame d) {
		GridBagConstraints gbc;
		JPanel pie = new JPanel(new GridBagLayout());
		pie.setBorder(new EmptyBorder(5, 15, 15, 15));
		pie.setOpaque(false);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(15, 15, 15, 15);
		gbc.gridwidth = 1;
		JLabel labl_pie = new JLabel(label_pie, JLabel.LEFT);
		pie.add(labl_pie, gbc);
		textfieldPie.setColumns(textfieldSize);
		textfieldPie.getDocument().addDocumentListener(changeListener);
		labl_pie.setLabelFor(textfieldPie);
		gbc.gridx++;
		gbc.gridwidth = 1;
		pie.add(textfieldPie, gbc);
		gbc.gridy++;
		gbc.gridx = 1;
		gbc.insets = new Insets(15, -90, 15, 15);
		JPanel botones = getBotonesSalir(guardar, d, 200);
		pie.add(botones, gbc);
		return pie;
	}

	private JButton getGuardarBtn(SaveOrUpdateAction guardar) {
		JButton guardarBtn = new JButton(guardar);
		if (isNew) {
			guardarBtn.setText("Crear");
			guardarBtn.setIcon(getIcon("button_crear"));
		} else {
			guardarBtn.setText("Guardar");
			guardarBtn.setIcon(getIcon("button_save"));
		}
		return guardarBtn;
	}

	private JPanel buildBotones(final Dimension dimension2, final JList left,
			final JList right) {

		JPanel botones = new JPanel(new GridBagLayout());
		botones.setPreferredSize(dimension2);
		JButton derecha = new JButton(LogicConstants.getIcon("button_right"));
		derecha.setBorderPainted(false);
		derecha.setOpaque(false);
		derecha.setContentAreaFilled(false);
		derecha.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cambios = true;
				for (Object o : left.getSelectedValues()) {
					((DefaultListModel) right.getModel()).addElement(o);
					rightItems.add(o);
				}
				for (Object o : left.getSelectedValues()) {
					((DefaultListModel) left.getModel()).removeElement(o);
					leftItems.remove(o);
				}
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		botones.add(derecha, gbc);
		izquierda.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cambios = true;
				for (Object o : right.getSelectedValues()) {
					((DefaultListModel) left.getModel()).addElement(o);
					leftItems.add(o);
				}
				for (Object o : right.getSelectedValues()) {
					((DefaultListModel) right.getModel()).removeElement(o);
					rightItems.remove(o);
				}
			}
		});
		gbc.gridy++;
		izquierda.setBorderPainted(false);
		izquierda.setOpaque(false);
		izquierda.setContentAreaFilled(false);
		botones.add(izquierda, gbc);
		botones.setOpaque(false);
		return botones;
	}

	private JPanel buildCabecera(final String label_cabecera,
			final int textfieldSize, final Icon icono, final String titulo,
			final String icono_seleccionado) {
		JLabel title = new JLabel(titulo, icono, SwingConstants.LEFT);
		title.setFont(LogicConstants.deriveBoldFont(12f));
		JPanel resultado = new JPanel(new BorderLayout(2, 2));
		resultado.setOpaque(false);
		resultado.add(title, BorderLayout.NORTH);
		JPanel cabecera = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		cabecera.setOpaque(false);

		JLabel nombre = new JLabel(label_cabecera, JLabel.RIGHT);
		gbc.gridx = 0;
		gbc.gridy = 0;
		cabecera.add(nombre, gbc);
		textfieldCabecera.setColumns(textfieldSize);
		textfieldCabecera.getDocument().addDocumentListener(changeListener);
		nombre.setLabelFor(textfieldCabecera);
		gbc.gridx++;
		cabecera.add(textfieldCabecera, gbc);

		if (icono_seleccionado != null) {
			JLabel nombre2 = new JLabel("Icono:", JLabel.RIGHT);
			gbc.gridx = 0;
			gbc.gridy++;
			cabecera.add(nombre2, gbc);
			((DefaultComboBoxModel) iconos.getModel()).removeAllElements();
			for (String icon : FlotaConsultas.getAllIcons("/images/"
					+ LogicConstants.DIRECTORIO_ICONOS_FLOTAS)) {
				((DefaultComboBoxModel) iconos.getModel()).addElement(icon);
			}
			for (ActionListener l : iconos.getActionListeners()) {
				iconos.removeActionListener(l);
			}
			iconos.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cambios = true;
					if (iconos.getSelectedItem() != null) {
						i.setIcon(LogicConstants
								.getIcon(LogicConstants.DIRECTORIO_ICONOS_FLOTAS
										+ iconos.getSelectedItem().toString()
										+ "_flota_preview"));
					}
					i.updateUI();
				}
			});
			iconos.setSelectedItem(icono_seleccionado);
			if (iconos.getSelectedIndex() == -1 || i.getIcon() == null) {
				if (iconos.getModel().getSize() > 0)
					iconos.setSelectedIndex(0);
			}
			cambios = false;
			nombre2.setLabelFor(iconos);
			gbc.gridx++;
			gbc.anchor = GridBagConstraints.WEST;
			cabecera.add(iconos, gbc);

			gbc.gridheight = 2;
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.insets = new Insets(2, 10, 2, 10);
			gbc.ipadx = 5;
			gbc.ipady = 5;
			i.setBorder(BorderFactory.createLineBorder(Color.black));
			cabecera.add(i, gbc);
		}

		resultado.add(cabecera, BorderLayout.CENTER);
		resultado.setBorder(new EmptyBorder(15, 15, 15, 15));
		return resultado;
	}

	private JPanel buildCenter(final String centered_label) {
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel central = new JPanel(new GridBagLayout());
		central.setBorder(new EmptyBorder(15, 15, 15, 15));
		central.setOpaque(false);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 15, 5, 15);
		gbc.gridwidth = 2;
		return central;
	}
}
