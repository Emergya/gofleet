/**
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marias@emergya.es">María Arias de Reyna</a>
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
package org.gofleet.module.routing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;
import org.gofleet.openls.RoutingServiceStub;
import org.gofleet.openls.RoutingServiceStub.GetTravellingSalesmanPlan;
import org.gofleet.openls.RoutingServiceStub.GetTravellingSalesmanPlanResponse;
import org.gofleet.openls.RoutingServiceStub.TSPPlan;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.mappaint.LineElemStyle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.WKTReader;

import edu.emory.mathcs.backport.java.util.Collections;
import es.emergya.actions.Authentication;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.IncidenciaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.gis.CustomMapView.InitAdapter;
import es.emergya.ui.gis.MapViewer;

public class RoutingMap extends MapViewer implements ActionListener {

	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(RoutingMap.class);
	private static final long serialVersionUID = -1837324556102054550L;
	private final WKTReader wktReader = new WKTReader();

	/**
	 * @param title
	 * @param type
	 * @param order
	 * @param icon
	 * @param layers
	 * @param mouseWheelListener
	 * @param mouseListener
	 * @param mouseMotionListener
	 * @param initAdapter
	 */
	public RoutingMap(String title, PluginType type, int order, String icon,
			List<MarkerLayer> layers, MouseWheelListener mouseWheelListener,
			MouseListener mouseListener,
			MouseMotionListener mouseMotionListener, InitAdapter initAdapter) {
		super(title, type, order, icon, layers, mouseWheelListener,
				mouseListener, mouseMotionListener, initAdapter);

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

	@GoWired
	public BasicWindow basicWindow;

	/**
	 * @return the i18n
	 */
	public BasicWindow getBasicWindow() {
		return basicWindow;
	}

	/**
	 * @param i18n
	 *            the i18n to set
	 */
	public void setBasicWindow(BasicWindow basicWindow) {
		this.basicWindow = basicWindow;
	}

	/**
	 * @param title
	 * @param type
	 * @param order
	 * @param icon
	 * @param layers
	 */
	public RoutingMap(String title, PluginType type, int order, String icon,
			List<MarkerLayer> layers) {
		super(title, type, order, icon, layers);
	}

	/**
	 * @param title
	 * @param type
	 * @param order
	 * @param icon
	 */
	public RoutingMap(String title, PluginType type, int order, String icon) {
		super(title, type, order, icon);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				String mapMenuNewPlanning = i18n
						.getString("map.menu.new.planning");
				try {
					if (e.getActionCommand().equals(mapMenuNewPlanning)) {
						LatLon from = RoutingMap.this.mapView
								.getLatLon(RoutingMap.this.mapView.lastMEvent
										.getPoint().x,
										RoutingMap.this.mapView.lastMEvent
												.getPoint().y);
						newPlan(from);
					} else {
						log.error("ActionCommand desconocido: "
								+ e.getActionCommand() + " vs "
								+ mapMenuNewPlanning);
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

	private void newPlan(LatLon from) {
		JDialog d = new JDialog(basicWindow.getFrame(), "Generating New Plan");
		try {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new RoutingFilter());
			fc.setAcceptAllFileFilterUsed(true);
			int returnVal = fc.showOpenDialog(basicWindow.getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				log.debug("Opening: " + file.getName());

				JProgressBar progressBar = new JProgressBar(0,
						getNumberLines(file) * 2);
				progressBar.setValue(0);
				progressBar.setPreferredSize(new Dimension(150, 50));
				progressBar.setStringPainted(true);

				d.add(progressBar);
				d.pack();
				d.setVisible(true);

				TSPPlan[] param = processFile(file, progressBar);

				Map<String, String> values = getValues(from);

				double[] origin = new double[2];
				origin[0] = new Double(values.get("origin_x"));
				origin[1] = new Double(values.get("origin_y"));

				TSPPlan[] res = calculateRouteOnWS(
						new Integer(values.get("maxDistance")), new Integer(
								values.get("maxTime")), origin, new Integer(
								values.get("startTime")), param, new Integer(
								values.get("timeSpentOnStop")));

				progressBar.setValue(progressBar.getMaximum() - res.length);

				processTSPPlan(res, progressBar);

			} else {
				log.trace("Open command cancelled by user.");
			}
		} catch (Throwable t) {
			log.error("Error computing new plan", t);
			JOptionPane.showMessageDialog(basicWindow.getFrame(), "<html><p>"
					+ i18n.getString("Main.Error") + ":</p><p>" + t.toString()
					+ "</p><html>", i18n.getString("Main.Error"),
					JOptionPane.ERROR_MESSAGE);
		} finally {
			d.setVisible(false);
			d.dispose();
		}

	}

	private Map<String, String> getValues(LatLon from) {
		final Map<String, String> mapa = new HashMap<String, String>();
		final JDialog frame = new JDialog(basicWindow.getFrame(),
				"Configuration");
		JPanel panel = new JPanel(new GridLayout(0, 2));
		int width = 10;

		final JTextField maxDistance = new JTextField(width);
		maxDistance.setText("10");
		final JTextField maxTime = new JTextField(width);
		maxTime.setText("8");
		final JTextField origin_x = new JTextField(width / 2);
		origin_x.setText((new Double(from.getX())).toString());
		final JTextField origin_y = new JTextField(width / 2);
		origin_y.setText((new Double(from.getY())).toString());
		final JTextField startTime = new JTextField(width);
		startTime.setText("7");
		final JTextField timeSpentOnStop = new JTextField(width);
		timeSpentOnStop.setText("1");
		JLabel lmaxDistance = new JLabel("Maximum Distance (km)");
		lmaxDistance.setLabelFor(maxDistance);
		JLabel lmaxTime = new JLabel("Maximum Time (hours)");
		lmaxTime.setLabelFor(maxTime);
		JLabel lorigin = new JLabel("Point of Origin");
		lorigin.setLabelFor(origin_x);
		JLabel lstartTime = new JLabel("Start Time of Plan (0-24)");
		lstartTime.setLabelFor(startTime);
		JLabel ltimeSpentOnStop = new JLabel("Time Spent on Stop (hours)");
		ltimeSpentOnStop.setLabelFor(timeSpentOnStop);

		JButton close = new JButton("OK");
		close.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = -8912729211256933464L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					mapa.put("maxDistance", maxDistance.getText());
					mapa.put("maxTime", maxTime.getText());
					mapa.put("origin_x", origin_x.getText());
					mapa.put("origin_y", origin_y.getText());
					mapa.put("startTime", startTime.getText());
					mapa.put("timeSpentOnStop", timeSpentOnStop.getText());
					frame.dispose();
				} catch (Throwable t) {
					log.error("Error configuring New Route Plan" + t);
					JOptionPane.showMessageDialog(RoutingMap.this,
							"Some values are wrong. Check them again.");
				}
			}
		});

		panel.add(lmaxDistance);
		panel.add(maxDistance);
		panel.add(lmaxTime);
		panel.add(maxTime);
		panel.add(lorigin);
		JPanel panel_origin = new JPanel();
		panel_origin.add(origin_x);
		panel_origin.add(origin_y);
		panel.add(panel_origin);
		panel.add(lstartTime);
		panel.add(startTime);
		panel.add(ltimeSpentOnStop);
		panel.add(timeSpentOnStop);
		panel.add(close);
		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setModalityType(ModalityType.APPLICATION_MODAL);
		frame.setVisible(true);

		return mapa;
	}

	private void processTSPPlan(TSPPlan[] res, JProgressBar progressBar) {
		Random random = new Random();
		try {

			@SuppressWarnings("unchecked")
			Collection<Layer> allLayers = Collections
					.unmodifiableCollection(this.mapView.getAllLayers());
			List<Layer> toremove = new LinkedList<Layer>();
			for (Layer l : allLayers) {
				if (l.name.startsWith("Route Plan")
						|| l.name.startsWith("Stops"))
					toremove.add(l);
			}
			for (Layer l : toremove)
				this.mapView.removeLayer(l);

			LatLon latlon_origin = null;
			int id_layer = 0;
			for (TSPPlan plan : res) {
				latlon_origin = new LatLon(plan.getOrigin()[1],
						plan.getOrigin()[0]);
				log.info(latlon_origin);
				LineElemStyle ls = new LineElemStyle();
				float f = random.nextFloat();
				ls.color = Color
						.getHSBColor(f * random.nextFloat(), 0.9f, 0.9f);
				ls.width = LogicConstants.getInt("PLAN_WAY_WIDTH", 2);
				MarkerLayer stops = new MarkerLayer(new GpxData(), "Stops "
						+ id_layer, File.createTempFile("stops", "tmp"),
						new GpxLayer(new GpxData()), this.mapView);
				stops.data.add(new StopMarker(latlon_origin, "origin",
						"tsp_stop", stops, 0, 0, ls.color));
				for (String stop : plan.getStops()) {
					String[] array = stop.split(",");
					double[] point = new double[2];
					point[1] = new Double(array[0]);
					point[0] = new Double(array[1]);

					LatLon ll = new LatLon(point[0], point[1]);
					log.info(ll);
					stops.data.add(new StopMarker(ll, array[2], "tsp_stop",
							stops, 0, 0, ls.color));
				}
				this.mapView.addLayer(stops, true);

				OsmDataLayer layer = new OsmDataLayer(new DataSet(),
						"Route Plan " + id_layer++, File.createTempFile(
								"planning", "route"));
				String way2 = plan.getWay();

				if (way2 != null) {
					Way way = new Way();
					LatLon info = null;

					MultiLineString multilinestring = (MultiLineString) wktReader
							.read(way2);
					multilinestring.getLength();
					int numGeometries = multilinestring.getNumGeometries();
					for (int i = 0; i < numGeometries; i++) {
						for (Coordinate coordenada : multilinestring
								.getGeometryN(i).getCoordinates()) {
							LatLon ll = new LatLon(coordenada.y, coordenada.x);
							way.addNode(new Node(ll));
							if (info == null)
								info = ll;
						}
						way.mappaintStyle = ls;
						layer.data.ways.add(way);
						way = new Way();
					}
					progressBar.setValue(progressBar.getValue() + 1);

					StopMarker marker = new StopMarker(info, (new Double(
							plan.getDistance())).toString().substring(0, 5)
							+ " km in "
							+ (new Double(plan.getTime() / 60)).toString()
									.substring(0, 3) + " hours", "tsp_stop",
							stops, 0, 0, ls.color);
					marker.setPaintIcon(false);
					stops.data.add(marker);
				}
				this.mapView.addLayer(layer, true);

				layer.visible = true;
				stops.visible = true;
			}
		} catch (Throwable e) {
			log.error("Error painting plan", e);
		}
	}

	private TSPPlan[] calculateRouteOnWS(Integer maxDistance, Integer maxTime,
			double[] doubles, Integer startTime, TSPPlan[] stops,
			Integer timeSpentOnStop) {

		try {
			TSPPlan[] tspplan = new TSPPlan[stops.length];
			RoutingServiceStub rss = new RoutingServiceStub(
					LogicConstants
							.get("URL_ROUTING",
									"http://46.105.24.58:8080/openLS/services/RoutingService.RoutingServiceHttpSoap12Endpoint/"));

			for (int i = 0; i < tspplan.length; i++) {
				final TSPPlan tspPlan2 = new TSPPlan();
				tspPlan2.setDistance(-1);
				tspPlan2.setOrigin(doubles);
				tspPlan2.setTime(-1);
				tspPlan2.setWay(null);
				tspPlan2.setStops(stops[i].getStops());
				tspplan[i] = tspPlan2;
				GetTravellingSalesmanPlan param = new GetTravellingSalesmanPlan();
				param.setMaxDistance(maxDistance);
				param.setMaxTime(maxTime);
				param.setStartTime(startTime);
				param.setTimeSpentOnStop(timeSpentOnStop);
				param.setParam(tspplan[i]);

				try {
					GetTravellingSalesmanPlanResponse res = rss
							.getTravellingSalesmanPlan(param);
					tspplan[i] = res.get_return();
				} catch (Throwable t) {
					log.error("error computing " + i + " plan", t);
				}
			}

			return tspplan;
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(basicWindow.getFrame(), "<html><p>"
					+ "Error" + ":</p><p>" + t.toString() + "</p><html>",
					i18n.getString("Main.Error"), JOptionPane.ERROR_MESSAGE);
			log.error(t, t);
		}
		return new TSPPlan[0];
	}

	private TSPPlan[] processFile(File file, JProgressBar progressbar) {
		try {
			LinkedList<TSPPlan> res = new LinkedList<TSPPlan>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int i = 0;
			TSPPlan plan = new TSPPlan();

			while ((line = br.readLine()) != null) {
				log.trace(line);
				progressbar.setValue(i++);

				if (!line.isEmpty())
					plan.addStops(line);
				else {
					res.add(plan);
					plan = new TSPPlan();
				}
			}
			res.add(plan);
			return res.toArray(new TSPPlan[0]);
		} catch (Throwable e) {
			log.error("Error processing file", e);
		}
		return null;
	}

	private int getNumberLines(File f) {
		LineNumberReader lineCounter;
		try {
			lineCounter = new LineNumberReader(new InputStreamReader(
					new FileInputStream(f.getPath())));

			while (lineCounter.readLine() != null)
				;
			return lineCounter.getLineNumber();
		} catch (Exception done) {
			log.error(done, done);
			return -1;
		}

	}

	@Override
	protected JPopupMenu getContextMenu() {
		JPopupMenu menu = new JPopupMenu();

		menu.setBackground(Color.decode("#E8EDF6"));

		String mapMenuTituloPlanning = i18n
				.getString("map.menu.titulo.planning");
		String mapMenuNewPlanning = i18n.getString("map.menu.new.planning");
		// Título
		final JMenuItem titulo = new JMenuItem(mapMenuTituloPlanning);
		titulo.setFont(LogicConstants.deriveBoldFont(10.0f));
		titulo.setBackground(Color.decode("#A4A4A4"));
		titulo.setFocusable(false);

		menu.add(titulo);

		// New Planning
		final JMenuItem to = new JMenuItem(mapMenuNewPlanning, KeyEvent.VK_F6);
		to.setIcon(LogicConstants.getIcon("menucontextual_icon_destinoruta"));
		to.addActionListener(this);
		menu.add(to);

		menu.addSeparator();

		return menu;
	}

	@Override
	protected void initializeLayers() {
		super.initializeLayers();
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

class RoutingFilter extends FileFilter {

	// gfr, GoFleetRouting
	@Override
	public boolean accept(File arg0) {
		return arg0.getName().endsWith(".gfr");
	}

	@Override
	public String getDescription() {
		return "GoFleet Routing files.";
	}

}