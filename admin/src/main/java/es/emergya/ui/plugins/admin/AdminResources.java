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

import java.util.Calendar;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTable;

import org.apache.commons.lang.StringUtils;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.actions.RecursoAdmin;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.FlotaConsultas;
import es.emergya.consultas.PatrullaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.admin.aux1.RecursoDialog;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class AdminResources extends Option {

	private static final long serialVersionUID = -9090199272942137174L;
	private static String ICON = "tittlemanage_icon_recursos";
	AdminPanel resources;
	protected static final String[] TIPO_VEHICULOS = new String[] { "",
			Recurso.PERSONA, Recurso.VEHICULO };
	protected static String[] patrullas;
	protected static String[] flotas;
	private Recurso lastExample = new Recurso();

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

	public AdminResources() {
		super("", PluginType.getType("ADMIN"), 4, "subtab_icon_recursos", null);
		setTitle(i18n.getString("Resources.resources"));
		resources = new AdminPanel(i18n.getString("admin.recursos.titulo"),
				LogicConstants.getIcon(ICON), this);
		resources.addColumnWidth(2, 60);
		resources.addColumnWidth(5, 90);
		resources.addColumnWidth(6, 65);
		resources.setNewAction(getSummaryAction(null));
		flotas = FlotaConsultas.getAllFilter();
		patrullas = PatrullaConsultas.getAllFilter();
		resources
				.generateTable(
						new String[] {
								i18n.getString("admin.recursos.tabla.titulo.nombre"),
								i18n.getString("admin.recursos.tabla.titulo.tipo"),
								i18n.getString("admin.recursos.tabla.titulo.subflota"),
								i18n.getString("admin.recursos.tabla.titulo.patrulla"),
								i18n.getString("admin.recursos.tabla.titulo.dispositivo"),
								i18n.getString("admin.recursos.tabla.titulo.habilitado"),
								i18n.getString("admin.recursos.tabla.titulo.ficha"),
								i18n.getString("admin.recursos.tabla.titulo.eliminar") },
						new String[][] { {}, TIPO_VEHICULOS, flotas, patrullas,
								{}, { "", "Habilitado", "Deshabilitado" } },
						getNoFiltrarAction(), getFiltrarAction());
		resources.setTableData(getAll(new Recurso()));
		resources.setErrorCause(i18n.getString("Resources.errorCause"));
		this.add(resources);
	}

	@Override
	public boolean needsUpdating() {
		final Calendar lastUpdated2 = RecursoConsultas.lastUpdated();
		if (lastUpdated2 == null && this.resources.getTotalSize() != 0) {
			return true;
		}

		return lastUpdated2.after(super.lastUpdated);
	}

	private Object[][] getAll(Recurso r) {
		lastExample = r;
		List<Recurso> recursos = RecursoConsultas.getByExample(r);

		int showed = recursos.size();
		int total = RecursoConsultas.getTotal();
		resources.setCuenta(showed, total);

		Object[][] res = new Object[recursos.size()][];
		int i = 0;
		for (Recurso recurso : recursos) {
			res[i] = new Object[8];
			res[i][0] = recurso.getNombre();
			res[i][1] = recurso.getTipo();
			if (recurso.getFlotas() != null) {
				res[i][2] = recurso.getFlotas().getNombre();
			} else {
				res[i][2] = "";
			}
			if (recurso.getPatrullas() != null) {
				res[i][3] = recurso.getPatrullas().getNombre();
			} else {
				res[i][3] = "";
			}
			if (recurso.getDispositivo() != null) {
				res[i][4] = StringUtils.leftPad(recurso.getDispositivo()
						.toString(), LogicConstants.LONGITUD_ISSI, '0');
			} else {
				res[i][4] = "";
			}
			if (recurso.getHabilitado() != null) {
				res[i][5] = recurso.getHabilitado();
			} else {
				res[i][5] = false;
			}
			res[i][6] = getSummaryAction(recurso);
			res[i++][7] = getDeleteAction(recurso);
		}
		return res;
	}

	private FiltrarAction getFiltrarAction() {
		return resources.new FiltrarAction() {

			private static final long serialVersionUID = 5868301640069298260L;

			@Override
			protected void applyFilter(JTable filters) {
				final Recurso example = new Recurso();

				Object valueAt = filters.getValueAt(0, 1);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setNombre(valueAt.toString());
				}

				valueAt = filters.getValueAt(0, 2);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setTipo(valueAt.toString());
				}

				valueAt = filters.getValueAt(0, 3);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setFlotas(FlotaConsultas.find(valueAt.toString()));
				}

				valueAt = filters.getValueAt(0, 4);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setPatrullas(PatrullaConsultas.find(valueAt
							.toString()));
				}

				valueAt = filters.getValueAt(0, 5);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.idpattern = valueAt.toString();
				}

				valueAt = filters.getValueAt(0, 6);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setHabilitado(valueAt.equals("Habilitado"));
				}

				resources.setTableData(getAll(example));
			}
		};
	}

	private NoFiltrarAction getNoFiltrarAction() {
		return resources.new NoFiltrarAction() {

			private static final long serialVersionUID = 5192590526106798800L;

			@Override
			protected void applyFilter() {
				resources.setTableData(getAll(new Recurso()));
			}
		};
	}

	protected SummaryAction getSummaryAction(final Recurso r) {
		SummaryAction action = new SummaryAction(r) {

			private static final long serialVersionUID = 1732358643670038475L;

			@Override
			protected JFrame getSummaryDialog() {
				d = new RecursoDialog(r, AdminResources.this);
				d.setResizable(false);
				return d;
			}
		};

		return action;
	}

	protected AdminPanel.DeleteAction<Recurso> getDeleteAction(Recurso r) {
		AdminPanel.DeleteAction<Recurso> action = resources.new DeleteAction<Recurso>(
				r) {

			private static final long serialVersionUID = -5680285797213726529L;

			@Override
			protected boolean delete(boolean show_alert) {
				return RecursoAdmin.delete(this.target);
			}
		};

		return action;
	}

	@Override
	public void refresh(PluginEvent event) {
		super.refresh(event);
		resources.setTableData(getAll(lastExample));
		flotas = FlotaConsultas.getAllFilter();
		patrullas = PatrullaConsultas.getAllFilter();
		resources.setFilter(3, flotas);
		resources.setFilter(4, patrullas);
	}

	@Override
	public void reboot() {
		getNoFiltrarAction().actionPerformed(null);
		this.resources.unckeckAll();
	}
}
