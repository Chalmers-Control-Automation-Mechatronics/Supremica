package org.supremica.external.operationframeworkto61131.layout.ladder;
/**
 * @author LC
 *
 */
import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;




public class RightPowerRail extends CommonLayoutObject {

	private int objWidth = 2;

	private int lineMargin = 90;

	private int footMargin = 20;

	public RightPowerRail(int localId, int numOfConIn) {

		Position[] conInRelPositionArray = new Position[numOfConIn];
		// four conOut
		// start 0
		// 1:20
		// 2:110
		// 3:200
		// 4:290
		// end 310
		for (int i = 0; i < numOfConIn; i++) {

			conInRelPositionArray[i] = new Position(0, lineMargin * i
					+ footMargin);

		}

		// int
		// objHeight=lineMargin*((numOfConIn>numOfConOut?numOfConIn:numOfConOut)-1)+footMargin*2;
		int objHeight = lineMargin * (numOfConIn - 1) + footMargin * 2;

		super.setDefault(localId, objHeight, objWidth, conInRelPositionArray,
				new Position[0]);

	}

	public Body.SFC.RightPowerRail getPLCOpenObject() {

		Body.SFC.RightPowerRail rightPowerRail = CommonLayoutObject.objectFactory
				.createBodySFCRightPowerRail();

		try {
			super.getPLCopenObject(rightPowerRail);

			if (!super.getConnectionPointInList().isEmpty()) {
				for (org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn connectionPointIn : super
						.getConnectionPointInList()) {

					rightPowerRail.getConnectionPointIn().add(
							connectionPointIn.getPLCOpenObject());

				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return rightPowerRail;

	}
}
