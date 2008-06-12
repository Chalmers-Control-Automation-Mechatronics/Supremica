package org.supremica.util.BDD;

import java.util.ArrayList;
import java.util.List;

public class NodeCountStatistics {
	public class NodeCount {
		private final long count;
		private final String description;
		NodeCount(int bdd, String description) {
			this.count = manager.nodeCount(bdd);
			this.description = description;
		}
		public long getCount() {
			return count;
		}
		public String getDescription() {
			return description;
		}
		
	}
	private JBDD manager;
	private List<NodeCount> nodeCounts = new ArrayList<NodeCount>();
	private static NodeCountStatistics instance; 
	protected NodeCountStatistics() {}
	public static NodeCountStatistics getInstance() {
		if (instance == null) instance = new NodeCountStatistics();
		return instance;
	}
	public void addBdd(int bdd, String description) {
		if (Options.collectNodeCountStatistics) nodeCounts.add(new NodeCount(bdd, description));
	}
	public void reset() {
		nodeCounts.clear();
	}
	public void setManager(JBDD manager) {
		this.manager = manager;
	}
	public List<NodeCount> getNodeCounts() {
		return nodeCounts;
	}
	public NodeCount maxNodeCount() {
		NodeCount max = null;
		for (NodeCount nodeCount : nodeCounts) {
			if (max == null || nodeCount.getCount() > max.getCount()) max = nodeCount;
		}
		return max;
	}
}
