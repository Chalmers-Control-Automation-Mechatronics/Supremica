package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */
import org.supremica.external.operationframeworkto61131.layout.common.CommonConnectionIn;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Connection;
import org.supremica.external.operationframeworkto61131.layout.common.Position;

public class Condition extends CommonConnectionIn {

	// FIXME InLine and Reference are needed.
	// They are not needed for COPs and EOPs yet. ignore first
	
	
//	This relPosition does not exist in PLCopen object, this is only used to generate connection's starting point
	

	Condition() {

		super();
		
//		condition's default relPosition , but this attribute does not exist in PLCopen xml schema.
		super.setRelPosition(new Position(0,1));
	}

	public org.plcopen.xml.tc6.Body.SFC.Transition.Condition getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.Transition.Condition condition = CommonLayoutObject.objectFactory
				.createBodySFCTransitionCondition();

		for (Connection connection : super.getConnections()) {

			condition.getConnection().add(connection.getPLCOpenObject());

		}

		return condition;

	}
}
