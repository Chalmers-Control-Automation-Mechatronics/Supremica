package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;



/** monotonic equivalence classes */
class MEC {

	private int max, curr;
	private Node [] nodes;
	private Node [][] localnodes;

	public MEC(Node [] nodes) {
		this.nodes = nodes;
		this.max = 0;
		this.curr = 0;
		this.localnodes = null;
	}

	public void R(Node n1, Node n2) {
		if(n1.id != n2.id) {
		for(int i = 0; i < curr; i++)  // move anyone else in n1 class to n2
			if(nodes[i].id == n1.id) nodes[i].id = n2.id;
		n1.id = n2.id; // put it in the same class
		}
	}

	public void insert(Node n, int [] edges, int len) {
		localnodes = null;				// mark that we just changed something:
		n.id = -1;		// start up clean

		boolean connected = false;
		for(int i = 0; i < curr; i++) {
			if(edges[i] > 0){ // an edge?
				R(n, nodes[i]);
				connected = true;
			}
		}

		if(! connected) 	// see if it is disjoint
			n.id = max++; // create a new class

		nodes[curr++] = n; // put it in the vector
	}

	public Node [][] getClasses() {
		return localnodes;
	}



	// -------------------------------------------------------------------------
	/** update the Node.local_index member according to the current groups */
	private void computeLocalIndex() {
		for(int i = 0; i < localnodes.length; i++) {
			Node [] g = localnodes[i];
			for(int j = 0; j < g.length; j++) g[j].index_local = j;
		}
	}


	/** sort the groups, the smallest firs... */
	private void sortClasses() {
		// we used to have insert-sort here since we belived that automata are normally
		// not disjoint, but then we saw Sanchez benchmarks and changed this to quick sort

		int len = localnodes.length;
		if(len <= 1) return;

		double [] cost = new double[len]; // the cost is the length of the array...
		for(int i = 0; i < len; i++)  cost[i] = localnodes[i].length;

		QuickSort.sort(localnodes, cost, len, false);
	}

	public void precomputeLocalWeights() {
		if(localnodes != null)  return;

		Vector classes = new Vector();
		for(int i = 0; i < max; i++) { // for each possibly empty class
			Vector class_ = null;
			for(int j = 0; j < curr; j++) { // for each member
				if(nodes[j].id == i) { // member of this class
				if(class_ == null) class_ = new Vector();
				class_.add( nodes[j]);
				}
			}
			if(class_ != null) classes.add(class_);
		}

		// converts these to 2D arrays
		int num_of_classes = classes.size();
		localnodes = new Node[num_of_classes][];
		Enumeration e = classes.elements();

		for(int i = 0; e.hasMoreElements(); i++) {
			Vector this_class = (Vector) e.nextElement();

			// allocate the corresponding array
			int num_of_members = this_class.size();
			Node [] current = new Node[num_of_members];
			localnodes[i] = current;

			// copy this class to the vector
			Enumeration e2 = this_class.elements();
			for(int j = 0; e2.hasMoreElements(); j++) {
				Node n = (Node) e2.nextElement();
				current[j] = n;
			}
			copyToLocalWeights(current);
		}

		sortClasses(); // sort
		computeLocalIndex();
	}

	private void copyToLocalWeights(Node [] group) {

		int size = group.length;
		for(int i = 0; i < size; i++) group[i].wlocal = new int[size];

		for(int i = 0; i < size; i++) {
			int row = group[i].index;
			for(int j = 0; j <= i; j++) {
				int col = group[j].index;

				int w = group[j].weight[row];
				group[i].wlocal[j] = group[j].wlocal[i] = w;

				/*
				// sanity check
				int w2 = group[i].weight[col];
				if(w != w2) System.err.println(""+ w + "/" + w2 +
				":" + group[i].org.getName() +
				"/" + group[j].org.getName() +
				"(" + row + "," + col + ")"
				);
				*/
			}
		}
	}

	public void dump() {
		if(localnodes == null) {
		Options.out.println("INTERNAL: you must build localnodes first!");
		return;
		}
		Options.out.print("Automata classes : { ");
		for(int i = 0; i < localnodes.length; i++) {
		Options.out.print("{ ");
		for(int j = 0; j <localnodes[i].length; j++) {
			if(j != 0) Options.out.print(",");
			Options.out.print("" + localnodes[i][j].org.getName());
		}
		Options.out.print("}");
		if(i != 0)  Options.out.print(", ");
		}
		Options.out.println("}");
	}
}
