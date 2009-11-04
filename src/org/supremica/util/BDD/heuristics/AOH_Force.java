package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;
import org.supremica.util.BDD.graph.*;
import java.util.*;

/**
 * Ordering based on network optimization.
 * adopted from the FORCE algorithm by Aloul, Markov and Sakallah:
 *
 * <pre>
 * 1. randomly generate an initial order
 * 2. repeat limit times or until total span stops decresasing
 * 3.   for (each hyperedge e in E) compute center of gravity of e
 * 4.   for( each vertex v in V) compute tentative new location of v based on center of gravity of hyper edges
 * 5.   sort tentative vertex locations
 * 6.   assign integer indices (based on the newly sorted order) to the vertices
 * <pre>
 *
 *
 * In addition, we might use a exhaustive search in a window a four adjacent nodes.
 *
 *
 * NOTE: in this representation each disjunctive partition forms a hyperedge.
 *       the hyper edge is represented by automaton that is the center of the disj. partition.
 *
 *
 */

public class AOH_Force
	extends AutomataOrderingHeuristic
{


	// some internal constants
	private final static int TOTAL_ROUNDS = 50; // total force rounds
	private final static int MIN_ITR = 20;	// min iterations in a round
	private final static double STOP_CONST_C = 6; // constant c in the paper



	private int[] order, cutcount;
	private int size, cost_type;
	private Node [] nodes;
	private double [] weights, window_tmp;
	private double lowest_cost; // lowest cost so far
	private double edge_start, edge_end; // internal variables, do not use
	private int dfs_count; // class global variable for labelling
	private boolean use_win4; // are we useing the window4 methods or not?

	// -------------------------------------------------------------------------

	/**
	 * set use_win4 to true if you also would like to use a 4 node big sliding-window
	 * exhaustive search.
	 *
	 */

	public AOH_Force(boolean use_win4) {
		this.use_win4 = use_win4;
		this.cost_type = Options.ordering_force_cost;
	}

	public void init(Automata a)
		throws BDDException
	{

		// create the corresponding graph
		Graph  gf = FromAutomata.build(a);
		size = gf.numOfNodes();
		nodes = new Node[size];
		order = new int[size];
		cutcount = new int[size];
		weights = new double[size];
		window_tmp = new double[4]; // size of the window


		// create an array of nodes
		int idx = 0;
		for (Enumeration<?> it = gf.getNodes().elements(); it.hasMoreElements(); idx++)
		{
			nodes[idx] = (Node) it.nextElement();
		}



		// get |Ev| in extra2 and |e| in extra3
		compute_cardinality();


		// get ready
		int max_itr = (int)(STOP_CONST_C * Math.log(1+size)); // max number of iterations
		if(max_itr < MIN_ITR) max_itr = MIN_ITR;




		// iterate
		lowest_cost = Double.MAX_VALUE;


		for(int rounds = 0; rounds < TOTAL_ROUNDS; rounds ++) {

			// get inital order
			switch( rounds % 2) {
				case 0: create_dfs_order(); break;
				case 1: create_random_order(); break;
			}


			// do a series of iterations
			double span = iterate(max_itr);

			// see how good it was
			if(span < lowest_cost) {
				lowest_cost = span;
				extract_order();
			}
		}

		if(Options.profile_on)
		{
			Options.out.println("--> [AOH_Force] lowest cost = " + lowest_cost);
		}


	}

	// --------------------------------------------------------------

	public int[] ordering() {
		return order;
	}

	// --------------------------------------------------------------

	private boolean useWin4(double span) {
		if(! use_win4) return false;
		if(size < 4) return false;


		if(lowest_cost == Double.MAX_VALUE) return true;
		if(lowest_cost * 2.0 < span) return false; // see if it is so bad we dont wnat to wast any time on it

		return true;
	}
	// --------------------------------------------------------------

	/**
	 * do max_itr iterations
	 *
	 */
	private double iterate(int max_itr) {

		// stop_conv is the number of time we can allow the same ordering_cost before we terminate
		int stop_cong = max_itr / 3;
		if(stop_cong < 5) stop_cong = 5;

		double last = -1;
		int repreat = 0; // number of times the last number was repeated

		for(int itr = 0; itr < max_itr; itr++) {
			force();

			double tmp = ordering_cost();

			// dont keep one forever if we have already converged!
			if(tmp == last) {
				repreat++;
				if(repreat >= stop_cong) break; // return last;
			} else {
				repreat = 0;
				last = tmp;
			}
		}

		// making it event better by using the window4 method??
		if(useWin4(last)) {
			last = window4(last);
		}

		return  last;
	}



	/**
	 * one round of FORCE inner-loops
	 *
	 * assumes: card_e and card_Ev are computed, lv is the current order
	 */
	private void force() 
	{
		// 1. compute COG in extra4
		for(int i = 0; i < size; i++) {
			Node n1 = nodes[i];
			double sum = n1.lv;

			Edge e = n1.firstOut;
			while(e != null) { sum += e.n2.lv; 		e = e.next;		}

			e = n1.firstIn;
			while(e != null) {	sum += e.n1.lv;		e = e.prev;		}

			n1.cog = sum / n1.card_e;
		}

		// 2. center
		for(int i = 0; i < size; i++) {
			Node n1 = nodes[i];

			n1.lvp = n1.cog;

			Edge e = n1.firstOut;
			while(e != null) { n1.lvp += e.n2.lv; 		e = e.next;		}

			e = n1.firstIn;
			while(e != null) {	n1.lvp  += e.n1.lv;		e = e.prev;		}

			n1.lvp /= n1.card_Ev;

			weights[i] = n1.lvp;
		}

		// 3. sort  tentative vertex locations. smallest first
		QuickSort.sort(nodes, weights, size, false);

		// 4. assign integers...
		for(int i = 0; i < size; i++) {
			nodes[i].lv = i;
		}

	}

	/**
	 * extract the order of the current order
	 */
	private void extract_order() {
		for(int i = 0; i < size; i++)
			order[i] = nodes[i].extra1;
	}


	// --------------------------------------------------------
	/**
	 * try a permutation of all possible orderings. since this is exponential,
	 * we will only consider a sliding window of four elements
	 */
	private double window4(double current_span) {
		int len = size - 4;

		if(len < 1) return current_span; // nothing can be done :(


		// Options.out.println("STARTING WITH : " + ordering_cost() ); // DEBUG


		int save1, save2, save3, save4; // the best permutation is saved here
		double best = -1.0; // best span, 0.0 is to make the compiler shut up


		save1 = save2 = save3 = save4 = 0; // shut up stupid compiler!

		// the slide-window loop
		for(int i = 0; i < len; i++) {
			// save it
			for(int j = 0; j < 4; j++) window_tmp[j] = nodes[i+j].lv;

			best = Double.MAX_VALUE;

			// start window
			for(int i1 = 0; i1 < 4; i1++) {

				for(int i2 = 0; i2 < 4; i2++) {
					if(i2 == i1) continue;

					for(int i3 = 0; i3 < 4; i3++) {
						if(i3 == i2 || i3 == i1) continue;

						for(int i4 = 0; i4 < 4; i4++) {
							if(i4 == i3 || i4 == i2 || i4 == i1) continue;

							nodes[ i + 0].lv = window_tmp[i1];
							nodes[ i + 1].lv = window_tmp[i2];
							nodes[ i + 2].lv = window_tmp[i3];
							nodes[ i + 3].lv = window_tmp[i4];

							double span = ordering_cost();
							if(best > span) {
								best = span;
								save1 = i1;
								save2 = i2;
								save3 = i3;
								save4 = i4;
							}
						}
					}
				}
			}
			// end window

			// take the best order inside the window
			nodes[ i + 0].lv = window_tmp[save1];
			nodes[ i + 1].lv = window_tmp[save2];
			nodes[ i + 2].lv = window_tmp[save3];
			nodes[ i + 3].lv = window_tmp[save4];

		} // end of slide-window loop

		return best;
	}
	// --------------------------------------------------------


	/**
	 * get |Ev| in extra2 and |e| in extra3
	 */
	private void compute_cardinality() {

		// start with one instead of zero to include itself!
		for(int idx = 0; idx < size; idx++) {
			nodes[idx].card_e = 1;
			nodes[idx].card_Ev= 1;
		}

		for(int idx = 0; idx < size; idx++) {

			Edge e = nodes[idx].firstOut;
			while(e != null) {
				e.n1.card_e++;
				e.n2.card_Ev++;
				e = e.next;
			}

			e = nodes[idx].firstIn;
			while(e != null) {
				e.n2.card_e++;
				e.n1.card_Ev++;
				e = e.prev;
			}
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * compute some type of "COST" (lower is better) for the current ordering.
	 *
	 */
	private double ordering_cost() {
		switch(cost_type) {
			case Options.FORCE_TYPE_MAXCUT: return max_cut();
			case Options.FORCE_TYPE_TOTALSPAN: return total_span();
			case Options.FORCE_TYPE_MAXSPAN: return max_span();
			default:
			// should not happen!
			return 0;
		}
	}

	/**
	 * max cut is the size of the largest cut in the graph.
	 * the i:th cut is the number of connections across level i + 0.5,
	 */
	private double max_cut() {

		// XXX: i am sure that there is a better way for doing so, but im am too tired to figure that out right now

		for(int i = 0; i < size; i++) cutcount[i] = 0;

		for(int i = 0; i < size; i++) {

			// find the end points of this hyperedge
			find_hyperedge_ends(nodes[i]);

			for(int j = (int)edge_start; j < (int)edge_end; j++) cutcount[j]++;
		}


		int max = 0;
		for(int i = 0; i < size; i++) max = Math.max( max, cutcount[i]);

		return (double) max;

	}

	/**
	 * max span is the size of the ongest hyper-edge
	 *
	 */
	private double max_span() {
		double span = 0;

		for(int i = 0; i < size; i++) {
			find_hyperedge_ends(nodes[i]);
			span = Math.max(span, (edge_end - edge_start));
		}
		return span;
	}

	/**
	 *  compute the total span.
	 *
	 * Span of hyperedge:
	 * difference between the greatest and smallest vertices connected by the same hyperedge
	 *
	 */
	private double total_span() {
		double span = 0;

		for(int i = 0; i < size; i++) {
			find_hyperedge_ends(nodes[i]);
			span += (edge_end - edge_start);
		}

		return span;
	}


	/**
	 * internal function to find the edges of an hyperedge :)
	 * stores the results in edge_start and edge_end.
	 */
	private void find_hyperedge_ends(Node n1) {
		// find the end points of this hyperedge
		double min = n1.lv;
		double max = n1.lv;

		Edge e = n1.firstOut;
		while(e != null) {
			min = Math.min( min, e.n2.lv);
			max = Math.max( max, e.n2.lv);
			e = e.next;
		}

		e = n1.firstIn;
		while(e != null) {
			min = Math.min( min, e.n1.lv);
			max = Math.max( max, e.n1.lv);
			e = e.prev;
		}

		edge_start = min;
		edge_end = max;
	}

	// -- [ code to generate an initial ordering ] ---------------------

	/**
	 * we must start with some initial order ...
	 */
	private void create_random_order() {
		int [] perm = Util.permutate(size);
		for(int idx = 0; idx < size; idx++) {
			nodes[idx].lv = perm[idx];
		}

	}


	/**
	 * we could also start with a randomly started DFS order
	 */
	private void create_dfs_order() {

		dfs_label( nodes[ (int)(Math.random() * size)] );
	}


	// do DFS labelling starting from this node
	private void dfs_label(Node root) {
		dfs_count = 0;

		for(int i = 0; i < size; i++) nodes[i].lv = -1;
		dfs_label_rec(root);


		// we must also take care of those not conncted!
		for(int i = 0; i < size; i++)  dfs_label_rec(nodes[i]);
	}

	// the recusrive part of DFS_label
	private void dfs_label_rec(Node n) {
		if( n.lv != -1) return;
		n.lv = dfs_count++;

		Edge e = n.firstOut;
		while(e != null) { dfs_label_rec(e.n2); 		e = e.next;		}

		e = n.firstIn;
		while(e != null) {	dfs_label_rec(e.n1);		e = e.prev;		}
	}
}
