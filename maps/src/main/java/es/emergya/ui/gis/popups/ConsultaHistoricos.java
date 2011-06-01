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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freixas.jcalendar.DateEvent;
import org.freixas.jcalendar.DateListener;
import org.freixas.jcalendar.JCalendarCombo;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MyGpxLayer;
import org.openstreetmap.josm.io.GpxImporter;
import org.openstreetmap.josm.io.GpxWriter;

import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.HistoricoGPS;
import es.emergya.bbdd.bean.Incidencia;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.gis.CustomMapView;
import es.emergya.ui.gis.HistoryMapViewer;
import es.emergya.ui.gis.ICustomMapView;
import es.emergya.webservices.ServiceStub;
import es.emergya.webservices.ServiceStub.GetIncidenciasAbiertasEnPeriodo;
import es.emergya.webservices.ServiceStub.GetPosicionesIncidencias;
import es.emergya.webservices.ServiceStub.GetPosicionesIncidenciasResponse;
import es.emergya.webservices.ServiceStub.GetRecursosEnPeriodo;
import es.emergya.webservices.ServiceStub.GetRecursosEnPeriodoResponse;
import es.emergya.webservices.ServiceStub.GetRutasRecursos;
import es.emergya.webservices.ServiceStub.GetRutasRecursosResponse;
import es.emergya.webservices.ServiceStub.GetUltimasPosiciones;
import es.emergya.webservices.ServiceStub.GetUltimasPosicionesResponse;
import es.emergya.webservices.ServiceStub.IncidenciaWS;
import es.emergya.webservices.ServiceStub.Posicion;
import es.emergya.webservices.WSProvider;

public class ConsultaHistoricos extends JFrame {

	private static final Dimension DIMENSION_LABEL = new Dimension(100, 10);
	private static final Dimension DIMENSION_JLIST = new Dimension(250, 80);
	private static final Dimension DIMENSION_2JLIST = new Dimension(250, 2 * 80);
	static final Log log = LogFactory.getLog(ConsultaHistoricos.class);
	private static final long serialVersionUID = -6066807198392103411L;
	private static ConsultaHistoricos self;
	private List<Layer> capas = new ArrayList<Layer>();
	protected CustomMapView mapView;
	private DateFormat dateFormat;
	private JLabel cargando = new JLabel();
	private JCheckBox soloUltimas;
	private JCalendarCombo calendarfin;
	private JSpinner horaini;
	private JSpinner horafin;
	// private JList zona;
	private JList incidencias;
	private JList recursos;
	private static Set<Object> recursosMostrados = new HashSet<Object>();
	private static Set<Object> incidenciasMostradas = new HashSet<Object>();
	private JCalendarCombo calendarini;
	private JButton limpiar;
	private JButton consultar;
	private SwingWorker<Layer, Object> consulta = null;
	private SwingWorker<String, String> updateRecursos = null;
	final private JTextField mensaje = new JTextField("");
	private HistoryMapViewer visorHistorico = null;
	private final static Locale LOCALE = new Locale("es", "ES");

	public synchronized static void close() {
		if (self != null)
			self.closing.actionPerformed(null);
		self = null;
	}

	private void clearRecursos() {
		synchronized (recursosMostrados) {
			recursosMostrados.clear();
		}
	}

	private void addRecurso(Object r) {
		synchronized (recursosMostrados) {
			if (!recursosMostrados.contains(r)) {
				recursosMostrados.add(r);
			}
		}
	}

	public static List<Object> getCurrentRecursos() {
		synchronized (recursosMostrados) {
			LinkedList<Object> res = new LinkedList<Object>();
			res.addAll(recursosMostrados);
			return res;
		}
	}

	private void clearIncidencias() {
		synchronized (incidenciasMostradas) {
			incidenciasMostradas.clear();
		}
	}

	private void addIncidencia(Object r) {
		synchronized (incidenciasMostradas) {
			if (!incidenciasMostradas.contains(r)) {
				incidenciasMostradas.add(r);
			}
		}
	}

	public static List<Object> getCurrentIncidencias() {
		synchronized (incidenciasMostradas) {
			LinkedList<Object> res = new LinkedList<Object>();
			res.addAll(incidenciasMostradas);
			return res;
		}
	}

	public static List<Layer> getCapas() {
		if (self == null) {
			return new LinkedList<Layer>();
		}
		return self.capas;
	}

	private final ListSelectionListener listSelectionListener = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (recursos.getSelectedIndex() != -1
					|| incidencias.getSelectedIndex() != -1) {
				consultar.setEnabled(true);
			} else {
				consultar.setEnabled(false);
			}
		}
	};
	private final ActionListener actionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			updateRecursos();
		}
	};
	private final ChangeListener changeListener = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			updateRecursos();
		}
	};
	private ActionListener closing = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			self.setVisible(false);
			try {
				if (consulta != null) {
					consulta.cancel(true);
				}
			} catch (Throwable t) {
			}
			getLimpiar().doClick();
			HistoryMapViewer.refreshHistoryPanel();
			self = null;
		}
	};

	public synchronized static void showConsultaHistoricos(CustomMapView mapView) {
		if (self == null) {
			self = new ConsultaHistoricos(mapView);
		}
		self.setVisible(true);
		self.setExtendedState(JFrame.NORMAL);
	}

	public synchronized static void showConsultaHistoricos(
			CustomMapView mapView, HistoryMapViewer mapviewer) {
		if (self == null) {
			self = new ConsultaHistoricos(mapView, mapviewer);
		}
		self.setVisible(true);
		self.setExtendedState(JFrame.NORMAL);
	}

	private ConsultaHistoricos(CustomMapView mapView, HistoryMapViewer mapViewer) {
		this(mapView);
		this.visorHistorico = mapViewer;
	}

	private ConsultaHistoricos(CustomMapView mapView) {
		super("Consulta de Posiciones GPS");
		setResizable(false);
		setAlwaysOnTop(true);
		this.mapView = mapView;
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setIconImage(BasicWindow.getIconImage());

		dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// explicitly set timezone of input if needed
		dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		JPanel dialogo = new JPanel(new BorderLayout());
		dialogo.setBackground(Color.WHITE);
		dialogo.setBorder(new EmptyBorder(10, 10, 10, 10));

		dialogo.add(getCabecera(), BorderLayout.NORTH);
		dialogo.add(getCentral(), BorderLayout.CENTER);
		dialogo.add(getBotones(), BorderLayout.SOUTH);

		add(dialogo);
		setPreferredSize(new Dimension(430, 440));
		pack();
		setLocationRelativeTo(mapView);
		getLimpiar().doClick();

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				closing.actionPerformed(null);
			}
		});
	}

	private JPanel getCabecera() {
		JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT));
		cabecera.setOpaque(false);
		JLabel titulo = new JLabel("Consulta de Posiciones GPS en el Histórico");
		titulo.setFont(LogicConstants.deriveBoldFont(12.0f));
		titulo.setIcon(LogicConstants
				.getIcon("tittleventana_icon_consultahistorico"));
		cabecera.add(titulo);
		return cabecera;
	}

	private JPanel getCentral() {
		JPanel central = new JPanel(new FlowLayout());
		central.setOpaque(false);

		JPanel intervalo = getIntervalo();
		intervalo.setOpaque(false);
		intervalo.setPreferredSize(new Dimension(400, 100));
		central.add(intervalo);

		// JPanel consulta = getConsulta();
		// consulta.setOpaque(false);
		// consulta.setPreferredSize(new Dimension(400, 120));
		// central.add(consulta);

		JPanel elementos = getElementos();
		elementos.setOpaque(false);
		elementos.setPreferredSize(new Dimension(400, 210));
		central.add(elementos);

		JPanel ventanaMensaje = getVentanaMensaje();
		ventanaMensaje.setOpaque(false);
		ventanaMensaje.setPreferredSize(new Dimension(400, 30));
		central.add(ventanaMensaje);

		return central;
	}

	private JPanel getElementos() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panel.setOpaque(false);
		panel.setBorder(new TitledBorder("Elementos a Consultar"));

		JLabel jLabel = new JLabel("Recursos", SwingConstants.RIGHT);
		jLabel.setPreferredSize(ConsultaHistoricos.DIMENSION_LABEL);
		panel.add(jLabel);
		recursos = new JList(new DefaultListModel());
		recursos.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
				"selectAll");
		recursos.getActionMap().put("selectAll", new AbstractAction() {

			private static final long serialVersionUID = -5515338515763292526L;

			@Override
			public void actionPerformed(ActionEvent e) {
				recursos.setSelectionInterval(0,
						recursos.getModel().getSize() - 1);
			}
		});
		recursos.addListSelectionListener(listSelectionListener);
		final JScrollPane jScrollPaneR = new JScrollPane(recursos,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneR.getViewport().setPreferredSize(DIMENSION_JLIST);
		panel.add(jScrollPaneR);

		jLabel = new JLabel("Incidencias", SwingConstants.RIGHT);
		jLabel.setPreferredSize(ConsultaHistoricos.DIMENSION_LABEL);
		panel.add(jLabel);
		incidencias = new JList(new DefaultListModel());
		incidencias.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
				"selectAll");
		incidencias.getActionMap().put("selectAll", new AbstractAction() {

			private static final long serialVersionUID = -5515338515763292526L;

			@Override
			public void actionPerformed(ActionEvent e) {
				incidencias.setSelectionInterval(0, incidencias.getModel()
						.getSize() - 1);
			}
		});
		incidencias.addListSelectionListener(listSelectionListener);
		final JScrollPane jScrollPaneI = new JScrollPane(incidencias,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneI.getViewport().setPreferredSize(DIMENSION_JLIST);
		panel.setPreferredSize(DIMENSION_2JLIST);
		panel.add(jScrollPaneI);
		return panel;
	}

	// private JPanel getConsulta() {
	// JPanel panel = new JPanel(new FlowLayout());
	// panel.setOpaque(false);
	// panel.setBorder(new TitledBorder("Zona de Consulta"));
	// JLabel jLabel = new JLabel("Zona", SwingConstants.RIGHT);
	// jLabel.setPreferredSize(ConsultaHistoricos.DIMENSION_LABEL);
	// panel.add(jLabel);
	// zona = new JList(new DefaultListModel());
	// for (Zona z : ZonaConsultas.getAllZonas()) {
	// ((DefaultListModel) zona.getModel()).addElement(z);
	// }
	// zona.addListSelectionListener(listSelectionListener);
	// zona.addListSelectionListener(new ListSelectionListener() {
	//
	// @Override
	// public void valueChanged(ListSelectionEvent e) {
	// updateRecursos();
	//
	// }
	// });
	// zona.getInputMap().put(
	// KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
	// "selectAll");
	// zona.getActionMap().put("selectAll", new AbstractAction() {
	//
	// private static final long serialVersionUID = -5515338515763292526L;
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// zona.setSelectionInterval(0, zona.getModel().getSize() - 1);
	// }
	// });
	//
	// final JScrollPane jScrollPane = new JScrollPane(zona,
	// JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	// JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	// jScrollPane.getViewport().setPreferredSize(DIMENSION_JLIST);
	// panel.add(jScrollPane);
	// panel.setPreferredSize(DIMENSION_JLIST);
	// return panel;
	// }

	private JPanel getIntervalo() {
		JPanel intervalo = new JPanel(new GridBagLayout());
		intervalo.setOpaque(false);
		intervalo.setBorder(new TitledBorder("Intervalo Temporal de Consulta"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		intervalo.add(new JLabel("Fecha/Hora Inicial"), gbc);
		gbc.gridx++;
		calendarini = new JCalendarCombo();
		calendarini.addActionListener(actionListener);
		calendarini.addDateListener(dateListener);
		intervalo.add(calendarini, gbc);
		gbc.gridx++;
		final Calendar instance = Calendar.getInstance();
		instance.set(Calendar.HOUR, 0);
		instance.set(Calendar.MINUTE, 0);
		instance.set(Calendar.SECOND, 0);
		horaini = initializeSpinner(instance);
		intervalo.add(horaini, gbc);

		gbc.gridx = 0;
		gbc.gridy++;

		intervalo.add(new JLabel("Fecha/Hora Final"), gbc);
		gbc.gridx++;
		calendarfin = new JCalendarCombo();
		calendarfin.addActionListener(actionListener);
		calendarfin.addDateListener(dateListener);
		intervalo.add(calendarfin, gbc);
		gbc.gridx++;
		horafin = initializeSpinner(instance);
		intervalo.add(horafin, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.EAST;
		soloUltimas = new JCheckBox();
		soloUltimas.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateRecursos();
			}
		});
		soloUltimas.setOpaque(false);
		soloUltimas.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean enabled = !soloUltimas.isSelected();
				calendarini.setEnabled(enabled);
				calendarfin.setEnabled(enabled);
				horaini.setEnabled(enabled);
				horafin.setEnabled(enabled);
			}
		});
		intervalo.add(soloUltimas, gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.WEST;
		intervalo.add(new JLabel("Consultar Sólo Las Últimas Posiciones"), gbc);

		return intervalo;
	}

	private JSpinner initializeSpinner(final Calendar instance) {
		JSpinner res = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor startEditor = new JSpinner.DateEditor(res,
				"HH:mm:ss");
		res.setEditor(startEditor);
		res.setValue(instance.getTime());
		res.addChangeListener(changeListener);
		return res;
	}

	private JPanel getVentanaMensaje() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		mensaje.setOpaque(false);
		mensaje.setBorder(null);
		mensaje.setForeground(Color.RED);
		mensaje.setEditable(false);
		panel.add(mensaje, BorderLayout.CENTER);
		return panel;
	}

	private JPanel getBotones() {
		JPanel boton = new JPanel(new FlowLayout());
		boton.setOpaque(false);
		consultar = getConsultar();
		boton.add(consultar);
		limpiar = getLimpiar();
		boton.add(limpiar);
		cargando = new JLabel(LogicConstants.getIcon("48x48_transparente"));
		boton.add(cargando);
		boton.add(new JLabel(LogicConstants.getIcon("48x48_transparente")));
		JButton cancelar = getCancelar();
		boton.add(cancelar);
		return boton;
	}

	private JButton getCancelar() {
		JButton jButton = new JButton(LogicConstants.getIcon("button_cancel"));
		jButton.addActionListener(closing);
		jButton.setText("Cancelar");
		return jButton;
	}

	private JButton getLimpiar() {
		JButton jButton = new JButton(LogicConstants.getIcon("button_limpiar"));
		jButton.setText("Limpiar");
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() throws Exception {
						clearRecursos();
						clearIncidencias();
						cleanLayers();
						if (visorHistorico != null) {
							visorHistorico.updateControls();
						}

						return null;
					}

					@Override
					protected void done() {

						HistoryMapViewer.enableSaveGpx(false);
						HistoryMapViewer.getResultadoHistoricos().setSelected(
								false);

						recursos.setSelectedIndex(-1);
						incidencias.setSelectedIndex(-1);
						// zona.setSelectedIndex(-1);

						final Calendar instance = Calendar.getInstance();
						calendarini.setDate(instance.getTime());
						calendarfin.setDate(instance.getTime());

						horafin.setValue(instance.getTime());
						instance.set(Calendar.HOUR_OF_DAY, 0);
						instance.set(Calendar.MINUTE, 0);
						instance.set(Calendar.SECOND, 0);
						horaini.setValue(instance.getTime());

						soloUltimas.setSelected(false);
						calendarini.setEnabled(true);
						calendarfin.setEnabled(true);
						horaini.setEnabled(true);
						horafin.setEnabled(true);

						consultar.setEnabled(false);
						limpiar.setEnabled(false);
						setError("");
					}
				};
				sw.execute();

			}
		});
		return jButton;
	}

	private JButton getConsultar() {
		final JButton jButton = new JButton(
				LogicConstants.getIcon("historico_button_realizarconsulta"));
		jButton.setText("Consultar");
		jButton.setEnabled(false);
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cargando.setIcon(LogicConstants.getIcon("anim_calculando"));
				cargando.repaint();
				limpiar.setEnabled(false);
				jButton.setEnabled(false);
				consulta = new SwingWorker<Layer, Object>() {

					@Override
					protected Layer doInBackground() throws Exception {

						publish(new Object[0]);

						List<String> idRecursos = new ArrayList<String>();
						List<Long> idZonas = new ArrayList<Long>();
						for (Object o : recursos.getSelectedValues()) {
							if (o instanceof Recurso) {
								idRecursos.add(((Recurso) o).getIdentificador());
							} else {
								idRecursos.add(o.toString());
							}
						}
						// for (Object o : zona.getSelectedValues()) {
						// if (o instanceof Zona) {
						// idZonas.add(((Zona) o).getId());
						// }
						// }

						List<String> idIncidencias = new ArrayList<String>();
						for (Object o : incidencias.getSelectedValues()) {
							if (o instanceof Incidencia) {
								idIncidencias.add(((Incidencia) o).getId()
										.toString());
							} else {
								idIncidencias.add(o.toString());
							}
						}
						if (soloUltimas.isSelected()) {
							if (idRecursos.size() > 0) {
								getUltimasPosiciones(Authentication.getUsuario()
										.getNombreUsuario(), idRecursos,
										idZonas);
							}
							if (idIncidencias.size() > 0) {
								getPosicionesIncidencias(Authentication
										.getUsuario().getNombreUsuario(),
										idIncidencias, idZonas);
							}
						} else {
							if (idRecursos.size() > 0) {
								getRutas(Authentication.getUsuario()
										.getNombreUsuario(), idRecursos,
										getFechaIni(), getFechaFin());
							}
							if (idIncidencias.size() > 0) {
								getPosicionesIncidencias(Authentication
										.getUsuario().getNombreUsuario(),
										idIncidencias, idZonas);
							}
						}
						return null;
					}

					@Override
					protected void process(List<Object> chunks) {
						super.process(chunks);
						cleanLayers();
						cargando.setIcon(LogicConstants
								.getIcon("anim_calculando"));
						cargando.repaint();
					}

					@Override
					protected void done() {
						HistoryMapViewer.getResultadoHistoricos().setSelected(
								true);
						cargando.setIcon(LogicConstants
								.getIcon("48x48_transparente"));
						cargando.repaint();
					}
				};
				consulta.execute();
			}
		});
		return jButton;
	}

	private File getUltimasPosiciones(String nombreUsuario,
			List<String> idRecursos, List<Long> idZonas) {
		Posicion[] posiciones = new Posicion[0];
		clearIncidencias();
		clearRecursos();
		try {
			ServiceStub client = WSProvider.getServiceClient();
			GetUltimasPosiciones param = new GetUltimasPosiciones();
			param.setIdRecursos(idRecursos.toArray(new String[0]));
			param.setNombreUsuario(nombreUsuario);
			long[] zonas = new long[idZonas.size()];
			for (int i = 0; i < idZonas.size(); i++) {
				zonas[i] = idZonas.get(i);
			}

			param.setZonas(zonas);
			GetUltimasPosicionesResponse res = client
					.getUltimasPosiciones(param);
			posiciones = res.get_return();
		} catch (RemoteException e) {
			log.error("Error al conectar al servicio web", e);
			setError("Error al conectar al servicio web");
		}

		File file = writeTo(posiciones, false, true);
		cargarGpx(file, "ultimas_posiciones");
		for (Posicion p : posiciones) {
			Recurso r = new Recurso();
			r.setId(null);
			r.setNombre(p.getIdentificador());
			HistoricoGPS gps = new HistoricoGPS();
			gps.setPosX(p.getX());
			gps.setPosY(p.getY());
			gps.setRecurso(r.toString());
			r.setHistoricoGps(gps);
			addRecurso(r);
		}
		if (this.visorHistorico != null) {
			visorHistorico.updateControls();
		}
		return file;
	}

	private GpxLayer cargarGpx(File file, String name) {
		GpxImporter importer = new GpxImporter();
		GpxLayer layer = null;
		try {
			if (!importer.acceptFile(file)) {
				new IOException("Gpx inaccesible.");
			}
			importer.importData(file);
			layer = new MyGpxLayer(importer.getLastData(),
					file.getAbsolutePath(), true, this.mapView);
			layer.name = name;
			addCapa(layer);
		} catch (Throwable t) {
			log.error("Error al cargar la capa", t);
			setError("Error al cargar la capa");
		}
		return layer;
	}

	private void addCapa(final Layer layer) {
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				layer.visible = true;
				if (!Main.pref.putColor("layer " + layer.name,
						Color.decode(LogicConstants.getNextColor()))) {
					log.error("Error al asignar el color");
				}
				return null;
			}

			@Override
			protected void done() {
				super.done();
				mapView.addLayer(layer, false);
				capas.add(layer);
				limpiar.setEnabled(true);

				HistoryMapViewer.enableSaveGpx(true);
			}
		};
		sw.execute();
	}

	private synchronized void cleanLayers() {
		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				return null;
			}

			@Override
			protected void done() {
				for (Layer layer : capas) {
					mapView.removeLayer(layer);
				}
				capas.clear();
				mapView.repaint();
				LogicConstants.resetColor();
			}
		};
		sw.execute();
	}

	/**
	 * Escribe las posiciones en formato gpx y devuelve el descriptor del
	 * fichero. Devuelve null si hubo algun error.
	 * 
	 * @param posiciones
	 * @return
	 */
	private File writeTo(Posicion[] posiciones, boolean linea, boolean showTime) {
		File file = null;
		try {
			file = File.createTempFile("Historico", ".gpx");
			GpxData data = new GpxData();
			ImmutableGpxTrack track = new ImmutableGpxTrack(
					new LinkedList<Collection<WayPoint>>(),
					new LinkedHashMap<String, Object>());
			LinkedList<WayPoint> linkedList = new LinkedList<WayPoint>();
			if (posiciones != null) {
				for (Posicion pos : posiciones) {
					WayPoint way = buildWay(pos, linea, showTime);
					linkedList.add(way);
				}
			} else {
				setError("No se encontraron posiciones");
			}
			if (linea) {
				track.trackSegs.add(linkedList);
				data.tracks.add(track);
			}
			data.waypoints = linkedList;
			OutputStream out = new FileOutputStream(file);
			GpxWriter writer = new GpxWriter(out);
			writer.write(data);
			out.close();
		} catch (UnsupportedEncodingException e) {
			log.error("Error al escribir el gpx en el fichero temporal.", e);
			setError("Error al escribir el gpx en el fichero temporal.");
		} catch (IOException e) {
			log.error("No pude crear un fichero temporal", e);
			setError("No pude crear un fichero temporal");
		}
		return file;
	}

	private WayPoint buildWay(Posicion pos, boolean linea, boolean showTime) {
		WayPoint way = new WayPoint(new LatLon(pos.getY(), pos.getX()));
		way.attr.put("time",
				dateFormat.format(pos.getMarcaTemporal().getTime()));
		String name = pos.getIdentificador();

		if (showTime) {
			name += " "
					+ DateFormat.getDateTimeInstance(DateFormat.SHORT,
							DateFormat.MEDIUM, LOCALE).format(
							pos.getMarcaTemporal().getTime());
		}
		way.attr.put("name", name);

		way.setGarminCommentTime("time");
		way.drawLine = linea;
		return way;
	}

	private File getPosicionesIncidencias(String nombreUsuario,
			List<String> idIncidencias, List<Long> idZonas) {
		Posicion[] posiciones = new Posicion[0];
		clearRecursos();
		clearIncidencias();
		try {
			ServiceStub client = WSProvider.getServiceClient();
			GetPosicionesIncidencias param = new GetPosicionesIncidencias();
			param.setIdIncidencias(idIncidencias.toArray(new String[0]));
			param.setNombreUsuario(nombreUsuario);
			GetPosicionesIncidenciasResponse res = client
					.getPosicionesIncidencias(param);

			posiciones = res.get_return();

			for (Posicion p : posiciones) {
				addIncidencia(p.getIdentificador());
			}
		} catch (RemoteException e) {
			log.error("Error al conectar al servicio web", e);
			setError("Error al conectar al servicio web");
		}
		if (this.visorHistorico != null) {
			visorHistorico.updateControls();
		}
		File file = writeTo(posiciones, false, false);
		cargarGpx(file, "incidencias");
		return file;
	}

	private void getRutas(String nombreUsuario, List<String> idRecurso,
			Calendar ini, Calendar fin) {
		clearRecursos();
		clearIncidencias();
		try {
			ServiceStub client = WSProvider.getServiceClient();
			GetRutasRecursos param = new GetRutasRecursos();
			param.setNombreUsuario(nombreUsuario);
			param.setFechaInicio(ini);
			param.setFechaFin(fin);
			param.setListaRecursos(idRecurso.toArray(new String[0]));
			GetRutasRecursosResponse res = client.getRutasRecursos(param);
			String[] gpx = res.get_return();
			int i = 0;
			if (gpx != null) {
				for (String s : gpx) {
					if (s == null) {
						log.error("El recurso " + idRecurso.get(i++)
								+ " ha devuelto un camino nulo.");
					} else {
						try {
							final File file = File.createTempFile("historico",
									"gpx");
							FileWriter fstream = new FileWriter(file);
							BufferedWriter out = new BufferedWriter(fstream);
							out.write(s);
							out.close();
							if (i < idRecurso.size()) {
								addRecurso(cargarGpx(file, idRecurso.get(i++)));
							} else {
								cargarGpx(file,
										(new Long(System.currentTimeMillis()))
												.toString());
								log.error("Nos hemos quedado sin nombres de recursos. "
										+ "Esto tiene que ser porque se han devuelto mas gpx de "
										+ "los que hemos pedido. La capa se mostrara, pero hay que "
										+ "revisar la peticion al servicio web.");
							}
						} catch (Throwable t) {
							log.error("Error al cargar gpx", t);
						}
					}
				}
			}

		} catch (RemoteException e) {
			log.error("Error al conectar al servicio web", e);
			setError("Error al conectar al servicio web");
		}
		if (this.visorHistorico != null) {
			visorHistorico.updateControls();
		}
	}

	protected String[] getRecursosEnPeriodo(String nombreUsuario, Calendar ini,
			Calendar fin, long[] zonas) {
		try {
			ServiceStub client = WSProvider.getServiceClient();
			GetRecursosEnPeriodo param = new GetRecursosEnPeriodo();
			param.setFechaInicio(ini);
			param.setFechaFinal(fin);
			param.setZonas(zonas);
			param.setNombreUsuario(nombreUsuario);
			GetRecursosEnPeriodoResponse res = client
					.getRecursosEnPeriodo(param);
			String[] rec = res.get_return();
			if (rec != null) {
				log.trace("Nos ha devuelto " + rec.length + " recursos.");
				return rec;
			}
		} catch (RemoteException e) {
			log.error("Error al conectar al servicio web", e);
			setError("Error al conectar al servicio web");
		}
		return new String[] {};
	}

	protected Incidencia[] getIncidenciasEnPeriodo(String nombreUsuario,
			Calendar ini, Calendar fin, long[] zonas) {
		List<Incidencia> res = new ArrayList<Incidencia>();
		try {
			ServiceStub client = WSProvider.getServiceClient();
			GetIncidenciasAbiertasEnPeriodo param = new GetIncidenciasAbiertasEnPeriodo();
			param.setFechaInicio(ini);
			param.setFechaFinal(fin);
			param.setNombreUsuario(nombreUsuario);
			param.setZonas(zonas);
			IncidenciaWS[] rec = client.getIncidenciasAbiertasEnPeriodo(param)
					.get_return();
			if (rec != null) {
				for (IncidenciaWS in : rec) {
					try {
						Incidencia i = new Incidencia();
						BeanUtils.copyProperties(i, in);
						res.add(i);
					} catch (Throwable e) {
						log.error("Error al transformar de WS a objeto", e);
					}
				}
			}

		} catch (RemoteException e) {
			log.error("Error al conectar al servicio web", e);
			setError("Error al conectar al servicio web");
		}
		return res.toArray(new Incidencia[0]);
	}

	private synchronized void updateRecursos() {
		if (updateRecursos != null) {
			updateRecursos.cancel(true);
		}

		updateRecursos = new SwingWorker<String, String>() {

			private String[] recursosEnPeriodos = new String[0];
			private Incidencia[] incidenciasEnPeriodos = new Incidencia[0];

			@Override
			protected String doInBackground() throws Exception {
				cargando.setIcon(LogicConstants.getIcon("anim_calculando"));
				Calendar ini = null;
				Calendar fin = null;
				if (!soloUltimas.isSelected()) {
					ini = getFechaIni();
					fin = getFechaFin();
				}

				if (ini != null && fin != null && ini.after(fin)) {
					return null;
				}

				List<Long> idZonas = new ArrayList<Long>();
				// for (Object o : zona.getSelectedValues()) {
				// if (o instanceof Zona) {
				// idZonas.add(((Zona) o).getId());
				// }
				// }

				long[] zonas = new long[idZonas.size()];
				for (int i = 0; i < zonas.length; i++) {
					zonas[i] = idZonas.get(i);
				}

				recursosEnPeriodos = getRecursosEnPeriodo(Authentication
						.getUsuario().getNombreUsuario(), ini, fin, zonas);

				incidenciasEnPeriodos = getIncidenciasEnPeriodo(Authentication
						.getUsuario().getNombreUsuario(), ini, fin, zonas);

				return null;
			}

			@Override
			protected void done() {
				synchronized (recursos) {
					log.trace("Cargamos los recursos");
					((DefaultListModel) recursos.getModel())
							.removeAllElements();
					for (String recurso : recursosEnPeriodos) {
						log.trace("Mostramos en la ventana de consulta "
								+ recurso);
						((DefaultListModel) recursos.getModel())
								.addElement(recurso);
					}
				}
				synchronized (incidencias) {
					((DefaultListModel) incidencias.getModel())
							.removeAllElements();
					for (Incidencia i : incidenciasEnPeriodos) {
						log.trace("Mostramos en la ventana de consulta " + i);
						((DefaultListModel) incidencias.getModel())
								.addElement(i);
					}
				}
				limpiar.setEnabled(true);
				recursos.repaint();
				incidencias.repaint();
				cargando.setIcon(LogicConstants.getIcon("48x48_transparente"));
				cargando.repaint();
				updateRecursos = null;
			}
		};
		updateRecursos.execute();

	}

	private Calendar getFechaFin() {
		Calendar tmp2 = Calendar.getInstance();
		Calendar fin = calendarfin.getCalendar();
		tmp2.setTime((Date) horafin.getValue());
		fin.set(Calendar.HOUR_OF_DAY, tmp2.get(Calendar.HOUR_OF_DAY));
		fin.set(Calendar.MINUTE, tmp2.get(Calendar.MINUTE));
		fin.set(Calendar.SECOND, tmp2.get(Calendar.SECOND));
		return fin;
	}

	private Calendar getFechaIni() {
		Calendar tmp = Calendar.getInstance();
		Calendar ini = calendarini.getCalendar();
		tmp.setTime((Date) horaini.getValue());
		ini.set(Calendar.HOUR_OF_DAY, tmp.get(Calendar.HOUR_OF_DAY));
		ini.set(Calendar.MINUTE, tmp.get(Calendar.MINUTE));
		ini.set(Calendar.SECOND, tmp.get(Calendar.SECOND));
		return ini;
	}

	private Thread limpiarError = null;
	private final DateListener dateListener = new DateListener() {

		@Override
		public void dateChanged(DateEvent arg0) {
			updateRecursos();
		}
	};

	public synchronized void setError(final String e) {
		if (limpiarError != null && limpiarError.isAlive()) {
			limpiarError.interrupt();
		}

		SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				return null;
			}

			@Override
			protected void done() {
				mensaje.setText(e);
				mensaje.repaint();
				cargando.setIcon(LogicConstants.getIcon("48x48_transparente"));
			}
		};

		sw.execute();

		limpiarError = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000 * LogicConstants.getInt(
							"TIMEOUT_ERROR_WEBSERVICE", 15));

					SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {

						@Override
						protected Object doInBackground() throws Exception {
							mensaje.setText("");
							return null;
						}
					};

					sw.execute();
				} catch (InterruptedException e) {
				}

			}
		};
		limpiarError.start();
	}
}
