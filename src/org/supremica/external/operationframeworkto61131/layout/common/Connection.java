package org.supremica.external.operationframeworkto61131.layout.common;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;
import java.math.BigInteger;

public class Connection {

	private List<Position> route;

	private int refLocalId = 0;
	
	private String formalParameter;

	private static final int MAX = 100000;

	private static final int MIN = 0;

	public Connection() {

		route = new LinkedList<Position>();
	}

	public Connection(org.plcopen.xml.tc6.Connection connection) {

		route = new LinkedList<Position>();

		if (connection.getRefLocalId() != null) {

			this.setRefLocalId(connection.getRefLocalId().intValue());
		}
		if (connection.getFormalParameter() != null) {

			this.formalParameter=connection.getFormalParameter();
		}

		
		if (connection.getPosition() != null) {

			for (org.plcopen.xml.tc6.Position position : connection
					.getPosition()) {

				this.addPosition(new Position(position));

			}
		}

	}

	public org.plcopen.xml.tc6.Connection getPLCOpenObject() {

		org.plcopen.xml.tc6.Connection connection = CommonLayoutObject.objectFactory
				.createConnection();

		// Set refLocalId
		if (refLocalId != 0) {
			connection.setRefLocalId(BigInteger.valueOf(this.refLocalId));
		}
		
		if (this.formalParameter != null&&!formalParameter.isEmpty()) {
			connection.setFormalParameter(this.formalParameter);
		}


		if (!route.isEmpty()) {

			// Set list of Positions

			for (Position position : route) {

				connection.getPosition().add(position.getPLCOpenObject());

			}

		}

		return connection;

	}

	public void addPosition(Position position) {

		route.add(position);
	}

	// return the point with the smallest X
	public Position getLeftStartingPosition() {

		Position leftStartingPoint = new Position(MAX, MAX);
		for (Position temp : route) {

			if (leftStartingPoint.getX() >= temp.getX()) {

				leftStartingPoint = temp;
			}

		}

		return leftStartingPoint;
	}

	// return the point with the largest X
	public Position getRightEndingPosition() {

		Position rightEndingPoint = new Position(MIN, MIN);
		for (Position temp : route) {

			if (rightEndingPoint.getX() <= temp.getX()) {

				rightEndingPoint = temp;
			}

		}

		return rightEndingPoint;
	}

	// return the point with the smallest Y
	public Position getUpperStartingPosition() {

		Position upperStartingPoint = new Position(MIN, MIN);
		for (Position temp : route) {

			if (upperStartingPoint.getY() >= temp.getY()) {

				upperStartingPoint = temp;
			}

		}

		return upperStartingPoint;
	}

	// return the point with the largest Y
	public Position getLowerEndingPosition() {

		Position lowerEndingPoint = new Position(MIN, MIN);
		for (Position temp : route) {

			if (lowerEndingPoint.getY() <= temp.getY()) {

				lowerEndingPoint = temp;
			}

		}

		return lowerEndingPoint;
	}

	public List<Position> getPositionList() {
		return route;
	}

	public void setPositionList(List<Position> positionList) {
		this.route = positionList;
	}

	public int getRefLocalId() {
		return refLocalId;
	}

	public void setRefLocalId(int refLocalId) {
		this.refLocalId = refLocalId;
	}

	public String getFormalParameter() {
		return formalParameter;
	}

	public void setFormalParameter(String formalParameter) {
		this.formalParameter = formalParameter;
	}
	
	

}
