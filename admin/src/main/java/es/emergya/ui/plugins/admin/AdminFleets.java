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
package es.emergya.ui.plugins.admin;

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import es.emergya.actions.FlotaAdmin;
import es.emergya.bbdd.bean.Flota;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.FlotaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginEventHandler;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.AdminPanel.SaveOrUpdateAction;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class AdminFleets extends Option {

	private static final long serialVersionUID = -2978632104068099705L;
	private static String ICON = "tittlemanage_icon_subflotas";
	AdminPanel flotas;
	private Flota lastExample = new Flota();

	public AdminFleets() {
		super(getString("Fleets.subfleets"), PluginType.getType("ADMIN"), 6,
				"subtab_icon_subflotas", null);
		flotas = new AdminPanel(getString("admin.flotas.titulo"),
				LogicConstants.getIcon("tittlemanage_icon_subflotas"), this);
		flotas.setNewAction(getSummaryAction(null));
		final List<String> allIcons = new ArrayList<String>();
		allIcons.add("");
		allIcons.addAll(FlotaConsultas.getAllIcons("/images/"
				+ LogicConstants.DIRECTORIO_ICONOS_FLOTAS));
		flotas.generateTable(new String[] {
				getString("admin.flotas.tabla.titulo.nombre"),
				getString("admin.flotas.tabla.titulo.icono"),
				getString("admin.flotas.tabla.titulo.ficha"),
				getString("admin.flotas.tabla.titulo.eliminar") },
				new Object[][] { {}, allIcons.toArray() },
				getNoFiltrarAction(), getFiltrarAction());
		flotas.setTableData(getAll(new Flota()));
		flotas.setErrorCause(getString("Fleets.errorCause"));
		this.add(flotas);
	}

	private Object[][] getAll(Flota f) {
		lastExample = f;
		List<Flota> flts = FlotaConsultas.getByExample(f);

		int showed = flts.size();
		int total = FlotaConsultas.getTotal();
		this.flotas.setCuenta(showed, total);

		Object[][] res = new Object[showed][];
		int i = 0;
		for (Flota flota : flts) {
			res[i] = new Object[4];
			res[i][0] = flota.getNombre();
			res[i][1] = LogicConstants
					.getIcon(LogicConstants.DIRECTORIO_ICONOS_FLOTAS
							+ flota.getJuegoIconos() + "_flota_preview");
			res[i][2] = getSummaryAction(flota);
			res[i++][3] = getDeleteAction(flota);
		}

		return res;
	}

	private FiltrarAction getFiltrarAction() {
		return flotas.new FiltrarAction() {

			private static final long serialVersionUID = -2649238620656143656L;

			@Override
			protected void applyFilter(JTable filters) {
				final Flota example = new Flota();
				Object valueAt = filters.getValueAt(0, 1);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setNombre(valueAt.toString());
				}
				valueAt = filters.getValueAt(0, 2);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setJuegoIconos(valueAt.toString());
				}
				flotas.setTableData(getAll(example));
			}
		};
	}

	private NoFiltrarAction getNoFiltrarAction() {
		return flotas.new NoFiltrarAction() {

			private static final long serialVersionUID = -6248274825732325056L;

			@Override
			protected void applyFilter() {
				flotas.setTableData(getAll(new Flota()));
			}
		};
	}

	protected SummaryAction getSummaryAction(final Flota f) {
		SummaryAction action = new SummaryAction(f) {

			private static final long serialVersionUID = -1264520743687850985L;

			@Override
			protected JFrame getSummaryDialog() {
				final String label_cabecera = getString("admin.subflotas.nueva.nombre");
				final String label_pie = getString("admin.subflotas.nueva.infoAdicional");
				final String centered_label = getString("admin.subflotas.nueva.recursos");
				final String left_label = getString("admin.subflotas.nueva.recursosDisponibles");
				final String right_label = getString("admin.subflotas.nueva.recursosAsignados");
				String tituloVentana = null;
				String tituloCabecera = null;
				if (isNew) {
					tituloVentana = getString("admin.subflotas.nueva.titleBar");
					tituloCabecera = tituloVentana;
				} else {
					tituloVentana = getString("admin.subflotas.existente.titleBar");
					tituloCabecera = getString("admin.subflotas.existente.cabecera");
				}

				final Recurso[] left_items = RecursoConsultas.getNotAsigned(f);
				for (Recurso r : left_items)
					r.setTipoToString(Recurso.TIPO_TOSTRING.SUBFLOTA);
				final Recurso[] right_items = RecursoConsultas.getAsigned(f);
				for (Recurso r : right_items)
					r.setTipoToString(Recurso.TIPO_TOSTRING.SUBFLOTA);
				final String icono_seleccionado = ((f == null || f
						.getJuegoIconos() == null) ? "" : f.getJuegoIconos());
				Icon icono = LogicConstants.getIcon(ICON);
				SaveOrUpdateAction<Flota> guardar = flotas.new SaveOrUpdateAction<Flota>(
						f) {

					private static final long serialVersionUID = -1488749819768791775L;

					@Override
					public void actionPerformed(ActionEvent e) {

						if (textfieldCabecera.getText().trim().isEmpty()) {
							JOptionPane
									.showMessageDialog(
											super.frame,
											getString("admin.subflotas.nueva.validacion.nombreVacio"));
						} else if ((original == null || original.getId() == null)
								&& FlotaConsultas.existe(textfieldCabecera
										.getText())) {
							JOptionPane
									.showMessageDialog(
											super.frame,
											getString("admin.subflotas.nueva.validacion.nombreExistente"));

						} else if (cambios) {

							int i = JOptionPane
									.showConfirmDialog(
											super.frame,
											getString("admin.subflotas.nueva.cambios"),
											getString("admin.subflotas.nueva.boton.guardar"),
											JOptionPane.YES_NO_CANCEL_OPTION);

							if (i == JOptionPane.YES_OPTION) {

								if (original == null) {
									original = new Flota();
								}

								original.setInfoAdicional(textfieldPie
										.getText());
								original.setNombre(textfieldCabecera.getText());
								original.setJuegoIconos(iconos
										.getSelectedItem().toString());

								HashSet<Recurso> recursos = new HashSet<Recurso>();
								for (Object r : ((DefaultListModel) right
										.getModel()).toArray()) {
									if (r instanceof Recurso) {
										recursos.add((Recurso) r);
										((Recurso) r).setFlotas(original);
									}
								}
								original.setRecurso(recursos);

								FlotaAdmin.saveOrUpdate(original);

								PluginEventHandler.fireChange(AdminFleets.this);

								cambios = false;
								original = null;

								AdminFleets.this.flotas
										.setTableData(getAll(new Flota()));

								closeFrame();
							} else if (i == JOptionPane.NO_OPTION) {
								closeFrame();
							}
						} else {
							closeFrame();
						}
					}
				};
				// if (d == null)
				d = generateIconDialog(label_cabecera, label_pie,
						centered_label, tituloVentana, left_items, right_items,
						left_label, right_label, guardar, icono,
						tituloCabecera, icono_seleccionado);
				izquierda.setVisible(false);

				if (f != null) {
					textfieldCabecera.setText(f.getNombre());
					// textfieldCabecera.setEnabled(false);
					textfieldCabecera.setEditable(false);
					textfieldPie.setText(f.getInfoAdicional());
				} else {
					textfieldCabecera.setText("");
					textfieldCabecera.setEnabled(true);
					textfieldPie.setText("");
				}

				cambios = false;
				return d;
			}
		};

		return action;
	}

	protected AdminPanel.DeleteAction<Flota> getDeleteAction(Flota f) {
		AdminPanel.DeleteAction<Flota> action = flotas.new DeleteAction<Flota>(
				f) {

			private static final long serialVersionUID = -7933848051133871938L;

			@Override
			protected boolean delete(boolean show_alert) {
				if (!FlotaAdmin.delete(this.target)) {
					if (show_alert) {
						JOptionPane
								.showMessageDialog(
										AdminFleets.this,
										getString(
												"admin.subflotas.borrar.error.recursosAsignados",
												(this.target).getNombre()),
										null, JOptionPane.ERROR_MESSAGE);
					}
				} else {
					PluginEventHandler.fireChange(AdminFleets.this);
					return true;
				}
				return false;

			}
		};

		return action;
	}

	@Override
	public void refresh(PluginEvent event) {
		super.refresh(event);
		flotas.setTableData(getAll(lastExample));
	}

	@Override
	public boolean needsUpdating() {
		final Calendar lastUpdated2 = FlotaConsultas.lastUpdated();
		if (lastUpdated2 == null && this.flotas.getTotalSize() != 0) {
			return true;
		}

		return lastUpdated2.after(super.lastUpdated);
	}

	@Override
	public void reboot() {
		getNoFiltrarAction().actionPerformed(null);
		flotas.unckeckAll();
	}
}
