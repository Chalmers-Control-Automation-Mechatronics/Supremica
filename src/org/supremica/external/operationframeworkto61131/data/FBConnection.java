package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;

public class FBConnection {

	private String ioType;

	private Class dataType;

	private List<String> param;

	private List<String> variable;

	private String initValue;

	private Boolean isConstant=false;
	
	private Boolean isNegated = false;

	private Boolean isRetain = false;
	
	protected final String NOT_CONNECTED = "Not_Connected";

	protected final String EMPTY_PARAM = "empty param";
	

	protected FBConnection() {

		param = new LinkedList<String>();
		variable = new LinkedList<String>();

	}

	public FBConnection(String inParam, String inVariable, String inIOType,
			Class inDataType, String inInitValue) {

		param = new LinkedList<String>();
		variable = new LinkedList<String>();

		if (inParam.isEmpty()) {
			param.add(EMPTY_PARAM);
		} else {
			param.add(inParam);
		}
		if (inVariable.isEmpty()) {

			variable.add(NOT_CONNECTED);
		} else {
			variable.add(inVariable);
		}

		ioType = inIOType;
		dataType = inDataType;
		initValue = inInitValue;

	}

	public Class getDataType() {
		return dataType;
	}

	// Should not be changed externally
	protected void setDataType(Class dataType) {
		this.dataType = dataType;
	}

	public String getInitValue() {
		return initValue;
	}

	public void setInitValue(String initValue) {
		this.initValue = initValue;
	}

	public String getIOType() {
		return ioType;
	}

	// Should not be changed externally
	protected void setIOType(String ioType) {
		this.ioType = ioType;
	}

	public List<String> getParam() {
		return param;
	}

	public void setParam(List<String> param) {

		this.param = param;
	}

	public List<String> getVariable() {
		return variable;
	}

	public void setVariable(List<String> variable) {
		this.variable = variable;
	}

	public void addParam(String inPara) {

		param.add(inPara);

	}

	public void addVariable(String inVar) {

		variable.add(inVar);

	}
	
	public void setNegated() {

		this.isNegated = true;
	}

	public Boolean isNegated() {

		return this.isNegated;
	}

	public void setConstant() {

		this.isConstant = true;
	}

	public Boolean isConstant() {

		return this.isConstant;
	}

	public void setRetain() {

		this.isRetain = true;
	}

	public Boolean isRetain() {

		return this.isRetain;
	}
}