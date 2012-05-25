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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.actions.Authentication;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.ui.base.plugins.AbstractPlugin;
import es.emergya.ui.base.plugins.PluginContainer;

/**
 * Basic Window with exit button, update icon and company logo. It has also a
 * hidden message and a...
 * 
 * @author marias
 * 
 */
public class BasicWindow {
	private JFrame frame;
	private Cursor busyCursor;
	private Cursor defaultCursor;
	private Cursor handCursor;

	@GoWired
	public I18n i18n;

	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}
	
	@GoWired
	public Message message;

	public void setMessage(Message message) {
		this.message = message;
	}

	private final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(this.getClass());
	/** Plugin container @see {@link AbstractPlugin} **/
	private PluginContainer container;

	/** Default font size. */
	private final float DEFAULT_FONT_SIZE = 18.0f;
	private final Image ICON_IMAGE = getImageIcon("/images/iconoEF.png");

	private Image getImageIcon(String uri) {
		final URL resource = this.getClass().getResource(uri);
		if (resource != null) {
			final ImageIcon imageIcon = new ImageIcon(resource);
			if (imageIcon != null)
				return imageIcon.getImage();
		}
		LOG.error("No se pudo encontrar el icono " + uri);
		return null;
	}

	/**
	 * Build a basic
	 */
	public BasicWindow() {
		super();
		inicializar();
	}

	/**
	 * Initialize the window with default values.
	 */
	private void inicializar() {
		frame = new JFrame(i18n.getString("title")); //$NON-NLS-1$
		getFrame().setBackground(Color.WHITE);
		getFrame().setIconImage(ICON_IMAGE); //$NON-NLS-1$
		getFrame().addWindowListener(new RemoveClientesConectadosListener());

		getFrame().setMinimumSize(new Dimension(900, 600));
		getFrame().addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				resize();
			}

		});

		busyCursor = new Cursor(Cursor.WAIT_CURSOR);
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = getImageIcon("/images/hand.gif");
		if (image != null)
			handCursor = toolkit.createCustomCursor(image, new Point(0, 0),
					"hand"); //$NON-NLS-1$
	}

	/**
	 * Draws the frame.
	 */
	public void draw() {

		getFrame().getContentPane().removeAll();

		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrame().setFont(LogicConstants.deriveLightFont(DEFAULT_FONT_SIZE));

		// Añadimos los plugins;
		if (getPluginContainer() == null
				|| getPluginContainer().getPlugins().size() == 0)
			LOG.error("no hay plugins");
		else {
			getPluginContainer().setup();
			getFrame().getContentPane().add(getPluginContainer());
			getPluginContainer().maximizeAllDetachedTabs();
		}
		getFrame().pack();
		getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Display the window.
		getFrame().setVisible(true);
	}

	/**
	 * Calculate and resize all the components of the window.
	 */
	public void resize() {

		if (getFrame() == null)
			return;

		// Recorremos los plugins propios para redibujarlos con el nuevo
		// tamaño.
		getPluginContainer().resize();

	}

	/**
	 * Builds a new Window with this PluginContainer.
	 * 
	 * @param pc
	 */
	public void setPluginContainer(PluginContainer pc) {
		container = pc;
	}

	/**
	 * Gives a new color to the alert (the color of the main tab)
	 */
	public void recolorAlert() {
		if (getPluginContainer() != null)
			message.changeColor(getPluginContainer().getBackgroundColor());
	}

	/**
	 * 
	 * @return actual width
	 */
	public int getWidth() {
		// if (frame == null)
		// return DEFAULT_WIDTH;
		return getFrame().getWidth();
	}

	/**
	 * 
	 * @return actual height
	 */
	public int getHeight() {
		// if (frame == null)
		// return DEFAULT_HEIGHT;
		return getFrame().getHeight();
	}

	/**
	 * 
	 * @return the plugin container
	 */
	public PluginContainer getPluginContainer() {
		return container;
	}

	public JFrame getFrame() {
		return frame;
	}

	/** Set a wait cursor. */
	public void showAsBusy() {
		if (getFrame() != null)
			getFrame().setCursor(busyCursor);
	}

	/** Set a hand cursor. */
	public void showAsHand() {
		if (getFrame() != null)
			getFrame().setCursor(handCursor);
	}

	/** Set a default cursor */
	public void showIdle() {
		if (getFrame() != null)
			getFrame().setCursor(defaultCursor);
	}

	public void logOut() {
		ExitHandler eh = new ExitHandler();
		eh.actionPerformed(null);
	}

	public boolean isAuthenticated() {
		return Authentication.isAuthenticated();
	}

	public Image getIconImage() {
		return ICON_IMAGE;
	}

}

class RemoveClientesConectadosListener extends WindowAdapter {

	/**
	 * Elimina de ClientesConectados el cliente correspondiente a esta estación
	 * fija.
	 * 
	 * @param e
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		Authentication.logOut();
	}

}
