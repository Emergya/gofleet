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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.gofleet.context.GoWired;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.mappaint.LineElemStyle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import es.emergya.bbdd.dao.RoutingHome;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.RoutingConsultas;
import es.emergya.geo.util.UTM;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.gis.CustomMapView;

public class RouteDialog extends JFrame implements ActionListener {

	static final Log log = LogFactory.getLog(RouteDialog.class);
	private static RouteDialog instance;
	private LatLon from, to;
	private JTextField fx, fy, tx, ty;
	JLabel notification;
	JLabel progressIcon;
	Icon iconTransparente;
	Icon iconEnviando;
	JButton search, clear;
	CustomMapView view;
	OsmDataLayer route;

	@GoWired
	private BasicWindow basicWindow;

	/**
	 * @return the basicWindow
	 */
	public BasicWindow getBasicWindow() {
		return basicWindow;
	}

	/**
	 * @param basicWindow the basicWindow to set
	 */
	public void setBasicWindow(BasicWindow basicWindow) {
		this.basicWindow = basicWindow;
	}
	private RouteDialog() {
		super();
		setAlwaysOnTop(true);
		setResizable(false);
		iconTransparente = LogicConstants.getIcon("48x48_transparente");
		iconEnviando = LogicConstants.getIcon("anim_calculando");
		try {
			route = new OsmDataLayer(new DataSet(), "route", File
					.createTempFile("route", "route"));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				clear.doClick();
				setVisible(false);
			}
		});
		setTitle(getString("window.route.titleBar"));
		setMinimumSize(new Dimension(400, 200));
		setIconImage(getBasicWindow().getFrame().getIconImage());
		JPanel base = new JPanel();
		base.setBackground(Color.WHITE);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		// Icono del titulo
		JPanel title = new JPanel(new FlowLayout(FlowLayout.LEADING));
		title.setOpaque(false);
		final JLabel labelTitle = new JLabel(getString("window.route.title"),
				LogicConstants.getIcon("tittleventana_icon_calcularruta"),
				JLabel.LEFT);
		labelTitle.setFont(LogicConstants.deriveBoldFont(12.0f));
		title.add(labelTitle);
		base.add(title);

		JPanel content = new JPanel(new SpringLayout());
		content.setOpaque(false);

		// Coordenadas
		content.add(new JLabel(getString("window.route.origen"), JLabel.LEFT));
		JPanel coords = new JPanel(new GridLayout(1, 2));
		coords.setOpaque(false);
		fx = new JTextField(8);
		fx.setEditable(false);
		fy = new JTextField(8);
		fy.setEditable(false);
		coords.add(fy);
		coords.add(fx);
		content.add(coords);
		content.add(new JLabel(getString("window.route.destino"), JLabel.LEFT));
		JPanel coords2 = new JPanel(new GridLayout(1, 2));
		coords2.setOpaque(false);
		tx = new JTextField(8);
		tx.setEditable(false);
		ty = new JTextField(8);
		ty.setEditable(false);
		coords2.add(ty);
		coords2.add(tx);
		content.add(coords2);

		SpringUtilities.makeCompactGrid(content, 2, 2, 6, 6, 6, 6);
		base.add(content);

		// Area para mensajes
		JPanel notificationArea = new JPanel();
		notificationArea.setOpaque(false);
		notification = new JLabel("PLACEHOLDER");
		notification.setForeground(Color.WHITE);
		notificationArea.add(notification);
		base.add(notificationArea);

		JPanel buttons = new JPanel();
		buttons.setOpaque(false);
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		search = new JButton(getString("window.route.calcular"), LogicConstants
				.getIcon("ventanacontextual_button_calcularruta"));
		search.addActionListener(this);
		buttons.add(search);
		clear = new JButton(getString("window.route.limpiar"), LogicConstants
				.getIcon("button_limpiar"));
		clear.addActionListener(this);
		buttons.add(clear);
		buttons.add(Box.createHorizontalGlue());
		progressIcon = new JLabel(iconTransparente);
		buttons.add(progressIcon);
		buttons.add(Box.createHorizontalGlue());
		JButton cancel = new JButton(getString("Buttons.cancel"),
				LogicConstants.getIcon("button_cancel"));
		cancel.addActionListener(this);
		buttons.add(cancel);
		base.add(buttons);
		getContentPane().add(base);
		pack();
		int x;
		int y;

		Container myParent = getBasicWindow().getFrame().getContentPane();
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
	}

	/**
	 * @param from
	 *            the from to set
	 */
	public void setFrom(LatLon from) {
		if (from != null) {
			this.from = from;
			if (LogicConstants.get("FORMATO_COORDENADAS_MAPA", "UTM").equals(
					LogicConstants.COORD_UTM)) {
				UTM u = new UTM(LogicConstants.getInt("ZONA_UTM"));
				EastNorth enf = u.latlon2eastNorth(from);
				fy.setText(String.valueOf(enf.getX()));
				fx.setText(String.valueOf(enf.getY()));
			} else {
				fx.setText(String.valueOf(from.getX()));
				fy.setText(String.valueOf(from.getY()));
			}
		}
	}

	/**
	 * @param to
	 *            the to to set
	 */
	public void setTo(LatLon to) {
		if (to != null) {
			this.to = to;
			if (LogicConstants.get("FORMATO_COORDENADAS_MAPA", "UTM").equals(
					LogicConstants.COORD_UTM)) {
				// mirar LogicConstants.transform(Point geom, final String
				// sourceSRID, final String targetSRID)
				UTM u = new UTM(LogicConstants.getInt("ZONA_UTM"));
				EastNorth ent = u.latlon2eastNorth(to);
				ty.setText(String.valueOf(ent.getX()));
				tx.setText(String.valueOf(ent.getY()));
			} else {
				tx.setText(String.valueOf(to.getX()));
				ty.setText(String.valueOf(to.getY()));
			}
		}
	}

	public static void showRouteDialog(LatLon from, LatLon to, CustomMapView view) {
		JFrame f = getRouteDialog(from, to, view);
		f.setVisible(true);
		f.setExtendedState(JFrame.NORMAL);
		f.requestFocus();
	}

	public static JFrame getRouteDialog(LatLon from, LatLon to,
			CustomMapView view) {
		if (instance == null) {
			instance = new RouteDialog();
		}
		instance.view = view;

		instance.setFrom(from);
		instance.setTo(to);
		if (instance.from == null || instance.to == null) {
			instance.notification.setText(getString("progress.route.nopoints"));
			instance.notification.setForeground(Color.RED);

			instance.search.setEnabled(false);
		} else {
			instance.notification.setForeground(Color.WHITE);
			// instance.notification.setText("");
			instance.search.setEnabled(true);
		}
		if (instance.from == null && instance.to == null) {
			instance.clear.setEnabled(false);
		} else {
			instance.clear.setEnabled(true);
		}

		return instance;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(search)) {
			notification.setForeground(Color.WHITE);
			notification.updateUI();
			progressIcon.setIcon(iconEnviando);
			search.setEnabled(false);
			clear.setEnabled(false);
			new SwingWorker<Boolean, Object>() {
				List<Way> route = null;

				protected void done() {
					if (route != null && route.size() > 0) {
						setRoute(route);
					} else {
						notification
								.setText(getString("window.route.notification.noRoute"));
						notification.setForeground(Color.RED);
					}
					progressIcon.setIcon(iconTransparente);
					clear.setEnabled(true);
					search.setEnabled(true);
					notification.updateUI();
				}

				@Override
				protected Boolean doInBackground() throws Exception {
					try {
						route = getRoute();
						return true;
					} catch (Throwable t) {
						log.error("Error al calcular la ruta", t);
						notification.setText(getString("progress.route.error"));
						notification.setForeground(Color.RED);
						return false;
					}
				}

			}.execute();

		} else {
			from = null;
			fx.setText("");
			fy.setText("");
			to = null;
			tx.setText("");
			ty.setText("");
			search.setEnabled(false);
			clear.setEnabled(false);

			clearRoute();
			instance.notification.setText(getString("progress.route.nopoints"));
			instance.notification.setForeground(Color.RED);
			notification.updateUI();
			if (!e.getSource().equals(clear)) {
				setVisible(false);
			}
		}
	}

	public static void main(String[] args) {
		getRouteDialog(new LatLon(0, 0), new LatLon(1, 1), null).setVisible(
				true);
	}

	private void setRoute(List<Way> ways) {
		clearRoute();
		LineElemStyle ls = new LineElemStyle();
		ls.color = Color.decode(LogicConstants.get("WAY_COLOR"));
		ls.width = Integer.parseInt(LogicConstants.get("WAY_WIDTH"));
		for (Way way : ways) {
			route.data.ways.add(way);
			way.mappaintStyle = ls;
		}
		route.visible = true;
		view.addLayer(route, false);
		view.repaint();
	}

	private void clearRoute() {
		route.data.nodes.clear();
		route.data.ways.clear();
		Layer capaABorrar = null;
		try {
			for (Layer l : view.getAllLayers()) {
				if (l.name.equals(route.name)) {
					capaABorrar = l;
					break;
				}
			}
			if (capaABorrar != null) {
				view.removeLayer(capaABorrar);
			}
		} catch (ConcurrentModificationException cme) {
			log.error("Error eliminado la capa de rutas.", cme);
		}
		view.repaint();
	}

	private List<Way> getRoute() throws Throwable {
		LinkedList<Way> res = new LinkedList<Way>();
		GeometryFactory f = new GeometryFactory();
		Point origen = f.createPoint(new Coordinate(from.lon(), from.lat()));
		Point destino = f.createPoint(new Coordinate(to.lon(), to.lat()));

		destino.setSRID(4326);
		origen.setSRID(4326);

		log.debug("getRoute() from " + origen + " to " + destino);

		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:"
				+ RoutingHome.SRID);
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS,
				true);
		origen = (Point) JTS.transform(origen, transform).getCentroid();
		destino = (Point) JTS.transform(destino, transform).getCentroid();

		MultiLineString mls = RoutingConsultas.calculateRoute(origen, destino);
		if (mls != null) {
			for (int i = 0; i < mls.getNumGeometries(); i++) {
				Way way = new Way();
				for (Coordinate coordenada : ((LineString) mls.getGeometryN(i))
						.getCoordinates()) {
					Point p = f.createPoint(coordenada);
					p = (Point) JTS.transform(p, CRS.findMathTransform(
							targetCRS, sourceCRS));
					LatLon ll = new LatLon(p.getY(), p.getX());
					way.addNode(new Node(ll));
//					if (log.isTraceEnabled())
//						BasicWindow.showOnMap(ll, 1);
				}
				res.add(way);
			}
		}

		if (log.isTraceEnabled())
			log.trace("Ruta obtenida: " + res);

		return res;
	}
}
