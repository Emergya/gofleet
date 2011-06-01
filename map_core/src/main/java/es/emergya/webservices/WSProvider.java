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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.emergya.webservices;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.cliente.constants.LogicConstants;

/**
 * 
 * @author jlrodriguez
 * @author marias
 */
public class WSProvider {

	private static final Log log = LogFactory.getLog(WSProvider.class);
	public static ConfigurationContext context = initContext();
	protected static Integer numClients = 0;
	protected static Integer maxNumClients = LogicConstants.getInt(
			"maxNumClients", 10);

	private static final ConfigurationContext initContext() {
		ConfigurationContext localContext = null;
		try {
			localContext = ConfigurationContextFactory
					.createDefaultConfigurationContext();
		} catch (Exception ex) {
			log.error("Could not create Axis2 context", ex);
			throw new RuntimeException(ex);
		}

		return localContext;
	}

	public static final ServiceStub getServiceClient() {
		ServiceStub stub = null;

		if (numClients > maxNumClients)
			clearClients();
		numClients++;

		try {
			Options options = new Options();
			options.setTimeOutInMilliSeconds(LogicConstants.TIMEOUT * 1000);
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT,
					LogicConstants.TIMEOUT * 1000);
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT,
					LogicConstants.TIMEOUT * 1000);
			options.setTimeOutInMilliSeconds(LogicConstants.TIMEOUT * 1000);
			options.setProperty(
					org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT,
					LogicConstants.TIMEOUT * 1000);

			log.trace("Obteniendo cliente para la dirección "
					+ LogicConstants.URL_WEBSERVICE);
			stub = new ServiceStub(context, LogicConstants.URL_WEBSERVICE);
			stub._getServiceClient().setOverrideOptions(options);
		} catch (AxisFault ex) {
			Logger.getLogger(WSProvider.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return stub;
	}

	private static void clearClients() {
		context = initContext();
		numClients = 0;
	}
}
