package reportTool;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.emergya.constants.LogicConstants;
import es.emergya.consultas.GenericQueries;
import es.emergya.tools.ExtensionClassLoader;

/**
 * 
 * @author mlopez@emergya.com
 * @author marias@emergya.com
 * 
 */
public class ReportService implements IReportService {
	public static final Log log = LogFactory.getLog(ReportService.class);

	private static Set<String> REPORTTYPES;
	private final static Integer fetchSize = LogicConstants.getInt(
			"reportingTool.fetchSize", "5");

	static {
		String reportFile = LogicConstants.get("reportingTool.file",
				"reportTypes.properties");
		Properties reportTypesKeys = new Properties();

		try {
			ExtensionClassLoader ecl = new ExtensionClassLoader();
			InputStream is = ecl.getResourceAsStream(reportFile);
			if (is == null)
				is = LogicConstants.class.getResourceAsStream(reportFile);
			reportTypesKeys.load(is);
		} catch (Throwable t) {
			log.debug("No '" + reportFile + "' file found. Using default. ", t);
			reportTypesKeys.put("Incidencia", "");
		} finally {
			REPORTTYPES = reportTypesKeys.stringPropertyNames();
		}
	}

	// forbidden columns when making a DB query
	private static final String[] BLACKLIST = { "serialVersionUID", "test" };

	// allowed classes for column data
	@SuppressWarnings("rawtypes")
	private static final Class[] ALLOWEDCLASSES = { String.class,
			Integer.class, Long.class, Short.class, };

	/**
	 * @see IReportService#getReportType
	 */
	public Set<String> getReportType() {
		return REPORTTYPES;
	}

	/**
	 * 
	 * @see IReportService.reportTool.IService#getFields
	 */
	@Override
	public ReportField[] getFields(String reportType)
			throws ReportToolException {
		try {
			List<ReportField> rfields = null;
			Field[] farray = null;

			farray = Class.forName(reportType).getDeclaredFields();

			rfields = new ArrayList<ReportField>(farray.length);
			for (Field f : farray) {
				if (f.getName() != null && f.getType() != null
						&& !ArrayUtils.contains(BLACKLIST, f.getName())
						&& ArrayUtils.contains(ALLOWEDCLASSES, f.getType())) {
					rfields.add(new ReportField(f.getName(), f.getType()
							.toString()));
				}
			}
			return rfields.toArray(new ReportField[0]);
		} catch (Throwable t) {
			throw new ReportToolException("Unknown Error retrieving fields", t);
		}
	}

	/**
	 * @see IReportService#getReport
	 */
	@Override
	public Byte[][][] getReport(String reportType, Integer max,
			ReportRestriction[] restrictions, String[] fields) {

		log.debug("getReport(" + reportType + ", " + max + ",...)");

		if (log.isTraceEnabled()) {
			if (restrictions != null) {
				for (ReportRestriction r : restrictions) {
					log.trace("restriction: " + r);
				}
			}
			if (fields != null) {
				for (String r : fields) {
					log.trace("field: " + r);
				}
			}
		}

		// if no field selected, it will show all possible fields
		if (fields == null || fields.length == 0) {
			ReportField[] reportFields = getFields(reportType);
			fields = new String[reportFields.length];
			for (int i = 0; i < reportFields.length; i++) {
				fields[i] = reportFields[i].getField();
			}
		}

		// Prepare comma-separated fields string
		String commaFields = "";

		int i = 0;
		if (fields != null && fields.length > 0) {
			for (; i < fields.length; i++) {
				if (fields[i] != null)
					break;
			}

			if (i < fields.length)
				commaFields = fields[i++];

			for (; i < fields.length; i++) {
				if (fields[i] != null)
					commaFields += "," + fields[i];
			}
			log.trace("fields(" + fields.length + "): " + commaFields);
		}

		/* PREPARE WHERE CLAUSE */
		String whereClause = "";
		if (restrictions == null) {
			restrictions = new ReportRestriction[0];
		}
		for (ReportRestriction r : restrictions) {
			// Nulls to empty(""). Clean conflictive characters
			String conjunction = StringUtils.isBlank(whereClause) ? "WHERE "
					: " AND ";
			String field = escapeConflictiveCharacters(r.getField());
			String operator = escapeConflictiveCharacters(r.getOperator());
			String value = escapeConflictiveCharacters(r.getRestriction());

			// Filter invalid restrictions
			if (StringUtils.isBlank(field) || StringUtils.isBlank(operator)) {
				log.debug("Restriction invalid -> field or operator null: " + r);
				continue;

			} else if (StringUtils.isBlank(value)
					&& !"is null".equals(operator)) {
				log.debug("Restriction invalid -> no value for (" + operator
						+ "): " + r);
				continue;

			} else if (StringUtils.isNotBlank(value)
					&& "is null".equals(operator)) {
				log.debug("Restriction invalid -> is null should not have value: "
						+ r);
				continue;
			}

			if ("like".equals(operator)) {
				value = "'" + value + "'";
			}

			whereClause += conjunction + field + " " + operator + " " + value;
		}

		/* SET AND LAUNCH QUERY */
		String orderColumn = "X_" + reportType; // TODO
		String hql = "SELECT " + commaFields + " FROM " + reportType + " "
				+ whereClause + " ORDER BY " + orderColumn + " DESC";
		log.debug("Query:\n---------" + hql);

		List<LinkedList<Object>> res = GenericQueries.runHQL(hql, max,
				fetchSize);

		try {
			Byte[][][] resultado = new Byte[res.size()][][];
			i = 0;
			for (LinkedList<Object> row : res) {
				resultado[i] = new Byte[row.size()][];
				int k = 0;
				for (Object o : row) {
					if (o == null)
						resultado[i][k++] = extractBytes("null");
					else
						resultado[i][k++] = extractBytes(o.toString());
				}
				i++;
			}
			log.trace("ResultSet length " + resultado.length);

			return resultado;
		} catch (Throwable e) {
			log.error("Error getting ", e);
		}

		return new Byte[0][0][];
	}

	private static String escapeConflictiveCharacters(String string) {
		if (string == null)
			return "";

		// remove single quotes, double quotes, semicolons
		return string.replaceAll("['\";]", "");
	}

	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static Byte[] extractBytes(final String string)
			throws UnsupportedEncodingException {
		if (string == null) {
			return null;
		}
		/*
		 * Workaround for Axis2 bug. We need to send a first known character for
		 * the Byte[] not to fail when sent. We'll use a space.
		 */
		byte[] bytes = (" " + string).getBytes("ISO-8859-6");
		Byte[] resultado = new Byte[bytes.length];
		int i = 0;
		for (byte b : bytes) {
			resultado[i] = b;
			i++;
		}
		return resultado;
	}

}
