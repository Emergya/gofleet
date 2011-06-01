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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.GpxWriter;

import es.emergya.cliente.constants.LogicConstants;
import es.emergya.i18n.Internacionalization;
import es.emergya.ui.base.BasicWindow;

public class SaveGPXDialog extends JFrame {
	static final Log log = LogFactory.getLog(SaveGPXDialog.class);
	private static final long serialVersionUID = -6066807198392103411L;
	private static SaveGPXDialog self;

	public synchronized static void close() {
		if (self != null)
			self.dispose();
		self = null;
	}

	public synchronized static void showDialog(List<Layer> capas) {
		if (self == null)
			self = new SaveGPXDialog(capas);
		self.setVisible(true);
		self.setExtendedState(JFrame.NORMAL);
	}

	private SaveGPXDialog(final List<Layer> capas) {
		super("Consulta de Posiciones GPS");
		setResizable(false);
		setAlwaysOnTop(true);
		this.setIconImage(BasicWindow.getIconImage());
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JPanel dialogo = new JPanel(new BorderLayout());
		dialogo.setBackground(Color.WHITE);
		dialogo.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel central = new JPanel(new FlowLayout());
		central.setOpaque(false);
		final JTextField nombre = new JTextField(15);
		nombre.setEditable(false);
		central.add(nombre);
		final JButton button = new JButton("Examinar...", LogicConstants
				.getIcon("button_nuevo"));
		central.add(button);
		final JButton aceptar = new JButton("Guardar", LogicConstants
				.getIcon("button_save"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showSaveDialog(SaveGPXDialog.this) == JFileChooser.APPROVE_OPTION) {
					nombre.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
					aceptar.setEnabled(true);
				}
			}
		});

		dialogo.add(central, BorderLayout.CENTER);

		JPanel botones = new JPanel(new FlowLayout());
		botones.setOpaque(false);

		aceptar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String base_url = nombre.getText() + "_";
				for (Layer layer : capas) {
					if (layer instanceof GpxLayer) {
						GpxLayer gpxLayer = (GpxLayer) layer;
						File f = new File(base_url + gpxLayer.name + ".gpx");

						boolean sobreescribir = !f.exists();

						try {
							while (!sobreescribir) {
								String original = f.getCanonicalPath();
								f = checkFileOverwritten(nombre, f);
								sobreescribir = !f.exists()
										|| original
												.equals(f.getCanonicalPath());
							}
						} catch (NullPointerException t) {
							log.debug("Cancelando creacion de fichero: " + t);
							sobreescribir = false;
						} catch (Throwable t) {
							log.error("Error comprobando la sobreescritura", t);
							sobreescribir = false;
						}

						if (sobreescribir) {
							try {
								f.createNewFile();
							} catch (IOException e1) {
								log.error(e1, e1);
							}
							if (!(f.isFile() && f.canWrite()))
								JOptionPane.showMessageDialog(
										SaveGPXDialog.this,
										"No tengo permiso para escribir en "
												+ f.getAbsolutePath());
							else {
								try {
									OutputStream out = new FileOutputStream(f);
									GpxWriter writer = new GpxWriter(out);
									writer.write(gpxLayer.data);
									out.close();
								} catch (Throwable t) {
									log.error("Error al escribir el gpx", t);
									JOptionPane.showMessageDialog(
											SaveGPXDialog.this,
											"Ocurrió un error al escribir en "
													+ f.getAbsolutePath());
								}
							}
						} else
							log
									.error("Por errores anteriores no se escribio el fichero");
					} else
						log.error("Una de las capas no era gpx: " + layer.name);
				}
				SaveGPXDialog.this.dispose();

			}

			private File checkFileOverwritten(final JTextField nombre, File f)
					throws Exception {
				String nueva = JOptionPane.showInputDialog(
						nombre,
						Internacionalization.getString(
								"savegpxdialog.overwrite", f.getName()),
						"Sobreescribir archivo", JOptionPane.QUESTION_MESSAGE,
						null, null, f.getCanonicalPath()).toString();
				log.debug("Nueva ruta: " + nueva);
				return new File(nueva);
			}
		});

		JButton cancelar = new JButton("Cancelar", LogicConstants
				.getIcon("button_cancel"));

		cancelar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SaveGPXDialog.this.dispose();
			}
		});

		aceptar.setEnabled(false);
		botones.add(aceptar);
		botones.add(cancelar);
		dialogo.add(botones, BorderLayout.SOUTH);

		add(dialogo);
		setPreferredSize(new Dimension(300, 200));
		pack();

		int x;
		int y;

		Container myParent = BasicWindow.getFrame().getContentPane();
		Point topLeft = myParent.getLocationOnScreen();
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

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				nombre.setText("");
				nombre.repaint();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				nombre.setText("");
				nombre.repaint();
			}
		});
	}
}