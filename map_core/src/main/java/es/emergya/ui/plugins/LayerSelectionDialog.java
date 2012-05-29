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
package es.emergya.ui.plugins;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoClassLoader;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.josm.gui.layer.Layer;

import es.emergya.actions.Authentication;
import es.emergya.actions.UsuarioAdmin;
import es.emergya.bbdd.bean.CapaInformacion;
import es.emergya.bbdd.bean.CapaInformacionUsuario;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.CapaConsultas;
import es.emergya.consultas.UsuarioConsultas;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.gis.CustomMapView;
import es.emergya.ui.gis.WmsTileSource;
import es.emergya.ui.gis.layers.MapViewerLayer;

public class LayerSelectionDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 7032893596071705890L;
	static final Log log = LogFactory.getLog(LayerSelectionDialog.class);
	private List<LayerElement> layers;
	private CustomMapView mv;
	private JPanel list;
	JLabel actualizando;
	JDialog self;
	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(LayerSelectionDialog.class);

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
	public LayerSelectionDialog(CustomMapView gmv) {
		super();
		self = this;
		this.setTitle("Otras Capas");
		actualizando = new JLabel(LogicConstants.getIcon("anim_actualizando"));
		this.setAlwaysOnTop(true);
		this.mv = gmv;
		this.layers = new ArrayList<LayerElement>();
		try {
			setIconImage(((BasicWindow) GoClassLoader.getGoClassLoader().load(
					BasicWindow.class)).getIconImage());
		} catch (Throwable e1) {
			LOG.error("Couldn't find icon image", e1);
		}

		JPanel base = new JPanel();
		base.setPreferredSize(new Dimension(240, 150));
		base.setBackground(Color.WHITE);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
		base.add(new JLabel(i18n
				.getString("map.layers.avaliable")));
		list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		list.add(actualizando);
		list.setBackground(Color.WHITE);
		// list.setPreferredSize(new Dimension(100, 100));
		final JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBackground(Color.WHITE);

		base.add(scrollPane);

		mv.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
			}
		});

		add(base);
		pack();
	}

	public void init() {
		initOptions(list);
		addLayers();
	}

	private void initOptions(final JPanel list) {
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				publish(new Object[0]);
				for (CapaInformacion ci : CapaConsultas.getAllOrderedByOrden()) {
					if (ci.getOpcional() && ci.getHabilitada()) {
						layers.add(new LayerElement(ci.getNombre(),
								ci.getUrl(), wasVisible(ci)));
					}
				}
				return null;
			}

			@Override
			protected void process(List<Object> chunks) {
				actualizando
						.setIcon(es.emergya.cliente.constants.LogicConstants
								.getIcon("anim_actualizando"));
			}

			@Override
			protected void done() {
				super.done();
				actualizando.setIcon(null);
				list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
				for (LayerElement le : layers) {
					JCheckBox cb = new JCheckBox(le.name, le.active);
					cb.setBackground(Color.WHITE);
					cb.addActionListener(LayerSelectionDialog.this);
					list.add(cb);
					list.revalidate();
				}

				// self.pack();
			}
		};
		sw.execute();
	}

	private void addLayers() {
		for (LayerElement le : layers) {
			if (le.active) {
				enableLayer(le);
				log.debug("cargando capa: " + le.name);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String name = e.getActionCommand();
		for (LayerElement le : layers) {
			if (le.name.equals(name)) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					enableLayer(le);
				} else {
					if (le.layer == null) {
						for (Layer l : mv.getAllLayers()) {
							if (l.name.equals(le.name)) {
								le.layer = l;
								break;
							}
						}
					}

					if (le.layer != null) {
						le.layer.visible = false;
					}
					le.active = false;
				}
				save(name, le.active);
				mv.repaint();
			}
		}
	}

	private void enableLayer(LayerElement le) {
		if (le.layer == null) {

			for (Layer l : mv.getAllLayers()) {
				if (l.name.equals(le.name)) {
					le.layer = l;
					break;
				}
			}

			if (le.layer == null)
				le.layer = new MapViewerLayer(le.name,
						new WmsTileSource(le.url), new MemoryTileCache(), 6);
			// le.layer = new WMSLayer(le.name, le.url, null, mv);
			mv.addLayer(le.layer, false);
		}
		le.layer.visible = true;
		le.active = true;
	}

	private void save(String layerName, boolean visible) {
		CapaInformacionUsuario cu = null;

		for (CapaInformacionUsuario c : UsuarioConsultas
				.getCapas(Authentication.getUsuario())) {
			if (c.getCapaInformacion().getNombre().equals(layerName)) {
				cu = c;
			}
		}

		if (cu == null) {
			cu = new CapaInformacionUsuario(); // Creamos una nueva relacion
			CapaInformacion capa = CapaConsultas.getByNombre(layerName);
			cu.setVisibleHistorico(false); // la iniciamos todo a falso y con
			// sus valores
			cu.setVisibleGPS(false);
			cu.setCapaInformacion(capa);
			cu.setUsuario(Authentication.getUsuario());
		}

		// XXX Esto es malvado y petara tarde o temprano
		// if (mv.getParent() instanceof HistoryMapViewer) {
		cu.setVisibleHistorico(visible);
		// } else if (mv.getParent() instanceof FleetControlMapViewer) {
		// cu.setVisibleGPS(visible);
		// }
		log.debug("guardando estado de la capa " + cu.toString());

		UsuarioAdmin.updateCapasInformacion(cu);
	}

	private boolean wasVisible(CapaInformacion capa) {
		for (CapaInformacionUsuario c : UsuarioConsultas
				.getCapas(Authentication.getUsuario())) {
			if (c.getCapaInformacion().getNombre().equals(capa.getNombre())) {
				log.debug("Comprobando si la capa" + capa.getNombre()
						+ " estaba activa");
				// XXX Esto es malvado y petara tarde o temprano
				// if (mv.getParent() instanceof HistoryMapViewer) {
				return c.getVisibleHistorico();
				// } else if (mv.getParent() instanceof FleetControlMapViewer) {
				// return c.getVisibleGPS();
				// }
			}
		}
		return false;
	}

	private class LayerElement {

		boolean active;
		String name;
		String url;
		Layer layer;

		public LayerElement(String name, String url, boolean active) {
			this.active = active;
			this.name = name;
			this.url = url;

			for (Layer l : mv.getAllLayers()) {
				if (l.name.equals(name)) {
					layer = l;
					this.active = layer.visible;
					break;
				}
			}

		}
	}
}
