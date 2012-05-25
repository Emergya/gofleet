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
package es.emergya.ui.plugins.admin.aux1;

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;

import es.emergya.actions.RecursoAdmin;
import es.emergya.bbdd.bean.Patrulla;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.FlotaConsultas;
import es.emergya.consultas.HistoricoGPSConsultas;
import es.emergya.consultas.PatrullaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.plugins.admin.AdminResources;

public class RecursoDialog extends JFrame {

	private static final long serialVersionUID = -3952140587477087365L;
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(RecursoDialog.class);
	private Boolean cambios = false;
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

	public RecursoDialog(final Recurso rec, final AdminResources adminResources) {
		super();
		setAlwaysOnTop(true);
		setSize(600, 400);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				if (cambios) {
					int res = JOptionPane
							.showConfirmDialog(
									RecursoDialog.this,
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
		final Recurso r = (rec == null) ? null : RecursoConsultas.get(rec
				.getId());
		if (r != null) {
			setTitle(getString("Resources.summary.titleWindow") + " "
					+ r.getIdentificador());
		} else {
			setTitle(getString("Resources.summary.titleWindow.new"));
		}
		setIconImage(getBasicWindow().getFrame().getIconImage());
		JPanel base = new JPanel();
		base.setBackground(Color.WHITE);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		// Icono del titulo
		JPanel title = new JPanel(new FlowLayout(FlowLayout.LEADING));
		title.setOpaque(false);
		JLabel labelTitulo = null;
		if (r != null) {
			labelTitulo = new JLabel(getString("Resources.summary"),
					LogicConstants.getIcon("tittleficha_icon_recurso"),
					JLabel.LEFT);

		} else {
			labelTitulo = new JLabel(getString("Resources.cabecera.nuevo"),
					LogicConstants.getIcon("tittleficha_icon_recurso"),
					JLabel.LEFT);

		}
		labelTitulo.setFont(LogicConstants.deriveBoldFont(12f));
		title.add(labelTitulo);
		base.add(title);

		// Nombre
		JPanel mid = new JPanel(new SpringLayout());
		mid.setOpaque(false);
		mid.add(new JLabel(getString("Resources.name"), JLabel.RIGHT));
		final JTextField name = new JTextField(25);
		if (r != null) {
			name.setText(r.getNombre());
		}

		name.getDocument().addDocumentListener(changeListener);
		name.setEditable(r == null);
		mid.add(name);

		// patrullas
		final JLabel labelSquads = new JLabel(getString("Resources.squad"),
				JLabel.RIGHT);
		mid.add(labelSquads);
		List<Patrulla> pl = PatrullaConsultas.getAll();
		pl.add(0, null);
		final JComboBox squads = new JComboBox(pl.toArray());
		squads.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXX");
		squads.addActionListener(changeSelectionListener);
		squads.setOpaque(false);
		labelSquads.setLabelFor(squads);
		if (r != null) {
			squads.setSelectedItem(r.getPatrullas());
		} else {
			squads.setSelectedItem(null);
		}
		squads.setEnabled((r != null && r.getHabilitado() != null) ? r
				.getHabilitado() : true);
		mid.add(squads);

		// // Identificador
		// mid.setOpaque(false);
		// mid.add(new JLabel(getString("Resources.identificador"),
		// JLabel.RIGHT));
		// final JTextField identificador = new JTextField("");
		// if (r != null) {
		// identificador.setText(r.getIdentificador());
		// }
		// identificador.getDocument().addDocumentListener(changeListener);
		// identificador.setEditable(r == null);
		// mid.add(identificador);
		// Espacio en blanco
		// mid.add(Box.createHorizontalGlue());
		// mid.add(Box.createHorizontalGlue());

		// Tipo
		final JLabel labelTipoRecursos = new JLabel(
				getString("Resources.type"), JLabel.RIGHT);
		mid.add(labelTipoRecursos);
		final JComboBox types = new JComboBox(RecursoConsultas.getTipos());
		labelTipoRecursos.setLabelFor(types);
		types.addActionListener(changeSelectionListener);
		if (r != null) {
			types.setSelectedItem(r.getTipo());
		} else {
			types.setSelectedItem(0);
		}
		// types.setEditable(true);
		types.setEnabled(true);
		mid.add(types);

		// Estado Eurocop
		mid.add(new JLabel(getString("Resources.status"), JLabel.RIGHT));
		final JTextField status = new JTextField();
		if (r != null && r.getEstadoEurocop() != null) {
			status.setText(r.getEstadoEurocop().getIdentificador());
		}
		status.setEditable(false);
		mid.add(status);

		// Subflota y patrulla
		mid.add(new JLabel(getString("Resources.subfleet"), JLabel.RIGHT));
		final JComboBox subfleets = new JComboBox(
				FlotaConsultas.getAllHabilitadas());
		subfleets.addActionListener(changeSelectionListener);
		if (r != null) {
			subfleets.setSelectedItem(r.getFlotas());
		} else {
			subfleets.setSelectedIndex(0);
		}
		subfleets.setEnabled(true);
		subfleets.setOpaque(false);
		mid.add(subfleets);

		// Referencia humana
		mid.add(new JLabel(getString("Resources.incidences"), JLabel.RIGHT));
		final JTextField rhumana = new JTextField();
		// if (r != null && r.getIncidencias() != null) {
		// rhumana.setText(r.getIncidencias().getReferenciaHumana());
		// }
		rhumana.setEditable(false);
		mid.add(rhumana);

		// dispositivo
		mid.add(new JLabel(getString("Resources.device"), JLabel.RIGHT));
		final PlainDocument plainDocument = new PlainDocument() {

			private static final long serialVersionUID = 4929271093724956016L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (this.getLength() + str.length() <= LogicConstants.LONGITUD_ISSI) {
					super.insertString(offs, str, a);
				}
			}
		};
		final JTextField issi = new JTextField(plainDocument, "",
				LogicConstants.LONGITUD_ISSI);
		plainDocument.addDocumentListener(changeListener);
		issi.setEditable(true);
		mid.add(issi);
		mid.add(new JLabel(getString("Resources.enabled"), JLabel.RIGHT));
		final JCheckBox enabled = new JCheckBox("", true);
		enabled.addActionListener(changeSelectionListener);
		enabled.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (enabled.isSelected()) {
					squads.setSelectedIndex(0);
				}
				squads.setEnabled(enabled.isSelected());
			}
		});
		enabled.setEnabled(true);
		enabled.setOpaque(false);
		if (r != null) {
			enabled.setSelected(r.getHabilitado());
		} else {
			enabled.setSelected(true);
		}
		if (r != null && r.getDispositivo() != null) {
			issi.setText(StringUtils.leftPad(
					String.valueOf(r.getDispositivo()),
					LogicConstants.LONGITUD_ISSI, '0'));
		}

		mid.add(enabled);

		// Fecha ultimo gps
		mid.add(new JLabel(getString("Resources.lastPosition"), JLabel.RIGHT));
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

		// informacion adicional
		JPanel infoPanel = new JPanel(new SpringLayout());
		final JTextField info = new JTextField(25);
		info.getDocument().addDocumentListener(changeListener);
		infoPanel.add(new JLabel(getString("Resources.info")));
		infoPanel.add(info);
		infoPanel.setOpaque(false);
		info.setOpaque(false);
		SpringUtilities.makeCompactGrid(infoPanel, 1, 2, 6, 6, 6, 18);

		if (r != null) {
			info.setText(r.getInfoAdicional());
		} else {
			info.setText("");
		}
		info.setEditable(true);

		// Espacio en blanco
		mid.add(Box.createHorizontalGlue());
		mid.add(Box.createHorizontalGlue());

		SpringUtilities.makeCompactGrid(mid, 5, 4, 6, 6, 6, 18);
		base.add(mid);
		base.add(infoPanel);

		JPanel buttons = new JPanel();
		buttons.setOpaque(false);
		JButton accept = null;
		if (r == null) {
			accept = new JButton("Crear",
					LogicConstants.getIcon("button_crear"));
		} else {
			accept = new JButton("Guardar",
					LogicConstants.getIcon("button_save"));
		}
		accept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (cambios || r == null || r.getId() == null) {
						boolean shithappens = true;
						if ((r == null || r.getId() == null)) { // Estamos
							// creando
							// uno nuevo
							if (RecursoConsultas.alreadyExists(name.getText())) {
								shithappens = false;
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.nombreUnico"));
							} else if (issi.getText() != null
									&& issi.getText().length() > 0
									&& StringUtils.trimToEmpty(issi.getText())
											.length() != LogicConstants.LONGITUD_ISSI) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString(
														"admin.recursos.popup.error.faltanCifras",
														LogicConstants.LONGITUD_ISSI));
								shithappens = false;
							} else if (issi.getText() != null
									&& issi.getText().length() > 0
									&& LogicConstants.isNumeric(issi.getText())
									&& RecursoConsultas
											.alreadyExists(new Integer(issi
													.getText()))) {
								shithappens = false;
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.dispositivoUnico"));
							}
						}
						if (shithappens) {
							if (name.getText().isEmpty()) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.nombreNulo"));
							} else if (issi.getText() != null
									&& issi.getText().length() > 0
									&& StringUtils.trimToEmpty(issi.getText())
											.length() != LogicConstants.LONGITUD_ISSI) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString(
														"admin.recursos.popup.error.faltanCifras",
														LogicConstants.LONGITUD_ISSI));
							} else if (issi.getText() != null
									&& issi.getText().length() > 0
									&& LogicConstants.isNumeric(issi.getText())
									&& r != null
									&& r.getId() != null
									&& RecursoConsultas.alreadyExists(
											new Integer(issi.getText()),
											r.getId())) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.issiUnico"));
							} else if (issi.getText() != null
									&& issi.getText().length() > 0
									&& !LogicConstants.isNumeric(issi.getText())) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.noNumerico"));
								// } else if (identificador.getText().isEmpty())
								// {
								// JOptionPane
								// .showMessageDialog(
								// RecursoDialog.this,
								// getString("admin.recursos.popup.error.identificadorNulo"));
							} else if (subfleets.getSelectedIndex() == -1) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.noSubflota"));
							} else if (types.getSelectedItem() == null
									|| types.getSelectedItem().toString()
											.trim().isEmpty()) {
								JOptionPane
										.showMessageDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.error.noTipo"));
							} else {
								int i = JOptionPane
										.showConfirmDialog(
												RecursoDialog.this,
												getString("admin.recursos.popup.dialogo.guardar.titulo"),
												getString("admin.recursos.popup.dialogo.guardar.guardar"),
												JOptionPane.YES_NO_CANCEL_OPTION);

								if (i == JOptionPane.YES_OPTION) {

									Recurso recurso = r;

									if (r == null) {
										recurso = new Recurso();
									}

									recurso.setInfoAdicional(info.getText());
									if (issi.getText() != null
											&& issi.getText().length() > 0) {
										recurso.setDispositivo(new Integer(issi
												.getText()));
									} else {
										recurso.setDispositivo(null);
									}
									recurso.setFlotas(FlotaConsultas
											.find(subfleets.getSelectedItem()
													.toString()));
									if (squads.getSelectedItem() != null
											&& enabled.isSelected()) {
										recurso.setPatrullas(PatrullaConsultas
												.find(squads.getSelectedItem()
														.toString()));
									} else {
										recurso.setPatrullas(null);
									}
									recurso.setNombre(name.getText());
									recurso.setHabilitado(enabled.isSelected());
									// recurso.setIdentificador(identificador
									// .getText());
									recurso.setTipo(types.getSelectedItem()
											.toString());
									dispose();

									RecursoAdmin.saveOrUpdate(recurso);
									adminResources.refresh(null);

									PluginEventHandler
											.fireChange(adminResources);
								} else if (i == JOptionPane.NO_OPTION) {
									dispose();
								}
							}
						}
					} else {
						log.debug("No hay cambios");
						dispose();
					}

				} catch (Throwable t) {
					log.error("Error guardando un recurso", t);
				}
			}
		});
		buttons.add(accept);

		JButton cancelar = new JButton("Cancelar",
				LogicConstants.getIcon("button_cancel"));

		cancelar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (cambios) {
					if (JOptionPane
							.showConfirmDialog(
									RecursoDialog.this,
									"Existen cambios sin guardar. ¿Seguro que desea cerrar la ventana?",
									"Cambios sin guardar",
									JOptionPane.OK_CANCEL_OPTION) != JOptionPane.CANCEL_OPTION) {
						dispose();
					}
				} else {
					dispose();
				}
			}
		});

		buttons.add(cancelar);

		base.add(buttons);

		getContentPane().add(base);
		setLocationRelativeTo(null);
		cambios = false;
		if (r == null) {
			cambios = true;
		}

		pack();

		int x;
		int y;

		Container myParent = getBasicWindow().getPluginContainer()
				.getDetachedTab(0);
		Point topLeft = myParent.getLocationOnScreen();
		Dimension parentSize = myParent.getSize();

		Dimension mySize = getSize();

		if (parentSize.width > mySize.width) {
			x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
		} else {
			x = topLeft.x;
		}

		if (parentSize.height > mySize.height) {
			y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
		} else {
			y = topLeft.y;
		}

		setLocation(x, y);
		cambios = false;
	}
}
