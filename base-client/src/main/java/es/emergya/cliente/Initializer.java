/*
 * Copyright (C) 2012, Emergya (http://www.emergya.com)
 *
 * @author <a href="mailto:marias@emergya.com">María Arias de Reyna</a>
 *
 * This file is part of GoFleetLS
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
package es.emergya.cliente;

import org.gofleet.context.GoWired;
import org.gofleet.internacionalization.I18n;

import es.emergya.ui.base.BasicWindow;
import es.emergya.ui.base.LoginWindow;
import es.emergya.ui.base.plugins.PluginContainer;

/**
 * Initializes the GUI.
 * 
 */
class Initializer implements Runnable {

	protected static PluginContainer container = new PluginContainer();
	@GoWired
	public BasicWindow window;

	public void setWindow(BasicWindow window) {
		this.window = window;
	}

	@GoWired
	public I18n i18n;

	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}

	@GoWired
	public LoginWindow loginWindow;

	public void setLoginWindow(LoginWindow loginWindow) {
		this.loginWindow = loginWindow;
	}
	/**
	 * @param loader
	 */
	Initializer() {
	}

	/** Initializtes the GUI. */
	@Override
	public void run() {
		try {
			if (Loader._this == null)
				throw new NullPointerException("We have no Main");

			Loader._this.configureUI();
			Loader._this.createAndShowGUI();
			Loader._this.loadModules(container);
			Loader._this.loadJobs();

			this.loginWindow.showLogin();
		} catch (Throwable t) {
			Loader.LOG.error("Fallo al inicializar la aplicacion", t);
			Loader.showError(t);
		}
	}
}