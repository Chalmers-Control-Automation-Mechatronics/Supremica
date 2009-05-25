/**
 *
 *
 * @author LC 
 * @Created on May 25, 2009 2:59:51 PM
 * 
 */
package org.supremica.external.operationframeworkto61131.layout.sfc;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;


public class SelectionDivergence extends CommonLayoutObject {

	// FIXME move to Constant
	public static final int objHeight = 1;
	public static final int widthUnit = 120;

	public SelectionDivergence(int localId, int numOfConOut) {
		
//		The Positions of connectionPointOut

		Position[] conOutRelPositionArray = new Position[numOfConOut];

		for (int i = 0; i < numOfConOut; i++) {

			conOutRelPositionArray[i] = new Position(widthUnit * i, objHeight);

		}

//		Width equal the position.x of last connectionPointOut
		int objWidth = conOutRelPositionArray[numOfConOut - 1].getX();

		// round to integer
		int halfWidth = BigDecimal.valueOf(objWidth).divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		// the relPosition of ConnectionPointIn, relPosition.x=width/2, y=0
		Position[] conInRelPosition = { new Position(halfWidth, 0) };

		super.setDefault(localId, objHeight, objWidth, conInRelPosition,
				conOutRelPositionArray);

	}

	public Body.SFC.SelectionDivergence getPLCOpenObject() {

		Body.SFC.SelectionDivergence selectionDivergence = CommonLayoutObject.objectFactory
				.createBodySFCSelectionDivergence();

		try {
			super.getPLCopenObject(selectionDivergence);
			super.getCommenConInPLCOpenObject(selectionDivergence);

			if (!super.getConnectionPointOutList().isEmpty()) {
				for (org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut : super
						.getConnectionPointOutList()) {

					selectionDivergence
							.getConnectionPointOut()
							.add(
									this
											.getSelectionDivergenceConnectionPointOutPLCopenObj(connectionPointOut));
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return selectionDivergence;

	}

	private Body.SFC.SelectionDivergence.ConnectionPointOut getSelectionDivergenceConnectionPointOutPLCopenObj(
			org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut) {
		
		Body.SFC.SelectionDivergence.ConnectionPointOut conOut = CommonLayoutObject.objectFactory
				.createBodySFCSelectionDivergenceConnectionPointOut();

		if (connectionPointOut.getFormalParameter() != null) {

			conOut.setFormalParameter(connectionPointOut.getFormalParameter());
		} else {

			conOut.setFormalParameter("");
		}

		if (connectionPointOut.getExpression() != null) {

			conOut.setExpression(connectionPointOut.getExpression());
		}

		conOut.setRelPosition(connectionPointOut.getRelPosition()
				.getPLCOpenObject());

		return conOut;

	}

}