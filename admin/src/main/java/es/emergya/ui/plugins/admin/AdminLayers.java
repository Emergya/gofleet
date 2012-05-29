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
package es.emergya.ui.plugins.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;
import org.gvsig.remoteClient.wms.ICancellable;
import org.gvsig.remoteClient.wms.WMSClient;
import org.gvsig.remoteClient.wms.WMSLayer;
import org.gvsig.remoteClient.wms.WMSStyle;

import es.emergya.actions.CapaInformacionAdmin;
import es.emergya.bbdd.bean.Capa;
import es.emergya.bbdd.bean.CapaInformacion;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.CapaConsultas;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.plugins.Option;
import es.emergya.ui.base.plugins.PluginEvent;
import es.emergya.ui.base.plugins.PluginType;
import es.emergya.ui.plugins.AdminPanel;
import es.emergya.ui.plugins.AdminPanel.FiltrarAction;
import es.emergya.ui.plugins.AdminPanel.NoFiltrarAction;
import es.emergya.ui.plugins.admin.aux1.SummaryAction;

public class AdminLayers extends Option {

	private static final long serialVersionUID = 194960720891943261L;
	private static String ICON = "tittlemanage_icon_capas";
	static final Log log = LogFactory.getLog(AdminLayers.class);
	AdminPanel layers;
	private String version;
	private CapaInformacion lastExample;

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

	public AdminLayers() {
		super("", PluginType.getType("ADMIN"), 5, "subtab_icon_capas", null);
		super.setTitle(i18n.getString("Layers.layers"));
		layers = new AdminPanel(i18n.getString("admin.capas.titulo"),
				LogicConstants.getIcon(ICON), this);
		layers.setColumnToReselect(2);
		layers.addColumnWidth(1, 40);
		layers.addColumnWidth(4, 65);
		layers.addColumnWidth(5, 65);
		layers.addColumnWidth(6, 65);
		layers.addColumnWidth(7, 65);
		layers.addInvisibleFilterCol(1);
		layers.addInvisibleFilterCol(6);
		layers.addInvisibleFilterCol(7);
		layers.setNewAction(getSummaryAction(null));
		layers.generateTable(
				new String[] {
						i18n.getString("admin.capas.tabla.titulo.orden"),
						i18n.getString("admin.capas.tabla.titulo.nombre"),
						i18n.getString("admin.capas.tabla.titulo.url"),
						i18n.getString("admin.capas.tabla.titulo.tipo"),
						i18n.getString("admin.capas.tabla.titulo.habilitado"),
						i18n.getString("admin.capas.tabla.titulo.subir"),
						i18n.getString("admin.capas.tabla.titulo.bajar"),
						i18n.getString("admin.capas.tabla.titulo.ficha"),
						i18n.getString("admin.capas.tabla.titulo.eliminar"), },
				new Object[][] { {}, {}, {}, { "", "Base", "Opcional" },
						{ "", "Habilitada", "Deshabilitada" } },
				getNoFiltrarAction(), getFiltrarAction());
		layers.setTableData(getAll(new CapaInformacion()));
		layers.setErrorCause(i18n.getString("Layers.errorCause"));
		this.add(layers);
	}

	private Object[][] getAll(CapaInformacion c) {
		lastExample = c;
		List<CapaInformacion> capas = CapaConsultas.getByExample(c);

		Object[][] res = new Object[capas.size()][];

		int showed = capas.size();
		int total = CapaConsultas.getTotal();
		layers.setCuenta(showed, total);

		int i = 0;
		for (CapaInformacion capa : capas) {
			res[i] = new Object[9];
			if (capa.getOrden() != null) {
				res[i][0] = capa.getOrden();

			} else {
				res[i][0] = i + 1;

			}
			res[i][1] = capa.getNombre();
			res[i][2] = capa.getUrl_visible();
			if (capa.isOpcional()) {
				res[i][3] = "Opcional";

			} else {
				res[i][3] = "Base";

			}
			if (capa.isHabilitada() == null) {
				capa.setHabilitada(false);

			}
			res[i][4] = capa.isHabilitada();
			res[i][5] = subeCapaAction(capa);
			res[i][6] = bajaCapaAction(capa);
			res[i][7] = getSummaryAction(capa);
			res[i++][8] = getDeleteAction(capa);
		}

		return res;
	}

	private NoFiltrarAction getNoFiltrarAction() {
		return layers.new NoFiltrarAction() {

			private static final long serialVersionUID = -6566681011645411911L;

			@Override
			protected void applyFilter() {
				layers.setTableData(getAll(new CapaInformacion()));
			}
		};
	}

	private FiltrarAction getFiltrarAction() {
		return layers.new FiltrarAction() {

			private static final long serialVersionUID = -8261691115496760409L;

			@Override
			protected void applyFilter(JTable filters) {
				final CapaInformacion example = new CapaInformacion();
				Object valueAt = filters.getValueAt(0, 1);
				if (valueAt != null
						&& StringUtils.isNumeric(valueAt.toString())
						&& valueAt.toString().length() > 0) {
					example.setOrden(new Integer(valueAt.toString()));

				}
				valueAt = filters.getValueAt(0, 2);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setNombre(valueAt.toString());

				}
				valueAt = filters.getValueAt(0, 3);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setUrl(valueAt.toString());

				}
				valueAt = filters.getValueAt(0, 4);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setOpcional(valueAt.equals("Opcional"));

				}
				valueAt = filters.getValueAt(0, 5);
				if (valueAt != null && valueAt.toString().trim().length() > 0) {
					example.setHabilitada(valueAt.equals("Habilitada"));

				}
				layers.setTableData(getAll(example));
			}
		};
	}

	protected SummaryAction getSummaryAction(
			final CapaInformacion capaInformacion) {
		SummaryAction action = new SummaryAction(capaInformacion) {

			private static final long serialVersionUID = -3691171434904452485L;

			@Override
			protected JFrame getSummaryDialog() {

				if (capaInformacion != null) {
					d = getDialog(capaInformacion, null, "", null, "image/png");
					return d;
				} else {
					JDialog primera = getJDialog();
					primera.setResizable(false);
					primera.setVisible(true);
					primera.setAlwaysOnTop(true);
				}
				return null;
			}

			private JDialog getJDialog() {
				final JDialog dialog = new JDialog();
				dialog.setTitle(i18n.getString("admin.capas.nueva.titleBar"));
				dialog.setIconImage(getBasicWindow().getIconImage());

				dialog.setLayout(new BorderLayout());

				JPanel centro = new JPanel(new FlowLayout());
				centro.setOpaque(false);
				JLabel label = new JLabel(
						i18n.getString("admin.capas.nueva.url"));
				final JTextField url = new JTextField(50);
				final JLabel icono = new JLabel(
						LogicConstants.getIcon("48x48_transparente"));
				label.setLabelFor(url);
				centro.add(label);
				centro.add(url);
				centro.add(icono);
				dialog.add(centro, BorderLayout.CENTER);

				JPanel pie = new JPanel(new FlowLayout(FlowLayout.TRAILING));
				pie.setOpaque(false);
				final JButton siguiente = new JButton(
						i18n.getString("admin.capas.nueva.boton.siguiente"),
						LogicConstants.getIcon("button_next"));
				JButton cancelar = new JButton(
						i18n.getString("admin.capas.nueva.boton.cancelar"),
						LogicConstants.getIcon("button_cancel"));
				final SiguienteActionListener siguienteActionListener = new SiguienteActionListener(
						url, dialog, icono, siguiente);
				url.addActionListener(siguienteActionListener);

				siguiente.addActionListener(siguienteActionListener);

				cancelar.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});
				pie.add(siguiente);
				pie.add(cancelar);
				dialog.add(pie, BorderLayout.SOUTH);

				dialog.getContentPane().setBackground(Color.WHITE);

				dialog.pack();
				dialog.setLocationRelativeTo(null);
				return dialog;
			}

			private JFrame getDialog(final CapaInformacion c,
					final Capa[] left_items, final String service,
					final Map<String, Boolean> transparentes, final String png) {

				if (left_items != null && left_items.length == 0) {
					JOptionPane
							.showMessageDialog(
									AdminLayers.this,
									i18n.getString("admin.capas.nueva.error.noCapasEnServicio"));
				} else {

					final String label_cabecera = i18n
							.getString("admin.capas.nueva.nombreCapa");
					final String label_pie = i18n
							.getString("admin.capas.nueva.infoAdicional");
					final String centered_label = i18n
							.getString("admin.capas.nueva.origenDatos");
					final String left_label = i18n
							.getString("admin.capas.nueva.subcapasDisponibles");
					final String right_label;
					if (left_items != null) {
						right_label = i18n
								.getString("admin.capas.nueva.capasSeleccionadas");
					} else {
						right_label = i18n
								.getString("admin.capas.ficha.capasSeleccionadas");
					}
					final String tituloVentana, cabecera;
					if (c.getNombre() == null) {
						tituloVentana = i18n
								.getString("admin.capas.nueva.titulo.nuevaCapa");
						cabecera = i18n
								.getString("admin.capas.nueva.cabecera.nuevaCapa");
					} else {
						tituloVentana = i18n
								.getString("admin.capas.nueva.titulo.ficha");
						cabecera = i18n
								.getString("admin.capas.nueva.cabecera.ficha");
					}

					final Capa[] right_items = c.getCapas()
							.toArray(new Capa[0]);
					final AdminPanel.SaveOrUpdateAction<CapaInformacion> guardar = layers.new SaveOrUpdateAction<CapaInformacion>(
							c) {

						private static final long serialVersionUID = 7447770296943341404L;

						@Override
						public void actionPerformed(ActionEvent e) {

							if (isNew
									&& CapaConsultas
											.alreadyExists(textfieldCabecera
													.getText())) {
								JOptionPane
										.showMessageDialog(
												super.frame,
												i18n.getString("admin.capas.nueva.error.nombreCapaYaExiste"));

							} else if (textfieldCabecera.getText().isEmpty()) {
								JOptionPane
										.showMessageDialog(
												super.frame,
												i18n.getString("admin.capas.nueva.error.nombreCapaEnBlanco"));

							} else if (((DefaultListModel) right.getModel())
									.size() == 0) {
								JOptionPane
										.showMessageDialog(
												super.frame,
												i18n.getString("admin.capas.nueva.error.noCapasSeleccionadas"));

							} else if (cambios) {
								int i = JOptionPane
										.showConfirmDialog(
												super.frame,
												i18n.getString("admin.capas.nueva.confirmar.guardar.titulo"),
												i18n.getString("admin.capas.nueva.confirmar.boton.guardar"),
												JOptionPane.YES_NO_CANCEL_OPTION);

								if (i == JOptionPane.YES_OPTION) {

									if (original == null) {
										original = new CapaInformacion();

									}
									original.setInfoAdicional(textfieldPie
											.getText());
									original.setNombre(textfieldCabecera
											.getText());
									original.setHabilitada(habilitado
											.isSelected());
									original.setOpcional(comboTipoCapa
											.getSelectedIndex() != 0);

									boolean transparente = true;

									HashSet<Capa> capas = new HashSet<Capa>();
									List<Capa> capasEnOrdenSeleccionado = new ArrayList<Capa>();
									int indice = 0;
									for (Object c : ((DefaultListModel) right
											.getModel()).toArray()) {
										if (c instanceof Capa) {
											transparente = transparente
													&& (transparentes != null
															&& transparentes
																	.get(((Capa) c)
																			.getNombre()) != null && transparentes
																.get(((Capa) c)
																		.getNombre()));
											capas.add((Capa) c);
											capasEnOrdenSeleccionado
													.add((Capa) c);
											((Capa) c)
													.setCapaInformacion(original);
											((Capa) c).setOrden(indice++);
											// ((Capa)
											// c).setNombre(c.toString());
										}

									}
									original.setCapas(capas);

									if (original.getId() == null) {
										String url = nombre.getText();

										if (url.indexOf("?") > -1) {
											if (!url.endsWith("?")) {
												url += "&";

											}
										} else {
											url += "?";

										}
										url += "VERSION="
												+ version
												+ "&REQUEST=GetMap&FORMAT="
												+ png
												+ "&SERVICE="
												+ service
												+ "&WIDTH={2}&HEIGHT={3}&BBOX={1}&SRS={0}";
										// if (transparente)
										url += "&TRANSPARENT=TRUE";
										url += "&LAYERS=";

										String estilos = "";
										final String coma = "%2C";
										if (capasEnOrdenSeleccionado.size() > 0) {
											for (Capa c : capasEnOrdenSeleccionado) {
												url += c.getTitulo()
														.replaceAll(" ", "+")
														+ coma;
												estilos += c.getEstilo() + coma;
											}
											estilos = estilos.substring(
													0,
													estilos.length()
															- coma.length());

											estilos = estilos.replaceAll(" ",
													"+");

											url = url.substring(0, url.length()
													- coma.length());
										}
										url += "&STYLES=" + estilos;
										original.setUrl_visible(original
												.getUrl());
										original.setUrl(url);
									}
									CapaInformacionAdmin.saveOrUpdate(original);

									cambios = false;

									layers.setTableData(getAll(new CapaInformacion()));

									closeFrame();
								} else if (i == JOptionPane.NO_OPTION) {
									closeFrame();

								}
							} else {
								closeFrame();

							}
						}
					};
					JFrame segunda = generateUrlDialog(label_cabecera,
							label_pie, centered_label, tituloVentana,
							left_items, right_items, left_label, right_label,
							guardar,
							LogicConstants.getIcon("tittleficha_icon_capa"),
							cabecera, c.getHabilitada(), c.getOpcional(),
							c.getUrl_visible());
					segunda.setResizable(false);

					if (c != null) {
						textfieldCabecera.setText(c.getNombre());
						textfieldPie.setText(c.getInfoAdicional());
						nombre.setText(c.getUrl_visible());
						nombre.setEditable(false);
						if (c.getHabilitada() == null) {
							c.setHabilitada(false);

						}
						habilitado.setSelected(c.getHabilitada());
						if (c.isOpcional() != null && c.isOpcional()) {
							comboTipoCapa.setSelectedIndex(1);
						} else {
							comboTipoCapa.setSelectedIndex(0);
						}
					}

					if (c.getId() == null) {
						habilitado.setSelected(true);
						comboTipoCapa.setSelectedIndex(1);
					}

					habilitado.setEnabled(true);
					if (c == null || c.getId() == null) {
						textfieldCabecera.setEditable(true);
					} else {
						textfieldCabecera.setEditable(false);
					}

					cambios = false;

					segunda.pack();
					segunda.setLocationRelativeTo(null);
					segunda.setVisible(true);
					return segunda;
				}
				return null;
			}

			class SiguienteActionListener implements ActionListener {

				private final JTextField url;
				private final JDialog dialog;
				private final JLabel icono;
				private final JButton siguiente;

				public SiguienteActionListener(JTextField url, JDialog dialog,
						JLabel icono, JButton siguiente) {
					this.url = url;
					this.dialog = dialog;
					this.icono = icono;
					this.siguiente = siguiente;
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					final CapaInformacion ci = new CapaInformacion();
					ci.setUrl(url.getText());
					ci.setCapas(new HashSet<Capa>());
					SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

						private List<Capa> res = new LinkedList<Capa>();
						private String service = "WMS";
						private String png = null;
						private Map<String, Boolean> transparentes = new HashMap<String, Boolean>();
						private ArrayList<String> errorStack = new ArrayList<String>();
						private Boolean goOn = true;

						@SuppressWarnings(value = "unchecked")
						@Override
						protected Object doInBackground() throws Exception {
							try {
								final String url2 = ci.getUrl();
								WMSClient client = new WMSClient(url2);
								client.connect(new ICancellable() {

									@Override
									public boolean isCanceled() {
										return false;
									}

									@Override
									public Object getID() {
										return System.currentTimeMillis();
									}
								});

								version = client.getVersion();

								for (final String s : client.getLayerNames()) {
									WMSLayer layer = client.getLayer(s);
									// this.service =
									// client.getServiceName();
									final Vector allSrs = layer.getAllSrs();
									boolean epsg = (allSrs != null) ? allSrs
											.contains("EPSG:4326") : false;
									final Vector formats = client.getFormats();
									if (formats.contains("image/png")) {
										png = "image/png";
									} else if (formats.contains("IMAGE/PNG")) {
										png = "IMAGE/PNG";
									} else if (formats.contains("png")) {
										png = "png";
									} else if (formats.contains("PNG")) {
										png = "PNG";
									}
									boolean image = png != null;
									if (png == null) {
										png = "IMAGE/PNG";
									}
									if (epsg && image) {
										boolean hasTransparency = layer
												.hasTransparency();
										this.transparentes.put(s,
												hasTransparency);
										Capa capa = new Capa();
										capa.setCapaInformacion(ci);
										if (layer.getStyles().size() > 0) {
											capa.setEstilo(((WMSStyle) layer
													.getStyles().get(0))
													.getName());
										}
										capa.setNombre(layer.getTitle());
										capa.setTitulo(s);
										res.add(capa);
										if (!hasTransparency) {
											errorStack
													.add(i18n
															.getString(
																	Locale.ROOT,
																	"admin.capas.nueva.error.capaNoTransparente",
																	layer.getTitle()));
										}
									} else {
										String error = "";
										// if (opaque)
										// error += "<li>Es opaca</li>";
										if (!image) {
											error += i18n
													.getString("admin.capas.nueva.error.formatoPNG");
										}
										if (!epsg) {
											error += i18n
													.getString("admin.capas.nueva.error.projeccion");
										}
										final String cadena = i18n
												.getString(
														Locale.ROOT,
														"admin.capas.nueva.error.errorCapa",
														new Object[] { s, error });
										errorStack.add(cadena);
									}
								}
							} catch (final Throwable t) {
								log.error("Error al parsear el WMS", t);
								goOn = false;
								icono.setIcon(LogicConstants
										.getIcon("48x48_transparente"));

								JOptionPane
										.showMessageDialog(
												dialog,
												i18n.getString("admin.capas.nueva.error.errorParseoWMS"));

								siguiente.setEnabled(true);
							}
							return null;
						}

						@Override
						protected void done() {
							super.done();
							if (goOn) {

								dialog.dispose();
								ci.setUrl_visible(ci.getUrl());
								final JFrame frame = getDialog(ci,
										res.toArray(new Capa[0]), service,
										transparentes, png);
								if (!errorStack.isEmpty()) {
									String error = "<html>";
									for (final String s : errorStack) {
										error += s + "<br/>";
									}
									error += "</html>";
									final String errorString = error;
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											JOptionPane.showMessageDialog(
													frame, errorString);
										}
									});
								}
							}
						}
					};
					sw.execute();
					icono.setIcon(LogicConstants.getIcon("anim_conectando"));
					icono.repaint();
					siguiente.setEnabled(false);
				}
			}
		};

		return action;
	}

	protected AdminPanel.DeleteAction<CapaInformacion> getDeleteAction(
			CapaInformacion c) {
		AdminPanel.DeleteAction<CapaInformacion> action = layers.new DeleteAction<CapaInformacion>(
				c) {

			private static final long serialVersionUID = -1670721584457849308L;

			@Override
			protected boolean delete(boolean show_alert) {
				return CapaInformacionAdmin.delete(this.target);
			}
		};

		return action;
	}

	protected Action subeCapaAction(final CapaInformacion capa) {
		Action a = new AbstractAction("", LogicConstants.getIcon("button_up")) {

			private static final long serialVersionUID = 912391796510206341L;

			@Override
			public void actionPerformed(ActionEvent e) {
				log.debug("subeCapaAction(" + capa + ")");
				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() throws Exception {
						CapaInformacionAdmin.sube(capa);
						return null;
					}

					@Override
					protected void done() {
						super.done();
						AdminLayers.this.refresh(null);
					}
				};
				sw.execute();
			}
		};

		return a;
	}

	protected Action bajaCapaAction(final CapaInformacion capa) {
		Action a = new AbstractAction("", LogicConstants.getIcon("button_down")) {

			private static final long serialVersionUID = -4001983030571380494L;

			@Override
			public void actionPerformed(ActionEvent e) {
				log.debug("bajaCapaAction(" + capa + ")");
				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() throws Exception {
						CapaInformacionAdmin.baja(capa);
						return null;
					}

					@Override
					protected void done() {
						super.done();
						AdminLayers.this.refresh(null);
					}
				};
				sw.execute();
			}
		};

		return a;
	}

	@Override
	public void refresh(PluginEvent event) {
		super.refresh(event);
		layers.setTableData(getAll(lastExample));
	}

	@Override
	public boolean needsUpdating() {
		final Calendar lastUpdated2 = CapaConsultas.lastUpdated();
		if (lastUpdated2 == null && this.layers.getTotalSize() != 0) {
			return true;
		}

		return lastUpdated2.after(super.lastUpdated);
	}

	@Override
	public void reboot() {
		getNoFiltrarAction().actionPerformed(null);
		this.layers.unckeckAll();
	}
}
