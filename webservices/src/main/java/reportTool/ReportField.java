/**
 * 
 */
package reportTool;

/**
 * @author mlopez
 *
 */
public class ReportField implements IField {
	private String field;
	private String type;
	
	/**
	 * @param field
	 * @param type
	 */
	public ReportField(String field, String type) {
		super();
		this.field = field;
		this.type = type;
	}
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
