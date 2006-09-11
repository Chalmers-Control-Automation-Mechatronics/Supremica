package net.sourceforge.waters.mbt.translator;

import java.util.HashMap;
import java.util.LinkedList;

public class EdgesHashMap extends HashMap {

	// constructor

	public EdgesHashMap() {

	}

	public void add(final String event, final String begin, final String end) {

		LinkedList<EdgeNode> EdgesList;
		EdgeNode EdgeN;

		if (this.containsKey(event)) {

			EdgesList = (LinkedList) this.get(event);

		} else {

			EdgesList = new LinkedList<EdgeNode>();

		}

		EdgeN = new EdgeNode(begin, end);
		EdgesList.add(EdgeN);
		this.put(event, EdgesList);
	}

}
