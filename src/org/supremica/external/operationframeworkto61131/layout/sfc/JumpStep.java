package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;




public class JumpStep extends org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject {

	public final static int height = Constant.JumpStepHeight.intValue();

	public final static int width = Constant.JumpStepWidth.intValue();

	private String targetName;

	public JumpStep() {
	}

	public JumpStep(int localId) {

		// round to integer
		int halfWidth = Constant.JumpStepWidth.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		// Set default conOut relPosition
		Position conInRelPosition = new Position(halfWidth, 0);

		super.setDefault(localId, height, width, conInRelPosition, null);

	}

	public org.plcopen.xml.tc6.Body.SFC.JumpStep getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.JumpStep jumpStep = CommonLayoutObject.objectFactory
				.createBodySFCJumpStep();

		try {

			super.getPLCopenObject(jumpStep);
			super.getCommenConInPLCOpenObject(jumpStep);
			super.getCommenConOutPLCOpenObject(jumpStep);
		

			if (this.targetName != null && !this.targetName.isEmpty()) {

				jumpStep.setTargetName(targetName);
			}


		} catch (Exception e) {

			e.printStackTrace();

		}

		return jumpStep;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

}
