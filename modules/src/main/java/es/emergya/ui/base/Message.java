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

import es.emergya.cliente.constants.LogicConstants;

public class Message {
	private static final long serialVersionUID = -6691454170753397497L;
	private static Stack<String> colaMensajes;
	private static Font font = null;
	private static Color color;
	private static Date fecha;
	static final Log log = LogFactory.getLog(Message.class);

	/** Message font size. */
	private static final float MESSAGE_FONT_SIZE = 20.0f;

	static {
		colaMensajes = new java.util.Stack<String>();
		color = new Color(248, 216, 152);

		Message.fecha = Calendar.getInstance().getTime();

		font = LogicConstants.deriveBoldFont(MESSAGE_FONT_SIZE);

	}

	private static void inicializar(final String texto) {
		log.trace("inicializar(" + texto + ")");
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				log.trace("Sacamos un nuevo mensaje: " + texto);
				JDialog frame = new JDialog(BasicWindow.getFrame(), true);
				frame.setUndecorated(true);
				frame.getContentPane().setBackground(Color.WHITE);
				frame.setLocation(150, BasicWindow.getHeight() - 140);
				frame.setSize(new Dimension(BasicWindow.getWidth() - 160, 130));
				frame.setName("Incoming Message");
				frame.setBackground(Color.WHITE);
				frame.getRootPane().setBorder(
						new MatteBorder(4, 4, 4, 4, color));

				frame.setLayout(new BorderLayout());
				if (font != null)
					frame.setFont(font);

				JLabel icon = new JLabel(new ImageIcon(Message.class
						.getResource("/images/button-ok.png")));
				icon.setToolTipText("Cerrar");

				icon.removeMouseListener(null);
				icon.addMouseListener(new Cerrar(frame));

				JLabel text = new JLabel(texto);
				text.setBackground(Color.WHITE);
				text.setForeground(Color.BLACK);
				frame.add(text, BorderLayout.WEST);
				frame.add(icon, BorderLayout.EAST);

				frame.setVisible(true);
			}
		});
	}

	public static void setMessage(String message) {

		if (message == null || message.trim().equals(""))
			return;
		log.trace("setMessage(" + message + ")");

		colaMensajes.add("<html>" + message + "</html>");

		getNext();
	}

	public static void updateAll() {
		try {
			// List<Avisos> avisos = AvisosHome.getNotRead(Message.fecha);
			// for (Avisos a : avisos) {
			// setMessage(a.getTexto());
			// if (a.getHora().after(Message.fecha))
			// Message.fecha = a.getHora();
			// }

			// getNext();
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public static void changeColor(Color color) {
		log.trace("changeColor()");
		Message.color = color;
	}

	protected static void getNext() {
		log.trace("getNext()");

		if (!BasicWindow.isAuthenticated())
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

	Cerrar(JDialog frame) {
		super();
		this.frame = frame;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 1) {
			Message.log.trace("mouseClicked");
			if (this.frame != null)
				this.frame.dispose();
			Message.getNext();
		}
	}
}