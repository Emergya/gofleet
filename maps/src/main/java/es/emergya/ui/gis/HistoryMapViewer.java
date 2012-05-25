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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.Layer;

import edu.emory.mathcs.backport.java.util.Collections;
import es.emergya.actions.Authentication;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.i18n.Internacionalization;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.gis.popups.ConsultaHistoricos;
import es.emergya.ui.gis.popups.ListaCapas;
import es.emergya.ui.gis.popups.SaveGPXDialog;

/**
 * @author fario
 * 
 */
public class HistoryMapViewer extends MapViewer {

	private static final Log log = LogFactory.getLog(HistoryMapViewer.class);
	private static final long serialVersionUID = 7210058575472737291L;
	private JPanel controls;
	private static JToggleButton saveGpx;
	private static JToggleButton historico;
	private JToggleButton gpxToggleButton;
	private static JToggleButton resultadoHistorico;
	private MouseEvent eventOriginal;
	
	@GoWired
	private ListaCapas listaCapas;
	

	/**
	 * @param title
	 * @param type
	 * @param order
	 * @param icon
	 */
	public HistoryMapViewer() {
		super(Internacionalization.getString("Main.GPS"), PluginType
				.getType("GPS"), 2, "tab_icon_historico");

		// Colores de los marcadores
		Main.pref.putColor("text",
				Color.decode(LogicConstants.get("TEXT_COLOR", "0xFFFFFF")));
		Main.pref.putColor("node",
				Color.decode(LogicConstants.get("NODE_COLOR", "0xFFFFFF")));
		Main.pref.putInteger("mappaint.fontsize",
				Integer.parseInt(LogicConstants.get("TEXT_SIZE", "12")));

		resultadoHistorico = getResultadosButton();

		super.mapType = true;
	}

	public static JToggleButton getResultadoHistoricos() {
		return resultadoHistorico;
	}

	/**
	 * 
	 * @see MapViewer#reset()
	 */
	@Override
	public void setup() {
		log.trace("setup()");
		super.setup();

		gpxToggleButton = null;
		ListaCapas.quitListaCapas();
		this.mapView.layerControls.add(getGPXButton());
		this.mapView.layerControls.add(getResultadoHistoricos());

		this.mapView.setInitAdapter(this.mapView.new InitAdapter() {

			@Override
			protected void initControlPanels() {
				super.initControlPanels();
				mapView.add(createHistoryControls());
			}

			@Override
			protected void setResizeListeners() {
				super.setResizeListeners();
				mapView.addComponentListener(new ComponentAdapter() {

					@Override
					public void componentResized(ComponentEvent e) {
						controls.setBounds(50, 5, 400, 50);
					}
				});
			}
		});
	}

	private JToggleButton getResultadosButton() {
		final JToggleButton jToggleButton = new JToggleButton(
				getString("map.history.button.results"),
				LogicConstants.getIcon("capas_button_resultado"));
		jToggleButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (jToggleButton.isSelected()) {
					for (Layer l : ConsultaHistoricos.getCapas()) {
						l.visible = true;
					}
				} else {
					for (Layer l : ConsultaHistoricos.getCapas()) {
						l.visible = false;
					}
				}
				mapView.repaint();

			}
		});
		return jToggleButton;
	}

	public JToggleButton getGPXButton() {
		if (gpxToggleButton == null) {
			gpxToggleButton = new JToggleButton(
					getString("map.history.button.loadGpx"),
					LogicConstants.getIcon("capas_button_gpx"));
			gpxToggleButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (gpxToggleButton.isSelected()) {
						getListaCapas().showListaCapas(mapView,
								HistoryMapViewer.this);
					} else {
						getListaCapas().hideListaCapas();
					}

				}
			});
		}
		return gpxToggleButton;
	}

	private JToggleButton getConsultaHistoricos() {
		if (historico == null) {
			historico = new JToggleButton(
					getString("map.history.button.showSearchWindow"),
					LogicConstants.getIcon("historico_button_consultar"));
			historico.addActionListener(new HistoricoActionListener((this)));
		}
		return historico;
	}

	public static void refreshHistoryPanel() {
		historico.setSelected(false);
		enableSaveGpx(false);
	}

	public static void enableSaveGpx(final boolean enabled) {
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				return null;
			}

			@Override
			protected void done() {
				if (saveGpx != null) {
					saveGpx.setEnabled(enabled);
					saveGpx.updateUI();
				}
			}
		};
		sw.execute();
	}

	private JToggleButton getSaveGpx() {
		if (saveGpx == null) {
			saveGpx = new JToggleButton(getString("map.history.button.save"),
					LogicConstants.getIcon("historico_button_exportargpx"));
			saveGpx.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					SaveGPXDialog.showDialog(ConsultaHistoricos.getCapas());
					saveGpx.setSelected(false);
				}
			});
		}
		enableSaveGpx(false);
		return saveGpx;
	}

	private JPanel createHistoryControls() {
		controls = new JPanel();

		controls.add(getConsultaHistoricos());
		controls.add(getSaveGpx());
		controls.setBounds(50, 5, 400, 50);
		controls.setOpaque(false);
		return controls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateControls() {
		if (Authentication.isAuthenticated()) {
			controlPanel.setAvaliableResources(Collections
					.synchronizedCollection(ConsultaHistoricos
							.getCurrentRecursos()));
			controlPanel.setAvaliableIncidences(Collections
					.synchronizedCollection(ConsultaHistoricos
							.getCurrentIncidencias()));
		}
	}

	@Override
	protected JPopupMenu getContextMenu() {
		JPopupMenu menu = new JPopupMenu();
		// Centrar aqui
		JMenuItem cent = new JMenuItem(
				Internacionalization.getString("map.menu.centerHere"),
				KeyEvent.VK_C);
		cent.setIcon(LogicConstants.getIcon("menucontextual_icon_centrar"));
		cent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mapView.zoomToFactor(mapView.getEastNorth(eventOriginal.getX(),
						eventOriginal.getY()), mapView.zoomFactor);
			}
		});
		menu.add(cent);

		menu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				eventOriginal = HistoryMapViewer.this.mapView.lastMEvent;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}
		});
		return menu;
	}

	/**
	 * @return the listaCapas
	 */
	public ListaCapas getListaCapas() {
		return listaCapas;
	}

	/**
	 * @param listaCapas the listaCapas to set
	 */
	public void setListaCapas(ListaCapas listaCapas) {
		this.listaCapas = listaCapas;
	}

	class HistoricoActionListener implements ActionListener {

		private HistoryMapViewer mv;

		public HistoricoActionListener(HistoryMapViewer mapViewer) {
			mv = mapViewer;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ConsultaHistoricos.showConsultaHistoricos(mapView, mv);
			historico.setSelected(true);
		}
	}
}
