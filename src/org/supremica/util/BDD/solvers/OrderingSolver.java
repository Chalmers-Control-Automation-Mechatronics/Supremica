
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;

/**
 * This will replace the PCG ordering herusitics.
 * The code is so messy, even I cant remember what it does :(
 *
 *
 * The codes splitts the model in disjoint graphs [if any], and uses an _ordering_ based algos
 * as specified by Options.ordering_algorithm. See getShortestPath() near the bottom.
 *
 */

public class OrderingSolver {
    private int size, index;
    // private HashMap map;
    private Node [] nodes;
    private MEC mec;		// used to find disjoing sub-graphs
    private int [] order;

    public OrderingSolver( int size) {
		this.size = size;
		this.nodes = new Node[size];
		this.mec = new MEC(nodes);
		this.index = 0;
		this.order = null;

    }
    public void addNode(PCGNode n, int [] w, int w_len) {
		Node nod = new Node();
		nod.org    = n;
		nod.index  = index;
		nod.index_local = index; // temporary
		nod.weight = w;
		nod.size   = n.getSize();
		nodes[index] = nod;

		mec.insert(nod,w,w_len);
		index++;
    }

	/**
	 * get a "solved" ordering in each subgraph and concatenate all such
	 * orderings to a single ordering.
	 */

    public int [] getGoodOrder() {
		if(order == null) {
			mec.precomputeLocalWeights();
			Node [][]classes = mec.getClasses();


			int offset = 0;
			order = new int[size];

			for(int g = 0; g < classes.length; g++) { // list is sorted on size!
				Node [] class_ = classes[g];
				Node [] ordred_class = getShortestPath(class_); // solve localy

				// append to list
				for(int e = 0; e < ordred_class.length; e++)
					order[offset++] = ordred_class[e].index;
			}

			if (Options.debug_on) dump();
		}
		return order;
    }

    public void dump() {
		mec.dump();
		if(order != null ){
			Options.out.println("Group ordering: ");
			for(int i = 0; i < order.length; i++) Options.out.print(" " + nodes[order[i]].org.getName());
			Options.out.println();
		}
    }


    public void test() {
		mec.precomputeLocalWeights();
		Node [][]classes = mec.getClasses();


		PCGNode [] ordering = new PCGNode[size];
		int offset = 0;

		for(int g = 0; g < classes.length; g++) { // list is sorted on size!
			Node [] class_ = classes[g];
			Node [] ordred_class = getShortestPath(class_); // solve localy

			// append to list
			for(int e = 0; e < ordred_class.length; e++)
			ordering[offset++] = ordred_class[e].org;
		}
    }

    // --------------------------------------------------------

	/** get ordering for _one_ (disjoint) subpgrah */
    private Node [] getShortestPath(Node [] nods) {
		Solver sol = null;
		switch(Options.ordering_algorithm) {
			case Options.AO_HEURISTIC_TSP:
				sol = new TSPSolver(nods);
				break;
			case Options.AO_HEURISTIC_DFS:
				sol = new DFSSolver(nods);
				break;
			case Options.AO_HEURISTIC_BFS:
				sol = new BFSSolver(nods);
				break;
			case Options.AO_HEURISTIC_STCT:
				sol = new STCTSolver(nods);
				break;
			case Options.AO_HEURISTIC_TSP_STCT:
				sol = new BootstrapSTCTSolver(nods);
				break;
			case Options.AO_HEURISTIC_TSP_SIFT:
				sol = new SiftTCPSolver(nods);
				break;
			default:
				System.err.println("[INTERNAL] unknown ordering-solver!");
		}


		return sol.getShortestPath();
    }
}
