/**
 *
 *
 * @author LC 
 * @Created on May 25, 2009 4:18:38 PM
 * 
 */
package org.supremica.external.operationframeworkto61131.layout.sfc;

import java.math.BigDecimal;

import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;

public class SimultaneousConvergence extends CommonLayoutObject {

	// FIXME move to Constant
	public static final int objHeight = 3;
	public static final int widthUnit = 120;

	public SimultaneousConvergence(int localId, int numOfConIn) {

		// The Positions of connectionPointIn

		Position[] conInRelPositionArray = new Position[numOfConIn];

		for (int i = 0; i < numOfConIn; i++) {

			conInRelPositionArray[i] = new Position(widthUnit * i, 0);

		}

		// Width equal the position.x of last connectionPointIn
		int objWidth = conInRelPositionArray[numOfConIn - 1].getX();

		// round to integer
		int halfWidth = BigDecimal.valueOf(objWidth).divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		// the relPosition of ConnectionPointOut, relPosition.x=width/2, y=1
		Position[] conOutRelPosition = { new Position(halfWidth, objHeight) };

		super.setDefault(localId, objHeight, objWidth, conInRelPositionArray,
				conOutRelPosition);

	}

	public Body.SFC.SimultaneousConvergence getPLCOpenObject() {

		Body.SFC.SimultaneousConvergence simultaneousConvergence = CommonLayoutObject.objectFactory
				.createBodySFCSimultaneousConvergence();

		try {
			super.getPLCopenObject(simultaneousConvergence);
			super.getCommenConOutPLCOpenObject(simultaneousConvergence);

			if (!super.getConnectionPointInList().isEmpty()) {
				for (org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn connectionPointIn : super
						.getConnectionPointInList()) {

					simultaneousConvergence.getConnectionPointIn().add(
							connectionPointIn.getPLCOpenObject());

				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return simultaneousConvergence;

	}

}
