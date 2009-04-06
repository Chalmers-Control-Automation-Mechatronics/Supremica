package org.supremica.external.operationframeworkto61131.layout.common;
/**
 * @author LC
 *
 */
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.LinkedList;

import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;




public class CommonLayoutObject {

	private int localId = 0;

	private int height = 0;

	private int width = 0;

	private Position position;

	// This field is only used for connecting variable to block. It does not
	// exist in plcopen object.
	private Boolean negated = false;

	public List<ConnectionPointIn> connectionPointInList;

	public List<ConnectionPointOut> connectionPointOutList;

	public static org.plcopen.xml.tc6.ObjectFactory objectFactory = new org.plcopen.xml.tc6.ObjectFactory();

	public CommonLayoutObject() {

		connectionPointInList = new LinkedList<ConnectionPointIn>();
		connectionPointOutList = new LinkedList<ConnectionPointOut>();
	}

	public void setDefault(int localId, int objHeight, int objWidth,
			Position[] conInRelPositionArray, Position[] conOutRelPositionArray) {

		this.setLocalId(localId);
		this.setHeight(objHeight);
		this.setWidth(objWidth);

		for (int i = 0; i < conInRelPositionArray.length; i++) {
			ConnectionPointIn conIn = new ConnectionPointIn();
			conIn.setRelPosition(conInRelPositionArray[i]);
			this.addToConnectionPointInList(conIn);
		}

		for (int i = 0; i < conOutRelPositionArray.length; i++) {
			ConnectionPointOut conOut = new ConnectionPointOut();
			conOut.setRelPosition(conOutRelPositionArray[i]);
			this.addToConnectionPointOutList(conOut);
		}

		return;
	}

	public void setDefault(int localId, int objHeight, int objWidth,
			Position conInRelPosition, Position conOutRelPosition) {

		Position[] conInRelPositionArray = new Position[0];
		Position[] conOutRelPositionArray = new Position[0];

		if (conInRelPosition != null) {

			conInRelPositionArray = new Position[] { conInRelPosition };

		}

		if (conOutRelPosition != null) {
			conOutRelPositionArray = new Position[] { conOutRelPosition };
		}
		setDefault(localId, objHeight, objWidth, conInRelPositionArray,
				conOutRelPositionArray);

		return;
	}

	public ConnectionPointIn getConnectionPointIn() {

		if (!connectionPointInList.isEmpty()) {

			for (ConnectionPointIn connectionPointIn : connectionPointInList) {

				if (connectionPointIn != null) {

					return connectionPointIn;
				}
			}

		}
		return null;
	}

	public void setConnectionPointIn(ConnectionPointIn conIn) {

		connectionPointInList.clear();
		connectionPointInList.add(conIn);

		return;

	}

	// Return the n th one
	public ConnectionPointIn getConnectionPointIn(int n) {

		if (!connectionPointInList.isEmpty()) {

			return connectionPointInList.get(n);

		}
		return null;
	}

	// Return the first one or the only one
	public ConnectionPointOut getConnectionPointOut() {

		if (!connectionPointOutList.isEmpty()) {

			for (ConnectionPointOut connectionPointOut : connectionPointOutList) {

				if (connectionPointOut != null) {

					return connectionPointOut;
				}
			}

		}
		return null;
	}

	// Return the n th one
	public ConnectionPointOut getConnectionPointOut(int n) {

		if (!connectionPointOutList.isEmpty()) {

			return connectionPointOutList.get(n);

		}
		return null;
	}

	public void setConnectionPointOut(ConnectionPointOut conOut) {

		connectionPointOutList.clear();
		connectionPointOutList.add(conOut);

		return;

	}

	// public void addToConnectionPointInConnections(Connection con) {
	//
	// ConnectionPointIn conIn = this.getConnectionPointIn();
	// if (conIn == null) {
	//
	// conIn = new ConnectionPointIn();
	// }
	//
	// conIn.addToConnections(con);
	// this.setConnectionPointIn(conIn);
	// return;
	// }

	// public void updateConnectionPointInConnections(List<Connection> conList)
	// {
	//
	// ConnectionPointIn conIn = this.getConnectionPointIn();
	//
	// if (conIn == null) {
	//
	// conIn = new ConnectionPointIn();
	// }
	//
	// conIn.setConnections(conList);
	// this.setConnectionPointIn(conIn);
	//
	// return;
	//
	// }

	public void addToConnectionPointInList(ConnectionPointIn conIn) {

		connectionPointInList.add(conIn);
	}

	public void addToConnectionPointOutList(ConnectionPointOut conOut) {

		connectionPointOutList.add(conOut);
	}

	public List<ConnectionPointIn> getConnectionPointInList() {
		return connectionPointInList;
	}

	public void setConnectionPointInList(
			List<ConnectionPointIn> connectionPointInList) {
		this.connectionPointInList = connectionPointInList;
	}

	public List<ConnectionPointOut> getConnectionPointOutList() {
		return connectionPointOutList;
	}

	public void setConnectionPointOutList(
			List<ConnectionPointOut> connectionPointOutList) {
		this.connectionPointOutList = connectionPointOutList;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLocalId() {
		return localId;
	}

	public void setLocalId(int localId) {
		this.localId = localId;
	}

	public Position getPosition() {

		if (position == null) {
			return null;
		} else {
			return new Position(position);
		}
	}

	public void setPosition(Position position) {
		this.position = new Position(position);
	}

	public void setPosition(int x, int y) {

		this.position = new Position(x, y);

	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Boolean getNegated() {
		return negated;
	}

	public void setNegated() {
		this.negated = true;
	}

	public void getPLCopenObject(Object subClassObject) throws Exception {

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		if (this.getLocalId() != 0) {
			reflectionUtil.invokeMethod(subClassObject, "setLocalId",
					BigInteger.valueOf(this.getLocalId()));
		}

		// FIXME height=0? width=0?
		if (this.getHeight() != 0) {

			reflectionUtil.invokeMethod(subClassObject, "setHeight", BigDecimal
					.valueOf(this.getHeight()));

		}

		if (this.getWidth() != 0) {

			reflectionUtil.invokeMethod(subClassObject, "setWidth", BigDecimal
					.valueOf(this.getWidth()));

		}

		if (this.getPosition() != null) {

			reflectionUtil.invokeMethod(subClassObject, "setPosition", this
					.getPosition().getPLCOpenObject());
		}

	}

	public void getCommenConInPLCOpenObject(Object subClassObject)
			throws Exception {

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		if (!this.getConnectionPointInList().isEmpty()) {
			org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn connectionPointIn = this
					.getConnectionPointIn();

			reflectionUtil.invokeMethod(subClassObject, "setConnectionPointIn",
					connectionPointIn.getPLCOpenObject());

		}

	}

	public void getCommenConOutPLCOpenObject(Object subClassObject)
			throws Exception {

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		if (!this.getConnectionPointOutList().isEmpty()) {
			org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut connectionPointOut = this
					.getConnectionPointOut();

			reflectionUtil.invokeMethod(subClassObject,
					"setConnectionPointOut", connectionPointOut
							.getPLCOpenObject());
		}

	}

	public void connectToOut(CommonLayoutObject target,
			ConnectionPointOut conOut, CommonConnectionIn commonConIn,
			Position midWayPoint, Position distanceToTarget) {

		// set the current object's position first
		if (distanceToTarget != null) {

			Position nextPosition = this.getNextPosition(target, conOut,
					commonConIn, distanceToTarget);

			this.setPosition(nextPosition);

		}

		// add route point from left to right, bottom up

		Connection con = new Connection();
		if (conOut.getFormalParameter() != null) {

			con.setFormalParameter(conOut.getFormalParameter());

		}

		con.setRefLocalId(target.getLocalId());

		Position startPosition = target.getPosition().add(
				conOut.getRelPosition());
		Position endPosition = this.getPosition().add(
				commonConIn.getRelPosition());

		con.addPosition(endPosition);

		// ConnectionPointOut is on the left side
		if (conOut.getRelPosition().getX() == target.getWidth()) {

			if (startPosition.getY() != endPosition.getY()) {

				// Position route1 = new Position(startPosition.getX() +
				// halfDistance,
				// startPosition.getY());

				// If the midWay is not specified, use middle point as default
				if (midWayPoint == null) {

					BigDecimal distance = BigDecimal.valueOf(endPosition.getX()
							- startPosition.getX());

					int halfDistance = distance.divideToIntegralValue(
							BigDecimal.valueOf(2)).intValue();

					Position route1 = new Position(startPosition.getX()
							+ halfDistance, startPosition.getY());
					Position route2 = new Position(route1.getX(), endPosition
							.getY());
					con.addPosition(route2);
					con.addPosition(route1);

				} else {

					Position route1 = new Position(midWayPoint.getX(),
							startPosition.getY());
					Position route2 = new Position(route1.getX(), endPosition
							.getY());
					con.addPosition(route2);
					con.addPosition(route1);

				}

			}
		} else {
			// suppose the conOut is at the bottom
			if (startPosition.getX() != endPosition.getX()) {

				// Position route1 = new Position(startPosition.getX() +
				// halfDistance,
				// startPosition.getY());

				// If the midWay is not specified, use middle point as default
				if (midWayPoint == null) {

					BigDecimal distance = BigDecimal.valueOf(endPosition.getX()
							- startPosition.getX());

					int halfDistance = distance.divideToIntegralValue(
							BigDecimal.valueOf(2)).intValue();
					Position route1 = new Position(startPosition.getX(),
							startPosition.getY() + halfDistance);
					Position route2 = new Position(endPosition.getX(), route1
							.getY());
					con.addPosition(route2);
					con.addPosition(route1);

				} else {

					Position route1 = new Position(startPosition.getX(),
							midWayPoint.getY());
					Position route2 = new Position(endPosition.getX(), route1
							.getY());
					con.addPosition(route2);
					con.addPosition(route1);

				}

			}
		}

		con.addPosition(startPosition);

		commonConIn.addToConnections(con);

	}

	public void connectToOut(CommonLayoutObject target, int indexOfConOut,
			CommonConnectionIn commonConIn, Position midWayPoint,
			Position distanceToTarget) {

		connectToOut(target, target.getConnectionPointOut(indexOfConOut),
				commonConIn, midWayPoint, distanceToTarget);
	}

	public void connectToOut(CommonLayoutObject target, int indexOfConOut,
			int indexOfConIn, Position midWayPoint, Position distanceToTarget) {

		ConnectionPointIn conIn = this.getConnectionPointInList().get(
				indexOfConIn);

		connectToOut(target, indexOfConOut, conIn, midWayPoint,
				distanceToTarget);

	}

	public void connectToOut(CommonLayoutObject target,
			Position distanceToTarget) {

		connectToOut(target, 0, 0, null, distanceToTarget);

	}

	// connect to a ConnectionPointOut
	public void connectToOut(CommonLayoutObject target) {

		connectToOut(target, 0, 0, null, null);

	}

	// connect to a ConnectionPointIn
	public void connectToIn(CommonLayoutObject target,
			CommonConnectionIn commonConIn, Position distance) {

		// calculate current object position first, then use connectToOut
		this.setNextPositionFromCommonConnectionPointIn(target, commonConIn, 0,
				distance);

		target.connectToOut(this, 0, commonConIn, null, null);

	}

	public Position getNextPosition(CommonLayoutObject target,
			int indexOfConOut, int indexOfConIn, Position distance) {

		return getNextPosition(target, indexOfConOut, this
				.getConnectionPointIn(indexOfConIn), distance);

		// Position lastPosition = target.getPosition();
		// Position lastConnectionPointOutPosition = new Position();
		// Position lastConnectionPointOutRelPosition = target
		// .getConnectionPointOutList().get(indexOfConOut)
		// .getRelPosition();
		//
		// lastConnectionPointOutPosition = lastConnectionPointOutRelPosition
		// .add(lastPosition);
		//
		// // add both distance x and y
		// lastConnectionPointOutPosition.add(distance);
		//
		// Position nextConnectionPointInRelPosition = new Position();
		//
		// nextConnectionPointInRelPosition =
		// this.getConnectionPointInList().get(
		// indexOfConIn).getRelPosition();
		//
		// Position rightPosition = lastConnectionPointOutPosition
		// .subtract(nextConnectionPointInRelPosition);
		//

		// return rightPosition;
	}

	private Position getNextPosition(CommonLayoutObject target,
			int indexOfConOut, CommonConnectionIn commonConIn, Position distance) {

		return getNextPosition(target, target.getConnectionPointOutList().get(
				indexOfConOut), commonConIn, distance);
	}

	private Position getNextPosition(CommonLayoutObject target,
			ConnectionPointOut conOut, CommonConnectionIn commonConIn,
			Position distance) {

		Position lastPosition = target.getPosition();
		Position lastConnectionPointOutPosition = new Position();
		Position lastConnectionPointOutRelPosition = conOut.getRelPosition();

		lastConnectionPointOutPosition = lastConnectionPointOutRelPosition
				.add(lastPosition);

		// add both distance x and y
		lastConnectionPointOutPosition.add(distance);

		Position nextConnectionPointInRelPosition = new Position();

		nextConnectionPointInRelPosition = commonConIn.getRelPosition();

		Position rightPosition = lastConnectionPointOutPosition
				.subtract(nextConnectionPointInRelPosition);

		return rightPosition;

	}

	public void setNextPositionFromCommonConnectionPointIn(
			CommonLayoutObject target, CommonConnectionIn commonConIn,
			int indexOfConOut, Position distance) {

		Position targetPosition = target.getPosition();
		Position targetConInRelPosition = commonConIn.getRelPosition();

		Position targetConInPosition = targetPosition
				.add(targetConInRelPosition);

		Position conOutPosition = targetConInPosition.subtract(distance);

		Position position = conOutPosition.subtract(this.getConnectionPointOut(
				indexOfConOut).getRelPosition());
		this.setPosition(position);

	}

	// set Default value from plcopen object, it's the reverse process of
	// getPLCopenObj
	public void fromPLCopenObj(Object plcopenObj) {

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		try {
			if (reflectionUtil.hasMethod(plcopenObj, "getLocalId")) {

			}

			// private int localId = 0;
			//
			// private int height = 0;
			//
			// private int width = 0;
			//
			// private Position position;

		} catch (Exception e) {

			e.printStackTrace();
			return;
		}

	}

}
