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
package es.emergya.ui.plugins.list;

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;

import org.openstreetmap.josm.data.coor.LatLon;

import com.vividsolutions.jts.geom.Point;

import es.emergya.bbdd.bean.CategoriaIncidencia;
import es.emergya.bbdd.bean.EstadoIncidencia;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.FlotaConsultas;
import es.emergya.consultas.IncidenciaConsultas;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.gis.popups.IncidenceDialog;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class ListIncidences extends Option {

	private final class ZoomAction extends AbstractAction {
		private final Incidencia objeto;
		private static final long serialVersionUID = 1682697573887756465L;

		private ZoomAction(Incidencia objeto) {
			super("", LogicConstants.getIcon("map_button_centrar"));
			this.objeto = objeto;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (objeto == null || objeto.getId() == null)
				return;
			IncidenciaConsultas.get(objeto.getId());
			if (objeto.getGeometria() == null)
				return;

			Point geom = objeto.getGeometria().getCentroid();
			LatLon ll = new LatLon(geom.getCoordinate().y,
					geom.getCoordinate().x);
//			BasicWindow.showOnMap(ll, 1);
		}
	}

	private static final long serialVersionUID = -2978632104068099705L;
	private static String ICON = "tittlemanage_icon_incidences";
	private static String string = getString("Incidences.incidences");
	private String[] categorias = new String[0];
	private static String[] prioridades = LogicConstants.getPriorities(true);
	private String[] statuses = new String[0];
	protected AdminPanel listado;
	private Incidencia lastExample = new Incidencia();
	private HashMap<String, Incidencia> incidenciasMostradas = new HashMap<String, Incidencia>();

	public ListIncidences() {
		super(string, PluginType.getType("LIST"), 1, "subtab_icon_incidences", null);

		Object[] tmp = IncidenciaConsultas.getCategorias(true);
		categorias = new String[tmp.length];
		for (int i = 0; i < categorias.length; i++)
			categorias[i] = tmp[i].toString();

		tmp = IncidenciaConsultas.getStatuses(true);
		statuses = new String[tmp.length];
		for (int i = 0; i < statuses.length; i++)
			statuses[i] = tmp[i].toString();

		listado = new AdminPanel(string, LogicConstants.getIcon(ICON), this,
				true, false);
		listado.setNewAction(getSummaryAction(null));
		listado.addInvisibleFilterCol(5);
		listado.setMyRendererColoring(listado.new MyRendererColoring() {
			@Override
			public Color getColor(Object object) {
				Incidencia i = incidenciasMostradas.get(object.toString());
				if (i == null || i.getEstado() == null)
					return null;
				else
					return Color.decode(LogicConstants.get("COLOR_ESTADO_INC_"
							+ i.getEstado().getId(), "#000000"));
			}
		});
		listado.generateTable(new String[] { getString("Incidences.title"),
				getString("Incidences.category"),
				getString("Incidences.priority"),
				getString("Incidences.status"), getString("Incidences.zoom"),
				getString("tabla.ficha") }, new String[][] { {}, categorias,
				prioridades, statuses }, getNoFiltrarAction(),
				getFiltrarAction());
		listado.addColumnWidth(5, 65);
		listado.setTableData(getAll(lastExample));
		this.add(listado);
	}

	private Object[][] getAll(Incidencia f) {
		lastExample = f;
		List<Incidencia> flts = IncidenciaConsultas.getByExample(f);

		int showed = flts.size();
		int total = FlotaConsultas.getTotal();
		this.listado.setCuenta(showed, total);

		Object[][] res = new Object[showed][];
		int i = 0;
		for (Incidencia objeto : flts) {
			res[i] = new Object[6];
			res[i][0] = objeto.getTitulo();
			res[i][1] = objeto.getCategoria();
			res[i][2] = objeto.getPrioridad();
			res[i][3] = objeto.getEstado();
			res[i][4] = getZoomAction(objeto);
			res[i++][5] = getSummaryAction(objeto);
			incidenciasMostradas.put(objeto.getTitulo(), objeto);
		}

		return res;
	}

	private ZoomAction getZoomAction(final Incidencia objeto) {
		ZoomAction zoom = new ZoomAction(objeto);
		return zoom;
	}

	private FiltrarAction getFiltrarAction() {
		return listado.new FiltrarAction() {

			private static final long serialVersionUID = -2649238620656143656L;

			@Override
			protected void applyFilter(JTable filters) {
				final Incidencia example = new Incidencia();
				Object valueAt = filters.getValueAt(0, 1);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setTitulo(valueAt.toString());
				}
				valueAt = filters.getValueAt(0, 2);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					CategoriaIncidencia ci = new CategoriaIncidencia();
					ci.setIdentificador(valueAt.toString().trim());
					example.setCategoria(ci);
				}
				valueAt = filters.getValueAt(0, 3);
				if (valueAt != null
						&& LogicConstants.isNumeric(valueAt.toString().trim())) {
					example.setPrioridad(new Integer(valueAt.toString()));
				}
				valueAt = filters.getValueAt(0, 4);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					EstadoIncidencia ei = new EstadoIncidencia();
					ei.setIdentificador(valueAt.toString().trim());
					example.setEstado(ei);
				}
				listado.setTableData(getAll(example));
			}
		};
	}

	private NoFiltrarAction getNoFiltrarAction() {
		return listado.new NoFiltrarAction() {

			private static final long serialVersionUID = -6248274825732325056L;

			@Override
			protected void applyFilter() {
				listado.setTableData(getAll(new Incidencia()));
			}
		};
	}

	protected SummaryAction getSummaryAction(final Incidencia f) {
		SummaryAction action = new SummaryAction(f) {

			private static final long serialVersionUID = -1264520743687850985L;

			@Override
			protected JFrame getSummaryDialog() {
				String titulo = getString("Incidences.nuevaIncidencia");
				if (f != null)
					titulo = f.getTitulo();
				IncidenceDialog id = new IncidenceDialog(f,
						getString("Incidences.summary.title") + " " + titulo,
						"tittleficha_icon_recurso");
				return id;
			}
		};

		return action;
	}

	protected AdminPanel.DeleteAction<Incidencia> getDeleteAction(Incidencia f) {
		AdminPanel.DeleteAction<Incidencia> action = listado.new DeleteAction<Incidencia>(
				f) {

			private static final long serialVersionUID = -7933848051133871938L;

			@Override
			protected boolean delete(boolean show_alert) {
				return false;
			}
		};

		return action;
	}

	@Override
	public void refresh(PluginEvent event) {
		super.refresh(event);
		listado.setTableData(getAll(lastExample));
	}

	@Override
	public boolean needsUpdating() {
		final Calendar lastUpdated2 = IncidenciaConsultas.lastUpdated();
		if (lastUpdated2 == null && this.listado.getTotalSize() != 0) {
			return true;
		}

		return lastUpdated2.after(super.lastUpdated);
	}

	@Override
	public void reboot() {
		getNoFiltrarAction().actionPerformed(null);
		listado.unckeckAll();
	}
}
