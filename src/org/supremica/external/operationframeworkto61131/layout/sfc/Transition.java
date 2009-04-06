package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Connection;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;




public class Transition extends CommonLayoutObject {



	private org.supremica.external.operationframeworkto61131.layout.sfc.Condition condition;

	public Transition(int localId) {
		
		int height = Constant.TransitionHeight.intValue();

		int width = Constant.TransitionWidth.intValue();

		// round to integer
		int halfWidth = Constant.TransitionWidth.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		// Set default conIn ,conOut and outAction relPosition
		Position conInRelPosition = new Position(halfWidth, 0);

		Position conOutRelPosition = new Position(halfWidth, height);

		super.setDefault(localId, height, width, conInRelPosition,
				conOutRelPosition);
		
		this.condition=new Condition();

	}

	public org.plcopen.xml.tc6.Body.SFC.Transition getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.Transition transition = CommonLayoutObject.objectFactory
				.createBodySFCTransition();

		try {

			super.getPLCopenObject(transition);

			super.getCommenConInPLCOpenObject(transition);
			super.getCommenConOutPLCOpenObject(transition);

			if (this.condition != null) {
				transition.setCondition(this.condition.getPLCOpenObject());
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return transition;
	}


	public org.supremica.external.operationframeworkto61131.layout.sfc.Condition getCondition() {
		return condition;
	}

	public void setCondition(org.supremica.external.operationframeworkto61131.layout.sfc.Condition condition) {
		this.condition = condition;
	}

	
	
}
