

package org.supremica.util.BDD.graph;

/**
 * A node in a graph, node in V[G]<br>
 *<p> Note that default weight is 0 (compares to 1.0 for edges)<br>
 *
 * (stolen from JDD)
 */

public class Node {


	public int flags;	/** Node falgs */
	public int id;
	public int extra1, extra2;	/** extra members used by algorithms */

	public double extra3, extra4, weight;	/** extra members used by algorithms */

	public int card_e, card_Ev; /** for the force algo */
	public double cog, lv, lvp; /** for the force algo */

	/** the in/out-going edges as a linked list */
	public Edge firstOut, firstIn; // outgoing edges linked list

	/** label of this Node */
	public String label;
	public Object owner;


	public Node(int id) { this(id,null); }

	public Node(int id, String label) {
		this.id = id;
		this.weight = 0.0;
		this.firstOut = null;
		this.firstIn  = null;
		this.owner = null;
		this.label = label;
	}


	// ----------------------------------------------

	/**
	 * computes the degree of the Node, i.e. | { (u,v) \in E : this node is either u or v } |
	 * <p>may be expensive, call it once and save the results instead of recalling each time
	 */
	public int getDegree() {
		int ret = 0;
		Edge e = firstOut; while(e != null) { ret ++; e = e.next; }
		e = firstIn; while(e != null) { ret ++; e = e.prev; }
		return ret;
	}
}
