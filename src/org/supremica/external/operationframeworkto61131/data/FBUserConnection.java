package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import org.supremica.external.operationframeworkto61131.util.PLCopenUtil;
import org.supremica.external.operationframeworkto61131.util.StringUtil;

public class FBUserConnection extends FBConnection {

	public FBUserConnection(org.supremica.manufacturingtables.xsd.fid.UserConnection userConnection) {

		if (userConnection.getIOType() == null) {
			// if the variable is not specified, set a empty string
			super.setIOType("");
		} else {
			super.setIOType(userConnection.getIOType().value());
		}

		if (userConnection.getParam() != null
				&& userConnection.getParam().size() > 0) {
			// if the variable is not specified, set a empty string

			super.setParam(userConnection.getParam());
		} else {

			super.addParam(super.EMPTY_PARAM);
			// super.setParam(userConnection.getParam());
		}
		// Will be logged if error occur in parsing data type
		super.setDataType(PLCopenUtil.getDateTypeClass(userConnection
				.getDataType().value()));

		if (userConnection.getVariable() != null
				&& userConnection.getVariable().size() > 0) {
			

			super.setVariable(userConnection.getVariable());
		} else {
//			 if the variable is not specified, set a default string
			super.addVariable(super.NOT_CONNECTED);
			
		}

		if (StringUtil.isEmpty(userConnection.getInitValue())) {
			// if the variable is not specified, set a empty string
			super.setInitValue("");
		} else {
			super.setInitValue(userConnection.getInitValue());
		}

	}
}
