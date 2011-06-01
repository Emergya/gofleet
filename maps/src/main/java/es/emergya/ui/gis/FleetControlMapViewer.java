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
 * 19/08/2009
 */

package es.emergya.ui.gis;

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.logging.LogFactory;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.IncidenciaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.i18n.Internacionalization;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.gis.CustomMapView.InitAdapter;
import es.emergya.ui.gis.markers.CustomMarker;
import es.emergya.ui.gis.popups.GPSDialog;
import es.emergya.ui.gis.popups.IncidenceDialog;
import es.emergya.ui.gis.popups.NearestResourcesDialog;
import es.emergya.ui.gis.popups.RouteDialog;
import es.emergya.ui.gis.popups.SummaryDialog;

/**
 * @author fario
 * 
 */
public class FleetControlMapViewer extends MapViewer implements ActionListener {

	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(FleetControlMapViewer.class);
	private static final long serialVersionUID = -1837324556102054550L;
	public Object menuObjective;
	protected EastNorth routeFrom, routeTo;
	private MouseEvent eventOriginal;

	/**
	 * @param title
	 * @param type
	 * @param order
	 * @param icon
	 */
	public FleetControlMapViewer() {
		super(Internacionalization.getString("Main.FleetControl"), PluginType
				.getType("GPS"), 1, "tab_icon_controlflota");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {

		final CustomMapView mapViewLocal = this.mapView;

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {

				try {
					final MouseEvent mouseEvent = FleetControlMapViewer.this.eventOriginal;
					if (e.getActionCommand().equals(// Centrar aqui
							Internacionalization
									.getString("map.menu.centerHere"))) {
						mapViewLocal.zoomToFactor(mapViewLocal.getEastNorth(
								mouseEvent.getX(), mouseEvent.getY()),
								mapViewLocal.zoomFactor);

					} else if (e.getActionCommand().equals(// nueva incidencia
							Internacionalization
									.getString("map.menu.newIncidence"))) {
						Incidencia f = new Incidencia();
						f.setCreador(Authentication.getUsuario());
						LatLon from = mapViewLocal.getLatLon(mouseEvent.getX(),
								mouseEvent.getY());
						GeometryFactory gf = new GeometryFactory();
						f.setGeometria(gf.createPoint(new Coordinate(
								from.lon(), from.lat())));
						IncidenceDialog id = new IncidenceDialog(
								f,
								getString("Incidences.summary.title")
										+ " "
										+ getString("Incidences.nuevaIncidencia"),
								"tittleficha_icon_recurso");
						id.setVisible(true);

					} else if (e.getActionCommand().equals(// ruta desde
							Internacionalization
									.getString("map.menu.route.from"))) {
						RouteDialog.showRouteDialog(mapViewLocal.getLatLon(
								mouseEvent.getX(), mouseEvent.getY()), null,
								mapViewLocal);

					} else if (e
							.getActionCommand()
							.equals(// ruta hasta
							Internacionalization.getString("map.menu.route.to"))) {
						RouteDialog.showRouteDialog(null,
								mapViewLocal.getLatLon(mouseEvent.getX(),
										mouseEvent.getY()), mapViewLocal);

					} else if (e.getActionCommand().equals(// Actualizar gps
							Internacionalization.getString("map.menu.gps"))) {
						if (!(menuObjective instanceof Recurso)) {
							return null;
						}

						GPSDialog sdsDialog = null;
						for (Frame f : Frame.getFrames()) {
							if (f instanceof GPSDialog)
								if (((GPSDialog) f).getRecurso().equals(
										menuObjective))
									sdsDialog = (GPSDialog) f;
						}
						if (sdsDialog == null)
							sdsDialog = new GPSDialog((Recurso) menuObjective);
						sdsDialog.setVisible(true);
						sdsDialog.setExtendedState(JFrame.NORMAL);

					} else if (e.getActionCommand().equals(// Ficha
							Internacionalization.getString("map.menu.summary"))) {
						if (log.isTraceEnabled()) {
							log.trace("Mostramos la ficha del objetivo del menu");
						}
						if (menuObjective instanceof Recurso) {
							log.trace(">recurso");
							SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

								@Override
								protected Object doInBackground()
										throws Exception {
									for (Frame f : JFrame.getFrames()) {
										if (f.getName().equals(
												((Recurso) menuObjective)
														.getIdentificador())
												&& f instanceof SummaryDialog) {
											if (f.isShowing()) {
												f.toFront();
												f.setExtendedState(JFrame.NORMAL);
												return null;
											}
										}
									}
									new SummaryDialog((Recurso) menuObjective)
											.setVisible(true);
									return null;
								}
							};

							sw.execute();
						} else if (menuObjective instanceof Incidencia) {
							if (log.isTraceEnabled()) {
								log.trace(">incidencia");
							}
							SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

								@Override
								protected Object doInBackground()
										throws Exception {
									for (Frame f : JFrame.getFrames()) {
										if (f.getName().equals(
												((Incidencia) menuObjective)
														.getTitulo())
												&& f instanceof IncidenceDialog) {
											if (log.isTraceEnabled()) {
												log.trace("Ya lo tenemos abierto");
											}
											if (f.isShowing()) {
												f.toFront();
												f.setExtendedState(JFrame.NORMAL);
											} else {
												f.setVisible(true);
												f.setExtendedState(JFrame.NORMAL);
											}
											return null;
										}
									}
									if (log.isTraceEnabled()) {
										log.trace("Abrimos uno nuevo");
									}
									new IncidenceDialog(
											(Incidencia) menuObjective,
											getString("Incidences.summary.title")
													+ " "
													+ ((Incidencia) menuObjective)
															.getTitulo(),
											"tittleficha_icon_recurso")
											.setVisible(true);
									return null;
								}
							};

							sw.execute();
						} else {
							return null;
						}

					} else if (e.getActionCommand().equals( // Mas cercanos
							Internacionalization
									.getString("map.menu.showNearest"))) {
						if (log.isTraceEnabled()) {
							log.trace("showNearest");
						}

						if (menuObjective != null) {
							for (Frame f : JFrame.getFrames()) {
								String identificador = menuObjective.toString();
								if (menuObjective instanceof Recurso) {
									identificador = ((Recurso) menuObjective)
											.getIdentificador();
								}
								if (menuObjective != null
										&& f.getName().equals(identificador)
										&& f instanceof NearestResourcesDialog
										&& f.isDisplayable()) {
									if (log.isTraceEnabled()) {
										log.trace("Encontrado " + f);
									}
									if (f.isShowing()) {
										f.toFront();
										f.setExtendedState(JFrame.NORMAL);
									} else {
										f.setVisible(true);
										f.setExtendedState(JFrame.NORMAL);
									}
									return null;
								}
							}
						}
						NearestResourcesDialog d;
						if (menuObjective instanceof Recurso) {
							d = new NearestResourcesDialog(
									(Recurso) menuObjective, mapViewLocal);
						} else if (menuObjective instanceof Incidencia) {
							d = new NearestResourcesDialog(
									(Incidencia) menuObjective,
									mapViewLocal.getLatLon(mouseEvent.getX(),
											mouseEvent.getY()), mapViewLocal);
						} else {
							d = new NearestResourcesDialog(
									mapViewLocal.getLatLon(mouseEvent.getX(),
											mouseEvent.getY()), mapViewLocal);
						}
						d.setVisible(true);

					} else {
						log.error("ActionCommand desconocido: "
								+ e.getActionCommand());
					}
				} catch (Throwable t) {
					log.error(
							"Error al ejecutar la accion del menu contextual",
							t);
				}
				return null;
			}
		};

		sw.execute();
	}

	@Override
	protected JPopupMenu getContextMenu() {
		JPopupMenu menu = new JPopupMenu();

		menu.setBackground(Color.decode("#E8EDF6"));

		// Título
		final JMenuItem titulo = new JMenuItem(
				getString("map.menu.titulo.puntoGenerico"));
		titulo.setFont(LogicConstants.deriveBoldFont(10.0f));
		titulo.setBackground(Color.decode("#A4A4A4"));
		titulo.setFocusable(false);

		menu.add(titulo);

		menu.addSeparator();

		// Actualizar posición
		final JMenuItem gps = new JMenuItem(
				Internacionalization.getString("map.menu.gps"), KeyEvent.VK_P);
		gps.setIcon(LogicConstants.getIcon("menucontextual_icon_actualizargps"));
		menu.add(gps);
		gps.addActionListener(this);
		gps.setEnabled(false);

		menu.addSeparator();

		// Mostrar más cercanos
		final JMenuItem mmc = new JMenuItem(
				Internacionalization.getString("map.menu.showNearest"),
				KeyEvent.VK_M);
		mmc.setIcon(LogicConstants.getIcon("menucontextual_icon_mascercano"));
		mmc.addActionListener(this);
		menu.add(mmc);
		// Centrar aqui
		final JMenuItem cent = new JMenuItem(
				Internacionalization.getString("map.menu.centerHere"),
				KeyEvent.VK_C);
		cent.setIcon(LogicConstants.getIcon("menucontextual_icon_centrar"));
		cent.addActionListener(this);
		menu.add(cent);
		// Nueva Incidencia
		final JMenuItem newIncidence = new JMenuItem(
				Internacionalization.getString("map.menu.newIncidence"),
				KeyEvent.VK_I);
		newIncidence.setIcon(LogicConstants
				.getIcon("menucontextual_icon_newIncidence"));
		newIncidence.addActionListener(this);
		menu.add(newIncidence);
		// Calcular ruta desde aqui
		final JMenuItem from = new JMenuItem(
				Internacionalization.getString("map.menu.route.from"),
				KeyEvent.VK_D);
		from.setIcon(LogicConstants.getIcon("menucontextual_icon_origenruta"));
		from.addActionListener(this);
		menu.add(from);
		// Calcular ruta hasta aqui
		final JMenuItem to = new JMenuItem(
				Internacionalization.getString("map.menu.route.to"),
				KeyEvent.VK_H);
		to.setIcon(LogicConstants.getIcon("menucontextual_icon_destinoruta"));
		to.addActionListener(this);
		menu.add(to);

		menu.addSeparator();

		// Ver ficha [recurso / incidencia]
		final JMenuItem summary = new JMenuItem(
				Internacionalization.getString("map.menu.summary"),
				KeyEvent.VK_F);
		summary.setIcon(LogicConstants.getIcon("menucontextual_icon_ficha"));
		summary.addActionListener(this);
		menu.add(summary);
		summary.setEnabled(false);

		menu.addPopupMenuListener(new PopupMenuListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				eventOriginal = FleetControlMapViewer.this.mapView.lastMEvent;
				gps.setEnabled(false);
				summary.setEnabled(false);
				titulo.setText(getString("map.menu.titulo.puntoGenerico"));
				menuObjective = null;
				Point p = new Point(mapView.lastMEvent.getX(),
						mapView.lastMEvent.getY());
				for (Layer l : mapView.getAllLayers()) { // por cada capa...
					if (l instanceof MarkerLayer) { // ...de marcadores
						for (Marker marker : ((MarkerLayer) l).data) { // miramos
							// los
							// marcadores
							if ((marker instanceof CustomMarker)
									&& marker.containsPoint(p)) { // y si
								// estamos
								// pinchando
								// en uno
								CustomMarker m = (CustomMarker) marker;
								log.trace("Hemos pinchado en " + marker);

								switch (m.getType()) {
								case RESOURCE:
									Recurso r = (Recurso) m.getObject();
									log.trace("Es un recurso: " + r);
									if (r != null) {
										menuObjective = r;
										if (r.getPatrullas() != null) {
											titulo.setText(getString(
													"map.menu.titulo.recursoPatrulla",
													r.getIdentificador(),
													r.getPatrullas()));
										} else {
											titulo.setText(r.getIdentificador());
										}
										gps.setEnabled(true);
										summary.setEnabled(true);
									}
									break;
								case INCIDENCE:
									Incidencia i = (Incidencia) m.getObject();
									log.trace("Es una incidencia: " + i);
									if (i != null) {
										menuObjective = i;
										titulo.setText(i.getTitulo());
										gps.setEnabled(false);
										summary.setEnabled(true);
									}
									break;
								case UNKNOWN:
									log.trace("Hemos pinchado en un marcador desconocido");
									break;
								}

							}
						}
					}
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		return menu;
	}

	@Override
	protected void initializeLayers() {
		super.initializeLayers();
		this.mapView.addLayer(loadPersonas());
		this.mapView.addLayer(loadVehiculos());
		this.mapView.addLayer(loadIncidences());
	}

	private Layer loadIncidences() {
		MarkerLayer incidences = null;
		try {
			incidences = new MarkerLayer(new GpxData(),
					Internacionalization.getString("Incidences.incidences"),
					File.createTempFile("incidences", "tmp"), new GpxLayer(
							new GpxData()), this.mapView);
			incidences.visible = Authentication.getUsuario()
					.getIncidenciasVisibles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return incidences;
	}

	private Layer loadPersonas() {
		MarkerLayer resources = null;
		try {
			resources = new MarkerLayer(new GpxData(),
					Internacionalization
							.getString("Resources.resources.people"),
					File.createTempFile("layer_res", "tmp"), new GpxLayer(
							new GpxData()), this.mapView);
			resources.visible = Authentication.getUsuario()
					.getPersonasVisibles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resources;
	}

	private Layer loadVehiculos() {
		MarkerLayer resources = null;
		try {
			resources = new MarkerLayer(new GpxData(),
					Internacionalization
							.getString("Resources.resources.vehicles"),
					File.createTempFile("layer_res", "tmp"), new GpxLayer(
							new GpxData()), this.mapView);
			resources.visible = Authentication.getUsuario()
					.getVehiculosVisibles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resources;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateControls() {
		if (Authentication.isAuthenticated()) {
			controlPanel.setAvaliableResources(Collections
					.synchronizedCollection(RecursoConsultas
							.getAll(Authentication.getUsuario())));
			controlPanel.setAvaliableIncidences(Collections
					.synchronizedCollection(IncidenciaConsultas.getOpened()));
		}
	}
}
