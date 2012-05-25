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
package es.emergya.ui.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;

import es.emergya.cliente.constants.LogicConstants;

public class Message {
	private final long serialVersionUID = -6691454170753397497L;
	private Stack<String> colaMensajes;
	private Font font = null;
	private Color color;
	private Date fecha;
	final Log log = LogFactory.getLog(this.getClass());

	@GoWired
	public BasicWindow window;

	public void setWindow(BasicWindow window) {
		this.window = window;
	}

	/** Message font size. */
	private final float MESSAGE_FONT_SIZE = 20.0f;

	{
		colaMensajes = new java.util.Stack<String>();
		color = new Color(248, 216, 152);

		this.fecha = Calendar.getInstance().getTime();

		font = LogicConstants.deriveBoldFont(MESSAGE_FONT_SIZE);

	}

	private void inicializar(final String texto) {
		log.trace("inicializar(" + texto + ")");
		final Message message_ = this;
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				log.trace("Sacamos un nuevo mensaje: " + texto);
				JDialog frame = new JDialog(window.getFrame(), true);
				frame.setUndecorated(true);
				frame.getContentPane().setBackground(Color.WHITE);
				frame.setLocation(150, window.getHeight() - 140);
				frame.setSize(new Dimension(window.getWidth() - 160, 130));
				frame.setName("Incoming Message");
				frame.setBackground(Color.WHITE);
				frame.getRootPane().setBorder(
						new MatteBorder(4, 4, 4, 4, color));

				frame.setLayout(new BorderLayout());
				if (font != null)
					frame.setFont(font);

				JLabel icon = new JLabel(new ImageIcon(this.getClass()
						.getResource("/images/button-ok.png")));
				icon.setToolTipText("Cerrar");

				icon.removeMouseListener(null);
				icon.addMouseListener(new Cerrar(frame, message_));

				JLabel text = new JLabel(texto);
				text.setBackground(Color.WHITE);
				text.setForeground(Color.BLACK);
				frame.add(text, BorderLayout.WEST);
				frame.add(icon, BorderLayout.EAST);

				frame.setVisible(true);
			}
		});
	}

	public void setMessage(String message) {

		if (message == null || message.trim().equals(""))
			return;
		log.trace("setMessage(" + message + ")");

		colaMensajes.add("<html>" + message + "</html>");

		getNext();
	}

	public void updateAll() {
		try {
			// List<Avisos> avisos = AvisosHome.getNotRead(this.fecha);
			// for (Avisos a : avisos) {
			// setMessage(a.getTexto());
			// if (a.getHora().after(this.fecha))
			// this.fecha = a.getHora();
			// }

			// getNext();
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public void changeColor(Color color) {
		log.trace("changeColor()");
		this.color = color;
	}

	protected void getNext() {
		log.trace("getNext()");

		if (!window.isAuthenticated())
			return;

		try {
			String s = null;
			while (!colaMensajes.empty() && s == null) {
				s = colaMensajes.remove(0);
				if (s != null) {
					log.trace("Mostramos " + s);
					inicializar(s);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}

	}

}

class Cerrar extends MouseAdapter {

	private JDialog frame = null;
	private Message m = null;
	
	Cerrar(JDialog frame, Message m) {
		super();
		this.frame = frame;
		this.m = m;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 1) {
			if (this.frame != null)
				this.frame.dispose();
			this.m.getNext();
		}
	}
}