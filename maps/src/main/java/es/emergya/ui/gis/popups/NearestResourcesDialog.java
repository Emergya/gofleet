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
package es.emergya.ui.gis.popups;

import static es.emergya.i18n.Internacionalization.getString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;

import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.HistoricoGPS;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.consultas.HistoricoGPSConsultas;
import es.emergya.consultas.IncidenciaConsultas;
import es.emergya.consultas.RecursoConsultas;
import es.emergya.geo.util.UTM;
import es.emergya.ui.SpringUtilities;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.gis.CustomMapView;
import es.emergya.ui.plugins.CenterRenderer;
import es.emergya.ui.plugins.JButtonCellEditor;

/**
 * @author fario
 * @author marias
 * 
 */
public class NearestResourcesDialog extends JFrame implements ActionListener {
	protected static List<Recurso> ALL = RecursoConsultas.getAll(Authentication
			.getUsuario());

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

	private final class CustomTableModel extends DefaultTableModel {
		private static final long serialVersionUID = -6354135444747786582L;

		private CustomTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == getColumnCount() - 1;
		}

		public void setData(Vector<Vector<Object>> rows) {
			this.dataVector = rows;
			this.fireTableDataChanged();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			try {
				if (columnIndex == 4)
					return Integer.class;

				return getValueAt(0, columnIndex) == null ? super
						.getColumnClass(columnIndex) : getValueAt(0,
						columnIndex).getClass();
			} catch (Throwable t) {
				return Object.class;
			}
		}
	}

	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(NearestResourcesDialog.class);
	private static final long serialVersionUID = -3444390839756697775L;

	JTextField coordX, coordY;
	JTextField datetime;
	JTable results;
	JLabel notification;
	JLabel progressIcon;
	CustomMapView view;
	LatLon point;
	Icon iconTransparente;
	Icon iconEnviando;
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			DateFormat.LONG);
	Recurso recurso = null;
	JButton search;

	/**
	 * 
	 * @param r
	 */
	public NearestResourcesDialog(Recurso r, CustomMapView view) {
		this(new LatLon(r.getHistoricoGps().getPosY(), r.getHistoricoGps()
				.getPosX()), view);
		if (!isVisible(r)) {
			notification
					.setText(getString("window.nearest.notificacion.resourceNotVisible"));
			notification.updateUI();
		}
		setTitle(getString("window.nearest.titleBar.recurso") + " "
				+ r.getIdentificador());
		setName(r.getIdentificador());
		this.recurso = r;
	}

	public NearestResourcesDialog(Incidencia i, LatLon ll, CustomMapView view) {
		this(ll, view);
		i = IncidenciaConsultas.get(i.getId());
		if (i.getEstado().getId().equals(6l)) {
			notification
					.setText(getString("window.nearest.notificacion.closedIncidence"));
			notification.updateUI();
		}

		// setTitle(getString("window.nearest.titleBar.incidencia") + " "
		// + i.getReferenciaHumana());
		// setName(i.getReferenciaHumana());
	}

	/**
	 * 
	 * @param ll
	 */
	public NearestResourcesDialog(LatLon ll, CustomMapView view) {
		super();
		setAlwaysOnTop(true);
		setResizable(false);
		try {
			setPreferredSize(new Dimension(560, 600));

			iconTransparente = LogicConstants.getIcon("48x48_transparente");
			iconEnviando = LogicConstants.getIcon("anim_calculando");
			this.view = view;
			this.point = ll;
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setTitle(getString("window.nearest.titleBar.puntoGenerico"));
			setIconImage(getBasicWindow().getFrame().getIconImage());
			JPanel base = new JPanel();
			base.setBackground(Color.WHITE);
			base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

			// Icono del titulo
			JPanel title = new JPanel(new FlowLayout(FlowLayout.LEADING));
			final JLabel labelTitle = new JLabel(
					getString("window.nearest.title"),
					LogicConstants.getIcon("tittleventana_icon_mascercano"),
					JLabel.LEFT);
			labelTitle.setFont(LogicConstants.deriveBoldFont(12.0f));
			title.add(labelTitle);
			title.setOpaque(false);
			base.add(title);

			JPanel content = new JPanel(new SpringLayout());
			content.setOpaque(false);

			// Coordenadas
			content.add(new JLabel(getString("map.location"), JLabel.RIGHT));
			JPanel coords = new JPanel(new GridLayout(1, 2));
			coords.setOpaque(false);
			coordX = new JTextField(8);
			coordX.setEditable(false);
			coordY = new JTextField(8);
			coordY.setEditable(false);
			coords.add(coordY);
			coords.add(coordX);
			content.add(coords);
			printCoordinates(ll);

			// Fecha y hora
			content.add(new JLabel(getString("Admin.dateTime"), JLabel.RIGHT));
			datetime = new JTextField(df.format(Calendar.getInstance()
					.getTime()));
			datetime.setEditable(false);

			content.add(datetime);

			SpringUtilities.makeCompactGrid(content, 2, 2, 6, 6, 6, 6);
			base.add(content);

			// Tabla con los resultados
			DefaultTableModel model = new CustomTableModel(new String[] {
					getString("window.nearest.table.title.orden"),
					getString("window.nearest.table.title.nombre"),
					getString("window.nearest.table.title.patrulla"),
					getString("window.nearest.table.title.estado"),
					getString("window.nearest.table.title.distancia") + "(m)",
					"" }, 0);
			results = new JTable(model);

			RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);

			List<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>();
			keys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
			sorter.setSortKeys(keys);
			results.setRowSorter(sorter);

			results.setBackground(Color.WHITE);
			results.setFillsViewportHeight(true);
			results.setDragEnabled(false);
			results.setShowVerticalLines(false);

			results.setDefaultRenderer(Object.class, new CenterRenderer());
			results.setDefaultRenderer(Integer.class, new CenterRenderer());

			results.setDefaultRenderer(JButton.class, new TableCellRenderer() {

				@Override
				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {
					JButton b = (JButton) value;
					b.setBorderPainted(false);
					b.setContentAreaFilled(false);
					return b;
				}
			});
			results.setDefaultEditor(JButton.class, new JButtonCellEditor());

			TableColumn column = results.getColumnModel().getColumn(5);
			column.setPreferredWidth(40);
			column.setMinWidth(40);
			column.setMaxWidth(40);

			column = results.getColumnModel().getColumn(0);
			column.setPreferredWidth(40);
			column.setMinWidth(40);
			column.setMaxWidth(40);

			JScrollPane scroll = new JScrollPane(results);
			scroll.setBackground(Color.WHITE);
			scroll.setBorder(new TitledBorder(BorderFactory
					.createLineBorder(Color.BLACK),
					getString("window.nearest.table.scroll.titulo")));

			base.add(scroll);

			// Area para mensajes
			JPanel notificationArea = new JPanel();
			notificationArea.setOpaque(false);
			notification = new JLabel("");
			notification.setForeground(Color.RED);
			notificationArea.add(notification);
			base.add(notificationArea);

			JPanel buttons = new JPanel();
			buttons.setOpaque(false);
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
			search = new JButton(getString("Buttons.refresh"),
					LogicConstants.getIcon("button_refrescar"));
			search.addActionListener(this);
			buttons.add(search);
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
			setLocationRelativeTo(null);
			calculaMasCercanos(search);
		} catch (Throwable t) {
			log.error("Error al mostrar los más cercanos.", t);
		}
		pack();
		setLocationRelativeTo(getBasicWindow().getFrame());
	}

	private void printCoordinates(LatLon ll) {
		if (ll != null) {
			if (LogicConstants.get("FORMATO_COORDENADAS_MAPA", "UTM").equals(
					LogicConstants.COORD_UTM)) {
				UTM u = new UTM(LogicConstants.getInt("ZONA_UTM"));
				EastNorth en = u.latlon2eastNorth(ll);
				coordY.setText(String.valueOf(en.getX()));
				coordX.setText(String.valueOf(en.getY()));
			} else {
				coordX.setText(String.valueOf(ll.getX()));
				coordY.setText(String.valueOf(ll.getY()));
			}
		} else {
			coordX.setText(null);
			coordY.setText(null);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			calculaMasCercanos((JButton) e.getSource());
			notification.setText("");
			notification.updateUI();
		}
	}

	private Action centerIn(final LatLon ll, final Recurso r) {
		return new AbstractAction("",
				LogicConstants.getIcon("map_button_centrar")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				view.zoomTo(Main.proj.latlon2eastNorth(ll), view.getScale());

				if (!isVisible(r)) {
					notification
							.setText(getString("window.nearest.notificacion.noDisponible"));
					notification.updateUI();
				} else {
					notification.setText("");
					notification.updateUI();
				}
			}
		};
	}

	private boolean isVisible(Recurso r) {
		try {
			boolean res = r
					.getHistoricoGps()
					.getMarcaTemporal()
					.after(new Date(System.currentTimeMillis()
							- LogicConstants.getInt("AVL_TIMEOUT") * 60000));

			res = res && ALL.contains(r);

			return res;
		} catch (Throwable t) {
			log.error("Error al mirar la fecha de ultimo gps", t);
			return false;
		}
	}

	private void calculaMasCercanos(JButton b) {
		ALL = RecursoConsultas.getAll(Authentication.getUsuario());
		try {
			if (b.getActionCommand().equals(getString("Buttons.refresh"))) {

				b.setEnabled(false);
				progressIcon.setIcon(iconEnviando);
				SwingWorker<Boolean, Object> w = new SwingWorker<Boolean, Object>() {
					@Override
					protected Boolean doInBackground() throws Exception {
						try {
							CustomTableModel m = (CustomTableModel) results
									.getModel();
							while (m.getRowCount() > 0) {
								m.removeRow(0);

							}
							datetime.setText(df.format(Calendar.getInstance()
									.getTime()));// pone
							int num = LogicConstants
									.getInt("MAX_NEAREST_RESOURCES") + 1;
							// la
							// fecha
							// y
							// hora
							// de
							// hoy
							if (recurso != null) {
								HistoricoGPS ultimaPosicionRecurso = HistoricoGPSConsultas
										.lastGPSForRecurso(recurso);
								// Si hay última posición y es visible
								if (ultimaPosicionRecurso != null
										&& !ultimaPosicionRecurso
												.getMarcaTemporal()
												.before(new Date(
														System.currentTimeMillis()
																- LogicConstants
																		.getInt("AVL_TIMEOUT")
																* 6000))) {
									point = new LatLon(
											ultimaPosicionRecurso.getPosY(),
											ultimaPosicionRecurso.getPosX());
									printCoordinates(point);
								}
							}

							log.info("Buscamos los más cercanos a " + point);
							Recurso[] rs = RecursoConsultas.getNearest(
									point.getX(), point.getY(), num,
									Authentication.getUsuario());
							log.info("Tenemos los más cercanos a " + point
									+ ", que son " + rs.length);
							Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
							int k = 0;
							for (Integer i = 0; i < rs.length && k < num - 1; i++) {
								k++;
								if (!isVisible(rs[i])
										|| (recurso != null && recurso.getId()
												.equals(rs[i].getId()))) {
									log.info("No se muestra " + rs[i]);
									k--;
									continue;

								}
								HistoricoGPS h = rs[i].getHistoricoGps();

								LatLon ll = new LatLon(h.getPosY(), h.getPosX());
								Integer dist = (int) ll
										.greatCircleDistance(point);

								Vector<Object> row = new Vector<Object>();
								row.add(i);
								row.add(rs[i].getIdentificador());
								row.add(rs[i].getPatrullas() != null ? rs[i]
										.getPatrullas().getNombre() : "");
								row.add(rs[i].getEstadoEurocop());
								row.add(dist);
								row.add(new JButton(centerIn(ll, rs[i])));
								rows.add(row);
							}
							Collections.sort(rows, new Comparator() {

								@Override
								public int compare(Object arg0, Object arg1) {
									if (!(arg0 instanceof Vector)
											|| !(arg1 instanceof Vector))
										return 0;
									try {
										final Integer a = new Integer(
												((Vector<Object>) arg0).get(4)
														.toString());
										final Integer b = new Integer(
												((Vector<Object>) arg1).get(4)
														.toString());
										return (a.compareTo(b));
									} catch (Throwable t) {
										return 0;
									}
								}
							});

							int i = 1;
							for (Vector<Object> row : rows)
								((Vector<Object>) row).set(0, i++);
							m.setData(rows);
							log.debug("Mostramos " + m.getRowCount());
							return true;
						} catch (Throwable t) {
							log.error("Error al sacar los mas cercanos", t);
							return false;
						}
					}

					@Override
					protected void done() {
						progressIcon.setIcon(iconTransparente);
						search.setEnabled(true);
						results.updateUI();
					}
				};
				w.execute();
			}

			if (b.getActionCommand().equals(getString("Buttons.cancel"))) {
				dispose();
			}
		} catch (Throwable t) {
			log.error("Error al calcular los mas cercanos", t);
		}
	}
}
