package reportTool;

import java.util.Set;

public interface IReportService {
	/**
	 * Returns an array of the different report types.
	 * 
	 * @return
	 */
	public abstract Set<String> getReportType();

	/**
	 * Given a report type from {@link #getReportType()}, it returns the
	 * different fields which can be viewed on the Report (fields of the
	 * database table).
	 * 
	 * @param reportType
	 * @return
	 */
	public abstract IField[] getFields(String reportType)
			throws ReportToolException;

	/**
	 * Given a reportType, a set of restrictions and the fields to show, it
	 * returns the data of the reportTool following this hql:
	 * 
	 * select fields from reportType where restrictions;
	 * 
	 * The data will come as an array of rows. The order of the fields on each
	 * row will be the same order as in the fields parameter. All values will be
	 * converted to String.
	 * 
	 * @param reportType
	 * @param restrictions
	 * @param fields
	 * @return
	 */
	public abstract Byte[][][] getReport(String reportType, Integer maxResults,
			ReportRestriction[] restrictions, String[] fields)
			throws ReportToolException;
}
