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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.MemoryTileCache;

import es.emergya.actions.Authentication;
import es.emergya.ui.base.plugins.DetachedTab;

/**
 * Log out the application.
 * 
 * @author marias
 * 
 */
public class ExitHandler implements ActionListener {
	
	List<ActionListener> listeners = new ArrayList<ActionListener>();
	/**
	 * Log out the application.
	 * 
	 * @param e
	 *            event.
	 */
	public void actionPerformed(final ActionEvent e) {

		Authentication.logOut();
		
		for(ActionListener al : listeners){
			al.actionPerformed(e);
		}

//		ConsultaHistoricos.close();
//		SaveGPXDialog.close();
//		MemoryTileCache.resetGlobalSize();

		for (Frame f : Frame.getFrames())
			f.dispose();
		LoginWindow.showLogin();
		for (DetachedTab t : BasicWindow.getPluginContainer().getPane()
				.getDetachedTabs())
			t.dispose();

		for (Frame f : JFrame.getFrames())
			if (!((f instanceof LoginWindow) || (f.equals(BasicWindow
					.getFrame())))) {
				f.dispose();
			}

		BasicWindow.getFrame().setVisible(false);
	}
}