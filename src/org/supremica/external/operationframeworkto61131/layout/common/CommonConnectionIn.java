package org.supremica.external.operationframeworkto61131.layout.common;
/**
 * @author LC
 *
 */
import java.util.LinkedList;
import java.util.List;

public class CommonConnectionIn {
	
	
	private Position relPosition;

	private List<Connection> connections;

	public CommonConnectionIn() {

		connections = new LinkedList<Connection>();
	}

	public void clearConnectionList() {

		if (connections != null) {
			connections.clear();
		} else {

			connections = new LinkedList<Connection>();
		}
	}

	public void addToConnections(Connection connection) {

		this.connections.add(connection);
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	public void updateConnection(Connection inCon) {

		// FIXME live list?
		List<Connection> newList = new LinkedList<Connection>();

		for (Connection connection : connections) {

			if (connection.getRefLocalId() == inCon.getRefLocalId()) {

				newList.add(inCon);

			} else {

				newList.add(connection);
			}

		}

		connections = newList;
	}

	public Position getRelPosition() {
		return new Position(relPosition);
	}

	public void setRelPosition(Position relPosition) {
		this.relPosition = new Position(relPosition);
	}


}
