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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import org.freixas.jcalendar.JCalendarCombo;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.actions.Authentication;
import es.emergya.actions.IncidenciaAdmin;
import es.emergya.bbdd.bean.CategoriaIncidencia;
import es.emergya.bbdd.bean.EstadoIncidencia;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.IncidenciaConsultas;

public class IncidenceDialog extends GenericDialog<Incidencia> {

	private static final String INCIDENCES_GEOMETRIA = "Incidences.geometria";
	private static final String INCIDENCES_STATUS = "Incidences.status";
	private static final String INCIDENCES_CATEGORY = "Incidences.category";
	private static final String INCIDENCES_PRIORITY = "Incidences.priority";
	private static final String INCIDENCES_FECHA_CIERRE = "Incidences.fechaCierre";
	private static final String INCIDENCES_CREACION = "Incidences.creacion";
	private static final String INCIDENCES_DESCRIPCION = "Incidences.descripcion";
	private static final String INCIDENCES_TITLE = "Incidences.title";

	public IncidenceDialog(Incidencia in, String titulo, String icon) {
		super(in, titulo, icon);
	}

	private static final long serialVersionUID = -8310858894186958778L;

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

	@Override
	protected void loadDialog(Incidencia i) {
		// Refrescamos el objeto
		if (i == null) {
			i = new Incidencia();
			i.setTitulo(i18n.getString("Incidences.nuevaIncidencia"));
			i.setCreador(Authentication.getUsuario());
		} else if (i.getId() != null) {
			i = IncidenciaConsultas.get(i.getId());
			log.trace("Hemos refrescado los valores de " + i);
		}

		// Identificamos el frame
		setName(i.getTitulo());

		// Nos ponemos a meter los campos:
		addString(i.getTitulo(), INCIDENCES_TITLE);

		addString(i.getDescripcion(), INCIDENCES_DESCRIPCION);

		addDate(i.getFechaCreacion(), "Incidences.creacion", false);

		addDate(i.getFechaCierre(), INCIDENCES_FECHA_CIERRE, true);

		Integer prioridad = i.getPrioridad();
		if (prioridad == null)
			addComboBox(null, LogicConstants.getPriorities(false),
					INCIDENCES_PRIORITY);
		else
			addComboBox(prioridad.toString(),
					LogicConstants.getPriorities(false), INCIDENCES_PRIORITY);

		addString_Fixed(i.getCreador().getNombreUsuario(), INCIDENCES_CREACION);

		addComboBox(i.getCategoria(), IncidenciaConsultas.getCategorias(false),
				INCIDENCES_CATEGORY);

		addComboBox(i.getEstado(), IncidenciaConsultas.getStatuses(false),
				INCIDENCES_STATUS);

		if (i.getGeometria() != null)
			addString_Fixed(i.getGeometria().toText(), INCIDENCES_GEOMETRIA);
		else
			addString_Fixed(LogicConstants.get("Incidences.notLocated",
					"Sin localización"), INCIDENCES_GEOMETRIA);

		this.setObject(i);
	}

	@Override
	protected ActionListener saveListener() {
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					Incidencia i = (Incidencia) IncidenceDialog.this
							.getObject();

					if (i == null) {
						i = new Incidencia();
						i.setCreador(Authentication.getUsuario());
					} else if (i.getId() != null)
						i = IncidenciaConsultas.get(i.getId());

					Calendar fecha_cierre = null;

					for (Component c : componentes) {
						try {
							if (c.getName() != null) {
								String name = c.getName();
								Object valor = null;
								if (c instanceof JTextField)
									valor = ((JTextField) c).getText();
								else if (c instanceof JSpinner) {
									JSpinner spin = (JSpinner) c;
									if (name.equals(INCIDENCES_FECHA_CIERRE)) {
										if (fecha_cierre == null)
											fecha_cierre = Calendar
													.getInstance();

										Calendar calendar = Calendar
												.getInstance();
										calendar.setTime(((SpinnerDateModel) spin
												.getModel()).getDate());

										for (Integer in : new Integer[] {
												Calendar.HOUR_OF_DAY,
												Calendar.MINUTE,
												Calendar.SECOND,
												Calendar.MILLISECOND })
											fecha_cierre.set(in,
													calendar.get(in));
									}
								} else if (c instanceof JCalendarCombo) {
									JCalendarCombo calendar = (JCalendarCombo) c;
									if (name.equals(INCIDENCES_FECHA_CIERRE)) {
										if (fecha_cierre == null)
											fecha_cierre = calendar
													.getCalendar();
										else {
											for (Integer in : new Integer[] {
													Calendar.YEAR,
													Calendar.MONTH,
													Calendar.DAY_OF_YEAR })
												fecha_cierre.set(in, calendar
														.getCalendar().get(in));
										}
									}
								} else if (c instanceof JComboBox)
									valor = ((JComboBox) c).getSelectedItem();

								if (valor != null) {
									if (name.equals(INCIDENCES_TITLE)) {
										i.setTitulo(valor.toString());
									} else if (name.equals(INCIDENCES_PRIORITY)
											&& LogicConstants.isNumeric(valor
													.toString())) {
										i.setPrioridad(new Integer(valor
												.toString()));
									} else if (name.equals(INCIDENCES_CATEGORY)
											&& (valor instanceof CategoriaIncidencia)) {
										i.setCategoria((CategoriaIncidencia) valor);
									} else if (name.equals(INCIDENCES_CREACION)) {
										i.setCreador(Authentication
												.getUsuario());
									} else if (name
											.equals(INCIDENCES_DESCRIPCION)) {
										i.setDescripcion(valor.toString());
									} else if (name.equals(INCIDENCES_STATUS)
											&& (valor instanceof EstadoIncidencia)) {
										i.setEstado((EstadoIncidencia) valor);
									}
								}
							}
						} catch (Throwable t) {
							log.error("Error al procesar " + c, t);
						}
					}

					if (fecha_cierre != null)
						i.setFechaCierre(fecha_cierre.getTime());

					IncidenciaAdmin.saveOrUpdate(i);
				} catch (Throwable t) {
					log.error("Error al salvar la incidencia", t);
				}
			}
		};
		return al;
	}
}
