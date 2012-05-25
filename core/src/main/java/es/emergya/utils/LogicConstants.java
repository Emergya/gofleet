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
/**
 * 
 * 
 */
package es.emergya.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.context.GoClassLoader;

/**
 * @author marias
 */
public class LogicConstants {
	private static Properties p;
	private static final Log LOG = LogFactory.getLog(LogicConstants.class);
	private static final String constantsProperties = "/conf/constants.properties";

	static {
		LogicConstants.p = new Properties();

		try {
			InputStream is = GoClassLoader.getGoClassLoader()
					.getResourceAsStream(LogicConstants.constantsProperties);
			if (is == null)
				is = LogicConstants.class
						.getResourceAsStream(LogicConstants.constantsProperties);
			p.load(is);
		} catch (FileNotFoundException fnf) {
			LOG.error("Fichero de configuarcion conf/contants.properties no encontrado"
					+ fnf);
		} catch (IOException e) {
			LOG.error(e);
		} catch (Exception e) {
			LOG.error("NO se pudo cargar el fichero de propiedades", e);
		}
	}

	public static final int MAX_SIZE_MESSAGE = getInt("MAX_SIZE_MESSAGE", 150);
	public static final String FIELD_SEPARATOR = get("FIELD_SEPARATOR", "|");
	public static final int SRID = getInt("SRID", 4326);
	public static final Integer[] MENSAJES_SIN_TIPO = new Integer[] { 2, 3 };

	public static boolean isNumeric(String i) {
		return (i != null && StringUtils.isNumeric(i) && !i.trim().equals(""));
	}

	public static String getGenericString(final String s) {
		if (s == null)
			return null;
		return s.replace('*', '%');
	}

	public static String get(String string, String defaultValue) {
		return LogicConstants.p.getProperty(string, defaultValue);
	}

	public static int getInt(String string, Integer def) {
		String s = LogicConstants.p.getProperty(string);
		if (!isNumeric(s))
			return def;
		return Integer.valueOf(s);
	}

}
