/*
 * Copyright (C) 2010, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jlrodriguez@emergya.es">Juan Luís Rodríguez</a>
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
 * @author <a href="mailto:aromero@emergya.es">Alejandro Romero Casado</a>
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
package es.emergya.cliente.constants;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.tools.ExtensionClassLoader;


/**
 * Contains the constants for the logic layer
 * 
 * @author aromero
 */
public class LogicConstants {

	private static Properties p;
	private static final Log LOG = LogFactory.getLog(LogicConstants.class);
	private static final String constantsProperties = "/conf/constants.properties";
	private static final HashMap<String, Icon> iconCache = new HashMap<String, Icon>();
	// Style
	public static final Font boldFont;
	public static final Font lightFont;
	private static Map<Integer, Font> fonts_bold = new HashMap<Integer, Font>();
	private static Map<Integer, Font> fonts_light = new HashMap<Integer, Font>();

	// Colores de capas
	private static List<String> colores = new ArrayList<String>();
	private static int color = 0;

	static {
		getIcon("image_missing");
		LogicConstants.p = new Properties();

		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			InputStream is = ecl
					.getResourceAsStream(LogicConstants.constantsProperties);
			if (is == null) {
				is = LogicConstants.class
						.getResourceAsStream(LogicConstants.constantsProperties);
			}
			LogicConstants.p.load(is);
		} catch (FileNotFoundException fnf) {
			LogicConstants.LOG
					.error("Fichero de configuarcion conf/contants.properties no encontrado",
							fnf);
		} catch (IOException e) {
			LogicConstants.LOG.error(e);
		} catch (Exception e) {
			LogicConstants.LOG.error(
					"NO se pudo cargar el fichero de propiedades", e);
		}

		for (int i = 1; i < 11; i++) {
			final String color_ = LogicConstants.get("GPS_HISTORY" + i);
			if (color_ != null)
				colores.add(color_);
		}
		boldFont = LogicConstants.getFont(null, LogicConstants.get("BOLD_FONT",
				"/fuentes/LiberationSerif-Bold.ttf"));
		lightFont = LogicConstants.getFont(null, LogicConstants.get(
				"DEFAULT_FONT", "/fuentes/LiberationSerif-Regular.ttf"));
	}

	public static String getNextColor() {
		return colores.get((color++ % colores.size())).toString();
	}

	/**
	 * Hace que la secuencia de selección de colores devueltos por
	 * getNextColor() se reinicie y el siguiente color elegido sea el primero
	 * del fichero de propiedades.
	 */
	public static void resetColor() {
		color = 0;

	}

	private static Font getFont(Integer type, String font) {
		Font f;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT,
					LogicConstants.class.getResourceAsStream(font));
		} catch (Exception e) {
			LogicConstants.LOG.error("No se pudo cargar el font bold", e);
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			String[] fontNames = ge.getAvailableFontFamilyNames();
			if (fontNames.length > 0) {
				f = Font.decode(fontNames[0]);
				if (type != null) {
					f = f.deriveFont(type);
				}
			} else {
				throw new NullPointerException("There is no font available: "
						+ font);
			}
		}
		return f;
	}

	/**
	 * Latitud inicial del mapa (en grados)
	 */
	public static double LATITUD_INICIAL = getDouble("LATITUD_INICIAL", 37.38);
	/**
	 * Longitud inicial dle mapa (en grados).
	 */
	public static double LONGITUD_INICIAL = getDouble("LONGITUD_INICIAL", -6);
	/** Zoom inicial. */
	public static int NIVEL_ZOOM_INICIAL = getInt("NIVEL_ZOOM_INICIAL", 15);

	// Other messages constants
	public static final int MAX_SIZE_MESSAGE = 150;
	public static final String FIELD_SEPARATOR = "|";
	public static final String FIELD_SEPARATOR_FORMS = "&";
	public static final String VALUE_SEPARATOR_FORMS = "@";
	public static final String DIRECTORIO_ICONOS_FLOTAS = LogicConstants
			.get("DIRECTORIO_ICONOS_FLOTAS");
	// WEB SERVICE
	public static final String URL_WEBSERVICE = LogicConstants
			.get("URL_WEBSERVICE");
	/** Axis2 repository location. */
	public static final String AXIS2_REPOSITORY = get("AXIS2_REPOSITORY");
	// Formato coordenadas
	public static final String COORD_UTM = "UTM";
	/**
	 * Número de minutos que tienen que pasar desde la última recepción de su
	 * posición para que desaparezca del AVL.
	 */
	public static final int AVL_TIMEOUT = LogicConstants.getInt("AVL_TIMEOUT",
			15);
	public static final String ZONA_HORARIA = get("ZONA_HORARIA",
			"Europe/Madrid");
	public static int TIMEOUT = LogicConstants.getInt("WS_TIMEOUT", 30);
	public static String PLANTILLA_LLAMADA_TETRA = get("PLANTILLA_LLAMADA_TETRA");
	public static String TIPO_LLAMADA_TETRA = get("TIPO_LLAMADA_TETRA");
	public static int PUERTO_LLAMADA_TETRA = getInt("PUERTO_LLAMADA_TETRA",
			33333);
	public static String SERVIDOR_LLAMADA_TETRA = get("SERVIDOR_LLAMADA_TETRA",
			"localhost");
	public static int LONGITUD_ISSI = getInt("LONGITUD_ISSI", 8);
	public static int MAX_STREET_AUTOCOMPLETE_RESULTS = getInt(
			"MAX_STREET_AUTOCOMPLETE_RESULTS", 10);

	public static String get(String string) {
		return LogicConstants.p.getProperty(string);
	}

	public static String get(String string, String defaultValue) {
		return LogicConstants.p.getProperty(string, defaultValue);
	}

	public static int getInt(String string) {
		String s = LogicConstants.p.getProperty(string);
		return Integer.valueOf(s);
	}

	// Style
	public static Font deriveBoldFont(Float f) {
		Integer i = f.intValue();
		Font font = fonts_bold.get(i);
		if (font == null) {
			font = LogicConstants.boldFont.deriveFont(f);
			fonts_bold.put(i, font);
		}
		return font;
	}

	public static Font deriveLightFont(Float f) {
		Integer i = f.intValue();
		Font font = fonts_light.get(i);
		if (font == null) {
			font = LogicConstants.lightFont.deriveFont(f);
			fonts_light.put(i, font);
		}
		return font;
	}

	public static Font getBoldFont() {
		return deriveBoldFont(12.0f);
	}

	public static Font getLightFont() {
		return deriveLightFont(10.0f);
	}

	public static String getTileUri() {
		return LogicConstants.get("TILE_URI");
	}

	public static String getWMSUrl() {
		return LogicConstants.get("WMS_URL");
	}

	public static int getMaxTileZoom() {
		return LogicConstants.getInt("MAX_TILE_ZOOM");
	}

	public static int getMinTileZoom() {
		return LogicConstants.getInt("MIN_TILE_ZOOM");
	}

	/**
	 * Gets the PNG icon with that name
	 * 
	 * @param name
	 *            Without the .png
	 * @return The icon
	 */
	public static Icon getIcon(String name) {
		if (name == null) {
			return null;
		}
		if (iconCache.containsKey(name)) {
			return iconCache.get(name);
		}
		String fullname = "/images/" + name + ".png";
		URL url = LogicConstants.class.getResource(fullname);
		try {
			if (url != null) {
				Icon i = new ImageIcon(url);
				iconCache.put(name, i);
				return i;
			} else {
				LOG.trace("No se pudo encontrar el archivo " + fullname
						+ ". Intentanamos buscar el .gif");
				fullname = "/images/" + name + ".gif";
				url = LogicConstants.class.getResource(fullname);
				if (url != null) {
					Icon i = new ImageIcon(url);
					iconCache.put(name, i);
					return i;
				} else {
					throw new NullPointerException(
							"no se pudo encontrar el icono " + fullname);
				}
			}
		} catch (Throwable t) {
			LOG.debug("Intentamos mostrar el icono por defecto, porque " + t);
			return iconCache.get("image_missing");
		}
	}

	public static boolean isNumeric(String i) {
		return (i != null && StringUtils.isNumeric(i) && !i.trim().equals(""));
	}

	public static int getInt(String string, int defaultValue) {
		String s = LogicConstants.p.getProperty(string,
				Integer.toString(defaultValue));
		return Integer.valueOf(s);
	}

	public static double getDouble(String string, double defaultValue) {
		String s = LogicConstants.p.getProperty(string,
				Double.toString(defaultValue));
		return Double.valueOf(s);
	}

	public static String[] getPriorities(boolean b) {
		LinkedList<String> res = new LinkedList<String>();
		if (b)
			res.add("");
		for (Integer i = LogicConstants.getInt("LOWEST_PRIORITY", 0); i < LogicConstants
				.getInt("HIGHEST_PRIORITY", 9); i++)
			res.add(i.toString());
		return res.toArray(new String[0]);
	}
}
