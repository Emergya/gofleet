package wmsplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import es.emergya.bbdd.bean.Capa;
import es.emergya.bbdd.bean.CapaInformacion;

public class ParseCapabilities extends DefaultHandler {
	static final Log log = LogFactory.getLog(ParseCapabilities.class);

	private List<Capa> capas = new ArrayList<Capa>();
	private READING definition = READING.UNKNOWN;
	private Stack<String> name = new Stack<String>();
	private Stack<Boolean> png = new Stack<Boolean>();
	private Stack<Boolean> EPSG4326 = new Stack<Boolean>();
	private Stack<Boolean> opaque = new Stack<Boolean>();
	private Stack<String> estilo = new Stack<String>();
	private Integer profundidad = 0;
	private String service = "";
	private HashMap<String, Boolean> isTransparent = new HashMap<String, Boolean>();

	private CapaInformacion ci = null;

	public ParseCapabilities(CapaInformacion ci) {
		log.trace("ParseCapabilities");
		this.ci = ci;
	}

	public String getService() {
		return this.service;
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		log.trace("Salimos De " + arg2);
		super.endElement(arg0, arg1, arg2);

		if (StringUtils.equalsIgnoreCase("Name", arg2)
				&& this.definition == READING.SERVICE_NAME) {
			this.definition = READING.UNKNOWN;
		} else if (StringUtils.equalsIgnoreCase("Style", arg2)
				&& this.definition == READING.STYLE) {
			this.definition = READING.LAYER;
		} else if (StringUtils.equalsIgnoreCase("Layer", arg2)) {
			final Boolean epsg = getBoolean(this.EPSG4326);
			final Boolean opaque = getBoolean(this.opaque);
			final Boolean image = getBoolean(this.png);
			final String nombre = getString(this.name);
			final String estilo = getString(this.estilo);

			log.trace("EPSG:" + epsg + " OPAQUE:" + opaque + " PNG/IMAGE:"
					+ image + " NOMBRE:" + nombre + " ESTILO:" + estilo);

			if (epsg && image && nombre != null) {
				boolean use = false;
				for (Capa capilla : ci.getCapas()) {
					if (capilla.getNombre().equals(nombre))
						use = true;
				}

				if (!use) {
					Capa c = new Capa();
					c.setNombre(nombre);
					c.setEstilo(estilo);
					c.setCapaInformacion(ci);
					this.capas.add(c);
					if (opaque) {
						this.isTransparent.put(nombre, false);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JOptionPane
										.showMessageDialog(
												null,
												"La capa "
														+ nombre
														+ " no es transparente. Si se usa sobre otras capas, las tapará.");
							}
						});
					} else
						this.isTransparent.put(nombre, true);
					log.debug("Capa : " + nombre);
				}

			} else {
				String error = "";
				if (nombre == null)
					error += "<li>No tiene nombre</li>";
				// if (opaque)
				// error += "<li>Es opaca</li>";
				if (!image)
					error += "<li>No existe en formato PNG</li>";
				if (!epsg)
					error += "<li>No existe en la proyección adecuada (EPSG:4326)</li>";
				final String cadena = "<html><p>La capa " + nombre
						+ " no puede ser considerada porque: </p><ul>" + error
						+ "</ul></html>";
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, cadena);
					}
				});
			}

			profundidad--;
			if (profundidad > 0)
				this.definition = READING.LAYER;
			else
				this.definition = READING.UNKNOWN;
			if (profundidad < 0)
				profundidad = 0;
		}
	}

	private String getString(Stack<String> stack) {
		String pop = null;
		while (!stack.isEmpty() && stack.size() >= profundidad)
			pop = stack.pop();

		return pop;
	}

	private Boolean getBoolean(Stack<Boolean> stack) {
		boolean res = false;
		while (!stack.isEmpty() && stack.size() >= profundidad)
			res = res || stack.pop();
		res = res || stack.contains(true);
		return res;
	}

	@Override
	public void error(SAXParseException arg0) throws SAXException {
		super.error(arg0);
		log.error("Error al parsear el WMS", arg0);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		log.trace("startDocument()");
	}

	@Override
	public void warning(SAXParseException arg0) throws SAXException {
		super.warning(arg0);
		log.warn("Warn en el WMS", arg0);
	}

	@Override
	public void fatalError(SAXParseException arg0) throws SAXException {
		super.fatalError(arg0);
		log.fatal("Error fatal en el WMS", arg0);
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		final String s = new String(ch, start, length);
		log.trace(s);
		if (StringUtils.equalsIgnoreCase("image/png", s))
			this.png.push(true);
		else if (this.definition == READING.CRS
				&& StringUtils.contains(s, "EPSG:4326"))
			this.EPSG4326.push(true);
		else if (this.definition == READING.LAYER_NAME) {
			this.name.push(s);
			this.definition = READING.LAYER;
		} else if (this.definition == READING.STYLE_NAME) {
			this.estilo.push(s);
			this.definition = READING.STYLE;
		} else if (this.definition == READING.SERVICE_NAME) {
			this.service = s;
			this.definition = READING.UNKNOWN;
		}
	}

	@Override
	public void startElement(String uri, String name, String qName,
			Attributes atts) throws SAXException {
		log.trace("Entramos en " + qName);
		super.startElement(uri, name, qName, atts);

		if (this.definition == READING.UNKNOWN
				&& StringUtils.equalsIgnoreCase(qName, "Name"))
			this.definition = READING.SERVICE_NAME;
		else if (StringUtils.equalsIgnoreCase(qName, "layer")) {
			final String value = atts.getValue("opaque");
			if (value != null) {
				final String opaque = value.toString();
				if ((StringUtils.equalsIgnoreCase(opaque, "1") || StringUtils
						.equalsIgnoreCase(opaque, "true")))
					this.opaque.push(true);
				else
					this.opaque.push(false);
			}
			this.definition = READING.LAYER;
			this.profundidad++;

		} else if (this.definition == READING.LAYER) {
			if (StringUtils.equalsIgnoreCase(qName, "name"))
				this.definition = READING.LAYER_NAME;
			else if (StringUtils.equalsIgnoreCase(qName, "BoundingBox")) {
				String s = atts.getValue("CRS");
				if (s == null)
					s = atts.getValue("SRS");
				if (StringUtils.contains(s, "EPSG:4326"))
					this.EPSG4326.push(true);
			} else if (StringUtils.equalsIgnoreCase(qName, "style"))
				this.definition = READING.STYLE;
			else if (StringUtils.equalsIgnoreCase(qName, "crs"))
				this.definition = READING.CRS;

		} else if (this.definition == READING.STYLE) {
			if (StringUtils.equalsIgnoreCase(qName, "name"))
				this.definition = READING.STYLE_NAME;
		}
	}

	public List<Capa> getCapas() {
		return capas;
	}

	enum READING {
		UNKNOWN, LAYER, LAYER_NAME, STYLE, STYLE_NAME, CRS, SERVICE_NAME
	}

	public HashMap<String, Boolean> getIsTransparent() {
		return isTransparent;
	}
}