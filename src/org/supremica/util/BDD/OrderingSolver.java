
package org.supremica.util.BDD;

import java.util.*;

/**
 * This will replace the PCG ordering herusitics.
 * The code is so messy, even I cant remember what it does :(
 */

public class OrderingSolver {
    private int size, index;
    private HashMap map;
    private Node [] nodes;
    private MEC mec;
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
		nod.weight = w;
		nod.size   = n.getSize();
		nodes[index] = nod;

		mec.insert(nod,w,w_len);
		index++;
    }

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
			System.out.println("Group ordering: ");
			for(int i = 0; i < order.length; i++) System.out.print(" " + nodes[order[i]].org.getName());
			System.out.println();
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
    private Node [] getShortestPath(Node [] nods) {
		Solver sol = null;
		switch(Options.ordering_algorithm) {
		case Options.ORDERING_ALGO_NEW_TSP:
			sol = new TSPSolver(nods);
			break;
		case Options.ORDERING_ALGO_RANDOM:
			sol = new RandomSolver(nods);
			break;
		default:
			System.err.println("[INTERNAL] unknown ordering-solver!");
		}


		return sol.getShortestPath();
    }

    // ----------------------------------------------------------

    /** Internal Node class */
    class Node {
		PCGNode org;
		int index, id, size;
		int [] weight, wlocal;
    }








    // ---------------------------------------------------------
    /** Ordering solver base class */
    abstract class Solver {
		protected Node [] org,solved;
		public Solver(Node [] org_) {
			this.org = org_;
			solved = new Node[org_.length];
			solve();
		}
		public Node [] getShortestPath() { return solved; }
		public abstract void solve();
    }








    /** Random group ordering, just to show how bad it can get :( */
    class RandomSolver extends Solver {
		public RandomSolver(Node [] org_ ) {  super(org_); }
		public void solve() {
			int len = org.length;

			// copy
			solved = new Node[len];
			for(int i = 0; i < len; i++) solved[i] = org[i];

			// permute
			for(int i = 0; i < len; i++) {
			int pos = (int)(Math.random() * len);
			Node tmp = solved[i];
			solved[i] = solved[pos];
			solved[pos] = tmp;
			}
		}
    }









    /** TSP ordering */
    class TSPSolver extends Solver {
    	public TSPSolver(Node [] org_ ) {  super(org_); }

	private double cost(int from, int to, int distance) {
	    // this is VERY non-theoretical :)
	    return Math.pow(org[from].wlocal[to], Math.log(1 + Math.abs(distance)));
	}
	public void solve() {
	    int size = org.length;
	    int [] tour = TSP_tour();



	    // find the best place to cut the loop:
	    // The idea is first to (A) find the place where the connection
	    // a-->b is weakest and store all such b's...


	    IntArray candidates = new IntArray();  // the set of those with smallest split-cost
		// candidates.min() is the cost of splitting an arc, less is good

	    for(int i = 0; i < size; i++) {
			int prev = (i + size - 1) % size;
			double cost = cost(tour[prev],tour[i],1);
			if( i == 0 || cost < candidates.getMin()) {
				candidates.clear();
				candidates.add(  i);
			} else if(cost == candidates.getMin()) {
				candidates.add( i);
			}
	    }

	    // .. then we (B) choose the smallest b in size, since we want the
	    // smallest trees near to the top [THIS IS NOT OPTIMAL]:
	    int start_index = 0;
	    int smallest = 0;

	    for(int i = 0; i < candidates.getSize(); i++) {

			int b = candidates.get(i);
			int siz = org[b].size;
			if(i == 0 || siz < smallest) {
				smallest = siz;
				start_index = b;
			}
	    }


	    // insert them in that order:
	    solved = new Node[size];
	    for(int i = 0; i < size; i++) solved[i] = org[ tour[(i + start_index) % size]];

	}

	// Currently, a 2-step look-ahead greedy max-weight TSP tour
	private int [] TSP_tour() {
	    int size = org.length;

	    int curr = 0;
	    int [] tour = new int[size];
	    boolean [] used = new boolean[size];
	    for(int i = 0; i < size; i++) used[i] = false;

	    // insert first:
	    int first = 0; // we could choose one at random too
	    used[first] = true;
	    tour[curr++] = first;
	    int last = first;

	    while(curr < size) {
			int best_index = -1;
			double best_cost = Double.NEGATIVE_INFINITY;

			for(int j = 0; j < size; j++) if(!used[j]) {
				if(curr == size - 1) { // only one left ?
					best_index = j;
				} else {
					used[j] = true;
					for(int k = 0; k < size /* && best_index != j*/ ; k++) if(!used[k]) {
						double cost = cost(last, j, 1) + cost(j,k,1);
						if(cost > best_cost) {
						best_cost = cost;
						best_index = j;
						}
					}
					used[j] = false;
					}
				}

				used[best_index] = true;
				tour[curr++] = last = best_index;
			}

			// DEBUG
			// System.out.print("tour = ");
			// for(int i = 0; i < size; i++) System.out.print(" " + org[tour[i]].org.getName());
			// System.out.println();

			return tour;
		}
    }















    // ---------------------------------------------------------
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

		private void sortClasses() {
			// bubblesort should do...
			int classes = localnodes.length;
			for (int i = classes; --i>0; ) {
				for (int j=0; j<i; j++) {
					if(localnodes[j].length > localnodes[i].length) {
					Node [] tmp = localnodes[j];
					localnodes[j] = localnodes[i];
					localnodes [i] = tmp;
					}
				}
			}
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
			System.out.println("INTERNAL: you must build localnodes first!");
			return;
			}
			System.out.print("Automata classes : { ");
			for(int i = 0; i < localnodes.length; i++) {
			System.out.print("{ ");
			for(int j = 0; j <localnodes[i].length; j++) {
				if(j != 0) System.out.print(",");
				System.out.print("" + localnodes[i][j].org.getName());
			}
			System.out.print("}");
			if(i != 0)  System.out.print(", ");
			}
			System.out.println("}");
		}
    }

}
