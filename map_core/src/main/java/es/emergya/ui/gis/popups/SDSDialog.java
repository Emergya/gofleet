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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

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

public class SDSDialog extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2956099114032301963L;
	static int maxChars = LogicConstants.getInt("MAX_CARACTERES_POR_SDS");
	private Outbox bandejaSalida;
	Recurso destino;
	Icon iconEnviando;
	Icon iconTransparente;
	JLabel notification;
	JLabel progressIcon;
	JTextArea sds;
	JButton send, cancel;
	private static final Log LOG = LogFactory.getLog(SDSDialog.class);

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

	public SDSDialog(Recurso r) {
		super();
		setAlwaysOnTop(true);
		setResizable(false);
		iconTransparente = LogicConstants.getIcon("48x48_transparente");
		iconEnviando = LogicConstants.getIcon("anim_enviando");
		destino = r;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				cancel.doClick();
			}
		});

		// setPreferredSize(new Dimension(400, 150));
		setTitle(i18n.getString("window.sds.titleBar") + " "
				+ r.getIdentificador());
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
		final JLabel titleLabel = new JLabel(
				i18n.getString("window.sds.title"),
				LogicConstants.getIcon("tittleventana_icon_enviarsds"),
				JLabel.LEFT);

		titleLabel.setFont(LogicConstants.deriveBoldFont(12f));
		title.add(titleLabel);
		title.setOpaque(false);
		base.add(title);

		// Espacio para el mensaje
		sds = new JTextArea(7, 40);
		sds.setLineWrap(true);

		final JScrollPane sdsp = new JScrollPane(sds);

		sdsp.setOpaque(false);
		sdsp.setBorder(new TitledBorder(BorderFactory
				.createLineBorder(Color.BLACK), i18n.getString("Admin.message")
				+ "\t (0/" + maxChars + ")"));
		sds.setDocument(new PlainDocument() {
			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (this.getLength() + str.length() <= maxChars) {
					super.insertString(offs, str, a);
				}
			}
		});
		sds.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateChars(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateChars(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateChars(e);
			}

			private void updateChars(DocumentEvent e) {
				((TitledBorder) sdsp.getBorder()).setTitle(i18n
						.getString("Admin.message")
						+ "\t ("
						+ sds.getText().length() + "/" + maxChars + ")");
				sdsp.repaint();
				send.setEnabled(!sds.getText().isEmpty());
				notification.setForeground(Color.WHITE);
				notification.setText("PLACEHOLDER");
			}
		});
		base.add(sdsp);

		// Area para mensajes
		JPanel notificationArea = new JPanel();

		notificationArea.setOpaque(false);
		notification = new JLabel("TEXT");
		notification.setForeground(Color.WHITE);
		notificationArea.add(notification);
		base.add(notificationArea);

		JPanel buttons = new JPanel();

		buttons.setOpaque(false);
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		send = new JButton(i18n.getString("Buttons.send"),
				LogicConstants.getIcon("ventanacontextual_button_enviarsds"));
		send.addActionListener(this);
		send.setEnabled(false);
		buttons.add(send);
		buttons.add(Box.createHorizontalGlue());
		progressIcon = new JLabel(iconTransparente);
		buttons.add(progressIcon);
		buttons.add(Box.createHorizontalGlue());
		cancel = new JButton(i18n.getString("Buttons.cancel"),
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

			private void deleteErrorMessage() {
				SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						if (bandejaSalida != null) {
							MessageGenerator.remove(bandejaSalida.getId());
						}

						bandejaSalida = null;

						return null;
					}

					@Override
					protected void done() {
						super.done();
						SDSDialog.this.sds.setText("");
						SDSDialog.this.sds.setEnabled(true);
						SDSDialog.this.sds.repaint();
						SDSDialog.this.progressIcon.setIcon(iconTransparente);
						SDSDialog.this.progressIcon.repaint();
						SDSDialog.this.notification.setText("");
						SDSDialog.this.notification.repaint();
					}
				};

				sw.execute();
			}
		});
	}

	public Recurso getRecurso() {
		return destino;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(i18n.getString("Buttons.send"))) {
			bandejaSalida = send();

			if (bandejaSalida == null) {
				return;
			}

			notification.setForeground(Color.WHITE);
			progressIcon.setIcon(iconEnviando);
			send.setEnabled(false);
			sds.setEnabled(false);

			SendActionListener listener = new SendActionListener(bandejaSalida);
			final Timer t = new Timer(1000, listener);

			listener.setTimer(t);
			t.start();
		} else if (e.getActionCommand()
				.equals(i18n.getString("Buttons.cancel"))) {
			if (bandejaSalida != null) {
				MessageGenerator.remove(bandejaSalida.getId());
			}

			bandejaSalida = null;
			dispose();
		}
	}

	private Outbox send() {
		TipoMensaje tmensaje = TipoMensajeConsultas
				.getTipoByCode(es.emergya.utils.LogicConstants
						.getInt("SDS", 31));

		if (destino.getDispositivo() == null) {
			notification.setText(i18n.getString("progress.message.nodevice"));
			notification.setForeground(Color.RED);

			return null;
		}

		try {
			return MessageGenerator.sendMessage(tmensaje.getCodigo(),
					tmensaje.getTipoTetra(), tmensaje.getPrioridad(),
					sds.getText(), destino.getDispositivo().toString());
		} catch (MessageGeneratingException ex) {
			notification.setText(i18n.getString("progress.message.fail"));
			notification.setForeground(Color.RED);

			return null;
		}
	}

	class SendActionListener implements ActionListener {
		private Timer t = null;
		private Outbox b;

		public SendActionListener(Outbox b) {
			this.b = b;
		}

		public void setTimer(Timer timer) {
			this.t = timer;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (bandejaSalida == null) {
				progressIcon.setIcon(iconTransparente);
				send.setEnabled(!sds.getText().isEmpty());
				sds.setEnabled(true);
				t.stop();
			} else if (!MessageGenerator.messageExists(bandejaSalida.getId())) {
				progressIcon.setIcon(iconTransparente);
				notification.setText(i18n
						.getString("window.sds.message.sended"));
				notification.setForeground(Color.RED);
				bandejaSalida = null;
				send.setEnabled(!sds.getText().isEmpty());
				sds.setEnabled(true);
				t.stop();
			}
		}
	}
}
