package es.emergya.ui.gis;

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

import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import es.emergya.bbdd.bean.CapaInformacion;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.CapaConsultas;
import es.emergya.ui.base.plugins.AbstractPluggable;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginListener;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.gis.CustomMapView.InitAdapter;
import es.emergya.ui.gis.layers.MapViewerLayer;

public abstract class MapViewer extends AbstractPluggable implements
		PluginListener, IMapViewer {
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(MapViewer.class);

	private static final long serialVersionUID = -2617268983963790121L;
	protected MapViewerLayer map;
	protected CustomMapView mapView;
	protected ControlPanel controlPanel;

	protected Boolean mapType = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.IMapViewer#getMapView()
	 */
	@Override
	public NavigatableComponent getMapView() {
		return (NavigatableComponent) mapView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.emergya.ui.gis.IMapViewer#setMapView(es.emergya.ui.gis.CustomMapView)
	 */
	@Override
	public void setMapView(ICustomMapView mapView) {
		this.mapView = (CustomMapView) mapView;
	}

	@Override
	public void refresh(PluginEvent event) {
		updateControls();
	}

	/**
	 * The most complete constructor. It configures everything that is
	 * configurable.
	 * 
	 * @param title
	 *            of the tab
	 * @param type
	 *            of plugin
	 * @param order
	 *            of tab
	 * @param icon
	 *            of tab
	 * @param layers
	 *            to be included on the mapView
	 * @param mouseWheelListener
	 *            (can be null)
	 * @param mouseListener
	 *            (can be null)
	 * @param mouseMotionListener
	 *            (can be null)
	 * @param initAdapter
	 *            (can be null)
	 */
	public MapViewer(String title, PluginType type, int order, String icon,
			List<MarkerLayer> layers, MouseWheelListener mouseWheelListener,
			MouseListener mouseListener,
			MouseMotionListener mouseMotionListener, InitAdapter initAdapter) {
		this(title, type, order, icon, layers);

		for (MouseWheelListener l : this.mapView.getMouseWheelListeners())
			this.mapView.removeMouseWheelListener(l);
		this.mapView.addMouseWheelListener(mouseWheelListener);

		for (MouseListener l : this.mapView.getMouseListeners())
			this.mapView.removeMouseListener(l);
		this.mapView.addMouseListener(mouseListener);

		for (MouseMotionListener l : this.mapView.getMouseMotionListeners())
			this.mapView.removeMouseMotionListener(l);
		this.mapView.addMouseMotionListener(mouseMotionListener);

		for (ComponentListener l : this.mapView.getComponentListeners())
			this.mapView.removeComponentListener(l);
		this.mapView.addComponentListener(initAdapter);
	}

	/**
	 * Preferred constructor.
	 * 
	 * @param title
	 *            of the tab
	 * @param type
	 *            of plugin
	 * @param order
	 *            of tab
	 * @param icon
	 *            of tab
	 * @param layers
	 *            to be included on the mapView
	 */
	public MapViewer(String title, PluginType type, int order, String icon,
			List<MarkerLayer> layers) {
		this.title = StringUtils.rightPad(title, 25);
		this.type = type;
		this.order = order;
		this.tip = title;

		if (icon != null)
			this.icon = LogicConstants.getIcon(icon);

		BorderLayout b = new BorderLayout();
		b.setVgap(10);
		b.setHgap(10);

		this.setLayout(b);

		// super.tab = content;
		// if (content != null)
		// this.add(content, BorderLayout.CENTER);
		((BorderLayout) getLayout()).setHgap(0);
		((BorderLayout) getLayout()).setVgap(0);
		Main.platform = Main.getPlatformHook();
		Main.platform.preStartupHook();
		Main.pref.init(true);
		Main.pref.putColor(marktr("scale"), Color.decode("#007f7f"));
		Main.pref.put("wmsplugin.alpha_channel", true);
		Main.preConstructorInit(new HashMap<String, Collection<String>>());
		new Main() {
		};

		for (MarkerLayer layer : layers) {
			this.mapView.addLayer(layer);
			layer.setMapView((MapView) this.mapView);
		}
	}

	/**
	 * Constructor that adds default sample layers
	 * 
	 * @param title
	 * @param type
	 * @param order
	 * @param icon
	 */
	public MapViewer(String title, PluginType type, int order, String icon) {
		this(title, type, order, icon, new ArrayList<MarkerLayer>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.emergya.ui.gis.IMapViewer#setup()
	 */
	@Override
	public void setup() {
		this.mapView = new CustomMapView();
		Main.mapView = this.mapView;
		this.removeAll();
		initializeLayers();
		this.mapView.setDefaultInitAdapter();

		add((Component) this.mapView, BorderLayout.CENTER);

		controlPanel = new ControlPanel((CustomMapView) this.mapView);

		add(controlPanel, BorderLayout.NORTH);

		this.mapView.setContextMenu(getContextMenu());
	}

	/**
	 * @return Menu contextual
	 */
	protected JPopupMenu getContextMenu() {
		return this.getComponentPopupMenu();
	}

	/**
	 * Adds layers
	 */
	protected void initializeLayers() {

		boolean someLayer = false;
		log.trace("initializeLayers()");
		for (CapaInformacion c : CapaConsultas.getAll(true, false)) {
			if (c.isHabilitada()) {
				log.info("Cargamos la capa " + c);
				this.mapView.addLayer(
						new MapViewerLayer(c.getNombre(), new WmsTileSource(c
								.getUrl()), new MemoryTileCache(), 6), false);
				someLayer = true;
			} else
				log.info("La capa " + c + " no esta habilitada");
		}

		if (!someLayer) {
			log.info("Cargamos la capa por defecto");
			this.mapView.addLayer(new MapViewerLayer("OSM",
					new OsmTileSource.Mapnik(), new MemoryTileCache(), 16),
					false);
		}

		// Capas opcionales:

		final List<CapaInformacion> todasOpcionales = CapaConsultas.getAll(
				false, null);
		for (CapaInformacion c : todasOpcionales) {
			if (c.isHabilitada()) {
				final MapViewerLayer layer = new MapViewerLayer(c.getNombre(),
						new WmsTileSource(c.getUrl()), new MemoryTileCache(), 6);
				layer.visible = false;
				log.debug("Añadimos la capa " + c);

				this.mapView.addLayer(layer, false);
			}
		}

		final List<CapaInformacion> misOpcionales = CapaConsultas.getAll(false,
				this.mapType);
		for (CapaInformacion c : misOpcionales) {
			for (Layer l : this.mapView.getAllLayers()) {

				if (c.getNombre().equalsIgnoreCase(l.name)) {
					l.visible = true;
					log.info("Cargamos la capa " + c);
				}
			}
		}

		mapView.setAutoTurn(false);
		mapView.setSmoothTurn(false);
		mapView.setAutoZoom(false);

		this.mapView.zoomToFactor(Main.proj
				.latlon2eastNorth(new LatLon(LogicConstants.LATITUD_INICIAL,
						LogicConstants.LONGITUD_INICIAL)),
				LogicConstants.NIVEL_ZOOM_INICIAL, 0);
	}

	/**
	 * Sample method to update the controls resources, leave unimplemented for
	 * default behavior
	 */
	protected void updateControls() {
	}

	public void updateGv() {
		CustomMapView gv = null;
		for (Component comp : this.getComponents()) {
			if (comp instanceof CustomMapView)
				gv = (CustomMapView) comp;
		}

		if (gv != null) {
			Main.main.menu = gv.getMenu();
			Main.contentPane = gv.getContentPane();

		}
	}
}
