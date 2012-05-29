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
package es.emergya.ui.gis;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.MyGpxLayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import edu.emory.mathcs.backport.java.util.Collections;
import es.emergya.bbdd.bean.HistoricoGPS;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.bbdd.bean.Routing;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.HistoricoGPSConsultas;
import es.emergya.consultas.IncidenciaConsultas;
import es.emergya.consultas.RoutingConsultas;
import es.emergya.geo.util.UTM;

public class ControlPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 5987017158504426714L;
	private static final Log log = LogFactory.getLog(ControlPanel.class);
	JPanel centerData;
	JComboBox centerOptions;
	JTextField cx, cy;
	JComboBox resources;
	JComboBox incidences;
	JTextField street;
	CustomMapView view;
	Vector<Object> avaliableResources = new Vector<Object>();
	Vector<Object> avaliableIncidences = new Vector<Object>();
	private final AutocompleteKeyListener autocompleteKeyListener;
	private boolean isComboResourcesShowing = false;
	private boolean isComboIncidencesShowing = false;
	SwingWorker<Object, Object> autocompleteSw, centerSw;
	private final Comparator<Object> comparator = new Comparator<Object>() {

		@Override
		public int compare(Object arg0, Object arg1) {
			return arg0.toString().compareTo(arg1.toString());
		}
	};

	@GoWired
	public I18n i18n;

	/**
	 * @return the i18n
	 */
	public I18n getI18n() {
		return i18n;
	}

	/**
	 * @param i18n the i18n to set
	 */
	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}
	public ControlPanel(final CustomMapView view) {
		super(new FlowLayout(FlowLayout.LEADING, 12, 0));
		this.view = view;
		// Posicion: panel con un label de icono y un textfield
		JPanel posPanel = new JPanel();
		posPanel.setOpaque(true);
		posPanel.setVisible(true);
		JLabel mouseLocIcon = new JLabel(LogicConstants
				.getIcon("map_icon_coordenadas"));
		posPanel.add(mouseLocIcon);
		final JTextField posField = new JTextField(15);
		posField.setEditable(false);
		posField.setBorder(null);
		posField.setForeground(UIManager.getColor("Label.foreground"));
		posField.setFont(UIManager.getFont("Label.font"));
		posPanel.add(posField);
		view.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				LatLon ll = ((ICustomMapView) e.getSource()).getLatLon(e.getX(),
						e.getY());
				String position = "";
				String format = LogicConstants.get("FORMATO_COORDENADAS_MAPA",
						"UTM");
				if (format.equals(LogicConstants.COORD_UTM)) {
					UTM u = new UTM(LogicConstants.getInt("ZONA_UTM"));
					EastNorth en = u.latlon2eastNorth(ll);
					position = String.format("x: %.1f y: %.1f", en.getX(), en
							.getY());
				} else {
					position = String.format("Lat: %.4f Lon: %.4f", ll.lat(),
							ll.lon());
				}

				posField.setText(position);
				validate();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		posPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(posPanel);

		// Panel de centrado: label, desplegable y parte cambiante
		JPanel centerPanel = new JPanel();
		centerPanel.add(new JLabel(i18n.getString("map.centerIn")));
		centerOptions = new JComboBox(new String[] { i18n.getString("map.street"),
				i18n.getString("map.resource"), i18n.getString("map.incidence"),
				i18n.getString("map.location") });
		centerPanel.add(centerOptions);

		centerData = new JPanel(new CardLayout());
		centerPanel.add(centerData);

		JPanel centerStreet = new JPanel();
		street = new JTextField(30);
		street.setName(i18n.getString("map.street"));
		autocompleteKeyListener = new AutocompleteKeyListener(street);
		street.addKeyListener(autocompleteKeyListener);
		street.addActionListener(this);
		centerStreet.add(street);
		centerData.add(centerStreet, i18n.getString("map.street"));

		JPanel centerResource = new JPanel();
		resources = new JComboBox(avaliableResources);
		resources.setName(i18n.getString("map.resource"));
		resources.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		resources.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				isComboResourcesShowing = true;
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				isComboResourcesShowing = false;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// view.repaint();
			}
		});
		centerResource.add(resources);
		centerData.add(centerResource, i18n.getString("map.resource"));

		centerResource = new JPanel();
		incidences = new JComboBox(avaliableIncidences);
		incidences.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		incidences.setName(i18n.getString("map.incidence"));
		incidences.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				isComboIncidencesShowing = true;
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				isComboIncidencesShowing = false;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});
		centerResource.add(incidences);
		centerData.add(centerResource, i18n.getString("map.incidence"));

		JPanel centerLocation = new JPanel();
		cx = new JTextField(10);
		cx.setName("x");
		cx.addActionListener(this);
		centerLocation.add(cx);
		cy = new JTextField(10);
		cy.setName("y");
		cy.addActionListener(this);
		centerLocation.add(cy);
		centerData.add(centerLocation, i18n.getString("map.location"));

		centerOptions.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				((CardLayout) centerData.getLayout()).show(centerData,
						(String) e.getItem());
			}
		});

		JButton centerButton = new JButton(i18n.getString("map.center"));
		centerButton.addActionListener(this);
		centerPanel.add(centerButton);
		add(centerPanel);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (centerSw != null && !centerSw.isDone() && !centerSw.isCancelled()) {
			centerSw.cancel(true);
		}
		centerSw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {

				if (centerOptions.getSelectedItem().equals(
						i18n.getString("map.location"))) { // Estamos
					// centrando
					// en
					// x,
					// y
					double x = 0, y = 0;
					try {
						x = Double.parseDouble(cx.getText().replace(',', '.'));
						y = Double.parseDouble(cy.getText().replace(',', '.'));
					} catch (NumberFormatException nfe) {
						log.debug("No se centra: formato erroneo");
						return null;
					}

					String format = LogicConstants.get(
							"FORMATO_COORDENADAS_MAPA", "UTM");
					if (format.equals(LogicConstants.COORD_UTM)) {
						UTM u = new UTM(LogicConstants.getInt("ZONA_UTM"));
						LatLon ll = u.eastNorth2latlon(new EastNorth(x, y));
						view.zoomTo(Main.proj.latlon2eastNorth(ll), view
								.getScale());
					} else {
						// en el latlong la x y la y van al reves
						view.zoomTo(Main.proj
								.latlon2eastNorth(new LatLon(x, y)), view
								.getScale());
					}
				} else if (centerOptions.getSelectedItem().equals(
						i18n.getString("map.street"))) {
					Routing r = RoutingConsultas.find(street.getText());
					if (r != null && r.getGeometria() != null) {
						Point center = r.getGeometria().getCentroid()
								.getCentroid();
						view
								.zoomTo(Main.proj.latlon2eastNorth(new LatLon(
										center.getY(), center.getX())), view
										.getScale());
					}

				} else if (centerOptions.getSelectedItem().equals(
						i18n.getString("map.incidence"))) {

					final Object incidencia = incidences.getSelectedItem();
					if (incidencia == null) {
						return null;
					}

					Incidencia i = null;
					if (incidencia instanceof Incidencia) {
						i = (Incidencia) incidencia;
					} else {
						i = IncidenciaConsultas.find(incidencia.toString());
					}

					Geometry geom = i.getGeometria();
					if (geom == null) {
						return null;
					}

					Point center = geom.getCentroid();
					view.zoomTo(Main.proj.latlon2eastNorth(new LatLon(center
							.getY(), center.getX())), view.getScale());
					return null;

				} else if (centerOptions.getSelectedItem().equals(
						i18n.getString("map.resource"))) {
					final Object selectedItem = resources.getSelectedItem();
					if (selectedItem == null) {
						return null;
					}
					if (selectedItem instanceof Recurso) {
						Recurso r = (Recurso) selectedItem;
						HistoricoGPS h = null;
						if (r.getId() != null) {
							h = HistoricoGPSConsultas.lastGPSForRecurso(r);
						} else
							try {
								h = r.getHistoricoGps();
							} catch (Throwable t) {
								h = null;
							}
						if (h == null) {
							return null;
						}
						view.zoomTo(Main.proj.latlon2eastNorth(new LatLon(h
								.getPosY(), h.getPosX())), view.getScale());
					} else if (selectedItem instanceof String) {
						String r = (String) selectedItem;
						HistoricoGPS h = HistoricoGPSConsultas
								.lastGPSForRecurso(r);
						if (h == null) {
							return null;
						}
						view.zoomTo(Main.proj.latlon2eastNorth(new LatLon(h
								.getPosY(), h.getPosX())), view.getScale());
					} else if (selectedItem instanceof GpxLayer) {
						MyGpxLayer r = (MyGpxLayer) selectedItem;
						if (r == null || r.getLatLon() == null) {
							return null;
						}

						view.zoomTo(Main.proj.latlon2eastNorth(r.getLatLon()),
								view.getScale());
					}
				}
				return null;
			}
		};
		centerSw.execute();

	}

	public void setAvaliableIncidences(Collection<Object> res) {
		avaliableIncidences = new Vector<Object>(res);
		Collections.sort(avaliableIncidences, comparator);

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				return null;
			}

			@Override
			protected void done() {
				super.done();
				if (!isComboIncidencesShowing) {
					Object selected = incidences.getSelectedItem();
					incidences.removeAllItems();
					for (Object o : avaliableIncidences) {
						incidences.addItem(o);
					}
					incidences.setSelectedItem(selected);
				}
				if (!isComboIncidencesShowing)
					incidences.updateUI();
			}
		};
		sw.execute();
	}

	public void setAvaliableResources(Collection<Object> res) {
		avaliableResources = new Vector<Object>(res);
		Collections.sort(avaliableResources, comparator);

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				return null;
			}

			@Override
			protected void done() {
				super.done();
				// actualizamos la lista
				if (!isComboResourcesShowing) {
					Object selected = resources.getSelectedItem();
					resources.removeAllItems();
					for (Object o : avaliableResources) {
						resources.addItem(o);
					}
					resources.setSelectedItem(selected);
				}
				if (!isComboResourcesShowing)
					resources.updateUI();
			}
		};
		sw.execute();
	}

	private class AutocompleteKeyListener extends KeyAdapter {

		JTextComponent targetComponent;
		JPopupMenu suggestions;

		public AutocompleteKeyListener(JTextComponent c) {
			super();
			targetComponent = c;
			suggestions = new JPopupMenu();
			suggestions.setFocusable(false);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN
					|| e.getKeyCode() == KeyEvent.VK_UP
					|| e.getKeyCode() == KeyEvent.VK_LEFT
					|| e.getKeyCode() == KeyEvent.VK_RIGHT) {
				return;
			}

			String text = targetComponent.getText().replace('*', '%');
			if (!text.endsWith("%")) {
				text += "%";
			}
			if (StringUtils.isBlank(text)
					|| StringUtils.trimToEmpty(text).matches("^(%+)$")) {
				return;
			}

			List<String> strts = RoutingConsultas.find(text,
					LogicConstants.MAX_STREET_AUTOCOMPLETE_RESULTS);

			suggestions.removeAll();
			if (!strts.isEmpty()) {
				for (String s : strts) {
					suggestions.add(new JMenuItem(new CentrarAction(s)));
				}

				suggestions.setVisible(false);
				suggestions.show(targetComponent, 0, targetComponent
						.getHeight());
			} else {
				suggestions.setVisible(false);
			}
			return;
		}
	}

	class CentrarAction extends AbstractAction {
		private static final long serialVersionUID = 4736673832374893782L;
		private String name = null;

		public CentrarAction(String texto) {
			super(texto);
			this.name = texto;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Routing r = RoutingConsultas.find(this.name);
			if (r != null && r.getGeometria() != null) {
				Point center = r.getGeometria().getCentroid().getCentroid();
				view.zoomTo(Main.proj.latlon2eastNorth(new LatLon(
						center.getY(), center.getX())), view.getScale());
			}
		}
	}

}
