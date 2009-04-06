package org.supremica.external.operationframeworkto61131.layout.sfc;

/**
 * @author LC
 *
 */
import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;



public class Step extends org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject {

	public final static int height = Constant.StepHeight.intValue();

	public final static int width = Constant.StepWidth.intValue();
	
	private String name;

	private Boolean isInitialStep = false;

	private ConnectionPointOut connectionPointOutAction;

	public Step() {
	}

	public Step(int localId) {



		// round to integer
		int halfHeight = Constant.StepHeight.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();
		int halfWidth = Constant.StepWidth.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		// Set default conIn ,conOut and outAction relPosition
		Position conInRelPosition = new Position(halfWidth, 0);

		Position conOutRelPosition = new Position(halfWidth, height);

		this.connectionPointOutAction = new ConnectionPointOut(new Position(
				width, halfHeight));

		super.setDefault(localId, height, width, conInRelPosition,
				conOutRelPosition);

	}

	public org.plcopen.xml.tc6.Body.SFC.Step getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.Step step = CommonLayoutObject.objectFactory
				.createBodySFCStep();
		try {

			super.getPLCopenObject(step);

			if (isInitialStep) {

				step.setInitialStep(true);
			}

			super.getCommenConInPLCOpenObject(step);

			// org.plcopen.xml.tc6.Body.SFC.Step.ConnectionPointOut
			// is different from org.plcopen.xml.tc6.ConnectionPointOut
			// So need a seperate function, but their fields are of the same
			// type and name
			if (!super.getConnectionPointOutList().isEmpty()) {
				org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut = super
						.getConnectionPointOut();

				org.plcopen.xml.tc6.Body.SFC.Step.ConnectionPointOut stepConnectionPointOut = CommonLayoutObject.objectFactory
						.createBodySFCStepConnectionPointOut();

				connectionPointOut.getPLCopenObject(stepConnectionPointOut);
				step.setConnectionPointOut(stepConnectionPointOut);

			}

			if (this.connectionPointOutAction != null) {

				org.plcopen.xml.tc6.Body.SFC.Step.ConnectionPointOutAction stepConnectionPointOutAction = CommonLayoutObject.objectFactory
						.createBodySFCStepConnectionPointOutAction();

				this.connectionPointOutAction
						.getPLCopenObject(stepConnectionPointOutAction);

				step.setConnectionPointOutAction(stepConnectionPointOutAction);

			}

			if (this.name != null && !this.name.isEmpty()) {

				step.setName(name);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return step;
	}

	public ConnectionPointOut getConnectionPointOutAction() {
		return connectionPointOutAction;
	}

	public void setConnectionPointOutAction(
			ConnectionPointOut connectionPointOutAction) {
		this.connectionPointOutAction = connectionPointOutAction;
	}

	public Boolean getIsInitialStep() {
		return isInitialStep;
	}

	public void setInitialStep() {
		this.isInitialStep = true;
		super.connectionPointInList.clear();
		this.connectionPointOutAction = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
