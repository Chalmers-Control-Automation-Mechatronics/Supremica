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
 * NOTE: in this representation each disjunctive partition forms a hyperedge.
 *       the hyper edge is represented by automaton that is the center of the disj. partition.
 *
 */

public class AOH_Force
	extends AutomataOrderingHeuristic
{


	// some internal constants
	private final static int MAX_ROUNDS = 10;	// number of rounds
	private final static int MIN_ITR = 10;	// min iterations in a round
	private final static double STOP_CONST_C = 4; // constant c in the paper


	private int[] order;
	private int size;
	private Node [] nodes;
	private double [] weights ;


	public AOH_Force() {
	}

	public int[] ordering() {
		return order;
	}

	public void init(Automata a)
		throws BDDException
	{

		// create the corresponding graph
		Graph  gf = FromAutomata.build(a);
		size = gf.numOfNodes();
		nodes = new Node[size];
		order = new int[size];
		weights = new double[size];


		// create an array of nodes
		int idx = 0;
		for (Enumeration it = gf.getNodes().elements(); it.hasMoreElements(); idx++)
		{
			nodes[idx] = (Node) it.nextElement();
		}



		// get |Ev| in extra2 and |e| in extra3
		compute_cardinality();


		// get ready
		int max_itr = (int)(STOP_CONST_C * Math.log(1+size)); // max number of iterations
		if(max_itr < MIN_ITR) max_itr = MIN_ITR;




		// iterate
		double best_span = Double.MAX_VALUE;


		for(int rounds = 0; rounds < 55; rounds ++) {

			// get inital order
			if(rounds < MAX_ROUNDS / 2) 	create_dfs_order();
			else create_random_order();


			// do a series of iterations
			double span = iterate(max_itr);

			// see how good it was
			if(span < best_span) {
				best_span = span;
				extract_order();
			}
		}

		if(Options.profile_on)
		{
			Options.out.println("--> [AOH_Force] best lowest total span = " + best_span);
		}


	}


	/**
	 * do max_itr iterations
	 *
	 */
	private double iterate(int max_itr) {

		// stop_conv is the number of time we can allow the same total_span before we terminate
		int stop_cong = max_itr / 3;
		if(stop_cong < 5) stop_cong = 5;

		double last = -1;
		int repreat = 0; // number of times the last number was repeated

		for(int itr = 0; itr < max_itr; itr++) {
			force();

			double tmp = total_span();

			// dont keep one forever if we have already converged!
			if(tmp == last) {
				repreat++;
				if(repreat >= stop_cong) return last;
			} else {
				repreat = 0;
				last = tmp;
			}
		}


		return  last;
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



	/**
	 * one round of FORCE inner-loops
	 *
	 * assumes: card_e and card_Ev are computed, lv is the current order
	 *
	 * @returns the "cost" for the computed ordering
	 */
	private void force() {


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
	 *  compute the toal span.
	 *
	 * Span of hyperedge:
	 * difference between the greatest and smallest vertices connected by the same hyperedge
	 *
	 */
	private double total_span() {
		double span = 0;

		for(int i = 0; i < size; i++) {
			Node n1 = nodes[i];

			double min = n1.lv;
			double max = n1.lv;

			n1.lvp = n1.cog;

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

			span += (max - min);
		}

		return span;

	}
	// --------------------------------------------------------------

	private int dfs_count; // class global variable for labelling

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
