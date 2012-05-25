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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MyGpxLayer;
import org.openstreetmap.josm.io.GpxImporter;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.gis.CustomMapView;
import es.emergya.ui.gis.IMapViewer;

public class ListaCapas extends JDialog implements ActionListener {

	private static final long serialVersionUID = -6066807198392103411L;
	private static final Log log = LogFactory.getLog(ListaCapas.class);
	private static ListaCapas self;
	private JFileChooser fileChooser;
	int contador = 0;
	JPanel capasGpx;
	CustomMapView mapView;
	List<Layer> capasActuales = new LinkedList<Layer>();

	@GoWired
	private BasicWindow basicWindow;

	/**
	 * @return the basicWindow
	 */
	public BasicWindow getBasicWindow() {
		return basicWindow;
	}

	/**
	 * @param basicWindow
	 *            the basicWindow to set
	 */
	public void setBasicWindow(BasicWindow basicWindow) {
		this.basicWindow = basicWindow;
	}

	public synchronized void showListaCapas(CustomMapView mapView,
			IMapViewer historyMapViewer) {
		if (self == null) {
			self = new ListaCapas(mapView, historyMapViewer);
		}
		self.requestFocusInWindow();
		self.setVisible(true);
	}

	public synchronized void hideListaCapas() {
		if (self != null) {
			self.setVisible(false);
		}
	}

	public synchronized static void quitListaCapas() {
		if (self != null) {
			for (Layer l : self.capasActuales)
				self.mapView.removeLayer(l);
			self.dispose();
			self = null;
		}
	}

	private ListaCapas(CustomMapView mapView, final IMapViewer historyMapViewer) {
		super();
		setTitle(getString("window.gpx.titleBar"));
		setLocationRelativeTo(getBasicWindow().getFrame());
		setResizable(false);
		setAlwaysOnTop(true);
		this.mapView = mapView;
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setIconImage(getBasicWindow().getFrame().getIconImage());
		JPanel dialogo = new JPanel(new BorderLayout());
		dialogo.setBackground(Color.WHITE);
		dialogo.setBorder(new EmptyBorder(10, 10, 10, 10));

		capasGpx = new JPanel();
		capasGpx.setBackground(Color.WHITE);
		capasGpx.setLayout(new BoxLayout(capasGpx, BoxLayout.Y_AXIS));

		JScrollPane lista = new JScrollPane(capasGpx);
		lista.setOpaque(false);
		lista.setBorder(new TitledBorder(getString("window.gpx.title")));
		dialogo.add(lista, BorderLayout.CENTER);

		JPanel boton = new JPanel(new FlowLayout());
		boton.setOpaque(false);
		JButton cargar = getCargarGPXButton();
		boton.add(cargar, FlowLayout.LEFT);
		dialogo.add(boton, BorderLayout.SOUTH);

		add(dialogo);
		setPreferredSize(new Dimension(400, 250));
		pack();
		setLocationRelativeTo((Component) mapView);

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosed(e);
				// historyMapViewer.getGPXButton().setSelected(false);
			}
		});

	}

	private JButton getCargarGPXButton() {
		JButton cargar = new JButton(getString("window.gpx.button.load"),
				LogicConstants.getIcon("historico_button_cargargpx"));
		cargar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						return (f.getName().toLowerCase().endsWith(".gpx"));
					}

					@Override
					public String getDescription() {
						return getString("window.gpx.filechooser.filter");
					}
				});

				try {
					if (fileChooser.showOpenDialog(self) == JFileChooser.APPROVE_OPTION) {
						cargarGpx(fileChooser.getSelectedFile());
					}
				} catch (Throwable t) {
					log.error("Error al cargar GPX " + t);
					JOptionPane.showMessageDialog(getBasicWindow().getFrame(),
							getString("window.gpx.loadError"));
				}
			}
		});
		return cargar;
	}

	private void cargarGpx(File file) throws IOException {
		GpxImporter importer = new GpxImporter();
		if (!importer.acceptFile(file)) {
			new IOException("Gpx inaccesible.");
		}
		final String absolutePath = file.getAbsolutePath();

		for (Layer l : capasActuales) {
			if (l.name.equals(file.getAbsolutePath()))
				return;
		}

		importer.importData(file);
		GpxLayer layer = new MyGpxLayer(importer.getLastData(), absolutePath,
				this.mapView);

		Main.pref.putColor("layer " + layer.name,
				Color.decode(LogicConstants.getNextColor()));
		addCapa(layer);
	}

	private void addCapa(final GpxLayer layer) {
		final JPanel capaP = new JPanel();
		capaP.setOpaque(false);
		layer.visible = true;
		mapView.addLayer(layer, false, capasActuales.size());
		capasActuales.add(layer);
		capaP.setLayout(new BoxLayout(capaP, BoxLayout.X_AXIS));

		final JCheckBox capa = new JCheckBox(layer.getAssociatedFile()
				.getAbsolutePath());
		capa.setSelected(layer.visible);
		capa.setBackground(Color.WHITE);
		capa.setToolTipText(getString("window.gpx.checkbox.show.tooltip"));
		capa.setActionCommand(layer.name);
		capa.addActionListener(this);
		capaP.add(capa);
		capaP.add(Box.createHorizontalGlue());
		JButton eliminar = new JButton(LogicConstants.getIcon("button_delone"));
		eliminar.setToolTipText(getString("window.gpx.button.delete.tooltip"));
		eliminar.setBorderPainted(false);
		eliminar.setContentAreaFilled(false);

		capaP.add(eliminar);
		eliminar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mapView.removeLayer(layer);
				mapView.repaint();
				capasGpx.remove(capaP);
				capasGpx.updateUI();
				capasActuales.remove(layer);
			}
		});

		capasGpx.add(capaP);
		capasGpx.updateUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (Layer l : mapView.getAllLayers()) {
			if (l.name.equals(e.getActionCommand())) {
				l.visible = ((JCheckBox) e.getSource()).isSelected();
				mapView.repaint();
				return;
			}
		}
	}
}
