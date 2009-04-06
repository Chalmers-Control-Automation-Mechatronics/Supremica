package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import java.math.BigInteger;

import org.supremica.external.operationframeworkto61131.util.PLCopenUtil;
import org.supremica.external.operationframeworkto61131.util.StringUtil;



public class FBSystemConnection extends FBConnection {

	private BigInteger stateID;

	private String functionType;

	public FBSystemConnection(
			org.supremica.manufacturingtables.xsd.fid.SystemConnection systemConnection) {

		if (systemConnection.getStateID() == null) {

			this.setStateID(BigInteger.ZERO);
		} else {

			this.setStateID(systemConnection.getStateID());
		}

		if (systemConnection.getFunctionType() == null) {
			this.setFunctionType("");

		} else {
			this.setFunctionType(systemConnection.getFunctionType().value());
		}

		if (systemConnection.getIOType() == null) {
			// if the variable is not specified, set a empty string
			super.setIOType("");
		} else {
			super.setIOType(systemConnection.getIOType().value());
		}

		if (systemConnection.getParam()!=null&&systemConnection.getParam().size()>0) {
			// if the variable is not specified, set a empty string
			

			super.setParam(systemConnection.getParam());
		} else {
			
			
			super.addParam(super.EMPTY_PARAM);
		}
		// Will be logged if error occur in parsing data type
		super.setDataType(PLCopenUtil.getDateTypeClass(systemConnection
				.getDataType().value()));

		if (systemConnection.getVariable()!=null&&systemConnection.getVariable().size()>0) {
			// if the variable is not specified, set a empty string
			super.setVariable(systemConnection.getVariable());
		} else {
			
//			 do nothing, leave it as null
		}

		if (StringUtil.isEmpty(systemConnection.getInitValue())) {
			// if the variable is not specified, set a empty string
			super.setInitValue(super.NOT_CONNECTED);
		} else {
			super.setInitValue(systemConnection.getInitValue());
		}

	}

	public String getFunctionType() {
		return functionType;
	}

	// Should not be changed externally
	private void setFunctionType(String functionType) {
		this.functionType = functionType;
	}

	public BigInteger getStateID() {
		return stateID;
	}

	// Should not be changed externally
	private void setStateID(BigInteger stateID) {
		this.stateID = stateID;
	}

}
