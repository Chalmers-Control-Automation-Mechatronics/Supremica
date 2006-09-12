package net.sourceforge.waters.mbt.translator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EdgesHashMap extends HashMap<String,List<EdgeNode>> {

	// constructor

	public EdgesHashMap() {

	}

	public void add(final String event, final String begin, final String end) {

		List<EdgeNode> EdgesList;
		EdgeNode EdgeN;

		if (this.containsKey(event)) {

			EdgesList = get(event);

		} else {

			EdgesList = new LinkedList<EdgeNode>();

		}

		EdgeN = new EdgeNode(begin, end);
		EdgesList.add(EdgeN);
		this.put(event, EdgesList);
	}

}
