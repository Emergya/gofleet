package reportTool;

import java.io.Serializable;

/**
 * Restriction for the ReportTool. It can be used like this:
 * 
 * "... WHERE getField() getOperator() getRestriction() AND ..."
 * 
 * for example:
 * 
 * "... WHERE ID LIKE "%3%" AND ..."
 * 
 * @author marias
 *
 */
public class ReportRestriction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2634560113217732641L;
	private String field;
	private String operator;
	private String restriction;
	
	
	/**
	 * 
	 */
	public ReportRestriction() {
		super();
	}

	/**
	 * @param field
	 * @param operator
	 * @param restriction
	 */
	public ReportRestriction(String field, String operator, String restriction) {
		super();
		this.field = field;
		this.operator = operator;
		this.restriction = restriction;
	}
	
	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}
	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}
	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}
	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	/**
	 * @return the restriction
	 */
	public String getRestriction() {
		return restriction;
	}
	/**
	 * @param restriction the restriction to set
	 */
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportRestriction [field=" + field + ", operator=" + operator
				+ ", restriction=" + restriction + "]";
	}
	
	
}
