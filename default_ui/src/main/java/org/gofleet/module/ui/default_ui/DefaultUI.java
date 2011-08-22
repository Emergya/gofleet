package org.gofleet.module.ui.default_ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import es.emergya.tools.ExtensionClassLoader;
import es.emergya.utils.LogicConstants;

/**
 * Default UI for gofleet client.
 * 
 * @author marias@emergya.com
 * 
 */
public class DefaultUI extends org.gofleet.module.UI {

	protected void loadUI() {

		boldFont = getFont(null, LogicConstants.get("BOLD_FONT",
				"/fuentes/LiberationSerif-Bold.ttf"));
		lightFont = getFont(null, LogicConstants.get("DEFAULT_FONT",
				"/fuentes/LiberationSerif-Regular.ttf"));

		UIManager.put("swing.boldMetal", Boolean.FALSE); //$NON-NLS-1$

		final Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, deriveLightFont(11.0f));
			}

		}
		UIManager.put("TableHeader.font", deriveBoldFont(10f));
		UIManager.put("TabbedPane.font", deriveLightFont(9f));
	}

	private static Map<Integer, Font> fonts_bold = new HashMap<Integer, Font>();
	private static Map<Integer, Font> fonts_light = new HashMap<Integer, Font>();
	public static Font boldFont;
	public static Font lightFont;

	private Font deriveBoldFont(Float f) {
		Integer i = f.intValue();
		Font font = null;
		synchronized (fonts_bold) {
			font = fonts_bold.get(i);
		}
		if (font == null) {
			font = boldFont.deriveFont(f);
			synchronized (fonts_bold) {
				fonts_bold.put(i, font);
			}
		}
		return font;
	}

	private Font deriveLightFont(Float f) {
		Integer i = f.intValue();
		Font font = null;
		synchronized (fonts_light) {
			font = fonts_light.get(i);
		}
		if (font == null) {
			font = lightFont.deriveFont(f);
			synchronized (fonts_light) {
				fonts_light.put(i, font);
			}
		}
		return font;
	}

	private Font getFont(Integer type, String font) {
		Font f = null;
		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();

			List<File> fonts = ecl.findFiles(font);

			if (fonts.size() > 0)
				f = Font.createFont(Font.TRUETYPE_FONT, fonts.get(0));
		} catch (Throwable e) {
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			String[] fontNames = ge.getAvailableFontFamilyNames();

			for (String s : fontNames) {
				if (s.equals(font)) {
					f = Font.decode(s);
					if (type != null) {
						f = f.deriveFont(type);
					}
					break;
				}
			}

			if (f == null) {
				f = Font.decode(fontNames[0]);
				if (type != null) {
					f = f.deriveFont(type);
				}
			}

			if (f == null) {
				throw new NullPointerException("There is no font available: "
						+ font);
			}
		}
		return f;
	}
}
