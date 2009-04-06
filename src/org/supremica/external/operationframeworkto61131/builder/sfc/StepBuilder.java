package org.supremica.external.operationframeworkto61131.builder.sfc;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.plcopen.xml.tc6.Body;
import org.plcopen.xml.tc6.ConnectionPointIn;
import org.plcopen.xml.tc6.Position;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.util.PLCopenUtil;



/**
 * StepBuilder.java can generate jump step or common step in SFC
 *
 * Created: Mar 31, 2009 5:48:58 PM
 *
 * @author LC
 * @version 1.0
 */
public class StepBuilder {

	public static org.supremica.external.operationframeworkto61131.layout.sfc.JumpStep generateJumpStep(int localId,
			CommonLayoutObject lastObj, String targetName,
			org.supremica.external.operationframeworkto61131.layout.common.Position distance) {

		org.supremica.external.operationframeworkto61131.layout.sfc.JumpStep jumpStep = new org.supremica.external.operationframeworkto61131.layout.sfc.JumpStep(
				localId);

		jumpStep.setTargetName(targetName);
		jumpStep.connectToOut(lastObj, distance);

		return jumpStep;
	}

	// Generate a new step and connection it to the input lastObj
	public static org.supremica.external.operationframeworkto61131.layout.sfc.Step generateStep(int localId,
			CommonLayoutObject lastObj, String name,
			org.supremica.external.operationframeworkto61131.layout.common.Position distance) {

		org.supremica.external.operationframeworkto61131.layout.sfc.Step step = new org.supremica.external.operationframeworkto61131.layout.sfc.Step(localId);

		step.setName(name);

		step.connectToOut(lastObj, distance);

		return step;
	}

}
