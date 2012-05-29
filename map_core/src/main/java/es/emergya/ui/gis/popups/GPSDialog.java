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

//~--- non-JDK imports --------------------------------------------------------

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoClassLoader;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.bbdd.bean.Outbox;
import es.emergya.bbdd.bean.Recurso;
import es.emergya.bbdd.bean.TipoMensaje;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.comunications.MessageGenerator;
import es.emergya.comunications.exceptions.MessageGeneratingException;
import es.emergya.consultas.TipoMensajeConsultas;
import es.emergya.ui.base.BasicWindow;

public class GPSDialog extends JFrame implements ActionListener {
	private static final long serialVersionUID = -3236008715561683102L;
	private static final org.apache.commons.logging.Log log = LogFactory
			.getLog(GPSDialog.class);
	private Outbox last_bandejaSalida = null;
	JButton actualizar;
	Icon iconEnviando;
	Icon iconTransparente;
	JLabel notification;
	JLabel progressIcon;
	Recurso target;
	private static final Log LOG = LogFactory.getLog(GPSDialog.class);

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
	public GPSDialog(Recurso r) {
		super();
		setAlwaysOnTop(true);
		setResizable(false);
		iconTransparente = LogicConstants.getIcon("48x48_transparente");
		iconEnviando = LogicConstants.getIcon("anim_actualizando");
		target = r;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 150));
		setTitle(i18n.getString("window.gps.titleBar") + " "
				+ target.getIdentificador());
		try {
			setIconImage(((BasicWindow) GoClassLoader.getGoClassLoader().load(
					BasicWindow.class)).getFrame().getIconImage());
		} catch (Throwable e) {
			LOG.error("There is no icon image", e);
		}

		JPanel base = new JPanel();

		base.setBackground(Color.WHITE);
		base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));

		// Icono del titulo
		JPanel title = new JPanel(new FlowLayout(FlowLayout.LEADING));
		final JLabel titleLabel = new JLabel(i18n.getString("window.gps.title"),
				LogicConstants.getIcon("tittleventana_icon_actualizargps"),
				JLabel.LEFT);

		titleLabel.setFont(LogicConstants.deriveBoldFont(12f));
		title.add(titleLabel);
		title.setOpaque(false);
		base.add(title);

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
		actualizar = new JButton(i18n.getString("window.gps.button.actualizar"),
				LogicConstants.getIcon("ventanacontextual_button_solicitargps"));
		actualizar.addActionListener(this);
		buttons.add(actualizar);
		buttons.add(Box.createHorizontalGlue());
		progressIcon = new JLabel(iconTransparente);
		buttons.add(progressIcon);
		buttons.add(Box.createHorizontalGlue());

		JButton cancel = new JButton(i18n.getString("Buttons.cancel"),
				LogicConstants.getIcon("button_cancel"));

		cancel.addActionListener(this);
		buttons.add(cancel);
		base.add(buttons);
		getContentPane().add(base);
		pack();

		int x;
		int y;
		Container myParent;
		try {
			myParent = ((BasicWindow) GoClassLoader.getGoClassLoader().load(
					BasicWindow.class)).getFrame().getContentPane();
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
		} catch (Throwable e1) {
			LOG.error("There is no basic window!", e1);
		}
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				deleteErrorMessage();
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				deleteErrorMessage();
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				deleteErrorMessage();
			}

			private void deleteErrorMessage() {
				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						if (last_bandejaSalida != null) {
							MessageGenerator.remove(last_bandejaSalida.getId());
						}

						return null;
					}

					@Override
					protected void done() {
						super.done();
						GPSDialog.this.progressIcon.setIcon(iconTransparente);
						GPSDialog.this.progressIcon.repaint();
						last_bandejaSalida = null;
						GPSDialog.this.notification.setText("");
						GPSDialog.this.notification.repaint();
					}
				};

				sw.execute();
			}
		});
	}

	public Recurso getRecurso() {
		return target;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(
				i18n.getString("window.gps.button.actualizar"))) {
			last_bandejaSalida = send();
			log.info("bandeja de salida: " + last_bandejaSalida);

			if (last_bandejaSalida == null) {
				return;
			}

			final JButton request = ((JButton) e.getSource());

			request.setEnabled(false);
			progressIcon.setIcon(iconEnviando);
			notification.setForeground(Color.WHITE);

			SolicitudGPSActionListener listener = new SolicitudGPSActionListener(
					last_bandejaSalida);
			Timer t = new Timer(1000, listener);

			listener.setTimer(t);
			t.start();
		}

		if (e.getActionCommand().equals(i18n.getString("Buttons.cancel"))) {
			if (last_bandejaSalida != null) {
				MessageGenerator.remove(last_bandejaSalida.getId());
			}

			last_bandejaSalida = null;
			dispose();
		}
	}

	private Outbox send() {
		TipoMensaje tmensaje = TipoMensajeConsultas
				.getTipoByCode(es.emergya.utils.LogicConstants
						.getInt("GPS", 32));

		if (target.getDispositivo() == null) {
			notification.setText(i18n.getString("progress.message.nodevice"));
			notification.setForeground(Color.RED);

			return null;
		}

		try {
			return MessageGenerator.sendMessage(tmensaje.getCodigo(), tmensaje
					.getTipoTetra(), tmensaje.getPrioridad(), LogicConstants
					.get("DATAGRAMA_SOLICITUD_GPS"), target.getDispositivo()
					.toString());
		} catch (MessageGeneratingException e) {
			notification.setText(i18n.getString("progress.message.fail"));
			notification.setForeground(Color.RED);

			return null;
		}
	}

	class SolicitudGPSActionListener implements ActionListener {
		private Outbox b;
		private Timer t;

		public SolicitudGPSActionListener(Outbox b) {
			this.b = b;
		}

		public void setTimer(Timer t) {
			this.t = t;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (last_bandejaSalida == null) {
				actualizar.setEnabled(true);
				progressIcon.setIcon(iconTransparente);
				t.stop();
			} else if (!MessageGenerator.messageExists(b.getId())) {
				progressIcon.setIcon(iconTransparente);
				notification.setText(i18n.getString("progress.updating"));
				notification.setForeground(Color.RED);
				actualizar.setEnabled(true);
				last_bandejaSalida = null;

				if (t != null) {
					t.stop();
				}
			}
		}
	}
}
