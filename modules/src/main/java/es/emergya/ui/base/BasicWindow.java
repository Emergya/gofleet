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

import es.emergya.actions.Authentication;
import es.emergya.bbdd.bean.Usuario;
import es.emergya.cliente.constants.LogicConstants;
import es.emergya.i18n.Internacionalization;
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
	private static JFrame frame;
	private static Cursor busyCursor;
	private static Cursor defaultCursor;
	private static Cursor handCursor;

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(BasicWindow.class);
	/** Plugin container @see {@link AbstractPlugin} **/
	private static PluginContainer container;
	// /** Default frame width. */
	// private static final int DEFAULT_WIDTH = 800; // 800 para las OBU
	// /** Default frame height. */
	// private static final int DEFAULT_HEIGHT = 650; // 650 para las OBU
	/** Default font size. */
	private static final float DEFAULT_FONT_SIZE = 18.0f;
	private static final Image ICON_IMAGE = getImageIcon("/images/iconoEF.png");

	private static Image getImageIcon(String uri) {
		final URL resource = BasicWindow.class.getResource(uri);
		if (resource != null) {
			final ImageIcon imageIcon = new ImageIcon(resource);
			if (imageIcon != null)
				return imageIcon.getImage();
		}
		LOG.error("No se pudo encontrar el icono " + uri);
		return null;
	}

	/**
	 * Build a basic BasicWindow.
	 */
	private BasicWindow() {
		super();
	}

	static {
		inicializar();
	}

	/**
	 * Initialize the window with default values.
	 */
	private static void inicializar() {
		BasicWindow.frame = new JFrame(
				Internacionalization.getString("BasicWindow.title")); //$NON-NLS-1$
		BasicWindow.getFrame().setBackground(Color.WHITE);
		BasicWindow.getFrame().setIconImage(ICON_IMAGE); //$NON-NLS-1$
		BasicWindow.getFrame().addWindowListener(
				new RemoveClientesConectadosListener());

		BasicWindow.getFrame().setMinimumSize(new Dimension(900, 600));
		BasicWindow.getFrame().addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				BasicWindow.resize();
			}

		});

		BasicWindow.busyCursor = new Cursor(Cursor.WAIT_CURSOR);
		BasicWindow.defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = getImageIcon("/images/hand.gif");
		if (image != null)
			BasicWindow.handCursor = toolkit.createCustomCursor(image,
					new Point(0, 0), "hand"); //$NON-NLS-1$
	}

	/**
	 * Draws the frame.
	 */
	public static void draw() {

		BasicWindow.getFrame().getContentPane().removeAll();

		BasicWindow.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BasicWindow.getFrame().setFont(
				LogicConstants.deriveLightFont(BasicWindow.DEFAULT_FONT_SIZE));

		// Añadimos los plugins;
		if (BasicWindow.getPluginContainer() == null
				|| BasicWindow.getPluginContainer().getPlugins().size() == 0)
			LOG.error("no hay plugins");
		else {
			BasicWindow.getPluginContainer().setup();
			BasicWindow.getFrame().getContentPane()
					.add(BasicWindow.getPluginContainer());
			BasicWindow.getPluginContainer().maximizeAllDetachedTabs();
		}
		BasicWindow.getFrame().pack();
		BasicWindow.getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Display the window.
		BasicWindow.getFrame().setVisible(true);
	}

	/**
	 * Calculate and resize all the components of the window.
	 */
	public static void resize() {

		if (BasicWindow.getFrame() == null)
			return;

		// Recorremos los plugins propios para redibujarlos con el nuevo
		// tamaño.
		BasicWindow.getPluginContainer().resize();

	}

	/**
	 * Builds a new Window with this PluginContainer.
	 * 
	 * @param pc
	 */
	public static void setPluginContainer(PluginContainer pc) {
		BasicWindow.container = pc;
	}

	/**
	 * Deprecated, usa mejor Autenticacion.getUsuario()
	 * 
	 * @see Authentication#getUsuario()
	 * @return
	 */
	@Deprecated
	public static Usuario getUsuario() {
		return Authentication.getUsuario();
	}

	/**
	 * Gives a new color to the alert (the color of the main tab)
	 */
	public static void recolorAlert() {
		if (BasicWindow.getPluginContainer() != null)
			Message.changeColor(BasicWindow.getPluginContainer()
					.getBackgroundColor());
	}

	/**
	 * 
	 * @return actual width
	 */
	public static int getWidth() {
		// if (BasicWindow.frame == null)
		// return BasicWindow.DEFAULT_WIDTH;
		return BasicWindow.getFrame().getWidth();
	}

	/**
	 * 
	 * @return actual height
	 */
	public static int getHeight() {
		// if (BasicWindow.frame == null)
		// return BasicWindow.DEFAULT_HEIGHT;
		return BasicWindow.getFrame().getHeight();
	}

	/**
	 * 
	 * @return the plugin container
	 */
	public static PluginContainer getPluginContainer() {
		return BasicWindow.container;
	}

	public static JFrame getFrame() {
		return BasicWindow.frame;
	}

	/** Set a wait cursor. */
	public static void showAsBusy() {
		if (BasicWindow.getFrame() != null)
			BasicWindow.getFrame().setCursor(BasicWindow.busyCursor);
	}

	/** Set a hand cursor. */
	public static void showAsHand() {
		if (BasicWindow.getFrame() != null)
			BasicWindow.getFrame().setCursor(BasicWindow.handCursor);
	}

	/** Set a default cursor */
	public static void showIdle() {
		if (BasicWindow.getFrame() != null)
			BasicWindow.getFrame().setCursor(BasicWindow.defaultCursor);
	}

	public static void logOut() {
		ExitHandler eh = new ExitHandler();
		eh.actionPerformed(null);
	}

	/**
	 * @see Authentication#isAuthenticated()
	 * @deprecated use isAuthenticated()
	 * @return
	 */
	@Deprecated
	public static boolean isAutenticated() {
		return Authentication.isAuthenticated();
	}

	public static boolean isAuthenticated() {
		return Authentication.isAuthenticated();
	}

	public static Image getIconImage() {
		return ICON_IMAGE;
	}

//	public static void showOnMap(EastNorth ea, int map) {
//		LOG.info("showOnMap(" + ea + ")");
//		for (AbstractPlugin ap : container.getPlugins()) {
//			if (ap instanceof IMapViewer) {
//				if (ap.getOrder() == map) {
//					NavigatableComponent mv = ((IMapViewer) ap).getMapView();
//					mv.zoomTo(ea, mv.getScale());
//					break;
//				}
//			}
//		}
//	}

	// public static void showOnMap(LatLon ll, int map) {
	// showOnMap(Main.proj.latlon2eastNorth(ll), map);
	// }

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
