package org.supremica.external.operationframeworkto61131.layout.ladder;
/**
 * @author LC
 *
 */
import java.util.List;

import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;



public class LeftPowerRail extends CommonLayoutObject {

	// FIXME move to Constant
	public static final int objWidth = 2;

	public static final int lineMargin = 90;

	public static final int footMargin = 20;

	public LeftPowerRail(int localId, int numOfConOut) {

		Position[] conOutRelPositionArray = new Position[numOfConOut];
		// four conOut, step 90
		// start 0
		// 1:20
		// 2:110
		// 3:200
		// 4:290
		// end 310
		for (int i = 0; i < numOfConOut; i++) {

			conOutRelPositionArray[i] = new Position(objWidth, lineMargin * i
					+ footMargin);

		}

		// int
		// objHeight=lineMargin*((numOfConIn>numOfConOut?numOfConIn:numOfConOut)-1)+footMargin*2;
		int objHeight = lineMargin * (numOfConOut - 1) + footMargin * 2;

		super.setDefault(localId, objHeight, objWidth, new Position[0],
				conOutRelPositionArray);

	}

	public Body.SFC.LeftPowerRail getPLCOpenObject() {

		Body.SFC.LeftPowerRail leftPowerRail = CommonLayoutObject.objectFactory
				.createBodySFCLeftPowerRail();

		try {
			super.getPLCopenObject(leftPowerRail);

			if (!super.getConnectionPointOutList().isEmpty()) {
				for (org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut : super
						.getConnectionPointOutList()) {

					leftPowerRail
							.getConnectionPointOut()
							.add(
									this
											.getLeftPowerRailConnectionPointOutPLCopenObj(connectionPointOut));
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return leftPowerRail;

	}

	private Body.SFC.LeftPowerRail.ConnectionPointOut getLeftPowerRailConnectionPointOutPLCopenObj(
			org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut) {

		Body.SFC.LeftPowerRail.ConnectionPointOut conOut = CommonLayoutObject.objectFactory
				.createBodySFCLeftPowerRailConnectionPointOut();

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

	public void extendFromRowI(int fromRow, int numberOfExtend) {

		List<org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut> conOutList = super
				.getConnectionPointOutList();

		int totalExtend = 0;
		for (int i = fromRow; i < conOutList.size(); i++) {

			Position conOutRelPosition = conOutList.get(i).getRelPosition();

			conOutRelPosition.addY(lineMargin * numberOfExtend);

			totalExtend = totalExtend + lineMargin * numberOfExtend;
			conOutList.get(i).setRelPosition(conOutRelPosition);

		}

		super.setHeight(totalExtend + super.getHeight());

	}

}
