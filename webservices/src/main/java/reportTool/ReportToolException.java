package reportTool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Exception thrown when the reporting tool receive a wrong parameter
 * 
 * @author marias
 * 
 */
public class ReportToolException extends RuntimeException {
	public static final Log log = LogFactory.getLog(ReportToolException.class);

	public ReportToolException(String string, Throwable t) {
		super(string, t);
		log.error(string, t);
	}

	private static final long serialVersionUID = -7470732825285957001L;

}
