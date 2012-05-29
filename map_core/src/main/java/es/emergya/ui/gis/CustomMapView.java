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
 * 01/07/2009
 */
package es.emergya.ui.gis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;
import org.openstreetmap.gui.jmapviewer.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapMover;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.preferences.ToolbarPreferences;

import es.emergya.actions.Authentication;
import es.emergya.actions.UsuarioAdmin;
import es.emergya.bbdd.bean.HistoricoGPS;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.IncidenciaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.consultas.UsuarioConsultas;
import es.emergya.ui.gis.layers.MapViewerLayer;
import es.emergya.ui.gis.markers.CustomMarker;
import es.emergya.ui.gis.markers.CustomMarker.Type;
import es.emergya.ui.gis.paintingThreads.Updatable;
import es.emergya.ui.plugins.LayerSelectionDialog;
import es.emergya.ui.plugins.ZoomPerformed;

/**
 * Extends the functionality of {@link MapView} Allowing access to some private
 * or protected methods, limiting the zoom scale to osm zoom factor equivalents
 * and enabling rotation operation
 * 
 * @author fario
 */
public class CustomMapView extends MapView implements RotatableView,
		MapMoveListener, ZoomPerformed, Contextful, ICustomMapView {

	// Constantes de utilidad
	private static final double PI2 = Math.PI * 2;
	private static final double PI_MEDIO = Math.PI / 2;
	/** Distancia (EastNorthing) minimo a partir del cual se seguira un marcador */
	private static final double FOLLOW_THRESSHOLD = Double
			.parseDouble(LogicConstants.get("FOLLOW_THRESSHOLD")); // in
	// eastnorthing
	// units
	private static final Log log = LogFactory.getLog(CustomMapView.class);
	public double fps = 2; // solo util para la obu
	private static final long serialVersionUID = 734833313834536432L;
	/** factor de zoom de mapnik */
	protected int zoomFactor;
	/** limites de zoom la capa raster base */
	protected int minZoom = LogicConstants.getInt("MIN_TILE_ZOOM"),
			maxZoom = LogicConstants.getInt("MAX_TILE_ZOOM"); // por defecto,
	// luego se les
	// da
	// valores
	/** Giro actual */
	protected double angle;
	protected double sin, cos = 1; // es util tenerlos a mano, Math.sin/cos son
	// lentos
	/**
	 * Este marcador estara siempre en el centro del mapa y si se mueve el mapa
	 * lo seguira
	 */
	protected Marker follow;
	/**
	 * Hacer zoom a una distancia apropiada para que
	 * {@link CustomMapView#follow} se vea
	 */
	protected boolean autoZoom = true;
	/** Girar en la direccion en la que {@link CustomMapView#follow} se mueve */
	protected boolean autoTurn = true;
	/** Suavizar el giro si autoTurn esta activado */
	protected boolean smoothTurn = true;
	/** Ultima posicion de {@link CustomMapView#follow} */
	private EastNorth lastFollowPos; // se usa para ver en que direccion se
	// mueve
	/** Ultima direccion en la que se movia {@link CustomMapView#follow} */
	private double lastFollowAngle = 0; // se usa para hacer smoothTurn
	/** La distancia que se movio {@link CustomMapView#follow} la ultima vez */
	private double lastDistance = 0; // se usa para hacer autoZoom
	protected Updater positionUpdater;
	protected JPanel layerControlPanel; // controles para mostrar/ocultar capas
	protected LayerSelectionDialog layerDialog;
	// protected JPanel controlPanel; // Diversos controles
	// protected ZoomControlPanel zoom; // controles para hacer zoom
	// contiene los botones de layerControlPanel, para poder ocultarlos o
	// mostrarlos
	protected JPanel zoom;
	protected List<JToggleButton> layerControls;
	protected ActionListener layerControlListener = new LayerControl();
	// lista de callbacks para movimiento y zoom (se necesitan para implementar
	// los listeners
	protected List<MapMoveListener.MapMovedCallback> movedCallbacks = new ArrayList<MapMovedCallback>();
	protected List<ZoomPerformed.ZoomCallback> zoomedCallbacks = new ArrayList<ZoomCallback>();
	protected JPopupMenu contextMenu;
	// metodos de utilidad de josm
	/**
	 * Use this to register shortcuts to
	 */
	public final JPanel contentPane = new JPanel(new BorderLayout());
	/**
	 * The MOTD Layer.
	 */
	// private GettingStarted gettingStarted = new GettingStarted();
	/**
	 * The main menu bar at top of screen.
	 */
	public final MainMenu menu;

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getMenu()
	 */
	@Override
	public MainMenu getMenu() {
		return menu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getContentPane()
	 */
	@Override
	public JPanel getContentPane() {
		return contentPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getPanel()
	 */
	@Override
	public JPanel getPanel() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#setPanel(javax.swing.JPanel)
	 */
	@Override
	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public JPanel panel = new JPanel(new BorderLayout());
	/**
	 * The toolbar preference control to register new actions.
	 */
	public ToolbarPreferences toolbar;

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

	public CustomMapView() {
		super();
		menu = new MainMenu();
		contentPane.add(panel, BorderLayout.CENTER);

		// iniciar los controles mostrar/ocultar capas
		layerControls = new LinkedList<JToggleButton>();

		JToggleButton botonMostrarOcultarBotones = new JToggleButton(
				getI18n().getString("map.layers.hideButtons"),
				LogicConstants.getIcon("capas_button_mostrar"), false);
		botonMostrarOcultarBotones.setSelected(true);
		botonMostrarOcultarBotones.setActionCommand("#hide");
		// b.setVerticalTextPosition(SwingConstants.BOTTOM);
		// b.setHorizontalTextPosition(SwingConstants.CENTER);
		botonMostrarOcultarBotones.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton b = (JToggleButton) e.getSource();
				if (e.getActionCommand().equals("#hide")) {
					layerControlPanel.removeAll();
					layerControlPanel.add(Box.createHorizontalStrut(10));
					layerControlPanel.add(b);
					b.setActionCommand("#show");
					b.setText(getI18n().getString("map.layers.showButtons"));
				} else {
					layerControlPanel.removeAll();
					layerControlPanel.add(Box.createHorizontalStrut(10));
					for (JToggleButton bt : layerControls) {
						layerControlPanel.add(bt);
						layerControlPanel.add(Box.createHorizontalGlue());
					}
					b.setActionCommand("#hide");
					b.setText(getI18n().getString("map.layers.hideButtons"));
				}
				layerControlPanel.updateUI();
			}
		});
		layerControls.add(botonMostrarOcultarBotones);

		final JToggleButton botonTodoasLasCapas = new JToggleButton(
				getI18n().getString("map.layers.allLayers"),
				LogicConstants.getIcon("capas_button_mostrar"), false);
		layerDialog = new LayerSelectionDialog(this);
		layerDialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				botonTodoasLasCapas.setSelected(false);
			}
		});
		botonTodoasLasCapas.setSelected(false);
		botonTodoasLasCapas.setActionCommand("#all");
		// all.setVerticalTextPosition(SwingConstants.BOTTOM);
		// all.setHorizontalTextPosition(SwingConstants.CENTER);
		botonTodoasLasCapas.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layerDialog.setLocationRelativeTo((Component) e.getSource());
				layerDialog.setVisible(!layerDialog.isShowing());
			}
		});
		layerControls.add(botonTodoasLasCapas);

		layerControlPanel = new JPanel();
		layerControlPanel.setLayout(new BoxLayout(layerControlPanel,
				BoxLayout.X_AXIS));

		Main.main.menu = this.menu;
		toolbar = new ToolbarPreferences();
		toolbar.refreshToolbarControl();
		// toolbar.control.updateUI();
		// contentPane.add(toolbar.control, BorderLayout.NORTH);

		contentPane.updateUI();
		panel.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#addLayer(org.openstreetmap.josm.gui.
	 * layer.Layer, boolean)
	 */
	@Override
	public void addLayer(Layer layer, boolean showOnButtonList) {
		addLayer(layer, showOnButtonList, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#addLayer(org.openstreetmap.josm.gui.
	 * layer.Layer, boolean, int)
	 */
	@Override
	public void addLayer(Layer layer, boolean showOnButtonList, int pos) {
		if (layer instanceof MapViewerLayer) {
			minZoom = Math.max(((MapViewerLayer) layer).getMinZoomLevel(),
					getMinZoom());
			maxZoom = Math.min(((MapViewerLayer) layer).getMaxZoomLevel(),
					getMaxZoom());
			if (zoomFactor > maxZoom || zoomFactor < minZoom) {
				zoomFactor = (maxZoom + minZoom) / 2;
			}
			zoomTo(center, zoom2Scale(zoomFactor));
		}
		if (showOnButtonList) {
			JToggleButton b = new JToggleButton(layer.name,
					LogicConstants.getIcon("capas_button_"
							+ layer.name.toLowerCase()), layer.visible);
			// b.setVerticalTextPosition(SwingConstants.BOTTOM);
			// b.setHorizontalTextPosition(SwingConstants.CENTER);
			b.setActionCommand(layer.name);
			b.addActionListener(layerControlListener);
			layerControls.add(b);
			layerControlPanel.removeAll();
			layerControlPanel.add(Box.createHorizontalStrut(10));
			for (JToggleButton bt : layerControls) {
				layerControlPanel.add(bt);
				layerControlPanel.add(Box.createHorizontalGlue());
			}
			layerControlPanel.updateUI();
		}

		super.addLayer(layer, pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#addLayer(org.openstreetmap.josm.gui.
	 * layer.Layer, boolean, java.lang.String)
	 */
	@Override
	public void addLayer(Layer layer, boolean showOnButtonList, String icon) {
		if (layer instanceof MapViewerLayer) {
			minZoom = Math.max(((MapViewerLayer) layer).getMinZoomLevel(),
					getMinZoom());
			maxZoom = Math.min(((MapViewerLayer) layer).getMaxZoomLevel(),
					getMaxZoom());
			if (zoomFactor > maxZoom || zoomFactor < minZoom) {
				zoomFactor = (maxZoom + minZoom) / 2;
			}
			zoomTo(center, zoom2Scale(zoomFactor));
		}
		if (showOnButtonList) {
			JToggleButton b = new JToggleButton(layer.name,
					LogicConstants.getIcon(icon), layer.visible);
			// b.setVerticalTextPosition(SwingConstants.BOTTOM);
			// b.setHorizontalTextPosition(SwingConstants.CENTER);
			b.setActionCommand(layer.name);
			b.addActionListener(layerControlListener);
			layerControls.add(b);
			layerControlPanel.removeAll();
			layerControlPanel.add(Box.createHorizontalStrut(10));
			for (JToggleButton bt : layerControls) {
				layerControlPanel.add(bt);
				layerControlPanel.add(Box.createHorizontalGlue());
			}
			layerControlPanel.updateUI();
		}

		super.addLayer(layer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#addLayer(org.openstreetmap.josm.gui.
	 * layer.Layer)
	 */
	@Override
	public void addLayer(Layer layer) {
		addLayer(layer, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#setActiveLayer(org.openstreetmap.josm
	 * .gui.layer.Layer)
	 */
	@Override
	public void setActiveLayer(Layer layer) { // modificado para que no pete
		if (!getAllLayers().contains(layer)) {
			throw new IllegalArgumentException("Layer must be in layerlist");
		}
		if (layer instanceof OsmDataLayer) {
			editLayer = (OsmDataLayer) layer;
			Main.ds = editLayer.data;
		} else {
			Main.ds.setSelected();
		}
		DataSet.fireSelectionChanged(Main.ds.getSelected());
		Layer old = getActiveLayer();
		setActiveLayerReflect(layer);

		if (old != layer) {
			for (Layer.LayerChangeListener l : Layer.listeners) {
				l.activeLayerChange(old, layer);
			}
		}

		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#zoomToEditLayerBoundingBox()
	 */
	@Override
	public boolean zoomToEditLayerBoundingBox() {
		return false;
	}

	/**
	 * Tries to bypass private field declaration for activeLayer field
	 * 
	 * @param layer
	 */
	private void setActiveLayerReflect(Layer layer) {
		activeLayer = layer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#zoomTo(org.openstreetmap.josm.data.coor
	 * .EastNorth, double)
	 */
	@Override
	public void zoomTo(EastNorth newCenter, double scale) {
		int factor = zoomFactor;
		if (this.scale > scale) {
			if (zoomFactor == getMaxZoom()) {
				return;
			}
			factor = Math.min(zoomFactor + 1, getMaxZoom());
		} else if (this.scale < scale) {
			if (zoomFactor == getMinZoom()) {
				return;
			}
			factor = Math.max(zoomFactor - 1, getMinZoom());
		}
		zoomToFactor(newCenter, factor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#setAutoZoom(boolean)
	 */
	@Override
	public void setAutoZoom(boolean autoZoom) {
		this.autoZoom = autoZoom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#setAutoTurn(boolean)
	 */
	@Override
	public void setAutoTurn(boolean autoTurn) {
		this.autoTurn = autoTurn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#setSmoothTurn(boolean)
	 */
	@Override
	public void setSmoothTurn(boolean smoothTurn) {
		this.smoothTurn = smoothTurn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#setContextMenu(javax.swing.JPopupMenu)
	 */
	@Override
	public void setContextMenu(JPopupMenu contextMenu) {
		this.contextMenu = contextMenu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#showMenu(java.awt.Component, int,
	 * int)
	 */
	@Override
	public void showMenu(Component parent, int x, int y) {
		if (this.contextMenu != null) { // si hay contextmenu
			this.contextMenu.show(parent, x, y);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#zoomPerformed()
	 */
	@Override
	public void zoomPerformed() {
		JobDispatcher.getInstance().cancelOutstandingJobs();
		updateMarkers();
		for (ZoomCallback zc : zoomedCallbacks) {
			zc.action();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#addCallback(es.emergya.ui.plugins.
	 * ZoomPerformed.ZoomCallback)
	 */
	@Override
	public void addCallback(ZoomCallback zcb) {
		if (!zoomedCallbacks.contains(zcb)) {
			zoomedCallbacks.add(zcb);
		}
	}

	// @Override
	// public void clearCallbacks() { zoomedCallbacks.clear(); }
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#removeCallback(es.emergya.ui.plugins
	 * .ZoomPerformed.ZoomCallback)
	 */
	@Override
	public boolean removeCallback(ZoomCallback zcb) {
		return zoomedCallbacks.remove(zcb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#zoomToFactor(org.openstreetmap.josm.
	 * data.coor.EastNorth, int)
	 */
	@Override
	public void zoomToFactor(EastNorth newCenter, int zoomFactor) {
		if (zoomFactor < getMinZoom() || zoomFactor > getMaxZoom()) {
			return;
		}
		this.zoomFactor = zoomFactor;
		super.zoomTo(newCenter, zoom2Scale(zoomFactor));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#zoomToFactor(org.openstreetmap.josm.
	 * data.coor.EastNorth, int, double)
	 */
	@Override
	public void zoomToFactor(EastNorth newCenter, int zoomFactor, double angle) {
		this.angle = normalizeAngle(angle);
		this.sin = Math.sin(angle);
		this.cos = Math.cos(angle);
		this.zoomToFactor(newCenter, zoomFactor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#zoomTo(org.openstreetmap.josm.data.coor
	 * .EastNorth, double, double)
	 */
	@Override
	public void zoomTo(EastNorth newCenter, double scale, double angle) {
		this.angle = normalizeAngle(angle);
		this.sin = Math.sin(angle);
		this.cos = Math.cos(angle);
		this.zoomTo(newCenter, scale);
	}

	/**
	 * @param angle
	 * @return el equivalente a angle entre 0 y 2*PI
	 */
	private double normalizeAngle(double angle) {
		while (angle > PI2) {
			angle -= PI2;
		}
		while (angle < -PI2) {
			angle += PI2;
		}
		return angle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Main.mapView = this;
		if (follow != null) {
			follow();
		}
		try {
			super.paint(g); // Esto ya llama a paintComponents
		} catch (ConcurrentModificationException ex) {
			log.warn("Redibujando, datos aun no cargados...");
			return;
		}

		// ============= DEBUG DEBAJO DE ESTA LINEA ================

		// g.drawString(
		// "zoom: " + zoomFactor + ", angle:" + Math.toDegrees(angle),
		// 300, 15);
		// g.drawString((Runtime.getRuntime().freeMemory() / (1024 * 1024))
		// + "M / " + (Runtime.getRuntime().totalMemory() / (1024 * 1024))
		// + "M", 20, getHeight() - 5);

		// Rectangle r = getBoundingBox();
		// // g.drawRect(r.x + 200, r.y + 200, r.width - 400, r.height - 400);
		// LatLon tl, br;
		// tl = getLatLon(r.x, r.y);
		// br = getLatLon(r.x + r.width, r.y + r.height);
		// UTM u = new UTM();
		// // EastNorth etl = u.latlon2eastNorth(tl);
		// // EastNorth ebr = u.latlon2eastNorth(br);
		// EastNorth etl = Main.proj.latlon2eastNorth(tl);
		// EastNorth ebr = Main.proj.latlon2eastNorth(br);
		// Point pmi, pma;
		// pmi = getPoint(etl);
		// pma = getPoint(ebr);
		// Rectangle draw = new Rectangle(pmi.x, pma.y, pma.x - pmi.x, pma.y -
		// pmi.y);
		// g.setColor(Color.GREEN);
		// g.drawRect(draw.x + 201, draw.y + 201, draw.width - 402, draw.height
		// - 402);
		// g.drawString("+  " + draw, getWidth() / 2, 300);

		// g.setColor(Color.RED);
		// g.drawRect(200, 200, getWidth() - 400, getHeight() - 400);

		// g.setColor(Color.RED);
		// Point p = getPoint(Main.proj.latlon2eastNorth(getLatLon(getWidth() /
		// 2,
		// getHeight() / 2 - 200)));
		// g.fillOval(p.x - 10, p.y - 10, 20, 20);
		// g.setColor(Color.BLACK);
		// g.fillRect(getWidth() / 2 - 5, getHeight() / 2 - 5, 10, 10);

		// EastNorth mi = getEastNorth(0, getHeight() - 1);
		// EastNorth ma = getEastNorth(getWidth() - 1, 0);
		// Point pmi, pma;
		// pmi = getPoint(mi);
		// pma = getPoint(ma);
		// Rectangle r = new Rectangle(pmi.x, pma.y, pma.x - pmi.x, pmi.y -
		// pma.y);
		// g.drawString("+  " + r, getWidth() / 2, 300);
		// g.drawRect(r.x + 200, r.y + 200, r.width - 400, r.height - 400);
		// LatLon ll = getLatLon(getWidth()/2, getHeight()/2);
		// g.drawString("+  " + ll.lat() + " x " + ll.lon(), getWidth()/2,
		// getHeight()/2);
		// drawCompass(g);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#updateMousePosition()
	 */
	@Override
	public void updateMousePosition() {
	}

	private Thread turner; // se usa para el giro suave

	/**
	 * Si algun {@link Marker} esta en {@link CustomMapView#follow} lo sigue,
	 * gira y hace zoom si corresponde.
	 */
	protected void follow() {
		if (lastFollowPos == null) {
			lastFollowPos = follow.eastNorth;
		} else {
			// distancias
			double dy = follow.eastNorth.getY() - lastFollowPos.getY();
			double dx = follow.eastNorth.getX() - lastFollowPos.getX();
			// si se ha movido lo bastante...
			if (Math.abs(dx) > FOLLOW_THRESSHOLD
					|| Math.abs(dy) > FOLLOW_THRESSHOLD) {
				if (autoTurn) {
					double na = Math.atan2(dy, dx);
					na -= lastFollowAngle;
					// Normalize the angle
					if (na < -Math.PI) {
						na += PI2;
					}
					if (na > Math.PI) {
						na -= PI2;
					}

					if (smoothTurn) {
						final double target = na; // para usarlo enel thread
						if (turner != null && turner.isAlive()) // paramos el
						// thread si
						// vamos a girar
						// otra vez
						{
							turner.interrupt();
						}
						turner = new Thread(new Runnable() {

							public void run() {
								double na = target;
								while (Math.abs(na) > 0) { // vamos girando 0.5
									// de lo que
									// queremos
									na /= 2;
									lastFollowAngle += na;
									zoomToFactor(center, zoomFactor, PI_MEDIO
											- lastFollowAngle);
									try {
										Thread.sleep(60);
									} catch (InterruptedException e) {
									}
								}
							}
						});
						turner.start();
					} else {
						lastFollowAngle += na; // giro brusco
					}

					// Normalize the angle
					if (lastFollowAngle < 0) {
						lastFollowAngle += PI2;
					}
					if (lastFollowAngle > PI2) {
						lastFollowAngle -= PI2;
					}
				} else {
					lastFollowAngle = -getAngle() + PI_MEDIO; // deshacemos el
					// giro si no se
					// gira
				}
				if (autoZoom) {
					// Adjusts the current zoom factor to match an appropiate
					// zoom
					// level for the speed of the followed object
					double dist = (lastDistance + Main.proj.eastNorth2latlon(
							follow.eastNorth).greatCircleDistance(
							Main.proj.eastNorth2latlon(lastFollowPos))) / 2;
					LatLon ll1 = getLatLon(0, 0);
					LatLon ll2 = getLatLon((int) (40 / fps), 0);
					double sampledist = ll1.greatCircleDistance(ll2); // get a
					// sample distance of 20px on the screen
					if (dist > sampledist * 1.5) { // if we move more than +-50%
						// of 20px -> zoom out
						zoomFactor = Math.max(zoomFactor - 1, getMinZoom());
						;
					} else if (dist < sampledist * 0.5) { // if we move less
						// than +-50% of
						// 20px -> zoom in
						zoomFactor = Math.min(zoomFactor + 1, getMaxZoom());
						;
					}
					lastDistance = dist;
				}

				zoomToFactor(follow.eastNorth, zoomFactor, PI_MEDIO
						- lastFollowAngle);

				lastFollowPos = new EastNorth(follow.eastNorth.east(),
						follow.eastNorth.north());

				// zoomTo(follow.eastNorth, getScale(), PI_MEDIO -
				// lastFollowAngle);
			}
		}
	}

	/**
	 * Dibuja una brujula apuntando al norte
	 * 
	 * @param g
	 */
	@SuppressWarnings("unused")
	private void drawCompass(Graphics g) {
		int x = 50, y = getHeight() - 50;
		// ((Graphics2D)g).rotate(Math.PI + lastFollowAngle, x, y);
		((Graphics2D) g).rotate(-PI_MEDIO - getAngle(), x, y);
		g.setColor(Color.LIGHT_GRAY);
		g.fillOval(0, getHeight() - 100, 100, 100);
		g.setColor(Color.BLACK);
		g.fillRect(x - 3, y - 3, 50, 6);
		g.setColor(Color.RED);
		g.fillRect(x + 42, y - 3, 6, 6);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#zoom()
	 */
	@Override
	public int zoom() {
		return zoomFactor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#zoom2Scale(int)
	 */
	@Override
	public double zoom2Scale(int zoom) {
		return Math.max(world.east() / OsmMercator.falseEasting(zoom),
				world.north() / OsmMercator.falseNorthing(zoom));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getMinZoom()
	 */
	@Override
	public int getMinZoom() {
		return minZoom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getMaxZoom()
	 */
	@Override
	public int getMaxZoom() {
		return maxZoom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#setInitAdapter(es.emergya.ui.gis.
	 * CustomMapView.InitAdapter)
	 */
	public void setInitAdapter(InitAdapter initAdapter) {
		for (ComponentListener l : this.getComponentListeners()) {
			this.removeComponentListener(l);
		}
		this.addComponentListener(initAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#setDefaultInitAdapter()
	 */
	@Override
	public void setDefaultInitAdapter() {
		setInitAdapter(new InitAdapter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#rotate(double)
	 */
	@Override
	public void rotate(double r) {
		angle += r;
		if (r > Math.PI * 2) {
			angle -= Math.PI * 2;
		}
		if (r < -Math.PI * 2) {
			angle += Math.PI * 2;
		}
		sin = Math.sin(getAngle());
		cos = Math.cos(getAngle());
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getAngle()
	 */
	@Override
	public double getAngle() {
		return angle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#setFollow(org.openstreetmap.josm.gui
	 * .layer.markerlayer.Marker)
	 */
	@Override
	public void setFollow(Marker m) {
		this.follow = m;
		this.lastFollowPos = m.eastNorth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.ICustomMapView#getPoint(org.openstreetmap.josm.data
	 * .coor.EastNorth)
	 */
	@Override
	public Point getPoint(EastNorth p) {
		Point pt = super.getPoint(p);
		return translateR(pt.x, pt.y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getEastNorth(int, int)
	 */
	@Override
	public EastNorth getEastNorth(int x, int y) {
		Point p = translate(x, y);
		return super.getEastNorth(p.x, p.y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getLatLon(int, int)
	 */
	@Override
	public LatLon getLatLon(int x, int y) {
		Point p = translate(x, y);
		return super.getLatLon(p.x, p.y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#getBoundingBox()
	 */
	@Override
	public Rectangle getBoundingBox() {
		int minx = 0;
		int miny = 0;
		int maxx = getWidth();
		int maxy = getHeight();
		Point p0 = translateR(minx, miny);// tl
		Point p1 = translateR(minx, maxy);// bl
		Point p2 = translateR(maxx, maxy);// br
		Point p3 = translateR(maxx, miny);// tr

		int[] x = { p0.x, p1.x, p2.x, p3.x };
		Arrays.sort(x);

		int[] y = { p0.y, p1.y, p2.y, p3.y };
		Arrays.sort(y);

		return new Rectangle(x[0], y[0], x[x.length - 1] - x[0],
				y[y.length - 1] - y[0]);
	}

	/**
	 * Returns the position of the point rotated the current rotation from the
	 * center
	 */
	private Point translate(Point p) {
		return translate(p.x, p.y);
	}

	/**
	 * Returns the position of the point rotated the oposite to the current
	 * rotation from the center
	 */
	private Point translateR(int x, int y) {
		double x0 = getWidth() / 2;
		double y0 = getHeight() / 2;
		double nx = x0 + cos * (x - x0) + sin * (y - y0);
		double ny = y0 - sin * (x - x0) + cos * (y - y0);
		return new Point((int) nx, (int) ny);
	}

	/**
	 * Returns the position of the point rotated the current rotation from the
	 * center
	 */
	private Point translate(int x, int y) {
		int x0 = getWidth() >> 1;
		int y0 = getHeight() >> 1;
		double nx = x0 + cos * (x - x0) - sin * (y - y0);
		double ny = y0 + sin * (x - x0) + cos * (y - y0);
		return new Point((int) nx, (int) ny);
	}

	public class InitAdapter extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {
			removeComponentListener(this);

			setMouseListeners();

			initControlPanels();

			setResizeListeners();

			// startPositionUpdater();

			zoomPerformed();
		}

		protected void setMouseListeners() {
			MapMover m = new MapMover(CustomMapView.this,
					CustomMapView.this.contentPane);
			m.setPanButton(MouseEvent.BUTTON1);
		}

		protected void startPositionUpdater() {
			positionUpdater = new Updater();
			new Thread(positionUpdater).start();
		}

		protected void setResizeListeners() {
			CustomMapView.this.addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					super.componentResized(e);
					int h = e.getComponent().getHeight();
					int w = e.getComponent().getWidth();
					layerControlPanel.setBounds(0, h - 100, w, 100);
					// controlPanel.setBounds(0, 0, w,
					// controlPanel.getHeight());
					zoom.setBounds(zoom.getX(), zoom.getY(), zoom.getWidth(),
							zoom.getHeight());
					layerControlPanel.updateUI();
					zoom.updateUI();
				}
			});
		}

		protected void initControlPanels() {
			// zoom = new ZoomControlPanel(CustomMapView.this);
			// add(zoom);
			// zoom.setBounds(0, 0, getWidth(), 100);
			zoom = new JPanel(new BorderLayout());
			zoom.setOpaque(false);
			zoom.setLayout(new BoxLayout(zoom, BoxLayout.Y_AXIS));
			JButton in = new JButton(
					LogicConstants.getIcon("map_button_zoommas"));
			in.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					zoomToFactor(getCenter(), zoom() + 1);
				}
			});
			zoom.add(in, BorderLayout.NORTH);
			JPanel foo = new JPanel();
			foo.setOpaque(false);
			foo.add(new ZoomMapSlider(CustomMapView.this));
			zoom.add(foo, BorderLayout.CENTER);
			JButton out = new JButton(
					LogicConstants.getIcon("map_button_zoommenos"));
			out.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					zoomToFactor(getCenter(), zoom() - 1);
				}
			});
			zoom.add(out, BorderLayout.SOUTH);
			add(zoom);
			zoom.setBounds(0, 10, 70, 270);

			// controlPanel = new ControlPanel(CustomMapView.this);
			//
			// add(controlPanel);
			// controlPanel.setBounds(0, 0, getWidth(), 40);

			layerDialog.init();

			layerControlPanel.setOpaque(false);
			layerControlPanel.removeAll();
			layerControlPanel.add(Box.createHorizontalStrut(10));
			for (JToggleButton bt : layerControls) {
				layerControlPanel.add(bt);
				layerControlPanel.add(Box.createHorizontalGlue());
			}
			add(layerControlPanel);
			layerControlPanel.setBounds(0, getHeight() - 100, getWidth(), 100);
		}
	}

	/**
	 * Cambia la visibilidad de los botones de
	 * {@link CustomMapView#layerControlPanel}
	 * 
	 * @author fario
	 * 
	 */
	private class LayerControl implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				boolean status = false;
				final String nombreCapa = e.getActionCommand();
				for (Layer l : getAllLayers()) {
					if (l.name.indexOf(nombreCapa) == 0) {
						l.visible = !l.visible;
						status = l.visible;
						updateMarkers();
					}
				}

				// vehiculos_visibles
				// personas_visibles
				// incidencias_visibles
				String capa_incidences = getI18n()
						.getString("Incidences.incidences");
				String capa_vehiculos = getI18n()
						.getString("Resources.resources.vehicles");
				String capa_people = getI18n()
						.getString("Resources.resources.people");
				Usuario u = UsuarioConsultas.find(Authentication.getUsuario()
						.getNombreUsuario());

				if (u != null) {
					if (nombreCapa.equalsIgnoreCase(capa_incidences))
						u.setIncidenciasVisibles(status);
					else if (nombreCapa.equalsIgnoreCase(capa_vehiculos))
						u.setVehiculosVisibles(status);
					else if (nombreCapa.equalsIgnoreCase(capa_people))
						u.setPersonasVisibles(status);

					UsuarioAdmin.saveOrUpdate(u);
				}

			} catch (Throwable t) {
				log.error("Error actualizando el estado de la capa", t);
			}

		}
	};

	/**
	 * Actualiza la capa con marcadores que simulan vehiculos que se mueven
	 * 
	 * @author fario
	 * 
	 */
	@Deprecated
	private class Updater implements Runnable {

		private boolean running = true;

		@Override
		public void run() {
			running = true;
			while (running) {
				// if(System.currentTimeMillis() - time > 1000/fps){
				// buscar todos los markers que son updatable en todas las
				// marker layers
				for (Layer l : getAllLayers()) {
					if (l instanceof MarkerLayer) {
						for (Marker m : ((MarkerLayer) l).data) {
							if (m instanceof Updatable) {
								((Updatable) m).update();
							}
						}
					}
				}

				repaint();
				// } else {
				try {
					Thread.sleep((long) (1000 / fps));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// }
			}
		}

		public void stop() {
			this.running = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#movementEnded()
	 */
	@Override
	public void movementEnded() {
		updateMarkers();
		for (MapMovedCallback m : this.movedCallbacks) {
			m.action();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#movementStarted()
	 */
	@Override
	public void movementStarted() {
	}

	;

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#addCallback(es.emergya.ui.gis.
	 * MapMoveListener.MapMovedCallback)
	 */
	@Override
	public void addCallback(MapMovedCallback mmc) {
		if (!this.movedCallbacks.contains(mmc)) {
			this.movedCallbacks.add(mmc);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#clearCallbacks()
	 */
	@Override
	public void clearCallbacks() {
		this.movedCallbacks.clear();
		this.zoomedCallbacks.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#removeCallback(es.emergya.ui.gis.
	 * MapMoveListener.MapMovedCallback)
	 */
	@Override
	public boolean removeCallback(MapMovedCallback mmc) {
		return this.movedCallbacks.remove(mmc);
	}

	// Separate threads to reload the markers
	private SwingWorker<Object, Object> mapUpdater;

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.ICustomMapView#updateMarkers()
	 */
	@Override
	public void updateMarkers() {
		Rectangle r = getBoundingBox();
		final LatLon tl, br;
		// tomamos los pois en un area de 3 pantallas cuadradas, de manera que
		// se pueda arrastrar sin notar que no estan cargados los poi de mas
		// allá, ya que solo se deberian cargar cuando deja de moverse.
		tl = getLatLon(r.x - r.width, r.y - r.height);
		br = getLatLon(r.x + 2 * r.width, r.y + 2 * r.height);

		if (mapUpdater == null || mapUpdater.isDone()) {
			mapUpdater = new SwingWorker<Object, Object>() {

				@Override
				protected Object doInBackground() throws Exception {
					try {
						updateIncidences(tl, br);
						updateResources(tl, br);
					} catch (Throwable t) {
						log.error("Error al actualizar marcadores", t);
					}
					return null;
				}

				@Override
				protected void done() {
					repaint();
				}
			};

			mapUpdater.execute();

		}
	}

	private void updateResources(final LatLon topleft, final LatLon bottomright) {
		MarkerLayer people = null, vehicles = null;
		// tomar las capas vehiculo y persona
		for (Layer l : getAllLayers()) {
			if (l.name.equalsIgnoreCase(getI18n()
					.getString("Resources.resources.people"))) {
				people = (MarkerLayer) l;
			}
			if (l.name.equalsIgnoreCase(getI18n()
					.getString("Resources.resources.vehicles"))) {
				vehicles = (MarkerLayer) l;
			}
			if (people != null && vehicles != null) {
				break;
			}
		}

		if (getParent() instanceof IMapViewer) { // actualizar los recursos
			// disponibles
			((MapViewer) getParent()).updateControls();
		}

		List<Recurso> allres = RecursoConsultas.getAll(Authentication
				.getUsuario());

		boolean peopleShowing = people != null && people.visible;
		boolean vehiclesShowing = vehicles != null && vehicles.visible;

		Collection<Marker> peop = new LinkedList<Marker>();
		Collection<Marker> veh = new LinkedList<Marker>();
		for (Recurso r : allres) {
			HistoricoGPS h = r.getHistoricoGps();
			if (h == null) {
				continue;
			}
			WayPoint w = new WayPoint(new LatLon(h.getPosY(), h.getPosX()));
			String name = r.getNombre();

			if (r.getPatrullas() != null)
				name += " (" + r.getPatrullas().getNombre() + ")";

			w.attr.put("name", name);
			w.attr.put("symbol", LogicConstants.get("DIRECTORIO_ICONOS_FLOTAS")
					+ "/" + r.getFlotas().getJuegoIconos());
			// w.attr.put("color", LogicConstants.get("COLOR_ESTADO_REC_"
			// + r.getEstadoEurocop().getId(), "#000000"));
			if (peopleShowing && r.getTipo().equalsIgnoreCase(Recurso.PERSONA)) {
				CustomMarker<String, Recurso> marker = new CustomMarker<String, Recurso>(
						w, people, r.getIdentificador(), Type.RESOURCE);
				marker.setObject(r);
				peop.add(marker);
			} else if (vehiclesShowing
					&& r.getTipo().equalsIgnoreCase(Recurso.VEHICULO)) {

				CustomMarker<String, Recurso> marker = new CustomMarker<String, Recurso>(
						w, vehicles, r.getIdentificador(), Type.RESOURCE);
				marker.setObject(r);
				veh.add(marker);
			}
		}
		if (people != null) {
			people.data = peop;
		}
		if (vehicles != null) {
			vehicles.data = veh;
		}
	}

	private void updateIncidences(final LatLon topleft, final LatLon bottomright) {
		MarkerLayer layer = null;
		// tomar las capas vehiculo y persona
		for (Layer l : getAllLayers()) {
			if (l.name.equals(getI18n().getString("Incidences.incidences"))) {
				layer = (MarkerLayer) l;
				break;
			}
		}
		if (layer == null)
			return;

		List<Incidencia> allres = IncidenciaConsultas.getOpened();
		Collection<Marker> nuevoData = new LinkedList<Marker>();

		for (Incidencia i : allres) {
			try {
				if (i.getGeometria() != null) {
					final com.vividsolutions.jts.geom.Point centroid = i
							.getGeometria().getCentroid();
					if (centroid != null) {
						LatLon latlon = new LatLon(centroid.getCoordinate().y,
								centroid.getCoordinate().x);
						WayPoint w = new WayPoint(latlon);
						w.attr.put("name",
								i.getTitulo() + " (" + i.getPrioridad() + ")");
						w.attr.put(
								"symbol",
								LogicConstants.get(
										"DIRECTORIO_ICONOS_INCIDENCIAS",
										"incidencia/")
										+ "incidencia_"
										+ i.getEstado().toString()
										+ "_"
										+ i.getCategoria().toString()
										+ "_"
										+ i.getPrioridad().toString());
						w.attr.put(
								"color",
								LogicConstants.get("COLOR_ESTADO_INC_"
										+ i.getEstado().getId(), "#000000"));

						CustomMarker<Long, Incidencia> marker = new CustomMarker<Long, Incidencia>(
								w, layer, i.getId(), Type.INCIDENCE);
						marker.setObject(i);
						nuevoData.add(marker);
					}
				}
			} catch (Throwable t) {
				log.error("Error al intentar pintar una incidencia", t);
			}
		}

		layer.data = nuevoData;
	}
}
