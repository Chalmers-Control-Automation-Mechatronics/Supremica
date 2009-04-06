package org.supremica.external.operationframeworkto61131.layout.common;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;

public class ConnectionPointIn extends CommonConnectionIn {

	String expression;

	public ConnectionPointIn() {
		super();
	}

	public ConnectionPointIn(
			org.plcopen.xml.tc6.ConnectionPointIn connectionPointIn) {

		if (connectionPointIn.getExpression() != null) {

			this.expression = connectionPointIn.getExpression();
		}

		if (connectionPointIn.getRelPosition() != null) {

			super.setRelPosition(new Position(connectionPointIn
					.getRelPosition()));
		}

		if (connectionPointIn.getConnection() != null) {

			for (org.plcopen.xml.tc6.Connection connection : connectionPointIn
					.getConnection()) {

				super.addToConnections(new Connection(connection));

			}
		}

	}

	public ConnectionPointIn(Position relPosition, List<Connection> connections) {
		super.setRelPosition(relPosition);
		super.setConnections(connections);
	}

	public org.plcopen.xml.tc6.ConnectionPointIn getPLCOpenObject() {

		org.plcopen.xml.tc6.ConnectionPointIn connectionPointIn = CommonLayoutObject.objectFactory
				.createConnectionPointIn();

		if (this.expression != null && !expression.isEmpty()) {

			connectionPointIn.setExpression(this.expression);
		}

		if (super.getRelPosition() != null) {
			connectionPointIn.setRelPosition(super.getRelPosition()
					.getPLCOpenObject());
		}

		if (!super.getConnections().isEmpty()) {

			for (Connection connection : super.getConnections()) {

				connectionPointIn.getConnection().add(
						connection.getPLCOpenObject());

			}

		}

		return connectionPointIn;

	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	// return the right ending poisiton
	public Position getRightEndingPosition() {

		Position rightEndingPosition = null;
		if (!super.getConnections().isEmpty()) {

			rightEndingPosition = super.getConnections().get(0)
					.getRightEndingPosition();

		}

		return rightEndingPosition;

	}

	// return the right ending poisiton
	public Position getLowerEndingPosition() {

		Position lowerEndingPosition = null;
		if (!super.getConnections().isEmpty()) {

			lowerEndingPosition = super.getConnections().get(0)
					.getLowerEndingPosition();

		}

		return lowerEndingPosition;

	}

}
