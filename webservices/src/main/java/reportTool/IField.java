package reportTool;

/**
 * 
 * Contains a field of a database table. For example:
 * 
 *  field:ID and type:String
 *  field:cvalve type:Integer
 * 
 * @author marias@emergya.com
 *
 */
public interface IField {
	public abstract String getField();
	public abstract String getType();
}
